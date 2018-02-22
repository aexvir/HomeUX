package com.dravite.homeux.drawerobjects.structures;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.os.TransactionTooLargeException;

/**
 * Specific {@link AppWidgetHost} that creates our {@link ClickableAppWidgetHostView}
 * which correctly captures all long-press events. This ensures that users can
 * always pick up and move widgets.
 */
public class ClickableAppWidgetHost extends AppWidgetHost {
    public ClickableAppWidgetHost(Context context, int hostId) {
        super(context, hostId);
    }

    @Override
    protected AppWidgetHostView onCreateView(Context context, int appWidgetId,
                                             AppWidgetProviderInfo appWidget) {
        return new ClickableAppWidgetHostView(context);
    }

    @Override
    public void startListening() {
        try {
            super.startListening();
        } catch (Exception e) {
            if (e.getCause() instanceof TransactionTooLargeException) {
                // We're willing to let this slide. The exception is being caused by the list of
                // RemoteViews which is being passed back. The startListening relationship will
                // have been established by this point, and we will end up populating the
                // widgets upon bind anyway. See issue 14255011 for more context.
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void stopListening() {
        super.stopListening();
        clearViews();
    }
}