package com.technoprimates.captain.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.technoprimates.captain.ItemViewModel;
import com.technoprimates.captain.R;
import com.technoprimates.captain.databinding.ItemEditBinding;
import com.technoprimates.captain.db.Item;

import java.util.Objects;

public class EditFragment extends Fragment {

    public static final String TAG = "EDITFRAG";

    //binding
    private ItemEditBinding binding;

    // ViewModel scoped to the Activity
    private ItemViewModel mViewModel;

    // Action to process (INSERT, UPDATE)
    private int mAction;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // add fragment-specific menu using MenuProvider
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_add, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_save) {
                    onSaveClicked();
                    return true;
                }
            return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        // binding
        binding = ItemEditBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);
        mAction = mViewModel.getActionMode();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switch (mAction) {

            case Item.MODE_UPDATE:
                // update an existing Item available in the ViewModel
                // fill the fields with existing Item
                setString(binding.contentItemname, mViewModel.getItemToProcess().getItemName());
                setString(binding.contentItemval, mViewModel.getItemToProcess().getItemValue());
                setString(binding.contentCategory, mViewModel.getItemToProcess().getItemCategory());
                setString(binding.contentComments, mViewModel.getItemToProcess().getItemComments());
                binding.checkboxFingerprint.setChecked((mViewModel.getItemToProcess().getItemProtectMode()) == Item.FINGERPRINT_PROTECTED) ;
                // Set the fragment title
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.title_update);
                break;

            case Item.MODE_INSERT:
                // insert a new Item : start with empty fields
                setString(binding.contentItemname, "");
                setString(binding.contentItemval, "");
                setString(binding.contentCategory, "");
                setString(binding.contentComments, "");
                binding.checkboxFingerprint.setChecked(false);
                // Set the fragment title
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.title_add);
        }

        // start with focus on first input
        binding.contentItemname.requestFocus();

        // set dropdown list for categories
        String[] categs = getResources().getStringArray(R.array.categs);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.categ_dropdown_item, categs);
        binding.autoCompleteTextView.setAdapter(arrayAdapter);

        // listeners, triggered when losing the focus, for clearing a previous "empty field" error message
        binding.inputItemname.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // lost the focus, there may be an error msg to clear
                if (binding.contentItemname.getError() != null) {
                    // there is currently an error msg, check if it is to be cleared
                    if ((binding.contentItemname.getEditText() != null)
                            && (!TextUtils.isEmpty(binding.contentItemname.getEditText().getText().toString()))) {
                        // Item name not empty, the error message can be cleared
                        binding.contentItemname.setError(null);
                    }
                }
            }
        });
        binding.inputItemval.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // lost the focus, there may be an error msg to clear
                if (binding.contentItemval.getError() != null) {
                    // there is currently an error msg, check if it is to be cleared
                    if ((binding.contentItemval.getEditText() != null)
                        && (!TextUtils.isEmpty(binding.contentItemval.getEditText().getText().toString()))) {
                        // Item name not empty, the error message can be cleared
                        binding.contentItemval.setError(null);
                    }
                }
            }
        });

    }


    /**
     * Gets user inputs and perform basic checks.
     *
     * @return A Item object containing user input
     */
    private Item getUserInput() {
        // get user inputs
        String name = getString(binding.contentItemname);
        String value = getString(binding.contentItemval);
        String categ = getString(binding.contentCategory);
        String comments = getString(binding.contentComments);
        int protectMode = binding.checkboxFingerprint.isChecked() ? Item.FINGERPRINT_PROTECTED : Item.NOT_FINGERPRINT_PROTECTED;

        // check name, value
        if (name.equals("")) {
            binding.contentItemname.setError(getString(R.string.err_noname));
            binding.contentItemname.requestFocus();
            return null;
        } else {
            binding.contentItemname.setError(null);
            binding.contentItemname.setHelperTextEnabled(false);
        }

        if (value.equals("")) {
            binding.contentItemval.setError(getString(R.string.err_noitemval));
            binding.contentItemval.requestFocus();
            return null;
        } else {
            binding.contentItemval.setError(null);
            binding.contentItemval.setHelperTextEnabled(false);
        }

        // checks ok, build Item object with user input
        return (new Item(name, value, categ, comments, protectMode));
    }

    private void onSaveClicked () {
        Item item = getUserInput();

        // perform checks via ViewModel and handle errors
        switch (mViewModel.checkItemBusinessLogic(item, mAction)) {
            // Item must be not null and item name must not be empty
            case ItemViewModel.NO_ITEM:
            case ItemViewModel.NO_ITEMNAME:
                return;
            // Cannot overwrite existing item
            case ItemViewModel.ITEMNAME_ALREADY_EXISTS:
                binding.contentItemname.setError(getString(R.string.err_name_already_exists));
                binding.contentItemname.requestFocus();
                return;
            case ItemViewModel.ITEM_OK:
                break;
            default:
                Log.e(TAG, "Checking item : unexpected return value");
        }

        if (mAction == Item.MODE_INSERT) {
            // set Insertion mode and Item to process in the ViewModel
            mViewModel.selectActionToProcess(Item.MODE_INSERT);
            mViewModel.selectItemToProcess(item);
            mViewModel.insertItem();

        } else {
            mViewModel.selectActionToProcess(Item.MODE_UPDATE);
            assert item != null;
            mViewModel.fillItemToProcess(item);
            mViewModel.updateItem();
        }

        // save completed, return to the list fragment
        NavHostFragment.findNavController(EditFragment.this).navigate(R.id.action_EditFragment_to_ListFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // utility methods for TextInputLayout
    private String getString(TextInputLayout textInputLayout) {
        return ((textInputLayout.getEditText() == null)? "" : textInputLayout.getEditText().getText().toString());
    }
    private void setString(TextInputLayout textInputLayout, String s) {
        if (textInputLayout.getEditText() != null)
            textInputLayout.getEditText().setText(s);
    }
}