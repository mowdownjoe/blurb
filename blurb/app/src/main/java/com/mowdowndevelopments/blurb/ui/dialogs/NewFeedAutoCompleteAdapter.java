package com.mowdowndevelopments.blurb.ui.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.databinding.AutocompleteItemBinding;
import com.mowdowndevelopments.blurb.network.ResponseModels.AutoCompleteResponse;

import java.util.List;

public class NewFeedAutoCompleteAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private List<AutoCompleteResponse> feeds;

    public NewFeedAutoCompleteAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        if (feeds != null){
            return feeds.size();
        }
        return 0;
    }

    @Override
    public AutoCompleteResponse getItem(int i) {
        if (feeds != null){
            return feeds.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.autocomplete_item, parent,false);
        }
        AutocompleteItemBinding binding = AutocompleteItemBinding.bind(view);
        if (feeds != null){
            binding.tvFeedLabel.setText(feeds.get(position).getFeedTitle());
            binding.tvFeedSubCount.setText(feeds.get(position).getSubscriberCount());
            binding.tvTagline.setText(feeds.get(position).getTagline());
        }
        return binding.getRoot();
    }

    public void setResponseData(List<AutoCompleteResponse> data){
        feeds = data;
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        //API handles its own filtering and only returns 4 results.
        return null;
    }
}
