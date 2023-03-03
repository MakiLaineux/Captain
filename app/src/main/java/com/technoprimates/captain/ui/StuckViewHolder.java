package com.technoprimates.captain.ui;

import android.widget.TextView;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import com.technoprimates.captain.R;

public class StuckViewHolder extends RecyclerView.ViewHolder {
    final TextView stuckId;
    final TextView stuckName;
    final TextView stuckUpdateDay;

    StuckViewHolder(View itemView) {
        super(itemView);
        stuckId = itemView.findViewById(R.id.stuck_id);
        stuckName = itemView.findViewById(R.id.stuck_name);
        stuckUpdateDay = itemView.findViewById(R.id.stuck_update_day);
    }
}