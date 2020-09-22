package com.mowdowndevelopments.blurb.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.databinding.FragmentInAppPurchaseDialogBinding;
import com.mowdowndevelopments.blurb.network.LoadingStatus;
import com.mowdowndevelopments.blurb.network.Singletons;
import com.mowdowndevelopments.blurb.ui.navHost.MainViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static java.util.Objects.requireNonNull;


public class InAppPurchaseDialogFragment extends DialogFragment implements InAppPurchaseItemAdapter.OnItemClickListener {

    private InAppPurchaseItemAdapter adapter;
    FragmentInAppPurchaseDialogBinding binding;
    private MainViewModel viewModel;

    public InAppPurchaseDialogFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        adapter = new InAppPurchaseItemAdapter(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        binding = FragmentInAppPurchaseDialogBinding.inflate(requireActivity().getLayoutInflater());

        binding.rvItemList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvItemList.setAdapter(adapter);

        builder.setView(binding.getRoot())
                .setNegativeButton(R.string.btn_cancel, (dialog, i) -> {
                    viewModel.resetRetryAttempts();
                    dialog.cancel();
                });

        return builder.create();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel.getInAppDialogStatus().observe(getViewLifecycleOwner(), loadingStatus -> {
            switch (loadingStatus){
                case LOADING:
                    binding.pbLoadingSpinner.setVisibility(View.VISIBLE);
                    binding.rvItemList.setVisibility(View.INVISIBLE);
                    binding.tvErrorText.setVisibility(View.INVISIBLE);
                    break;
                case WAITING:
                case DONE:
                    binding.pbLoadingSpinner.setVisibility(View.INVISIBLE);
                    binding.rvItemList.setVisibility(View.VISIBLE);
                    binding.tvErrorText.setVisibility(View.INVISIBLE);
                    break;
                case ERROR:
                    binding.pbLoadingSpinner.setVisibility(View.INVISIBLE);
                    binding.rvItemList.setVisibility(View.INVISIBLE);
                    binding.tvErrorText.setVisibility(View.VISIBLE);
                    break;
            }
        });

        beginDonationFlow();
    }

    private void beginDonationFlow() {
        viewModel.postInAppDialogStatus(LoadingStatus.LOADING);

        BillingClient billingClient = Singletons.getBillingClient(requireContext());
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    viewModel.postInAppDialogStatus(LoadingStatus.DONE);
                    getProducts();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                if (viewModel.canKeepRetryingPurchase()){
                    viewModel.incrementRetryAttempts();
                    billingClient.startConnection(this);
                } else {
                    viewModel.resetRetryAttempts();
                    viewModel.postInAppDialogStatus(LoadingStatus.ERROR);
                }
            }
        });
    }

    private void getProducts() {
        ArrayList<String> skuList = new ArrayList<>();
        skuList.add("blurb_donation_drink_level");
        skuList.add("blurb_donation_lunch_level");
        skuList.add("blurb_donation_pizza_level");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder()
                .setType(BillingClient.SkuType.INAPP)
                .setSkusList(skuList);
        BillingClient billingClient = Singletons.getBillingClient(requireActivity());
        billingClient.querySkuDetailsAsync(params.build(), (billingResult, detailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                adapter.setData(detailsList, billingClient.queryPurchases(BillingClient.SkuType.INAPP).getPurchasesList());
            }
        });
    }

    @Override
    public void onItemClick(SkuDetails itemDetails) {
        BillingFlowParams params = BillingFlowParams.newBuilder()
                .setSkuDetails(itemDetails)
                .build();
        int responseCode = Singletons.getBillingClient(requireContext())
                .launchBillingFlow(requireActivity(), params)
                .getResponseCode();
        viewModel.resetRetryAttempts();
        if (responseCode == BillingClient.BillingResponseCode.OK){
            dismiss();
        } else {
            requireNonNull(getDialog()).cancel();
            viewModel.postNewErrorMessage(getString(R.string.iap_error));
        }
    }
}