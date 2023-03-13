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
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.technoprimates.captain.StueckViewModel;

import com.technoprimates.captain.R;
import com.technoprimates.captain.db.Profile;
import com.technoprimates.captain.db.Stueck;

import java.util.Objects;

/*
No view binding for this fragment, as the numerous checkboxes are defined with generic xml names
 and retrieved via findViewById using programmatically determinated names
 */

public class EditFragment extends Fragment {

    public static final String TAG = "EDIT FRAG";

    // UI name and checkboxes
    private TextInputLayout mStueckName;
    private final CheckBox[] mProfileCheckBox = new CheckBox[Profile.NB_CHECKBOX];

    // ViewModel scoped to the Activity
    private StueckViewModel mViewModel;

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

        // no binding
        return inflater.inflate(R.layout.fragment_edit, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(StueckViewModel.class);
        mAction = mViewModel.getActionMode();
    }

    @SuppressWarnings({"DiscouragedApi", "ConstantConditions"})
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // find text area and checkboxes in the layout
        mStueckName = (TextInputLayout) getView().findViewById(R.id.stueck_name);
        for (int i = 0; i<Profile.NB_CHECKBOX ; i++) {
            String boxName = Profile.CHECKBOXNAME + i;
            int boxId = getResources().getIdentifier(boxName, "id", requireActivity().getPackageName());
            mProfileCheckBox[i] = getView().findViewById(boxId);
        }

        // fill UI fields
        switch (mAction) {
            case Stueck.MODE_UPDATE:
                // update an existing Stueck available in the ViewModel
                // fill the fields with existing Stueck
                setString(mStueckName, mViewModel.getStueckToProcess().getName());

                // Init checkboxes from unpacked values stored in the viewmodel current stueck
                String boolFields = mViewModel.getStueckToProcess().getBoolFields();
                assert (boolFields.length() == Profile.NB_CHECKBOX);

                for (int i = 0; i<Profile.NB_CHECKBOX ; i++) {
                    mProfileCheckBox[i].setChecked(boolFields.charAt(i) != ' ');
                }

                // Set the fragment title
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.title_update);
                break;

            case Stueck.MODE_INSERT:
                // insert a new Stueck : start with empty fields
                setString(mStueckName, "");
                for (int i = 0; i<Profile.NB_CHECKBOX ; i++) {
                    mProfileCheckBox[i].setChecked(true);
                }
                // Set the fragment title
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.title_add);
        }

        // start with focus on first input
        mStueckName.requestFocus();

        // listeners, triggered when losing the focus, for clearing a previous "empty field" error message
        mStueckName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // lost the focus, there may be an error msg to clear
                if (mStueckName.getError() != null) {
                    // there is currently an error msg, check if it is to be cleared
                    if ((mStueckName.getEditText() != null)
                            && (!TextUtils.isEmpty(mStueckName.getEditText().getText().toString()))) {
                        // Stueck name not empty, the error message can be cleared
                        mStueckName.setError(null);
                    }
                }
            }
        });
    }


    /**
     * Gets user inputs and perform basic checks.
     *
     * @return A Stueck object containing user input
     */
    private Stueck getUserInput() {

        // get name
        String name = getString(mStueckName);

        // fill a string with checkboxes values (' ' for false, 'X' for true)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<Profile.NB_CHECKBOX ; i++) {
            sb.append(mProfileCheckBox[i].isChecked() ? 'X' : ' ');
        }
        String boolFields = sb.toString();

        // check name
        if (name.equals("")) {
            mStueckName.setError(getString(R.string.err_noname));
            mStueckName.requestFocus();
            return null;
        } else {
            mStueckName.setError(null);
            mStueckName.setHelperTextEnabled(false);
        }

        // basic checks ok, build Stueck object with user input
        return (new Stueck(name, boolFields));
    }

    private void onSaveClicked () {
        Stueck stueck = getUserInput();

        // perform checks via ViewModel and handle errors
        switch (mViewModel.checkStueckBusinessLogic(stueck, mAction)) {
            // Stueck must be not null and stueck name must not be empty
            case StueckViewModel.NO_STUECK:
            case StueckViewModel.NO_STUECK_NAME:
                return;
            // Cannot overwrite existing stueck
            case StueckViewModel.STUECK_NAME_ALREADY_EXISTS:
                mStueckName.setError(getString(R.string.err_name_already_exists));
                mStueckName.requestFocus();
                return;
            // Cannot overwrite existing stueck
            case StueckViewModel.INVALID_STUECK_BOOLEANS:
                Snackbar snackbar = Snackbar.make(getView(), "invalid profile", Snackbar.LENGTH_LONG);
                snackbar.show();
                return;
            case StueckViewModel.STUECK_OK:
                break;
            default:
                Log.e(TAG, "Checking stueck : unexpected return value");
        }

        if (mAction == Stueck.MODE_INSERT) {
            // set Insertion mode and Stueck to process in the ViewModel
            mViewModel.selectActionToProcess(Stueck.MODE_INSERT);
            mViewModel.selectStueckToProcess(stueck);
            mViewModel.insertStueck();

        } else {
            mViewModel.selectActionToProcess(Stueck.MODE_UPDATE);
            assert stueck != null;
            mViewModel.fillStueckToProcess(stueck);
            mViewModel.updateStueck();
        }

        // save completed, return to the list fragment
        NavHostFragment.findNavController(EditFragment.this).navigateUp();
    }

    /* uncomment if binding
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
 */

    // utility methods for TextInputLayout
    private String getString(TextInputLayout textInputLayout) {
        return ((textInputLayout.getEditText() == null)? "" : textInputLayout.getEditText().getText().toString());
    }
    private void setString(TextInputLayout textInputLayout, String s) {
        if (textInputLayout.getEditText() != null)
            textInputLayout.getEditText().setText(s);
    }
}