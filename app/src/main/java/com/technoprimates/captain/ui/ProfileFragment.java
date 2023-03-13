package com.technoprimates.captain.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.technoprimates.captain.R;
import com.technoprimates.captain.StueckViewModel;
import com.technoprimates.captain.db.Profile;
import com.technoprimates.captain.db.Stueck;

import java.util.List;

/*
No view binding for this fragment, as checkboxes are retrieved via findViewById with programmatically defined xml names
 */
public class ProfileFragment extends Fragment {

    // UI checkboxes
    private final CheckBox[] mProfileCheckBox = new CheckBox[Profile.NB_CHECKBOX];

    // ViewModel scoped to the Activity
    private StueckViewModel mViewModel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(StueckViewModel.class);
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // no binding
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @SuppressWarnings({"DiscouragedApi", "ConstantConditions"})
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* Set the UI Views : For the profiling checkboxes, xml id names follow a common pattern
        which consists of appending to a string constant consecutive numbers (starting with 0) */

        for (int i = 0; i<Profile.NB_CHECKBOX ; i++) {
            // find checkbox
            String boxName = Profile.CHECKBOXNAME + i;
            int boxId = getResources().getIdentifier(boxName, "id", requireActivity().getPackageName());
            mProfileCheckBox[i] = getView().findViewById(boxId);
            // Init checkbox from boolean profile values stored in the ViewModel profile
            mProfileCheckBox[i].setChecked(mViewModel.getProfile().isEnabled(i));
        }

        getView().findViewById(R.id.button_second).setOnClickListener(view1 -> onSaveClicked());
    }

    private void onSaveClicked () {

        // build a string with checkboxes values (' ' for false, 'X' for true)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<Profile.NB_CHECKBOX ; i++) {
            sb.append(mProfileCheckBox[i].isChecked() ? 'X' : ' ');
        }

        // store the new profile in the Viewmodel
        boolean result = mViewModel.setProfile(sb.toString());

        // if ok navigate up, else display an error
        if (result)
            NavHostFragment.findNavController(ProfileFragment.this)
                    .navigateUp();
        else {
            Snackbar snackbar = Snackbar.make(getView(), "invalid profile", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }
}