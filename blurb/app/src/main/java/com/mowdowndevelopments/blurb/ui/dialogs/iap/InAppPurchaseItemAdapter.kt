package com.mowdowndevelopments.blurb.ui.dialogs.iap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.databinding.InAppPurchaseListItemBinding;

import java.util.List;

public class InAppPurchaseItemAdapter extends RecyclerView.Adapter<InAppPurchaseItemAdapter.InAppItemViewHolder> {

    public interface OnItemClickListener{
        void onItemClick(SkuDetails itemDetails);
    }

    private OnItemClickListener listener;
    private List<SkuDetails> skuDetailsList;
    private List<Purchase> purchases;

    public InAppPurchaseItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public InAppItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.in_app_purchase_list_item, parent, false);
        return new InAppItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InAppItemViewHolder holder, int position) {
        holder.bind(skuDetailsList.get(position));
    }

    @Override
    public int getItemCount() {
        if (skuDetailsList != null){
            return skuDetailsList.size();
        }
        return 0;
    }

    public void setData(List<SkuDetails> detailsList, List<Purchase> purchaseList){
        skuDetailsList = detailsList;
        purchases = purchaseList;
        notifyDataSetChanged();
    }

    public class InAppItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private InAppPurchaseListItemBinding binding;

        public InAppItemViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = InAppPurchaseListItemBinding.bind(itemView);
            itemView.setOnClickListener(this);
        }

        void bind(SkuDetails itemDetails){
            binding.tvInAppItemName.setText(itemDetails.getTitle());
            binding.tvPrice.setText(itemDetails.getPrice());
            Purchase maybeOwned = null;
            for (Purchase p: purchases) {
                if (itemDetails.getSku().equals(p.getSku()) && p.isAcknowledged()){
                    maybeOwned = p;
                    break;
                }
            }
            if (maybeOwned != null){
                binding.ivPurchaseCheckbox.setImageResource(R.drawable.ic_baseline_check_box);
            } else {
                binding.ivPurchaseCheckbox.setImageResource(R.drawable.ic_baseline_check_box_outline);
            }
        }

        @Override
        public void onClick(View view) {
            listener.onItemClick(skuDetailsList.get(getAdapterPosition()));
        }
    }

}
