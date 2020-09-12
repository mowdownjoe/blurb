package com.mowdowndevelopments.blurb.work;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.BlurbDb;
import com.mowdowndevelopments.blurb.network.ResponseModels.FeedContentsResponse;
import com.mowdowndevelopments.blurb.network.ResponseModels.GetStarredHashesResponse;
import com.mowdowndevelopments.blurb.network.Singletons;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static java.util.Objects.requireNonNull;

public class FetchStarredStoriesWorker extends Worker {
    public FetchStarredStoriesWorker(@NonNull Context c, @NonNull WorkerParameters workerParams) {
        super(c, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Singletons.getNewsBlurAPI(getApplicationContext()).getStarredStoryHashes().enqueue(new Callback<GetStarredHashesResponse>() {
            @Override
            public void onResponse(@NotNull Call<GetStarredHashesResponse> call, @NotNull Response<GetStarredHashesResponse> response) {
                if (response.isSuccessful()){
                    fetchStoriesAndCommitToDb(requireNonNull(response.body()).getStarredStoryHashes());
                } else {
                    String errorMsg = getApplicationContext().getString(R.string.error_star_fetch)
                            + getApplicationContext().getString(R.string.http_error, response.code());
                    Timber.e(errorMsg);
                    Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<GetStarredHashesResponse> call, @NotNull Throwable t) {
                String errorMsg = getApplicationContext().getString(R.string.error_star_fetch);
                Timber.e(t,"%s%s", errorMsg, t.getMessage());
                Toast.makeText(getApplicationContext(), errorMsg + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
        return Result.success();
    }

    private void fetchStoriesAndCommitToDb(@NonNull List<String> hashes){
        if (hashes.isEmpty()) return;
        HttpUrl.Builder builder = new HttpUrl.Builder()
                .scheme("https")
                .host("newsblur.com")
                .addPathSegments("reader/starred_stories");
        for (String storyHash : hashes) {
            builder.addQueryParameter("h", storyHash);
        }

        Request request = new Request.Builder()
                .url(builder.build())
                .get()
                .build();

        Singletons.getOkHttpClient(getApplicationContext()).newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful()){
                    FeedContentsResponse body = Singletons.getMoshi()
                            .adapter(FeedContentsResponse.class)
                            .fromJson(requireNonNull(response.body()).string());
                    BlurbDb.getInstance(getApplicationContext()).blurbDao()
                            .addStories(Arrays.asList(requireNonNull(body).getStories()));
                    response.body().close();
                } else {
                    String errorMsg = getApplicationContext().getString(R.string.error_star_fetch)
                            + getApplicationContext().getString(R.string.http_error, response.code());
                    Timber.e(errorMsg);
                    Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                String errorMsg = getApplicationContext().getString(R.string.error_star_fetch);
                Timber.e(e,"%s%s", errorMsg, e.getMessage());
                Toast.makeText(getApplicationContext(), errorMsg + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
