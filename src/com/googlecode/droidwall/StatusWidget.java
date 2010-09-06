/**
 * ON/OFF Widget implementation
 * 
 * Copyright (C) 2009-2010  Rodrigo Zechin Rosauro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Rodrigo Zechin Rosauro
 * @version 1.0
 */

package com.googlecode.droidwall;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * ON/OFF Widget implementation
 */
public class StatusWidget extends AppWidgetProvider {

	@Override
	public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (Api.STATUS_CHANGED_MSG.equals(intent.getAction())) {
        	// Broadcast sent when the DroidWall status has changed
            final Bundle extras = intent.getExtras();
            if (extras != null && extras.containsKey(Api.STATUS_EXTRA)) {
                final boolean firewallEnabled = extras.getBoolean(Api.STATUS_EXTRA);
                final AppWidgetManager manager = AppWidgetManager.getInstance(context);
                final int[] widgetIds = manager.getAppWidgetIds(new ComponentName(context, StatusWidget.class));
                showWidget(context, manager, widgetIds, firewallEnabled);
            }
        } else if (Api.TOGGLE_REQUEST_MSG.equals(intent.getAction())) {
        	// Broadcast sent to request toggling DroidWall's status
            final SharedPreferences prefs = context.getSharedPreferences(Api.PREFS_NAME, 0);
            boolean enabled = !prefs.getBoolean(Api.PREF_ENABLED, true);
    		final String pwd = prefs.getString(Api.PREF_PASSWORD, "");
    		if (!enabled && pwd.length() != 0) {
        		Toast.makeText(context, "Cannot disable firewall - password defined!", Toast.LENGTH_SHORT).show();
        		return;
    		}
            if (enabled) {
            	if (Api.applySavedIptablesRules(context, false)) {
            		Toast.makeText(context, "Firewall enabled!", Toast.LENGTH_SHORT).show();
            	} else {
            		Toast.makeText(context, "Error enabling firewall!", Toast.LENGTH_SHORT).show();
            		return;
            	}
            } else {
            	if (Api.purgeIptables(context, false)) {
            		Toast.makeText(context, "Firewall disabled!", Toast.LENGTH_SHORT).show();
            	} else {
            		Toast.makeText(context, "Error disabling firewall!", Toast.LENGTH_SHORT).show();
            		return;
            	}
            }
            Api.setEnabled(context, enabled);
        }
	}
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] ints) {
        super.onUpdate(context, appWidgetManager, ints);
        final SharedPreferences prefs = context.getSharedPreferences(Api.PREFS_NAME, 0);
        boolean enabled = prefs.getBoolean(Api.PREF_ENABLED, true);
        showWidget(context, appWidgetManager, ints, enabled);
    }

    private void showWidget(Context context, AppWidgetManager manager, int[] widgetIds, boolean enabled) {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.onoff_widget);
        final int iconId = enabled ? R.drawable.widget_on : R.drawable.widget_off;
        views.setImageViewResource(R.id.widgetCanvas, iconId);
        final Intent msg = new Intent(Api.TOGGLE_REQUEST_MSG);
        final PendingIntent intent = PendingIntent.getBroadcast(context, -1, msg, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widgetCanvas, intent);
        manager.updateAppWidget(widgetIds, views);
    }
    
}
