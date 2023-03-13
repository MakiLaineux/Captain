package com.technoprimates.captain.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.technoprimates.captain.StueckViewModel;
import com.technoprimates.captain.db.Stueck;


import java.util.List;

public class StueckListAdapter extends RecyclerView.Adapter<StueckViewHolder> {

    /**
     * Interface definition for a callback to be invoked when a Stueck object is clicked on a list
     */
    public interface StueckActionListener {

        /**
         * Called when a Stueck has been clicked
         * @param item The <item>Stueck</item> object that was clicked on
         */
        void onStueckClicked(Stueck item);
    }

    private final int mStueckItemLayout;

    // The viewmodel's list of good profiled stücks to be displayed in the RecyclerView
    // This is a reference which is set in the adapter constructor and doesn't change thereafter
    // the content of the list itself is changed in the viewmodel when its updateProfilesStuecksList method is called
    private final List<Stueck> mProfiledStuecksList;

    // An implementation of the StueckActionListener interface to call back when an item is clicked
    private final StueckActionListener mListener;


    // constructor with viewmodel to get a reference to the profiled stück list
    public StueckListAdapter(int layoutId, StueckActionListener listener, StueckViewModel stueckViewModel) {
        mStueckItemLayout = layoutId;
        this.mListener = listener;
        mProfiledStuecksList = stueckViewModel.getProfiledStuecksList();
    }

    @Nullable
    public Stueck getStueckAtPos(int pos) {
        Stueck stueck;
        try {
            stueck = mProfiledStuecksList.get(pos);
        } catch (Exception e) {
            return null;
        }
        return stueck;
    }

    @NonNull
    @Override
    public StueckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mStueckItemLayout, parent, false);
        return new StueckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final StueckViewHolder holder, final int listPosition) {
        //holder.stueckId.setText("text");
        holder.stueckId.setText(String.valueOf(mProfiledStuecksList.get(listPosition).getId()));
        holder.stueckName.setText(mProfiledStuecksList.get(listPosition).getName());
        holder.stueckUpdateDay.setText(mProfiledStuecksList.get(listPosition).getUpdateDay());
        holder.itemView.setOnClickListener(view -> mListener.onStueckClicked(getStueckAtPos(holder.getBindingAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return mProfiledStuecksList.size();
    }
}
