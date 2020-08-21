package com.mowdowndevelopments.blurb.ui.story;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mowdowndevelopments.blurb.AppExecutors;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.BlurbDb;
import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.network.Singletons;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class StoryViewModel extends AndroidViewModel {

    private MutableLiveData<Story[]> stories;
    private MutableLiveData<Integer> indexToView;
    private MutableLiveData<String> snackbarMessage;
    private LinkedList<Story> readStories;
    private Story activeStory = null;
    private LiveData<Boolean> isActiveStoryStarred;

    public StoryViewModel(Application app) {
        super(app);
        stories = new MutableLiveData<>();
        indexToView = new MutableLiveData<>();
        snackbarMessage = new MutableLiveData<>();
        readStories = new LinkedList<>();
    }

    public Story getActiveStory() {
        return activeStory;
    }

    public void setActiveStory(@NonNull Story activeStory) {
        this.activeStory = activeStory;
        AppExecutors.getInstance().diskIO().execute(() -> {
            isActiveStoryStarred = BlurbDb.getInstance(getApplication()).blurbDao().doesStoryExist(activeStory.getStoryHash());
        });
    }

    public LiveData<Story[]> getStories() {
        return stories;
    }

    public void setStories(Story[] newStories) {
        stories.setValue(newStories);
    }

    public LiveData<Integer> getIndexToView() {
        return indexToView;
    }

    public void setIndexToView(int newIndex) {
        indexToView.setValue(newIndex);
    }

    public LiveData<String> getSnackbarMessage() {
        return snackbarMessage;
    }

    public LiveData<Boolean> getIsActiveStoryStarred() {
        return isActiveStoryStarred;
    }

    public void enqueueMarkAsRead(@NonNull Story story){
        if (story.isRead()) return;
        story.setisRead(true);
        readStories.add(story);
    }

    public void removeFromMarkAsReadQueue(@NonNull Story story){
        if (!story.isRead()) return;
        story.setisRead(false);
        readStories.remove(story);
    }

    public void markQueueAsRead(){
        if (readStories.isEmpty()) return;
        try { //OkHttp is required due to an unknown amount of Hashes to mark. Boilerplate as follows:
            String encodedHash = URLEncoder.encode(readStories.get(0).getStoryHash(), StandardCharsets.UTF_8.toString());
            StringBuilder stringBuilder = new StringBuilder("story_hash=").append(encodedHash);
            for (int i = 1; i < readStories.size(); i++) { //Starts at 1, since 0 was grabbed outside loop
                encodedHash = URLEncoder.encode(readStories.get(i).getStoryHash(), StandardCharsets.UTF_8.toString());
                stringBuilder.append("&story_hash=").append(encodedHash);
            }

            MediaType type = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(type, stringBuilder.toString());
            Request request = new Request.Builder()
                    .url("https://newsblur.com/reader/mark_story_hashes_as_read")
                    .post(body)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .build();

            Singletons.getOkHttpClient(getApplication()).newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()){
                        Timber.d("Marked queue as read.");
                    } else {
                        Toast.makeText(getApplication(), getApplication()
                                .getString(R.string.http_error, response.code()), Toast.LENGTH_SHORT).show();
                        Timber.w("Could not mark as read. HTTP Error %o", response.code());
                    }
                }

                @Override
                public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                    Timber.e(e);
                    Toast.makeText(getApplication(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (UnsupportedEncodingException e) {
            Timber.e(e);
            Toast.makeText(getApplication(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    public void markStoryAsStarred(@NonNull Story story){
        Singletons.getNewsBlurAPI(getApplication()).markStoryAsStarred(story.getStoryHash())
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(@NotNull Call<Map<String, Object>> call, @NotNull Response<Map<String, Object>> response) {
                        if (response.isSuccessful()){
                            BlurbDb.getInstance(getApplication()).blurbDao().addStory(story);
                        } else {
                            snackbarMessage.postValue(getApplication().getString(R.string.http_error, response.code()));
                            Timber.w("Could not mark as starred. HTTP Error %o", response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<Map<String, Object>> call, @NotNull Throwable t) {
                        snackbarMessage.postValue(t.getLocalizedMessage());
                        Timber.e(t);
                    }
                });
    }

    public void removeStoryFromStarred(@NonNull Story story){
        Singletons.getNewsBlurAPI(getApplication()).removeStarredStory(story.getStoryHash())
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()){
                            BlurbDb.getInstance(getApplication()).blurbDao().removeStory(story);
                        } else {
                            snackbarMessage.postValue(getApplication().getString(R.string.http_error, response.code()));
                            Timber.w("Could not mark as starred. HTTP Error %o", response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        snackbarMessage.postValue(t.getLocalizedMessage());
                        Timber.e(t);
                    }
                });
    }

}