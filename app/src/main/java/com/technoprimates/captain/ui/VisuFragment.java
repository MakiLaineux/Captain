package com.technoprimates.captain.ui;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.technoprimates.captain.CodeViewModel;
import com.technoprimates.captain.R;
import com.technoprimates.captain.databinding.CodeViewBinding;
import com.technoprimates.captain.db.Code;


public class VisuFragment extends Fragment {

    public static final String TAG = "VISUFRAG";
    private CodeViewBinding binding;
    private CodeViewModel mViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(CodeViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // add fragment-specific menu using MenuProvider
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_visu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_edit) {
                    // Set in the ViewModel the action to process
                    // The Code to process is already set in the ViewModel
                    mViewModel.selectActionToProcess(Code.MODE_UPDATE);

                    // Navigate to EditFragment
                    NavHostFragment.findNavController(VisuFragment.this)
                            .navigate(R.id.action_VisuFragment_to_EditFragment);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        // binding
        binding = CodeViewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Code code = mViewModel.getCodeToProcess();
        if (code == null) {
            Log.e(TAG, "Error : no CurrentCode");
            return;
        }

        // fill fields
        binding.dispCateg.setText(code.getCodeCategory());
        binding.dispCodename.setText(code.getCodeName());
        binding.dispCodeval.setText(code.getCodeValue());
        binding.checkboxFingerprint.setChecked(code.getCodeProtectMode() == Code.FINGERPRINT_PROTECTED);
        binding.dispComments.setText(code.getCodeComments());
        binding.dispComments.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}