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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.technoprimates.captain.StuckViewModel;
import com.technoprimates.captain.R;
import com.technoprimates.captain.databinding.FragmentListBinding;
import com.technoprimates.captain.db.Stuck;

public class ListFragment extends Fragment implements StuckListAdapter.StuckActionListener {

    // ViewModel scoped to the Activity
    private StuckViewModel mViewModel;

    // binding
    private FragmentListBinding binding;

    // Adapter for the RecyclerView
    private StuckListAdapter adapter;

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
        mViewModel = new ViewModelProvider(requireActivity()).get(StuckViewModel.class);

        // Floating action button for adding a new Stuck item
        binding.fab.setOnClickListener(view1 -> {
            // Set in the ViewModel the action to process, no db-existing Stuck required in this case
            mViewModel.selectActionToProcess(Stuck.MODE_INSERT);
            mViewModel.selectStuckToProcess(null);

            // navigate to editFragment
            NavHostFragment.findNavController(ListFragment.this)
                    .navigate(R.id.action_ListFragment_to_EditFragment);
        });
        observerSetup();
        recyclerSetup();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Observe the LiveData List of Codes
    private void observerSetup() {
        mViewModel.getAllstucks().observe(getViewLifecycleOwner(),
                stucks -> {
                    adapter.setStuckList(stucks); // update RV
                });
    }

    //Sets the RecyclerView
    private void recyclerSetup() {
        adapter = new StuckListAdapter(R.layout.stuck_item, this);
        binding.stuckRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.stuckRecycler.setAdapter(adapter);

        // swipe detection
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override //not used
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {return false;}

            @Override //swipe
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                onDeleteStuckRequest(viewHolder.getBindingAdapterPosition());
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(binding.stuckRecycler);
    }

    /**
     * {@inheritDoc}
     * <p>This triggers a navigation to the Visualization Fragment.
     * If the Stuck is protected by fingerprint, a authentication is performed first.</p>
     */
    @Override
    public void onStuckClicked(Stuck stuck) {

        // Set in the ViewModel the action to process, and the Stuck to process
        mViewModel.selectActionToProcess(Stuck.MODE_UPDATE);
        mViewModel.selectStuckToProcess(stuck);

        // check if the Stuck is fingerprint protected
        if (stuck.getProtectMode() == Stuck.FINGERPRINT_PROTECTED) {

            // Stuck protected : Ask for the user's fingerprint
            BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(getActivity())
                    .setTitle(getString(R.string.app_name))
                    .setSubtitle(getString(R.string.prompt_authentication_required))
                    .setDescription((getString(R.string.prompt_item_protected_by_fingerprint)))
                    .setNegativeButton(getString(R.string.prompt_cancel), requireActivity().getMainExecutor(), (dialog, which) -> Toast.makeText(getActivity(), getString(R.string.toast_authentication_cancelled), Toast.LENGTH_LONG).show())
                    .build();

            // launch authentication : if successfull, the callback will launch the navigation to the visu fragment
            biometricPrompt.authenticate(getCancellationSignal(), requireActivity().getMainExecutor(), getAuthenticationCallback());

        } else {
            // No authentication required : navigate to visualization of currentCode
            NavHostFragment.findNavController(ListFragment.this)
                    .navigate(R.id.action_ListFragment_to_EditFragment);
        }
    }


    public void onDeleteStuckRequest(int pos) {

        // delete selected Stuck
        Stuck stuck = adapter.getStuckAtPos(pos);
        if (stuck == null) return;

        // Set in the ViewModel the action to process, and the Stuck to process
        mViewModel.selectActionToProcess(Stuck.MODE_DELETE);
        mViewModel.selectStuckToProcess(stuck);
        mViewModel.deleteStuck();

        // show snackbar with undo button
        Snackbar snackbar = Snackbar.make(binding.stuckRecycler, "Stuck deleted at pos : "+pos, Snackbar.LENGTH_LONG);
        //
        snackbar.setAction("UNDO", view -> {
            // Set in the ViewModel the action to process, and the Stuck to process
            mViewModel.selectActionToProcess(Stuck.MODE_INSERT);
            mViewModel.selectStuckToProcess(stuck);
            // Re-insert the Stuck
            mViewModel.reInsertStuck();
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
                Snackbar snackbar = Snackbar.make(binding.stuckRecycler, "Canceled via signal", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
        return cancellationSignal;
    }


}