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

    private static final int IMAGE_CODE = 1;
    private static final int DOCX_CODE = 2;
    private static final int PDF_CODE = 3;

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
        Document document = getItem(position);

        assert document != null;
        if (document.getCode() == IMAGE_CODE){
            logo_image.setImageResource(R.drawable.image);
        }
        else if (document.getCode() == PDF_CODE){
            logo_image.setImageResource(R.drawable.pdf_logo2);
        }
        else if (document.getCode() == DOCX_CODE){
            logo_image.setImageResource(R.drawable.word_logo);
        }
        document_name.setText(document.getName());

        return convertView;
    }
}
