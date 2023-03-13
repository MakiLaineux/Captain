package com.technoprimates.captain.ui;

import android.widget.TextView;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import com.technoprimates.captain.R;

public class StueckViewHolder extends RecyclerView.ViewHolder {
    final TextView stueckId;
    final TextView stueckName;
    final TextView stueckUpdateDay;

    StueckViewHolder(View itemView) {
        super(itemView);
        stueckId = itemView.findViewById(R.id.stueck_id);
        stueckName = itemView.findViewById(R.id.stueck_name);
        stueckUpdateDay = itemView.findViewById(R.id.stueck_update_day);
    }
}