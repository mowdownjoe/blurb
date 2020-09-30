package com.mowdowndevelopments.blurb.ui.dialogs.newFeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.databinding.AutocompleteItemBinding;
import com.mowdowndevelopments.blurb.network.responseModels.AutoCompleteResponse;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NewFeedAutoCompleteAdapter extends ArrayAdapter<AutoCompleteResponse> {

    private Context context;
    private List<AutoCompleteResponse> feeds;

    public NewFeedAutoCompleteAdapter(Context context) {
        super(context, R.layout.autocomplete_item, new ArrayList<>());
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

    @NotNull
    @Override
    public View getView(int position, View view, @NotNull ViewGroup parent) {
        if (view == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.autocomplete_item, parent,false);
        }
        AutocompleteItemBinding binding = AutocompleteItemBinding.bind(view);
        if (feeds != null){
            AutoCompleteResponse response = feeds.get(position);
            binding.tvFeedLabel.setText(response.getFeedTitle());
            binding.tvFeedSubCount.setText(String.format("%o", response.getSubscriberCount()));
            binding.tvTagline.setText(response.getTagline());
        }
        return binding.getRoot();
    }

    public void setResponseData(List<AutoCompleteResponse> data){
        feeds = data;
        notifyDataSetChanged();
    }
}
