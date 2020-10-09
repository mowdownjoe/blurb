package com.mowdowndevelopments.blurb.ui.dialogs.iap

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.SkuType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.databinding.FragmentInAppPurchaseDialogBinding
import com.mowdowndevelopments.blurb.network.LoadingStatus
import com.mowdowndevelopments.blurb.ui.navHost.MainViewModel
import java.util.*

class InAppPurchaseDialogFragment : DialogFragment(), InAppPurchaseItemAdapter.OnItemClickListener {
    private lateinit var adapter: InAppPurchaseItemAdapter
    private lateinit var billingClient: BillingClient
    lateinit var binding: FragmentInAppPurchaseDialogBinding
    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = InAppPurchaseItemAdapter(this)

        val listener = PurchasesUpdatedListener { billingResult: BillingResult?, list: List<Purchase?>? ->
            viewModel.resetRetryAttempts()
            when (billingResult?.responseCode){
                BillingResponseCode.OK -> {
                    if (list != null){
                        dismiss()
                    } else {
                        viewModel.postNewErrorMessage(getString(R.string.iap_error))
                        FirebaseCrashlytics.getInstance()
                                .log("Somehow received null purchase list. Debug message: ${billingResult.debugMessage}")
                        requireDialog().cancel()
                    }
                }
                BillingResponseCode.USER_CANCELED -> {
                    requireDialog().cancel()
                }
                else -> {
                    viewModel.postNewErrorMessage(getString(R.string.iap_error))
                    FirebaseCrashlytics.getInstance()
                            .log("Unknown error. Debug message: ${billingResult?.debugMessage}")
                    requireDialog().cancel()
                }
            }
        }
        billingClient = BillingClient.newBuilder(requireActivity()).run {
            setListener(listener)
            enablePendingPurchases()
            build()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireActivity())
        binding = FragmentInAppPurchaseDialogBinding.inflate(requireActivity().layoutInflater)
        binding.rvItemList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItemList.adapter = adapter
        builder.setView(binding.root)
                .setNegativeButton(R.string.btn_cancel) { dialog: DialogInterface, _: Int ->
                    viewModel.resetRetryAttempts()
                    dialog.cancel()
                }
        return builder.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.inAppDialogStatus.observe(viewLifecycleOwner, { loadingStatus: LoadingStatus? ->
            when (loadingStatus) {
                LoadingStatus.LOADING -> {
                    binding.pbLoadingSpinner.visibility = View.VISIBLE
                    binding.rvItemList.visibility = View.INVISIBLE
                    binding.tvErrorText.visibility = View.INVISIBLE
                }
                LoadingStatus.WAITING, LoadingStatus.DONE -> {
                    binding.pbLoadingSpinner.visibility = View.INVISIBLE
                    binding.rvItemList.visibility = View.VISIBLE
                    binding.tvErrorText.visibility = View.INVISIBLE
                }
                LoadingStatus.ERROR -> {
                    binding.pbLoadingSpinner.visibility = View.INVISIBLE
                    binding.rvItemList.visibility = View.INVISIBLE
                    binding.tvErrorText.visibility = View.VISIBLE
                }
            }
        })
        beginDonationFlow()
    }

    private fun beginDonationFlow() {
        viewModel.postInAppDialogStatus(LoadingStatus.LOADING)
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    viewModel.postInAppDialogStatus(LoadingStatus.DONE)
                    getProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
                if (viewModel.canKeepRetryingPurchase()) {
                    viewModel.incrementRetryAttempts()
                    billingClient.startConnection(this)
                } else {
                    viewModel.resetRetryAttempts()
                    viewModel.postInAppDialogStatus(LoadingStatus.ERROR)
                }
            }
        })
    }

    private fun getProducts() {
        val skuList = ArrayList<String>()
        skuList.add("blurb_donation_drink_level")
        skuList.add("blurb_donation_lunch_level")
        skuList.add("blurb_donation_pizza_level")
        val params = SkuDetailsParams.newBuilder()
                .setType(SkuType.INAPP)
                .setSkusList(skuList)
        billingClient.querySkuDetailsAsync(params.build()) { billingResult: BillingResult, detailsList: List<SkuDetails>? ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                adapter.setData(requireNotNull(detailsList),
                        billingClient.queryPurchases(SkuType.INAPP).purchasesList!!)
            }
        }
    }

    override fun onItemClick(itemDetails: SkuDetails?) {
        val params = BillingFlowParams.newBuilder()
                .setSkuDetails(itemDetails!!)
                .build()
        billingClient.launchBillingFlow(requireActivity(), params)
    }
}