package com.technoprimates.captain.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.technoprimates.captain.R;
import com.technoprimates.captain.StueckViewModel;
import com.technoprimates.captain.databinding.FragmentHomeBinding;
import com.technoprimates.captain.db.Stueck;

import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    // ViewModel scoped to the Activity
    private StueckViewModel mViewModel;


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
        mViewModel = new ViewModelProvider(requireActivity()).get(StueckViewModel.class);

        // sets observer (that do nothing for now) to ensure livedata is initialized
        mViewModel.getAllStuecksList().observe(getViewLifecycleOwner(),
                new Observer<List<Stueck>>() {
                    @Override
                    public void onChanged(List<Stueck> allStuecks) {
                        // do nothing
                    }
                });

        binding.textviewFirst.setText(mViewModel.getProfile().toString());
        if (mViewModel.getProfiledStuecksList() != null)
            binding.textviewSecond.setText(String.valueOf(mViewModel.getProfiledStuecksList().size()));

        binding.buttonProfile.setOnClickListener(view1 -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_HomeFragment_to_ProfileFragment));

        binding.buttonList.setOnClickListener(view2 -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_HomeFragment_to_ListFragment));

        binding.buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {
                binding.textviewThird.setText("A");
           }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}