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

import java.util.Objects;

import static com.example.documentslocker.UploadFragment.getDatabaseReference;
import static com.example.documentslocker.UploadFragment.getDocumentList;

public class MyDocumentsFragment extends Fragment {

    private static DocumentAdapter mDocumentAdapter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        getDatabaseReference().addChildEventListener(mChildEventListener);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_mydocuments, null);
        ListView documentListView = view.findViewById(R.id.documentListView);
        mDocumentAdapter = new DocumentAdapter(Objects.requireNonNull(getContext()), R.layout.document_item, getDocumentList());
        documentListView.setAdapter(mDocumentAdapter);
        return view;
    }
}
