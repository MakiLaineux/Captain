package com.technoprimates.captain.ui;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.technoprimates.captain.db.Item;


import java.util.List;

public class ItemListAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    /**
     * Interface definition for a callback to be invoked when a Item object is clicked on a list
     */
    public interface CodeActionListener {

        /**
         * Called when a Item has been clicked
         * @param item The <item>Item</item> object that was clicked on
         */
        void onCodeClicked(Item item);
    }

    private final int codeItemLayout;
    private List<Item> itemList;
    private final CodeActionListener listener;

    public ItemListAdapter(int layoutId, CodeActionListener listener) {
        codeItemLayout = layoutId;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setCodeList(List<Item> items) {
        itemList = items;
        notifyDataSetChanged();
    }

    // TODO change to private
    @Nullable
    public Item getCodeAtPos(int pos) {
        Item item;
        try {
            item = itemList.get(pos);
        } catch (Exception e) {
            return null;
        }
        return item;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(codeItemLayout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int listPosition) {
        holder.item_categ.setText(itemList.get(listPosition).getItemCategory());
        holder.item_codeName.setText(itemList.get(listPosition).getItemName());
        holder.item_codeUpdateDay.setText(itemList.get(listPosition).getItemUpdateDay());
        holder.itemView.setOnClickListener(view -> listener.onCodeClicked(getCodeAtPos(holder.getBindingAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }
}
