package com.example.foodplanner.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.widget.Toast;

import java.util.Calendar;
import java.util.TimeZone;

public class CalendarHelper {

    public static void addMealToCalendar(Context context, String title, String description, long requestDateMillis) {
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
            Toast.makeText(context, "No Calendar app found", Toast.LENGTH_SHORT).show();
        }
    }

    public static void addEventToCalendarProvider(Context context, String title, String description,
            long beginTimeMillis) {
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, beginTimeMillis);
        values.put(CalendarContract.Events.DTEND, beginTimeMillis + 60 * 60 * 1000); // 1 hour
        values.put(CalendarContract.Events.TITLE, "Meal: " + title);
        values.put(CalendarContract.Events.DESCRIPTION, description);
        values.put(CalendarContract.Events.CALENDAR_ID, 1); // Primary calendar
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        try {
            android.net.Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            if (uri != null) {
                Toast.makeText(context, "Meal added to calendar sync!", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Error adding to calendar", Toast.LENGTH_SHORT).show();
        }
    }
}
