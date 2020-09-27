package com.example.documentslocker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class UploadFragment extends Fragment {
    private TextInputEditText document_name_textview;
    private Button upload_button;
    private Button select_file;
    private FirebaseStorage mFirebaseStorage;
    private FirebaseDatabase mDocumentDatabase;
    private TextView selectedFileTextView;
    private Uri pdfUri;

    private static final int RC_PERMISSION = 1;
    private static final int RC_INTENT = 2;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, null);
        document_name_textview = view.findViewById(R.id.document_name);
        upload_button = view.findViewById(R.id.upload_button);
        select_file = view.findViewById(R.id.select_file);
        selectedFileTextView = view.findViewById(R.id.selectedFileId);
        mFirebaseStorage = FirebaseStorage.getInstance();
        mDocumentDatabase = FirebaseDatabase.getInstance();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        String[] type = new String[] {"jpeg/jpg", "pdf", "dox"};

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.type_menu_item, type);

        AutoCompleteTextView drop_down =  Objects.requireNonNull(getView()).findViewById(R.id.dropdown_menu_type);
        drop_down.setAdapter(typeAdapter);

        document_name_textview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String tempName = s.toString();
                if(tempName.contains(" ")){
                    document_name_textview.setError("Spaces not allowed");
                }
                else if(tempName.contains(".")){
                    document_name_textview.setError("(.) not allowed");
                }
                else{
                    document_name_textview.setError(null);
                }
                if (s.toString().trim().length() > 0) {
                    upload_button.setEnabled(true);
                } else {
                    upload_button.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        select_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    selectFile();
                }
                else {
                    ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RC_PERMISSION);
                }
            }
        });

        upload_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pdfUri != null)
                    uploadFile(pdfUri);
                else
                    Toast.makeText(getContext(), "Select a file to upload", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_PERMISSION &&  grantResults[0] == PackageManager.PERMISSION_GRANTED ){
            selectFile();
        }
        else {
            Toast.makeText(getContext(), "Need permission to upload", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectFile(){
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, RC_INTENT);
    }

    private void uploadFile(Uri uri){
        final String filename = System.currentTimeMillis() + "";

        StorageReference storageReference = mFirebaseStorage.getReference().child("Upload");
        storageReference.child(filename).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                String uri = firebaseUri.toString();

                DatabaseReference documentDatabaseReference = mDocumentDatabase.getReference().child(filename);
                documentDatabaseReference.setValue(uri).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Error : File cannot be uploaded", Toast.LENGTH_SHORT).show();
                        }
                        selectedFileTextView.setText(R.string.no_file_selected);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error : File cannot be uploaded", Toast.LENGTH_SHORT).show();
                selectedFileTextView.setText(R.string.no_file_selected);
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_INTENT && resultCode == RESULT_OK && data != null){
            pdfUri = data.getData();
            String text = "File selected : " + data.getData().getLastPathSegment();
            selectedFileTextView.setText(text);
        }else {
            Toast.makeText(getContext(), "File not selected", Toast.LENGTH_SHORT).show();
        }
    }
}
