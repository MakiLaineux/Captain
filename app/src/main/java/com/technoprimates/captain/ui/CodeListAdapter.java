package com.technoprimates.captain.ui;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.technoprimates.captain.db.Code;


import java.util.List;

public class CodeListAdapter extends RecyclerView.Adapter<CodeViewHolder> {

    // TODO change method signature to void onItemClicked(Code code)
    /**
     * Interface definition for a callback to be invoked when a Code object is clicked on a list
     */
    public interface CodeActionListener {

        /**
         * Called when a Code has been clicked
         * @param code The <code>Code</code> object that was clicked on
         */
        void onCodeClicked(Code code);
    }

    private final int codeItemLayout;
    private List<Code> codeList;
    private final CodeActionListener listener;

    public CodeListAdapter(int layoutId, CodeActionListener listener) {
        codeItemLayout = layoutId;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setCodeList(List<Code> codes) {
        codeList = codes;
        notifyDataSetChanged();
    }

    // TODO change to private
    @Nullable
    public Code getCodeAtPos(int pos) {
        Code code;
        try {
            code = codeList.get(pos);
        } catch (Exception e) {
            return null;
        }
        return code;
    }

    @NonNull
    @Override
    public CodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(codeItemLayout, parent, false);
        return new CodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CodeViewHolder holder, final int listPosition) {
        holder.item_categ.setText(codeList.get(listPosition).getCodeCategory());
        holder.item_codeName.setText(codeList.get(listPosition).getCodeName());
        holder.item_codeUpdateDay.setText(codeList.get(listPosition).getCodeUpdateDay());
        holder.itemView.setOnClickListener(view -> listener.onCodeClicked(getCodeAtPos(holder.getBindingAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return codeList == null ? 0 : codeList.size();
    }
}
