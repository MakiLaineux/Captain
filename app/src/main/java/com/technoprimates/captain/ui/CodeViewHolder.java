package com.technoprimates.captain.ui;

import android.widget.TextView;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import com.technoprimates.captain.R;

public class CodeViewHolder extends RecyclerView.ViewHolder {
    final TextView item_categ;
    final TextView item_codeName;
    final TextView item_codeUpdateDay;

    CodeViewHolder(View itemView) {
        super(itemView);
        item_categ = itemView.findViewById(R.id.categ);
        item_codeName = itemView.findViewById(R.id.codeName);
        item_codeUpdateDay = itemView.findViewById(R.id.codeUpdateDay);
    }
}