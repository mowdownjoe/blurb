package com.mowdowndevelopments.blurb.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.mowdowndevelopments.blurb.AppExecutors;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.BlurbDb;
import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.ui.story.StoryPagerActivity;
import com.mowdowndevelopments.blurb.ui.story.StoryPagerActivityArgs;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * Implementation of App Widget functionality.
 */
public class StarredStoriesWidget extends AppWidgetProvider {

    private static final String PREVIOUS_WIDGET_PAGE = "com.mowdowndevelopments.blurb.PREV_WIDGET_PAGE";
    private static final String NEXT_WIDGET_PAGE = "com.mowdowndevelopments.blurb.NEXT_WIDGET_PAGE";
    private static final String WIDGET_ID = "com.mowdowndevelopments.blurb.WIDGET_ID";
    private static final int REQUEST_CODE = 758;
    private static HashMap<Integer, Integer> widgetToActivePage;
    private static List<Story> storyList;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.starred_stories_widget);

        displayNewStory(context, appWidgetManager, appWidgetId, views);

    }

    private static void displayNewStory(Context context,
                                        AppWidgetManager appWidgetManager,
                                        int appWidgetId,
                                        RemoteViews views) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Story activeStory = storyList.get(widgetToActivePage.get(appWidgetId));
            String relatedFeedTitle = BlurbDb.getInstance(context).blurbDao().getFeedTitle(activeStory.getFeedId());

            if (relatedFeedTitle != null && !relatedFeedTitle.isEmpty()) {
                views.setTextViewText(R.id.tv_feed_name, relatedFeedTitle);
            } else {
                views.setTextViewText(R.id.tv_feed_name, context.getString(R.string.unknown));
            }
            views.setTextViewText(R.id.tv_headline, activeStory.getTitle());
            views.setTextViewText(R.id.tv_story_authors, activeStory.getAuthors());

            views.setOnClickPendingIntent(R.id.btn_prev_story, getPreviousPagePendingIntent(context, appWidgetId));
            views.setOnClickPendingIntent(R.id.btn_next_story, getNextPagePendingIntent(context, appWidgetId));
            views.setOnClickPendingIntent(R.id.fl_story_info_holder, getLaunchStoryPendingIntent(context, appWidgetId));
            views.setOnClickPendingIntent(R.id.tv_headline, getLaunchStoryPendingIntent(context, appWidgetId));
            views.setOnClickPendingIntent(R.id.tv_feed_name, getLaunchStoryPendingIntent(context, appWidgetId));

            Instant instant = Instant.ofEpochSecond(activeStory.getTimestamp());
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            views.setTextViewText(R.id.tv_story_time, dateTime.format(
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            ));

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        });
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        //Get data.
        int widgetId = intent.getIntExtra(WIDGET_ID, -1);
        if (widgetId == -1) return;
        String action = intent.getAction();
        if (action == null || action.isEmpty()) return;
        Integer currentPage = widgetToActivePage.get(widgetId);
        if (currentPage == null) return;

        //Increment or decrement Widget page.
        if (action.equals(PREVIOUS_WIDGET_PAGE)){
            ++currentPage;
            if (currentPage >= storyList.size()) currentPage = 0;
        } else if (action.equals(NEXT_WIDGET_PAGE)){
            --currentPage;
            if (currentPage < 0) currentPage = storyList.size() - 1;
        } else return;
        widgetToActivePage.put(widgetId, currentPage);
        Timber.d("Current widget page for widget %o is %o", widgetId, currentPage);

        //Create RemoteViews and AppWidgetManager.
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.starred_stories_widget);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);

        displayNewStory(context, manager, widgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (widgetToActivePage == null){
            onEnabled(context);
        }
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            if (!widgetToActivePage.containsKey(appWidgetId)){
                widgetToActivePage.put(appWidgetId, 0);
            }
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        widgetToActivePage = new HashMap<>();
        AppExecutors.getInstance().diskIO().execute(() ->
                storyList = BlurbDb.getInstance(context).blurbDao().getStarredStoryListForWidget());
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private static PendingIntent getPreviousPagePendingIntent(Context context, int widgetId){
        Intent intent = new Intent(PREVIOUS_WIDGET_PAGE)
                .setClass(context, StarredStoriesWidget.class)
                .putExtra(WIDGET_ID, widgetId);
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getNextPagePendingIntent(Context context, int widgetId){
        Intent intent = new Intent(NEXT_WIDGET_PAGE)
                .setClass(context, StarredStoriesWidget.class)
                .putExtra(WIDGET_ID, widgetId);
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getLaunchStoryPendingIntent(Context context, int widgetId){
        Story[] stories = new Story[storyList.size()];
        StoryPagerActivityArgs.Builder builder = new StoryPagerActivityArgs.Builder(storyList.toArray(stories))
                .setInitialStory(widgetToActivePage.get(widgetId));

        Intent intent = new Intent(context, StoryPagerActivity.class)
                .putExtras(builder.build().toBundle())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT);
    }
}

