package com.example.foodplanner.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.view.View;

import java.util.Calendar;
import java.util.TimeZone;

public class CalendarHelper {

    public static void addMealToCalendar(Context context, View view, String title, String description,
            long requestDateMillis) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, "Meal: " + title);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, description);
        intent.putExtra(CalendarContract.Events.ALL_DAY, true);

        // precise time handling
        Calendar beginTime = Calendar.getInstance();
        beginTime.setTimeInMillis(requestDateMillis);
        // Default to breakfast time (8 AM) if only date is provided, or use logic if
        // needed
        beginTime.set(Calendar.HOUR_OF_DAY, 8);
        beginTime.set(Calendar.MINUTE, 0);

        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, beginTime.getTimeInMillis() + 60 * 60 * 1000); // 1 hour
                                                                                                              // duration

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            SnackbarUtils.showError(view, "No Calendar app found");
        }
    }

    private static long getPrimaryCalendarId(Context context) {
        String[] projection = new String[] {
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.IS_PRIMARY
        };

        // Filter for calendars that are visible and owned by an account (not local-only
        // if possible)
        // But simpler: just get the first one or primary
        try (android.database.Cursor cursor = context.getContentResolver().query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                null)) {

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    // Check if is primary (column index 1)
                    // Some devices/API levels might not return IS_PRIMARY reliably,
                    // so we might just take the first one found as fallback.
                    // 1 = true
                    String isPrimary = cursor.getString(1);
                    if ("1".equals(isPrimary)) {
                        return id;
                    }
                }
                // If no primary found, try resetting and taking the first one
                if (cursor.moveToFirst()) {
                    return cursor.getLong(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1; // Fallback to 1 if query fails (risky but better than nothing)
    }

    public static void addEventToCalendarProvider(Context context, View view, String title, String description,
            long beginTimeMillis) {

        long calId = getPrimaryCalendarId(context);

        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, beginTimeMillis);
        values.put(CalendarContract.Events.DTEND, beginTimeMillis + 60 * 60 * 1000); // 1 hour
        values.put(CalendarContract.Events.TITLE, "Meal: " + title);
        values.put(CalendarContract.Events.DESCRIPTION, description);
        values.put(CalendarContract.Events.CALENDAR_ID, calId);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        try {
            android.net.Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            if (uri != null) {
                SnackbarUtils.showSuccess(view, "Meal added to calendar sync!");
            }
        } catch (SecurityException e) {
            SnackbarUtils.showError(view, "Permission denied");
        } catch (Exception e) {
            e.printStackTrace();
            SnackbarUtils.showError(view, "Error adding to calendar: " + e.getMessage());
        }
    }
}
