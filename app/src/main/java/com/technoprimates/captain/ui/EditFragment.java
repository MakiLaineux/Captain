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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.technoprimates.captain.StuckViewModel;
import com.technoprimates.captain.R;
import com.technoprimates.captain.databinding.StuckEditBinding;
import com.technoprimates.captain.db.Stuck;

import java.util.Objects;

public class EditFragment extends Fragment {

    public static final String TAG = "EDIT FRAG";

    //binding
    private StuckEditBinding binding;

    // ViewModel scoped to the Activity
    private StuckViewModel mViewModel;

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
        binding = StuckEditBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(StuckViewModel.class);
        mAction = mViewModel.getActionMode();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switch (mAction) {

            case Stuck.MODE_UPDATE:
                // update an existing Stuck available in the ViewModel
                // fill the fields with existing Stuck
                setString(binding.stuckName, mViewModel.getStuckToProcess().getName());

                // unpacking boolean fields
                char[] boolFields = new char[5];
                mViewModel.getStuckToProcess().getBoolFields().getChars(0,4, boolFields, 0);
                binding.ckbField1.setChecked(boolFields[0] == '1') ;
                binding.ckbField2.setChecked(boolFields[1] == '1') ;
                binding.ckbField3.setChecked(boolFields[2] == '1') ;
                binding.ckbField4.setChecked(boolFields[3] == '1') ;
                // Set the fragment title
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.title_update);
                break;

            case Stuck.MODE_INSERT:
                // insert a new Stuck : start with empty fields
                setString(binding.stuckName, "");
                binding.ckbField1.setChecked(false);
                binding.ckbField2.setChecked(false);
                binding.ckbField3.setChecked(false);
                binding.ckbField4.setChecked(false);
                // Set the fragment title
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.title_add);
        }

        // start with focus on first input
        binding.stuckName.requestFocus();


        // listeners, triggered when losing the focus, for clearing a previous "empty field" error message
        binding.stuckName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // lost the focus, there may be an error msg to clear
                if (binding.stuckName.getError() != null) {
                    // there is currently an error msg, check if it is to be cleared
                    if ((binding.stuckName.getEditText() != null)
                            && (!TextUtils.isEmpty(binding.stuckName.getEditText().getText().toString()))) {
                        // Stuck name not empty, the error message can be cleared
                        binding.stuckName.setError(null);
                    }
                }
            }
        });

    }


    /**
     * Gets user inputs and perform basic checks.
     *
     * @return A Stuck object containing user input
     */
    private Stuck getUserInput() {
        // get user inputs
        String name = getString(binding.stuckName);

        // pack boolean values
        StringBuilder sb = new StringBuilder();
        sb.append(binding.ckbField1.isChecked() ? '1' : '0');
        sb.append(binding.ckbField2.isChecked() ? '1' : '0');
        sb.append(binding.ckbField3.isChecked() ? '1' : '0');
        sb.append(binding.ckbField4.isChecked() ? '1' : '0');
        String boolFields = sb.toString();

        // check name
        if (name.equals("")) {
            binding.stuckName.setError(getString(R.string.err_noname));
            binding.stuckName.requestFocus();
            return null;
        } else {
            binding.stuckName.setError(null);
            binding.stuckName.setHelperTextEnabled(false);
        }

        // checks ok, build Stuck object with user input
        return (new Stuck(name, boolFields));
    }

    private void onSaveClicked () {
        Stuck item = getUserInput();

        // perform checks via ViewModel and handle errors
        switch (mViewModel.checkStuckBusinessLogic(item, mAction)) {
            // Stuck must be not null and item name must not be empty
            case StuckViewModel.NO_STUCK:
            case StuckViewModel.NO_STUCK_NAME:
                return;
            // Cannot overwrite existing item
            case StuckViewModel.STUCK_NAME_ALREADY_EXISTS:
                binding.stuckName.setError(getString(R.string.err_name_already_exists));
                binding.stuckName.requestFocus();
                return;
            case StuckViewModel.STUCK_OK:
                break;
            default:
                Log.e(TAG, "Checking stuck : unexpected return value");
        }

        if (mAction == Stuck.MODE_INSERT) {
            // set Insertion mode and Stuck to process in the ViewModel
            mViewModel.selectActionToProcess(Stuck.MODE_INSERT);
            mViewModel.selectStuckToProcess(item);
            mViewModel.insertStuck();

        } else {
            mViewModel.selectActionToProcess(Stuck.MODE_UPDATE);
            assert item != null;
            mViewModel.fillStuckToProcess(item);
            mViewModel.updateStuck();
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