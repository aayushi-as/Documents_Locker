package com.example.documentslocker;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static com.example.documentslocker.LoginActivity.getDocumentDatabase;
import static com.example.documentslocker.LoginActivity.getDocumentStorage;
import static com.example.documentslocker.LoginActivity.getUid;
import static com.example.documentslocker.UploadFragment.getDocumentList;

public class MyDocumentsFragment extends Fragment {

    public static ChildEventListener mChildEventListener;
    private static DocumentAdapter mDocumentAdapter;
    private static DatabaseReference mDatabaseReference;
    private static List<Document> favouriteList = new ArrayList<>();
    private ProgressBar progressBar;

    public static List<Document> getFavouriteList() {
        return favouriteList;
    }

    public static DocumentAdapter getDocumentAdapter() {
        return mDocumentAdapter;
    }


    public static void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            /*
             * Added ChildListener to read data from firebase
             * */
            ChildEventListener mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Document doc = snapshot.getValue(Document.class);
                    mDocumentAdapter.add(doc);
//                  Get favourites list in Favourites Fragment
                    assert doc != null;
                    if (doc.getIsFavourite() == 1)
                        favouriteList.add(doc);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            };
            mDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    public static void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mDocumentAdapter.clear();
            favouriteList.clear();
            mDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mydocuments, container, false);

        progressBar = view.findViewById(R.id.myDocuments_progressbar);
        ListView documentListView = view.findViewById(R.id.documentListView);
        mDocumentAdapter = new DocumentAdapter(Objects.requireNonNull(getContext()), R.layout.document_item, getDocumentList());
        documentListView.setAdapter(getDocumentAdapter());

        String uid = getUid();
        mDatabaseReference = getDocumentDatabase().getReference().child("users").child(uid);

        attachDatabaseReadListener();

        onItemLongPress(documentListView);

        onItemClick(documentListView);

        return view;
    }

    private void onItemLongPress(ListView documentListView) {

        /*
        * On long press : delete document
        * */

        documentListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            //_____________________________________________________________________________________________________________________
                final Document fileClicked = getDocumentList().get(position);

                String title = "Are you sure ?";
                String message = "Want to delete " + fileClicked.getName() + " ?";
                final String fileUrl = fileClicked.getUrl();
                final String file_name = fileClicked.getStorageFileName();

                new MaterialAlertDialogBuilder(Objects.requireNonNull(getContext())).setTitle(title).setMessage(message)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteFile(fileUrl, file_name);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();


                return true;
            //_____________________________________________________________________________________________________________________
            }
        });
    }

    private void onItemClick(ListView documentListView) {
    //_____________________________________________________________________________________________________________________
        documentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final Document docClicked = getDocumentList().get(position);
                String title = "Add to favourites or download ?";

                new MaterialAlertDialogBuilder(Objects.requireNonNull(getContext())).setTitle(title)
                        .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setNegativeButton(R.string.add_to_fav, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                addToFavourite(docClicked);
                            }
                        })
                        .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                downloadFile(docClicked);
                            }
                        }).show();
            }
        });
    }

    private void deleteFile(String url, String name) {

        /*
        * Match url and then delete document from firebase database and storage
        * */
        Query query = mDatabaseReference.orderByChild("url").equalTo(url);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    dataSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Unusual Error occurred", Toast.LENGTH_SHORT).show();
            }
        });

        StorageReference fileStorageReference = getDocumentStorage().getReference().child("uploads").child("users").child(name);
        fileStorageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(), "Document deleted!", Toast.LENGTH_SHORT).show();
                mDocumentAdapter.clear();
                favouriteList.clear();
                attachDatabaseReadListener();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error in deleting document", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void downloadFile(Document doc) {
        progressBar.setVisibility(View.VISIBLE);
        final String name = doc.getStorageFileName();
        StorageReference fileStorageReference = getDocumentStorage().getReference().child("uploads").child("users").child(name);

        fileStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                DownloadManager downloadManager = (DownloadManager) Objects.requireNonNull(getContext()).getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//                Documents will be downloaded in directory_downloads
                request.setDestinationInExternalFilesDir(getContext(), DIRECTORY_DOWNLOADS, name);
                downloadManager.enqueue(request);
                Toast.makeText(getContext(), "Download complete", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Download failed", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void addToFavourite(Document doc) {

        if (doc.getIsFavourite() == 1)
            Toast.makeText(getContext(), "Already added to favourites", Toast.LENGTH_SHORT).show();
        else {
            Query query = mDatabaseReference.orderByChild("url").equalTo(doc.getUrl());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String key = dataSnapshot.getRef().getKey();
                        assert key != null;
                        mDatabaseReference.child(key).child("isFavourite").setValue(1);
                        break;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

}
