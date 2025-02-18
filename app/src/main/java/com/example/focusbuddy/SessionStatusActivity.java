package com.example.focusbuddy;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SessionStatusActivity extends AppCompatActivity {

    private TextView textViewStatus;
    private TextView textViewPoints; // New TextView for points
    private TextView textViewBadges; // New TextView for badges
    private Handler handler = new Handler();
    private SessionHistoryDbHelper dbHelper;
    private int totalFocusTime;
    private int breakInterval;
    private int breakDuration;
    private String breakActivity;
    private int elapsedFocusTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_status);

        dbHelper = new SessionHistoryDbHelper(this);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewPoints = findViewById(R.id.textViewPoints); // Initialize the TextView for points
        textViewBadges = findViewById(R.id.textViewBadges); // Initialize the TextView for badges

        totalFocusTime = getIntent().getIntExtra("totalFocusTime", 0); // Fixed the key to match the correct one
        breakInterval = getIntent().getIntExtra("breakInterval", 0);
        breakDuration = getIntent().getIntExtra("breakDuration", 0);
        breakActivity = getIntent().getStringExtra("breakActivity");

        startBreakIntervals(elapsedFocusTime);
    }

    private void startBreakIntervals(final int elapsedFocusTime) {
        if (elapsedFocusTime < totalFocusTime) {
            textViewStatus.setText("Focus Session In Progress... Elapsed Time: " + elapsedFocusTime + " seconds");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showBreakReminder();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startBreakIntervals(elapsedFocusTime + breakInterval * 60 + breakDuration * 60);
                        }
                    }, breakDuration * 60000);
                }
            }, breakInterval * 60000);
        } else {
            saveSessionHistory(totalFocusTime, breakActivity);
            updatePointsAndBadges();
            updatePointsAndBadgesDisplay();
            textViewStatus.setText("Focus Session Completed!");
        }
    }

    private void showBreakReminder() {
        Toast.makeText(SessionStatusActivity.this, "Time to take a break! " + breakActivity + " for " + breakDuration + " minutes.", Toast.LENGTH_LONG).show();
    }

    private void saveSessionHistory(int focusTime, String breakActivity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("focusTime", focusTime);
        values.put("breakActivity", breakActivity);
        db.insert("SessionHistory", null, values);
    }

    private void updatePointsAndBadges() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Update points
        Cursor cursor = db.rawQuery("SELECT SUM(focusTime) FROM SessionHistory", null);
        int totalFocusTime = 0;
        if (cursor.moveToFirst()) {
            totalFocusTime = cursor.getInt(0);
        }
        cursor.close();

        int points = totalFocusTime / 10;  // Example: 1 point per 10 minutes of focus time
        ContentValues pointsValues = new ContentValues();
        pointsValues.put("points", points);
        db.insertWithOnConflict("Points", null, pointsValues, SQLiteDatabase.CONFLICT_REPLACE);

        // Check for badges
        if (points >= 100) {
            awardBadge("Focus Master");
        } else if (points >= 50) {
            awardBadge("Focus Pro");
        } else if (points >= 20) {
            awardBadge("Focus Novice");
        }
    }

    private void awardBadge(String badge) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues badgeValues = new ContentValues();
        badgeValues.put("badge", badge);
        db.insertWithOnConflict("Badges", null, badgeValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private void updatePointsAndBadgesDisplay() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Display points
        Cursor cursor = db.rawQuery("SELECT points FROM Points", null);
        int points = 0;
        if (cursor.moveToFirst()) {
            points = cursor.getInt(0);
        }
        cursor.close();
        textViewPoints.setText("Points: " + points); // Update the points TextView

        // Display badges
        cursor = db.rawQuery("SELECT badge FROM Badges", null);
        StringBuilder badges = new StringBuilder("Badges: ");
        while (cursor.moveToNext()) {
            badges.append(cursor.getString(0)).append(", ");
        }
        cursor.close();
        if (badges.length() > 8) {
            badges.setLength(badges.length() - 2);  // Remove the trailing comma and space
        } else {
            badges.append("None");
        }
        textViewBadges.setText(badges.toString()); // Update the badges TextView
    }
}
