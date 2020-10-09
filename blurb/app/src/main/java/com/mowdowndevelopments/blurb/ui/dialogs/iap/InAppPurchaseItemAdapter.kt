package com.mowdowndevelopments.blurb.ui.dialogs.iap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.databinding.InAppPurchaseListItemBinding
import com.mowdowndevelopments.blurb.ui.dialogs.iap.InAppPurchaseItemAdapter.InAppItemViewHolder

class InAppPurchaseItemAdapter(private val listener: OnItemClickListener) : RecyclerView.Adapter<InAppItemViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(itemDetails: SkuDetails?)
    }

    private lateinit var skuDetailsList: List<SkuDetails>
    private lateinit var purchases: List<Purchase>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InAppItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.in_app_purchase_list_item, parent, false)
        return InAppItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: InAppItemViewHolder, position: Int) {
        holder.bind(skuDetailsList[position])
    }

    override fun getItemCount(): Int {
        return if (::skuDetailsList.isInitialized) {
            skuDetailsList.size
        } else 0
    }

    fun setData(detailsList: List<SkuDetails>, purchaseList: List<Purchase>) {
        skuDetailsList = detailsList
        purchases = purchaseList
        notifyDataSetChanged()
    }

    inner class InAppItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val binding: InAppPurchaseListItemBinding = InAppPurchaseListItemBinding.bind(itemView)

        fun bind(itemDetails: SkuDetails) {
            binding.tvInAppItemName.text = itemDetails.title
            binding.tvPrice.text = itemDetails.price
            var maybeOwned: Purchase? = null
            for (p in purchases) {
                if (itemDetails.sku == p.sku && p.isAcknowledged) {
                    maybeOwned = p
                    break
                }
            }
            if (maybeOwned != null) {
                binding.ivPurchaseCheckbox.setImageResource(R.drawable.ic_baseline_check_box)
            } else {
                binding.ivPurchaseCheckbox.setImageResource(R.drawable.ic_baseline_check_box_outline)
            }
        }

        override fun onClick(view: View) {
            listener.onItemClick(skuDetailsList[adapterPosition])
        }

        init {
            itemView.setOnClickListener(this)
        }
    }
}