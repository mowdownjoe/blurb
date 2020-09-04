package com.mowdowndevelopments.blurb.ui.feeds.river;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.network.LoadingStatus;
import com.mowdowndevelopments.blurb.network.Singletons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

public class RiverOfNewsDataSource extends PageKeyedDataSource<Integer, Story> {
    private MutableLiveData<LoadingStatus> initialLoadStatus;
    private MutableLiveData<LoadingStatus> pageLoadStatus;
    private MutableLiveData<String> errorMessage;

    private Context context;
    private String readFilter;
    private String sortOrder;
    private List<Integer> feedIds;

    public LiveData<LoadingStatus> getInitialLoadStatus() {
        return initialLoadStatus;
    }

    public LiveData<LoadingStatus> getPageLoadStatus() {
        return pageLoadStatus;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public RiverOfNewsDataSource(Context context, String readFilter, String sortOrder, List<Integer> feedIds) {
        this.context = context;
        this.readFilter = readFilter;
        this.sortOrder = sortOrder;
        this.feedIds = feedIds;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Integer, Story> callback) {
        Singletons.getOkHttpClient(context).newCall(buildInitialRequest()).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //TODO Fill out
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Story> callback) {
        Singletons.getOkHttpClient(context).newCall(buildPageRequest(params.key)).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Story> callback) {
        //Unused. Will not prepend to initial load.
    }

    public void resetWithNewParameters(String newFilter, String newOrder){
        readFilter = newFilter;
        sortOrder = newOrder;
        invalidate();
    }

    public Request buildInitialRequest(){
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme("https")
                .host("newsblur.com")
                .addPathSegments("reader/river_stories")
                .addQueryParameter("read_filter", readFilter)
                .addQueryParameter("order", sortOrder);

        for (Integer id: feedIds){
            urlBuilder.addQueryParameter("feeds", id.toString());
        }

        return new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();
    }

    public Request buildPageRequest(int pageNo){
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme("https")
                .host("newsblur.com")
                .addPathSegments("reader/river_stories")
                .addQueryParameter("page", Integer.toString(pageNo))
                .addQueryParameter("read_filter", readFilter)
                .addQueryParameter("order", sortOrder);

        for (Integer id: feedIds){
            urlBuilder.addQueryParameter("feeds", id.toString());
        }

        return new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();
    }

    static class Factory extends DataSource.Factory<Integer, Story>{

        private MutableLiveData<RiverOfNewsDataSource> mostRecentDataSource;
        private Context context;
        private String readFilter;
        private String sortOrder;
        private Feed[] feeds;
        public Factory(Context context, String readFilter, String sortOrder, Feed... feeds) {
            this.context = context;
            this.readFilter = readFilter;
            this.sortOrder = sortOrder;
            this.feeds = feeds;
            mostRecentDataSource = new MutableLiveData<>();
        }

        @NonNull
        @Override
        public DataSource<Integer, Story> create() {
            ArrayList<Integer> ids = new ArrayList<>();
            for (Feed f : feeds) {
                ids.add(f.getId());
            }
            RiverOfNewsDataSource source = new RiverOfNewsDataSource(context, readFilter, sortOrder, ids);
            mostRecentDataSource.postValue(source);
            return source;
        }

        public MutableLiveData<RiverOfNewsDataSource> getMostRecentDataSource() {
            return mostRecentDataSource;
        }
    }
}
