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

import com.google.android.material.textfield.TextInputLayout;
import com.technoprimates.captain.StuckViewModel;

import com.technoprimates.captain.R;
import com.technoprimates.captain.db.Profile;
import com.technoprimates.captain.db.Stuck;

import java.util.Objects;

/*
No view binding for this fragment, as the numerous checkboxes are defined with generic xml names
 and retrieved via findViewById using programmatically determinated names
 */

public class EditFragment extends Fragment {

    public static final String TAG = "EDIT FRAG";

    // UI name and checkboxes
    private TextInputLayout mStuckName;
    private final CheckBox[] mProfileCheckBox = new CheckBox[Profile.NB_CHECKBOX];

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

        // no binding
        return inflater.inflate(R.layout.fragment_edit, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(StuckViewModel.class);
        mAction = mViewModel.getActionMode();
    }

    @SuppressWarnings({"DiscouragedApi", "ConstantConditions"})
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // find text area and checkboxes in the layout
        mStuckName = (TextInputLayout) getView().findViewById(R.id.stuck_name);
        for (int i = 0; i<Profile.NB_CHECKBOX ; i++) {
            String boxName = Profile.CHECKBOXNAME + i;
            int boxId = getResources().getIdentifier(boxName, "id", requireActivity().getPackageName());
            mProfileCheckBox[i] = getView().findViewById(boxId);
        }

        // fill UI fields
        switch (mAction) {
            case Stuck.MODE_UPDATE:
                // update an existing Stuck available in the ViewModel
                // fill the fields with existing Stuck
                setString(mStuckName, mViewModel.getStuckToProcess().getName());

                // Init checkboxes from unpacked values stored in the viewmodel current stueck
                String boolFields = mViewModel.getStuckToProcess().getBoolFields();
                assert (boolFields.length() == Profile.NB_CHECKBOX);

                for (int i = 0; i<Profile.NB_CHECKBOX ; i++) {
                    mProfileCheckBox[i].setChecked(boolFields.charAt(i) != ' ');
                }

                // Set the fragment title
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.title_update);
                break;

            case Stuck.MODE_INSERT:
                // insert a new Stuck : start with empty fields
                setString(mStuckName, "");
                for (int i = 0; i<Profile.NB_CHECKBOX ; i++) {
                    mProfileCheckBox[i].setChecked(true);
                }
                // Set the fragment title
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.title_add);
        }

        // start with focus on first input
        mStuckName.requestFocus();

        // listeners, triggered when losing the focus, for clearing a previous "empty field" error message
        mStuckName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // lost the focus, there may be an error msg to clear
                if (mStuckName.getError() != null) {
                    // there is currently an error msg, check if it is to be cleared
                    if ((mStuckName.getEditText() != null)
                            && (!TextUtils.isEmpty(mStuckName.getEditText().getText().toString()))) {
                        // Stuck name not empty, the error message can be cleared
                        mStuckName.setError(null);
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

        // get name
        String name = getString(mStuckName);

        // fill a string with checkboxes values (' ' for false, 'X' for true)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<Profile.NB_CHECKBOX ; i++) {
            sb.append(mProfileCheckBox[i].isChecked() ? 'X' : ' ');
        }
        String boolFields = sb.toString();

        // check name
        if (name.equals("")) {
            mStuckName.setError(getString(R.string.err_noname));
            mStuckName.requestFocus();
            return null;
        } else {
            mStuckName.setError(null);
            mStuckName.setHelperTextEnabled(false);
        }

        // basic checks ok, build Stuck object with user input
        return (new Stuck(name, boolFields));
    }

    private void onSaveClicked () {
        Stuck stuck = getUserInput();

        // perform checks via ViewModel and handle errors
        // TODO handle invalid booleans
        switch (mViewModel.checkStuckBusinessLogic(stuck, mAction)) {
            // Stuck must be not null and stuck name must not be empty
            case StuckViewModel.NO_STUCK:
            case StuckViewModel.NO_STUCK_NAME:
                return;
            // Cannot overwrite existing stuck
            case StuckViewModel.STUCK_NAME_ALREADY_EXISTS:
                mStuckName.setError(getString(R.string.err_name_already_exists));
                mStuckName.requestFocus();
                return;
            case StuckViewModel.STUCK_OK:
                break;
            default:
                Log.e(TAG, "Checking stuck : unexpected return value");
        }

        if (mAction == Stuck.MODE_INSERT) {
            // set Insertion mode and Stuck to process in the ViewModel
            mViewModel.selectActionToProcess(Stuck.MODE_INSERT);
            mViewModel.selectStuckToProcess(stuck);
            mViewModel.insertStuck();

        } else {
            mViewModel.selectActionToProcess(Stuck.MODE_UPDATE);
            assert stuck != null;
            mViewModel.fillStuckToProcess(stuck);
            mViewModel.updateStuck();
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