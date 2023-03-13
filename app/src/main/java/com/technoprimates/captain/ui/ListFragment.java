package com.technoprimates.captain.ui;

import android.annotation.SuppressLint;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.biometrics.BiometricPrompt.AuthenticationCallback;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.technoprimates.captain.StueckViewModel;
import com.technoprimates.captain.R;
import com.technoprimates.captain.databinding.FragmentListBinding;
import com.technoprimates.captain.db.Stueck;

import java.util.List;

public class ListFragment extends Fragment implements StueckListAdapter.StueckActionListener {

    // ViewModel scoped to the Activity
    private StueckViewModel mStueckViewModel;

    // binding
    private FragmentListBinding binding;

    // Adapter for the RecyclerView
    private StueckListAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // add fragment-specific menu using MenuProvider
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_list, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_profile) {
                    // navigate to editFragment
                    NavHostFragment.findNavController(ListFragment.this)
                            .navigate(R.id.action_ListFragment_to_ProfileFragment);
                    return true;
                }
                if (menuItem.getItemId() == R.id.action_settings) {
                    Toast.makeText(getActivity(), getString(R.string.toast_menu_settings), Toast.LENGTH_LONG).show();
                    return true;
                }
                if (menuItem.getItemId() == R.id.action_help) {
                    Toast.makeText(getActivity(), getString(R.string.toast_menu_help), Toast.LENGTH_LONG).show();
                    return true;
                }
                if (menuItem.getItemId() == R.id.action_about) {
                    Toast.makeText(getActivity(), getString(R.string.toast_menu_about), Toast.LENGTH_LONG).show();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        binding = FragmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Creates the ViewModel instance
        mStueckViewModel = new ViewModelProvider(requireActivity()).get(StueckViewModel.class);

        // Floating action button for adding a new Stueck item
        binding.fab.setOnClickListener(view1 -> {
            // Set in the ViewModel the action to process, no db-existing Stueck required in this case
            mStueckViewModel.selectActionToProcess(Stueck.MODE_INSERT);
            mStueckViewModel.selectStueckToProcess(null);

            // navigate to editFragment
            NavHostFragment.findNavController(ListFragment.this)
                    .navigate(R.id.action_ListFragment_to_EditFragment);
        });
        recyclerSetup();
        observerSetup();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //Sets the RecyclerView
    private void recyclerSetup() {
        adapter = new StueckListAdapter(R.layout.stueck_item, this, mStueckViewModel);
        binding.stueckRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.stueckRecycler.setAdapter(adapter);

        // swipe detection
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override //not used
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {return false;}

            @Override //swipe
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                onDeleteStueckRequest(viewHolder.getBindingAdapterPosition());
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(binding.stueckRecycler);
    }


    /*  Observe the LiveData List of all stücks
     Update viewmodel list of profiled stücks when this list is modified and refresh RV
     */
    @SuppressLint("NotifyDataSetChanged")
    private void observerSetup() {
        mStueckViewModel.getAllStuecksList().observe(getViewLifecycleOwner(),
                allStuecks -> {
                    // update viewmodel's profiled stücks list and update RV
                    mStueckViewModel.updateProfiledStuecksList();
                    adapter.notifyDataSetChanged();
                });
    }


    /**
     * {@inheritDoc}
     * <p>This triggers a navigation to the Visualization Fragment.
     */
    @Override
    public void onStueckClicked(Stueck stueck) {

        // Set in the ViewModel the action to process, and the Stueck to process
        mStueckViewModel.selectActionToProcess(Stueck.MODE_UPDATE);
        mStueckViewModel.selectStueckToProcess(stueck);

        NavHostFragment.findNavController(ListFragment.this)
                .navigate(R.id.action_ListFragment_to_EditFragment);
    }


    public void onDeleteStueckRequest(int pos) {

        // delete selected Stueck
        Stueck stueck = adapter.getStueckAtPos(pos);
        if (stueck == null) return;

        // Set in the ViewModel the action to process, and the Stueck to process
        mStueckViewModel.selectActionToProcess(Stueck.MODE_DELETE);
        mStueckViewModel.selectStueckToProcess(stueck);
        mStueckViewModel.deleteStueck();

        // show snackbar with undo button
        Snackbar snackbar = Snackbar.make(binding.stueckRecycler, "Stueck deleted at pos : "+pos, Snackbar.LENGTH_LONG);
        //
        snackbar.setAction("UNDO", view -> {
            // Set in the ViewModel the action to process, and the Stueck to process
            mStueckViewModel.selectActionToProcess(Stueck.MODE_INSERT);
            mStueckViewModel.selectStueckToProcess(stueck);
            // Re-insert the Stueck
            mStueckViewModel.reInsertStueck();
        });
        snackbar.show();
    }
}