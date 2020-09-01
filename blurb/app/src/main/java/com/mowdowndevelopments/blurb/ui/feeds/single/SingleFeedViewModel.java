package com.mowdowndevelopments.blurb.ui.feeds.single;

import android.app.Application;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.network.LoadingStatus;
import com.mowdowndevelopments.blurb.network.Singletons;
import com.mowdowndevelopments.blurb.ui.feeds.BaseFeedViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import timber.log.Timber;

public class SingleFeedViewModel extends BaseFeedViewModel {

    private static final int PAGE_SIZE = 6;
    private SingleFeedDataSource mostRecentDataSource;

    private MutableLiveData<LoadingStatus> pageLoadingStatus;
    private LiveData<PagedList<Story>> storyList;

    final private Observer<LoadingStatus> statusObserver = this::setLoadingStatus;
    final private Observer<String> errorMessageObserver = this::setErrorMessage;
    final private Observer<LoadingStatus> pageLoadingObserver = loadingStatus -> pageLoadingStatus.postValue(loadingStatus);

    public LiveData<PagedList<Story>> getStoryList() {
        return storyList;
    }

    public LiveData<LoadingStatus> getPageLoadingStatus() {
        return pageLoadingStatus;
    }

    public SingleFeedViewModel(@NonNull Application app, int feedId) {
        super(app);
        SharedPreferences prefs = app.getSharedPreferences(app.getString(R.string.shared_pref_file), 0);
        String sortOrder = prefs.getString(app.getString(R.string.pref_filter_key), "newest");
        String filter = prefs.getString(app.getString(R.string.pref_sort_key), "all");
        SingleFeedDataSource.Factory factory = new SingleFeedDataSource.Factory(
                getApplication(),
                feedId,
                sortOrder,
                filter
        );
        mostRecentDataSource = factory.create();
        storyList = new LivePagedListBuilder<>(factory, PAGE_SIZE).build();
        pageLoadingStatus = new MutableLiveData<>();
        setInternalObservers();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cleanUpObservers();
    }

    private void setInternalObservers(){
        mostRecentDataSource.getErrorMessage().observeForever(errorMessageObserver);
        mostRecentDataSource.getPageLoadingStatus().observeForever(pageLoadingObserver);
        mostRecentDataSource.getInitialLoadingStatus().observeForever(statusObserver);
    }

    private void cleanUpObservers() {
        if (mostRecentDataSource.getErrorMessage().hasObservers()) {
            mostRecentDataSource.getErrorMessage().removeObserver(errorMessageObserver);
            mostRecentDataSource.getPageLoadingStatus().observeForever(pageLoadingObserver);
            mostRecentDataSource.getInitialLoadingStatus().removeObserver(statusObserver);
        }
    }

    public void simpleRefresh(){
        mostRecentDataSource.invalidate();
    }

    public void refreshWithNewParameters(@NotNull Feed feed, String sortOrder, String filter){
        cleanUpObservers();
        SingleFeedDataSource.Factory factory = new SingleFeedDataSource.Factory(
                getApplication(),
                feed.getId(),
                sortOrder,
                filter
        );
        mostRecentDataSource = factory.create();
        storyList = new LivePagedListBuilder<>(factory, PAGE_SIZE).build();
        setInternalObservers();
    }

    public void markAllAsRead(){
        StringBuilder stringBuilder;
        try {
            List<Story> stories = Objects.requireNonNull(storyList.getValue());
            stringBuilder = new StringBuilder();
            for (Story i: stories) {
                String encodedHash = URLEncoder.encode(i.getStoryHash(), StandardCharsets.UTF_8.toString());
                stringBuilder.append("story_hash=").append(encodedHash).append('&');
            }
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
        } catch (UnsupportedEncodingException e) {
            Timber.e(e);
            String errorMsg = getApplication().getString(R.string.err_fail_mark_all)+e.getLocalizedMessage();
            Toast.makeText(getApplication(), errorMsg, Toast.LENGTH_LONG).show();
            FirebaseCrashlytics.getInstance().recordException(e);
            return;
        }

        MediaType type = MediaType.parse("application/x-www-form-urlencoded");
        Request request = new Request.Builder()
                .url(Singletons.BASE_URL+"reader/mark_story_hashes_as_read")
                .post(RequestBody.create(type, stringBuilder.toString()))
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build();

        Singletons.getOkHttpClient(getApplication()).newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) {
                if (response.isSuccessful()){
                    Timber.d("Marked feed as read.");
                } else {
                    Toast.makeText(getApplication(), getApplication()
                            .getString(R.string.http_error, response.code()), Toast.LENGTH_SHORT).show();
                    Timber.w("Could not mark as read. HTTP Error %o", response.code());
                }
            }

            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                Timber.e(e);
                FirebaseCrashlytics.getInstance().recordException(e);
                Toast.makeText(getApplication(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class Factory extends ViewModelProvider.AndroidViewModelFactory{

        private int feedId;
        private Application app;
        public Factory(@NonNull Application application, int feedId) {
            super(application);
            app = application;
            this.feedId = feedId;
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SingleFeedViewModel(app, feedId);
        }
    }
}