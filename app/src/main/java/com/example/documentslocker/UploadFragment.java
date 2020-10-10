package com.example.documentslocker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.example.documentslocker.LoginActivity.getDocumentDatabase;
import static com.example.documentslocker.LoginActivity.getDocumentStorage;
import static com.example.documentslocker.LoginActivity.getUid;
import static com.example.documentslocker.MyDocumentsFragment.getDocumentAdapter;

public class UploadFragment extends Fragment {
    private static final int RC_PERMISSION = 1;
    private static final int RC_INTENT = 2;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;

    private static List<Document> documentList = new ArrayList<>();

    public static List<Document> getDocumentList() {
        return documentList;
    }


    private TextInputEditText document_name_editText;
    private TextView selectedFileTextView;
    private ProgressBar progressBar;
    private Button upload_button;
    private Button select_file;

    private Uri pdfUri;
    private String file_name;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        /*
        * 1. Initialization of class variables
        * 2. Creation of View
        * */

        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        document_name_editText = view.findViewById(R.id.document_name);
        selectedFileTextView = view.findViewById(R.id.selectedFileId);

        upload_button = view.findViewById(R.id.upload_button);
        select_file = view.findViewById(R.id.select_file);
        progressBar = view.findViewById(R.id.progressbar);

        String uid = getUid();

        mDatabaseReference = getDocumentDatabase().getReference().child("users").child(uid);
        mStorageReference = getDocumentStorage().getReference().child("uploads").child("users");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        /*
        * addTextChangedListener        : To define action on change in document name EditText
        *                                 Marking error in document name (. and ' ' not allowed)
        * select_file setOnClickListener
        * upload_file setOnClickListener
        * */

        document_name_editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String tempName = s.toString();
                if (tempName.contains(" ")) {
                    document_name_editText.setError("Spaces not allowed");
                } else if (tempName.contains(".")) {
                    document_name_editText.setError("(.) not allowed");
                } else {
                    document_name_editText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        select_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                * Select file only if permission was granted,
                * else
                * Ask for permission : it's acknowledgement is done in onRequestPermissionResult()
                * */
                if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectFile();
                } else {
                    /*
                    * RC_PERMISSION : Request Code to ask for permission to read External Storage
                    * */
                    ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RC_PERMISSION);
                }
            }
        });

        upload_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int len = Objects.requireNonNull(document_name_editText.getText()).toString().trim().length();
                /*
                * Check for error in document name
                * or File not select
                * Otherwise Upload file
                * */
                if (len == 0 || document_name_editText.getError() != null){
                    Toast.makeText(getContext(), "Document name error", Toast.LENGTH_SHORT).show();
                }
                else if (pdfUri == null){
                    Toast.makeText(getContext(), "Select a file to upload", Toast.LENGTH_SHORT).show();
                }
                else {
                    uploadFile(pdfUri);
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectFile();
        } else {
            Toast.makeText(getContext(), "Need permission to upload", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectFile() {
        /*
        * Select pdf/docx/doc/jpeg/jpg file
        * */
        String[] mimeTypes = {"application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document","application/pdf","image/*"};
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, RC_INTENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*
        * Acknowledgement from startActivityForResult
        * Check request_code and result code
        * Get uri for that file
        * */
        if (requestCode == RC_INTENT && resultCode == RESULT_OK && data != null) {
            pdfUri = data.getData();
            assert pdfUri != null;
            String uri = pdfUri.toString();
            File file = new File(uri);

            if(uri.startsWith("content://")){
                Cursor cursor = null;
                try {
                    cursor = Objects.requireNonNull(getActivity()).getContentResolver().query(pdfUri, null, null,null,null);

                    if (cursor != null && cursor.moveToFirst()){
                        file_name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                }
                finally {
                    assert cursor != null;
                    cursor.close();
                }
            }else if (uri.startsWith("file://")){
                file_name = file.getName();
            }
            String text = "File selected : " + file_name;
            selectedFileTextView.setText(text);

        } else {
            Toast.makeText(getContext(), "File not selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile(Uri uri) {
        /*
        * 1. Upload file to Firebase Storage
        * 2. Update url of file in Realtime Database
        * */
//        final String filename = System.currentTimeMillis() + "";

        mStorageReference.child(file_name).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                String uri = firebaseUri.toString();

                int code = 0;

                if (file_name.endsWith(".pdf"))
                    code = 3;
                else if (file_name.endsWith(".jpeg") || file_name.endsWith(".jpg") || file_name.endsWith(".png"))
                    code = 1;
                else if (file_name.endsWith(".docx") || file_name.endsWith(".doc"))
                    code = 2;

                Document document = new Document(Objects.requireNonNull(document_name_editText.getText()).toString(), uri, code, file_name);
                mDatabaseReference.push().setValue(document).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                            document_name_editText.setText("");
                            getDocumentAdapter().clear();
                        }
                        else
                            Toast.makeText(getContext(), "Error : File cannot be uploaded", Toast.LENGTH_SHORT).show();

                        selectedFileTextView.setText(R.string.no_file_selected);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error : File cannot be uploaded", Toast.LENGTH_SHORT).show();
                selectedFileTextView.setText(R.string.no_file_selected);
                progressBar.setVisibility(View.INVISIBLE);
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

            }
        });
    }

}
