package com.example.documentslocker;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class DocumentAdapter extends ArrayAdapter<Document> {


    public DocumentAdapter(@NonNull Context context, int resource, List<Document> docs) {
        super(context, resource, docs);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = ( (Activity) getContext()).getLayoutInflater().inflate(R.layout.document_item, parent, false);

        TextView document_name = convertView.findViewById(R.id.document_name_textview);
        ImageView logo_image = convertView.findViewById(R.id.logo_id);
        ImageView image = convertView.findViewById(R.id.favourite);
        Document document = getItem(position);

        assert document != null;
        if (document.getIsFavourite() == 1)
            image.setVisibility(View.VISIBLE);
        else
            image.setVisibility(View.INVISIBLE);

        String file_name = document.getStorageFileName();
        if (file_name.endsWith(".jpeg") || file_name.endsWith(".jpg") || file_name.endsWith(".png")){
            logo_image.setImageResource(R.drawable.image_file);
        }
        else if (file_name.endsWith(".pdf")){
            logo_image.setImageResource(R.drawable.pdf_file);
        }
        else if (file_name.endsWith(".docx") || file_name.endsWith(".doc")){
            logo_image.setImageResource(R.drawable.word_file);
        }
        document_name.setText(document.getName());

        return convertView;
    }
}
