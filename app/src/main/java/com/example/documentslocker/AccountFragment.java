package com.example.documentslocker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static com.example.documentslocker.LoginActivity.getEmailId;
import static com.example.documentslocker.LoginActivity.getUserName;
import static com.example.documentslocker.MyDocumentsFragment.getFavouriteDocuments;
import static com.example.documentslocker.MyDocumentsFragment.getTotalDocs;

public class AccountFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        TextView user_name = view.findViewById(R.id.account);
        user_name.setText(getUserName());

        TextView email = view.findViewById(R.id.email);
        email.setText(getEmailId());

        TextView totalDocuments = view.findViewById(R.id.total_Documents);
        String total = "Total Documents         :   " + getTotalDocs();
        totalDocuments.setText(total);

        TextView favouriteDocuments = view.findViewById(R.id.favourite_documents);
        String fav =   "Favourite Documents    :   " + getFavouriteDocuments();
        favouriteDocuments.setText(fav);

        return view;
    }
}
