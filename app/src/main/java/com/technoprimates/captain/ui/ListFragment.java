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
import com.technoprimates.captain.ItemViewModel;
import com.technoprimates.captain.R;
import com.technoprimates.captain.databinding.FragmentListBinding;
import com.technoprimates.captain.db.Item;

public class ListFragment extends Fragment implements ItemListAdapter.CodeActionListener {

    // ViewModel scoped to the Activity
    private ItemViewModel mViewModel;

    // binding
    private FragmentListBinding binding;

    // Adapter for the RecyclerView
    private ItemListAdapter adapter;

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
        mViewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);

        // Floating action button for adding a new Item item
        binding.fab.setOnClickListener(view1 -> {
            // Set in the ViewModel the action to process, no db-existing Item required in this case
            mViewModel.selectActionToProcess(Item.MODE_INSERT);
            mViewModel.selectItemToProcess(null);

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
        mViewModel.getAllitems().observe(getViewLifecycleOwner(),
                codes -> {
                    adapter.setCodeList(codes); // update RV
                });
    }

    //Sets the RecyclerView
    private void recyclerSetup() {
        adapter = new ItemListAdapter(R.layout.item_item, this);
        binding.codeRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.codeRecycler.setAdapter(adapter);

        // swipe detection
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override //not used
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {return false;}

            @Override //swipe
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                onDeleteCodeRequest(viewHolder.getBindingAdapterPosition());
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(binding.codeRecycler);
    }

    /**
     * {@inheritDoc}
     * <p>This triggers a navigation to the Visualization Fragment.
     * If the Item is protected by fingerprint, a authentication is performed first.</p>
     */
    @Override
    public void onCodeClicked(Item item) {

        // Set in the ViewModel the action to process, and the Item to process
        mViewModel.selectActionToProcess(Item.MODE_VISU);
        mViewModel.selectItemToProcess(item);

        // check if the Item is fingerprint protected
        if (item.getItemProtectMode() == Item.FINGERPRINT_PROTECTED) {

            // Item protected : Ask for the user's fingerprint
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
                    .navigate(R.id.action_ListFragment_to_VisuFragment);
        }
    }


    public void onDeleteCodeRequest(int pos) {

        // delete selected Item
        Item item = adapter.getCodeAtPos(pos);
        if (item == null) return;

        // Set in the ViewModel the action to process, and the Item to process
        mViewModel.selectActionToProcess(Item.MODE_DELETE);
        mViewModel.selectItemToProcess(item);
        mViewModel.deleteItem();

        // show snackbar with undo button
        Snackbar snackbar = Snackbar.make(binding.codeRecycler, "Item deleted at pos : "+pos, Snackbar.LENGTH_LONG);
        //
        snackbar.setAction("UNDO", view -> {
            // Set in the ViewModel the action to process, and the Item to process
            mViewModel.selectActionToProcess(Item.MODE_INSERT);
            mViewModel.selectItemToProcess(item);
            // Re-insert the Item
            mViewModel.reInsertItem();
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
                        .navigate(R.id.action_ListFragment_to_VisuFragment);
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
                Snackbar snackbar = Snackbar.make(binding.codeRecycler, "Canceled via signal", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
        return cancellationSignal;
    }


}