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
import com.technoprimates.captain.CodeViewModel;
import com.technoprimates.captain.R;
import com.technoprimates.captain.databinding.CodeEditBinding;
import com.technoprimates.captain.db.Code;

import java.util.Objects;

public class EditFragment extends Fragment {

    public static final String TAG = "EDITFRAG";

    //binding
    private CodeEditBinding binding;

    // ViewModel scoped to the Activity
    private CodeViewModel mViewModel;

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
        binding = CodeEditBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(CodeViewModel.class);
        mAction = mViewModel.getActionMode();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switch (mAction) {

            case Code.MODE_UPDATE:
                // update an existing code available in the ViewModel
                // fill the fields with existing code
                setString(binding.contentCodename, mViewModel.getCodeToProcess().getCodeName());
                setString(binding.contentCodeval, mViewModel.getCodeToProcess().getCodeValue());
                setString(binding.contentCategory, mViewModel.getCodeToProcess().getCodeCategory());
                setString(binding.contentComments, mViewModel.getCodeToProcess().getCodeComments());
                binding.checkboxFingerprint.setChecked((mViewModel.getCodeToProcess().getCodeProtectMode()) == Code.FINGERPRINT_PROTECTED) ;
                // Set the fragment title
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.title_update);
                break;

            case Code.MODE_INSERT:
                // insert a new code : start with empty fields
                setString(binding.contentCodename, "");
                setString(binding.contentCodeval, "");
                setString(binding.contentCategory, "");
                setString(binding.contentComments, "");
                binding.checkboxFingerprint.setChecked(false);
                // Set the fragment title
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.title_add);
        }

        // start with focus on first input
        binding.contentCodename.requestFocus();

        // set dropdown list for categories
        String[] categs = getResources().getStringArray(R.array.categs);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.categ_dropdown_item, categs);
        binding.autoCompleteTextView.setAdapter(arrayAdapter);

        // listeners, triggered when losing the focus, for clearing a previous "empty field" error message
        binding.inputCodename.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // lost the focus, there may be an error msg to clear
                if (binding.contentCodename.getError() != null) {
                    // there is currently an error msg, check if it is to be cleared
                    if ((binding.contentCodename.getEditText() != null)
                            && (!TextUtils.isEmpty(binding.contentCodename.getEditText().getText().toString()))) {
                        // Code name not empty, the error message can be cleared
                        binding.contentCodename.setError(null);
                    }
                }
            }
        });
        binding.inputCodeval.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // lost the focus, there may be an error msg to clear
                if (binding.contentCodeval.getError() != null) {
                    // there is currently an error msg, check if it is to be cleared
                    if ((binding.contentCodeval.getEditText() != null)
                        && (!TextUtils.isEmpty(binding.contentCodeval.getEditText().getText().toString()))) {
                        // Code name not empty, the error message can be cleared
                        binding.contentCodeval.setError(null);
                    }
                }
            }
        });

    }


    /**
     * Gets user inputs and perform basic checks.
     *
     * @return A Code object containing user input
     */
    private Code getUserInput() {
        // get user inputs
        String name = getString(binding.contentCodename);
        String value = getString(binding.contentCodeval);
        String categ = getString(binding.contentCategory);
        String comments = getString(binding.contentComments);
        int protectMode = binding.checkboxFingerprint.isChecked() ? Code.FINGERPRINT_PROTECTED : Code.NOT_FINGERPRINT_PROTECTED;

        // check name, value
        if (name.equals("")) {
            binding.contentCodename.setError(getString(R.string.err_noname));
            binding.contentCodename.requestFocus();
            return null;
        } else {
            binding.contentCodename.setError(null);
            binding.contentCodename.setHelperTextEnabled(false);
        }

        if (value.equals("")) {
            binding.contentCodeval.setError(getString(R.string.err_nocodeval));
            binding.contentCodeval.requestFocus();
            return null;
        } else {
            binding.contentCodeval.setError(null);
            binding.contentCodeval.setHelperTextEnabled(false);
        }

        // checks ok, build Code object with user input
        return (new Code(name, value, categ, comments, protectMode));
    }

    private void onSaveClicked () {
        Code code = getUserInput();

        // perform checks via ViewModel and handle errors
        switch (mViewModel.checkCodeBusinessLogic(code, mAction)) {
            // Code must be not null and code name must not be empty
            case CodeViewModel.NO_CODE:
            case CodeViewModel.NO_CODENAME:
                return;
            // Cannot overwrite existing code
            case CodeViewModel.CODENAME_ALREADY_EXISTS:
                binding.contentCodename.setError(getString(R.string.err_name_already_exists));
                binding.contentCodename.requestFocus();
                return;
            case CodeViewModel.CODE_OK:
                break;
            default:
                Log.e(TAG, "Checking code : unexpected return value");
        }

        if (mAction == Code.MODE_INSERT) {
            // set Insertion mode and Code to process in the ViewModel
            mViewModel.selectActionToProcess(Code.MODE_INSERT);
            mViewModel.selectCodeToProcess(code);
            mViewModel.insertCode();

        } else {
            mViewModel.selectActionToProcess(Code.MODE_UPDATE);
            assert code != null;
            mViewModel.fillCodeToProcess(code);
            mViewModel.updateCode();
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