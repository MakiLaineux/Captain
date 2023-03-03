package com.technoprimates.captain.ui;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.technoprimates.captain.db.Stuck;


import java.util.List;

public class StuckListAdapter extends RecyclerView.Adapter<StuckViewHolder> {

    /**
     * Interface definition for a callback to be invoked when a Stuck object is clicked on a list
     */
    public interface StuckActionListener {

        /**
         * Called when a Stuck has been clicked
         * @param item The <item>Stuck</item> object that was clicked on
         */
        void onStuckClicked(Stuck item);
    }

    private final int stuckItemLayout;
    private List<Stuck> stuckList;
    private final StuckActionListener listener;

    public StuckListAdapter(int layoutId, StuckActionListener listener) {
        stuckItemLayout = layoutId;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setStuckList(List<Stuck> stucks) {
        stuckList = stucks;
        notifyDataSetChanged();
    }

    // TODO change to private
    @Nullable
    public Stuck getStuckAtPos(int pos) {
        Stuck stuck;
        try {
            stuck = stuckList.get(pos);
        } catch (Exception e) {
            return null;
        }
        return stuck;
    }

    @NonNull
    @Override
    public StuckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(stuckItemLayout, parent, false);
        return new StuckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final StuckViewHolder holder, final int listPosition) {
        //holder.stuckId.setText("text");
        holder.stuckId.setText(String.valueOf(stuckList.get(listPosition).getId()));
        holder.stuckName.setText(stuckList.get(listPosition).getName());
        holder.stuckUpdateDay.setText(stuckList.get(listPosition).getUpdateDay());
        holder.itemView.setOnClickListener(view -> listener.onStuckClicked(getStuckAtPos(holder.getBindingAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return stuckList == null ? 0 : stuckList.size();
    }
}
