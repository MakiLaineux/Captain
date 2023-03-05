package com.technoprimates.captain.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.technoprimates.captain.R;
import com.technoprimates.captain.db.Profile;

/*
No view binding for this fragment, as checkboxes are retrieved via findViewById with programmatically defined xml names
 */
public class ProfileFragment extends Fragment {

    // UI checkboxes
    private final CheckBox[] mProfileCheckBox = new CheckBox[Profile.NB_CHECKBOX];

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

        // The current user preferences for profiling are stored in SharedPreferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(requireActivity());

        /* Set the UI Views : For the profiling checkboxes, xml id names follow a common pattern
        which consists of appending to a string constant consecutive numbers (starting with 0) */

        for (int i = 0; i<Profile.NB_CHECKBOX ; i++) {
            // find checkbox
            String boxName = Profile.CHECKBOXNAME + i;
            int boxId = getResources().getIdentifier(boxName, "id", requireActivity().getPackageName());
            mProfileCheckBox[i] = getView().findViewById(boxId);
            // Init checkbox from boolean profile values stored in shared preferences
            String prefName = Profile.PROFILE_BOOL + i;
            mProfileCheckBox[i].setChecked(sharedPref.getBoolean(prefName, true));
        }

        getView().findViewById(R.id.button_second).setOnClickListener(view1 -> onSaveClicked());
    }

/* uncomment if binding
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
 */

    private void onSaveClicked () {

        // Get UI's checkbox status and update shared preferences
        boolean[] boolArray = new boolean[Profile.NB_CHECKBOX];

        //saving the profile booleans in shared preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        SharedPreferences.Editor editor = sharedPref.edit();

        for (int i = 0; i<Profile.NB_CHECKBOX ; i++) {
            String prefName = Profile.PROFILE_BOOL + i;
            boolArray[i] = mProfileCheckBox[i].isChecked();
            editor.putBoolean(prefName, boolArray[i]);
        }
        editor.apply();

        NavHostFragment.findNavController(ProfileFragment.this)
                .navigateUp();
    }
}