package com.example.documentslocker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.Objects;

import static com.example.documentslocker.LoginActivity.getDocumentDatabase;
import static com.example.documentslocker.LoginActivity.getUid;
import static com.example.documentslocker.UploadFragment.getDocumentList;

public class MyDocumentsFragment extends Fragment {

    public static ChildEventListener mChildEventListener;
    private static DocumentAdapter mDocumentAdapter;
    public static DocumentAdapter getDocumentAdapter() {
        return mDocumentAdapter;
    }
    private static DatabaseReference mDatabaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_mydocuments, container, false);
        ListView documentListView = view.findViewById(R.id.documentListView);
        mDocumentAdapter = new DocumentAdapter(Objects.requireNonNull(getContext()), R.layout.document_item, getDocumentList());
        documentListView.setAdapter(getDocumentAdapter());

        String uid = getUid();
        mDatabaseReference = getDocumentDatabase().getReference().child("users").child(uid);
        attachDatabaseReadListener();

        return view;
    }

    public static void attachDatabaseReadListener(){
        if (mChildEventListener == null) {
            /*
             * Added ChildListener to read data from firebase
             * */
            ChildEventListener mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Document doc = snapshot.getValue(Document.class);
                    mDocumentAdapter.add(doc);
                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            };
            mDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }
    public static void detachDatabaseReadListener(){
        if (mChildEventListener != null){
            mDocumentAdapter.clear();
            mDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

}
