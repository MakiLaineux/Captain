package com.technoprimates.captain.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.technoprimates.captain.R;
import com.technoprimates.captain.StueckViewModel;
import com.technoprimates.captain.databinding.FragmentHomeBinding;

import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    // ViewModel scoped to the Activity
    private StueckViewModel mStueckViewModel;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Creates the ViewModel instance
        mStueckViewModel = new ViewModelProvider(requireActivity()).get(StueckViewModel.class);

        binding.textviewSecond.setText(String.valueOf(mStueckViewModel.nbNextNames()));

        // observe livedata and update the profiled stücks list
        mStueckViewModel.getAllStuecksList().observe(getViewLifecycleOwner(),
                allStuecks -> {
                    // update viewmodel's profiled stücks list
                    mStueckViewModel.updateProfiledStuecksList();
                    binding.textviewSecond.setText(String.valueOf(mStueckViewModel.nbNextNames()));
                });

        binding.textviewFirst.setText(mStueckViewModel.getProfile().toString());

        binding.buttonProfile.setOnClickListener(view1 -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_HomeFragment_to_ProfileFragment));

        binding.buttonList.setOnClickListener(view2 -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_HomeFragment_to_ListFragment));

        // on click : pop name and display it
        binding.buttonNext.setOnClickListener(view2 -> displayName());

        // reset : rebuild nextnames list
        binding.buttonReset.setOnClickListener(view3 -> rebuildNames());
    }

    // Pop a name from list and display it
    private void displayName() {
        int nbOfNames = mStueckViewModel.nbNextNames();
        if ( nbOfNames== 0) {
            binding.textviewThird.setText("No name to display");
        } else {
            // get random position in list
            Random r = new Random();
            int i = r.nextInt(nbOfNames);
            binding.textviewThird.setText(mStueckViewModel.popNextName(i));
            binding.textviewSecond.setText(String.valueOf(mStueckViewModel.nbNextNames()));
        }
    }

    // Pop a name from list and display it
    private void rebuildNames() {
        mStueckViewModel.rebuildNextnames();
        binding.textviewSecond.setText(String.valueOf(mStueckViewModel.nbNextNames()));
        binding.textviewThird.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}