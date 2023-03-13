package com.technoprimates.captain.ui;

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
import androidx.lifecycle.Observer;
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
    private StueckViewModel mViewModel;

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
        mViewModel = new ViewModelProvider(requireActivity()).get(StueckViewModel.class);

        // Floating action button for adding a new Stueck item
        binding.fab.setOnClickListener(view1 -> {
            // Set in the ViewModel the action to process, no db-existing Stueck required in this case
            mViewModel.selectActionToProcess(Stueck.MODE_INSERT);
            mViewModel.selectStueckToProcess(null);

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
        adapter = new StueckListAdapter(R.layout.stueck_item, this, mViewModel);
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


    // Observe the LiveData List of all stücks and update adapter when this list is modified
    private void observerSetup() {
        mViewModel.getAllStuecksList().observe(getViewLifecycleOwner(),
                new Observer<List<Stueck>>() {
                    @Override
                    public void onChanged(List<Stueck> allStuecks) {
                        // update viewmodel's profiled stücks list and update RV
                        adapter.updateProfiledStuecksList();
                    }
                });
    }


    /**
     * {@inheritDoc}
     * <p>This triggers a navigation to the Visualization Fragment.
     */
    @Override
    public void onStueckClicked(Stueck stueck) {

        // Set in the ViewModel the action to process, and the Stueck to process
        mViewModel.selectActionToProcess(Stueck.MODE_UPDATE);
        mViewModel.selectStueckToProcess(stueck);

        NavHostFragment.findNavController(ListFragment.this)
                .navigate(R.id.action_ListFragment_to_EditFragment);
    }


    public void onDeleteStueckRequest(int pos) {

        // delete selected Stueck
        Stueck stueck = adapter.getStueckAtPos(pos);
        if (stueck == null) return;

        // Set in the ViewModel the action to process, and the Stueck to process
        mViewModel.selectActionToProcess(Stueck.MODE_DELETE);
        mViewModel.selectStueckToProcess(stueck);
        mViewModel.deleteStueck();

        // show snackbar with undo button
        Snackbar snackbar = Snackbar.make(binding.stueckRecycler, "Stueck deleted at pos : "+pos, Snackbar.LENGTH_LONG);
        //
        snackbar.setAction("UNDO", view -> {
            // Set in the ViewModel the action to process, and the Stueck to process
            mViewModel.selectActionToProcess(Stueck.MODE_INSERT);
            mViewModel.selectStueckToProcess(stueck);
            // Re-insert the Stueck
            mViewModel.reInsertStueck();
        });
        snackbar.show();
    }

    /**
     * Defines the methods to handle the authentication events
     */
    private AuthenticationCallback getAuthenticationCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                Toast.makeText(getActivity(), getString(R.string.toast_authentication_failed), Toast.LENGTH_LONG).show();
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                super.onAuthenticationHelp(helpCode, helpString);
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                Toast.makeText(getActivity(), getString(R.string.toast_authentication_success), Toast.LENGTH_LONG).show();
                super.onAuthenticationSucceeded(result);

                // Navigate to the visualization fragment
                NavHostFragment.findNavController(ListFragment.this)
                        .navigate(R.id.action_ListFragment_to_EditFragment);
            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(getActivity(), getString(R.string.toast_authentication_failed), Toast.LENGTH_LONG).show();
                super.onAuthenticationFailed();
            }
        };
    }

    /**
     * Defines the CancellationSignal object
     * Call cancel() on this object to cancel the authentication attempt
     */
    private CancellationSignal getCancellationSignal() {
        CancellationSignal cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            /**
             * Callback launched after cancellation
             */
            @Override
            public void onCancel() {
                Snackbar snackbar = Snackbar.make(binding.stueckRecycler, "Canceled via signal", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
        return cancellationSignal;
    }


}