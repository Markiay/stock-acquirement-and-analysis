package com.example.stocks;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class WidgetJava extends AppWidgetProvider {
	//This method is called when all the widgets on the desktop are deleted
		@Override
		public void onDisabled(Context context) {
			super.onDisabled(context);
			Intent stopUpdateIntent = new Intent(context, UpdateWidgetService.class);
			context.stopService(stopUpdateIntent);
		}
	//
//		//When the Widget is first created, the method is called, and the back-end service is started
		@Override
		public void onEnabled(Context context) {
			super.onEnabled(context);
			Intent startUpdateIntent = new Intent(context, UpdateWidgetService.class);
			context.startService(startUpdateIntent);
		}

		@Override
		public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
			super.onUpdate(context, appWidgetManager, appWidgetIds);
		}

		//This method is called several times in Widget usage
		@Override
		public void onReceive(Context context, Intent intent) {
			super.onReceive(context, intent);
			Intent startUpdateIntent = new Intent(context, UpdateWidgetService.class);
			context.startService(startUpdateIntent);
		}
}
