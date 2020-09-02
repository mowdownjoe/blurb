package com.mowdowndevelopments.blurb.ui.feeds.single;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.network.ResponseModels.FeedContentsResponse;
import com.mowdowndevelopments.blurb.network.Singletons;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class SingleFeedDataSource extends PageKeyedDataSource<Integer, Story> {

    private Context context;
    private int feedId;
    private String sortOrder;
    private String filter;


    public SingleFeedDataSource(Context context, int id, String sortOrder, String filter) {
        super();
        this.context = context;
        feedId = id;
        this.sortOrder = sortOrder;
        this.filter = filter;
    }


    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Integer, Story> callback) {
        Callback<FeedContentsResponse> responseCallback = new Callback<FeedContentsResponse>() {
            @Override
            public void onResponse(@NotNull Call<FeedContentsResponse> call, @NotNull Response<FeedContentsResponse> response) {
                Timber.d("Successfully received response. Response Code: %o", response.code());
                if (response.isSuccessful()) {
                    FeedContentsResponse body = Objects.requireNonNull(response.body());
                    callback.onResult(Arrays.asList(body.getStories()), null, 2);
                } else {
                    String toast = context.getString(R.string.http_error, response.code());
                    Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<FeedContentsResponse> call, @NotNull Throwable t) {
                Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                FirebaseCrashlytics.getInstance().recordException(t);
                Timber.e(t);
            }
        };
        Singletons.getNewsBlurAPI(context).getFeedContents(feedId, filter, sortOrder).enqueue(responseCallback);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Story> callback) {
        Callback<FeedContentsResponse> responseCallback = new Callback<FeedContentsResponse>() {
            @Override
            public void onResponse(@NotNull Call<FeedContentsResponse> call, @NotNull Response<FeedContentsResponse> response) {
                if (response.isSuccessful()){
                    Story[] stories = response.body().getStories();
                    if (stories.length > 0) {
                        callback.onResult(Arrays.asList(stories), params.key +1);
                    } else {
                        callback.onResult(Arrays.asList(stories), null);
                    }
                } else {
                    String toast = context.getString(R.string.http_error, response.code());
                    Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<FeedContentsResponse> call, @NotNull Throwable t) {
                Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                FirebaseCrashlytics.getInstance().recordException(t);
                Timber.e(t);
            }
        };
        Singletons.getNewsBlurAPI(context).getFeedContents(feedId, filter, sortOrder, params.key).enqueue(responseCallback);
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Story> callback) {
        //Will only append to initial load, not prepend
    }

    static class Factory extends DataSource.Factory<Integer, Story> {

        private Context context;
        private int feedId;
        private String sortOrder;
        private String filter;

        public Factory(Context context, int feedId, String sortOrder, String filter) {
            this.context = context;
            this.feedId = feedId;
            this.sortOrder = sortOrder;
            this.filter = filter;
        }

        @NonNull
        @Override
        public SingleFeedDataSource create() {
            return new SingleFeedDataSource(context, feedId, sortOrder, filter);
        }
    }
}
