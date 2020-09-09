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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.List;

/**
 * Implementation of App Widget functionality.
 */
public class StarredStoriesWidget extends AppWidgetProvider {

    public static final String NEXT_WIDGET_PAGE = "com.mowdowndevelopments.blurb.NEXT_WIDGET_PAGE";
    public static final String PREV_WIDGET_PAGE = "com.mowdowndevelopments.blurb.PREV_WIDGET_PAGE";
    public static final String WIDGET_ID = "widget_id";
    private static HashMap<Integer, Integer> widgetToActivePage;
    private static List<Story> stories;

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
            Story activeStory = stories.get(widgetToActivePage.get(appWidgetId));
            String relatedFeedTitle = BlurbDb.getInstance(context).blurbDao().getFeedTitle(activeStory.getFeedId());

            views.setTextViewText(R.id.tv_headline, activeStory.getTitle());
            views.setTextViewText(R.id.tv_story_authors, activeStory.getAuthors());
            views.setTextViewText(R.id.tv_feed_name, relatedFeedTitle);

            views.setOnClickPendingIntent(R.id.btn_next_story, getNextPagePendingIntent(context, appWidgetId));
            views.setOnClickPendingIntent(R.id.btn_prev_story, getPreviousPagePendingIntent(context, appWidgetId));

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
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
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
                stories = BlurbDb.getInstance(context).blurbDao().getStarredStoryListForWidget());
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        stories.clear();
        widgetToActivePage.clear();
    }

    protected static PendingIntent getNextPagePendingIntent(Context context, int widgetId){
        Intent intent = new Intent(NEXT_WIDGET_PAGE)
                .putExtra(WIDGET_ID, widgetId);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected static PendingIntent getPreviousPagePendingIntent(Context context, int widgetId){
        Intent intent = new Intent(PREV_WIDGET_PAGE)
                .putExtra(WIDGET_ID, widgetId);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}

