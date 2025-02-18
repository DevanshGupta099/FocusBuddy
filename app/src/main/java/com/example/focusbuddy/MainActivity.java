package com.example.focusbuddy;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editTextFocusTime;
    private EditText editTextFocusTimeSeconds;
    private EditText editTextBreakInterval;
    private EditText editTextBreakDuration;
    private Spinner spinnerBreakActivities;
    private Button buttonStart;
    private Button buttonStop;
    private Button buttonSettings;
    private TextView textViewPoints;
    private TextView textViewBadges;
    private TextView textViewTimer;

    private Handler handler = new Handler();
    private Runnable timerRunnable;
    private int elapsedTime = 0;
    private boolean isSessionActive = false;
    private String[] breakActivities = {"Dance", "Stretch", "Walk", "Meditate"};
    private SessionHistoryDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is logged in
        SharedPreferences preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            // Redirect to login activity if not logged in
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        dbHelper = new SessionHistoryDbHelper(this);

        editTextFocusTime = findViewById(R.id.editTextFocusTime);
        editTextFocusTimeSeconds = findViewById(R.id.editTextFocusTimeSeconds);
        editTextBreakInterval = findViewById(R.id.editTextBreakInterval);
        editTextBreakDuration = findViewById(R.id.editTextBreakDuration);
        spinnerBreakActivities = findViewById(R.id.spinnerBreakActivities);
        buttonStart = findViewById(R.id.buttonStart);
        buttonStop = findViewById(R.id.buttonStop);
        buttonSettings = findViewById(R.id.buttonSettings);
        textViewPoints = findViewById(R.id.textViewPoints);
        textViewBadges = findViewById(R.id.textViewBadges);
        textViewTimer = findViewById(R.id.textViewTimer);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, breakActivities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBreakActivities.setAdapter(adapter);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFocusSession();
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopFocusSession();
            }
        });

        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        updatePointsAndBadgesDisplay();
    }

    private void startFocusSession() {
        String focusTimeStr = editTextFocusTime.getText().toString();
        String focusTimeSecondsStr = editTextFocusTimeSeconds.getText().toString();
        String breakIntervalStr = editTextBreakInterval.getText().toString();
        String breakDurationStr = editTextBreakDuration.getText().toString();

        if (focusTimeSecondsStr.isEmpty()) {
            Toast.makeText(this, "Please enter focus time in seconds", Toast.LENGTH_SHORT).show();
            return;
        }

        int focusTime = focusTimeStr.isEmpty() ? 0 : Integer.parseInt(focusTimeStr);
        int focusTimeSeconds = Integer.parseInt(focusTimeSecondsStr);
        int totalFocusTime = focusTime * 60 + focusTimeSeconds;
        int breakInterval = Integer.parseInt(breakIntervalStr);
        int breakDuration = Integer.parseInt(breakDurationStr);
        String breakActivity = spinnerBreakActivities.getSelectedItem().toString();

        saveSessionHistory(totalFocusTime, breakActivity);
        isSessionActive = true;
        elapsedTime = 0;
        startTimer();
    }


    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isSessionActive) {
                    elapsedTime++;
                    textViewTimer.setText("Timer: " + elapsedTime + " seconds");

                    if (elapsedTime % 60 == 0) { // Update points every minute
                        updatePointsAndBadges();
                        updatePointsAndBadgesDisplay();
                    }

                    handler.postDelayed(this, 1000);  // Update every second
                }
            }
        };
        handler.post(timerRunnable);
    }


    private void stopFocusSession() {
        if (isSessionActive) {
            isSessionActive = false;
            handler.removeCallbacks(timerRunnable);
            Toast.makeText(this, "Session stopped. Points awarded: " + (elapsedTime / 60), Toast.LENGTH_SHORT).show();
            updatePointsAndBadges();
            updatePointsAndBadgesDisplay();
        }
    }


    private void saveSessionHistory(int focusTime, String breakActivity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("focusTime", focusTime);
        values.put("breakActivity", breakActivity);
        long result = db.insert("SessionHistory", null, values);

        if (result == -1) {
            Toast.makeText(this, "Error saving session history", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Session history saved successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePointsAndBadges() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getWritableDatabase();

            // Update points
            cursor = db.rawQuery("SELECT SUM(focusTime) FROM SessionHistory", null);
            int totalFocusTime = 0;
            if (cursor.moveToFirst()) {
                totalFocusTime = cursor.getInt(0);
            }

            int points = totalFocusTime / 10;  // Example: 1 point per 10 minutes of focus time

            ContentValues pointsValues = new ContentValues();
            pointsValues.put("points", points);
            long result = db.insertWithOnConflict("Points", null, pointsValues, SQLiteDatabase.CONFLICT_REPLACE);

            if (result == -1) {
                showToast("Error updating points");
            } else {
                showToast("Points updated successfully");
            }

            // Check for badges
            String badge = null;
            if (points >= 100) {
                badge = "Focus Master";
            } else if (points >= 50) {
                badge = "Focus Pro";
            } else if (points >= 20) {
                badge = "Focus Novice";
            }

            if (badge != null) {
                awardBadge(badge);
            }

        } catch (Exception e) {
            showToast("Exception: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    private void awardBadge(String badge) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues badgeValues = new ContentValues();
            badgeValues.put("badge", badge);
            long result = db.insertWithOnConflict("Badges", null, badgeValues, SQLiteDatabase.CONFLICT_REPLACE);

            if (result == -1) {
                showToast("Error awarding badge");
            } else {
                showToast("Badge awarded: " + badge);
            }
        } catch (Exception e) {
            showToast("Exception: " + e.getMessage());
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
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
        textViewPoints.setText("Points: " + points);

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
        textViewBadges.setText(badges.toString());
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    class SessionHistoryDbHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "sessionHistory.db";
        private static final int DATABASE_VERSION = 2;

        public SessionHistoryDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String SQL_CREATE_SESSION_ENTRIES =
                    "CREATE TABLE SessionHistory (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "focusTime INTEGER NOT NULL," +
                            "breakActivity TEXT NOT NULL," +
                            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ")";
            String SQL_CREATE_POINTS_ENTRIES =
                    "CREATE TABLE Points (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "points INTEGER NOT NULL" +
                            ")";
            String SQL_CREATE_BADGES_ENTRIES =
                    "CREATE TABLE Badges (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "badge TEXT NOT NULL" +
                            ")";
            db.execSQL(SQL_CREATE_SESSION_ENTRIES);
            db.execSQL(SQL_CREATE_POINTS_ENTRIES);
            db.execSQL(SQL_CREATE_BADGES_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 2) {
                String SQL_CREATE_POINTS_ENTRIES =
                        "CREATE TABLE Points (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "points INTEGER NOT NULL" +
                                ")";
                String SQL_CREATE_BADGES_ENTRIES =
                        "CREATE TABLE Badges (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "badge TEXT NOT NULL" +
                                ")";
                db.execSQL(SQL_CREATE_POINTS_ENTRIES);
                db.execSQL(SQL_CREATE_BADGES_ENTRIES);
            }
        }
    }
}