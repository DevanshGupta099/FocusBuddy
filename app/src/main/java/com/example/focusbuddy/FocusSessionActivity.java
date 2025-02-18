package com.example.focusbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class FocusSessionActivity extends AppCompatActivity {

    private EditText editTextFocusTime;
    private EditText editTextFocusTimeSeconds; // New EditText for seconds
    private EditText editTextBreakInterval;
    private EditText editTextBreakDuration;
    private Spinner spinnerBreakActivities;
    private Button buttonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_session);

        editTextFocusTime = findViewById(R.id.editTextFocusTime);
        editTextFocusTimeSeconds = findViewById(R.id.editTextFocusTimeSeconds); // Initialize the new EditText
        editTextBreakInterval = findViewById(R.id.editTextBreakInterval);
        editTextBreakDuration = findViewById(R.id.editTextBreakDuration);
        spinnerBreakActivities = findViewById(R.id.spinnerBreakActivities);
        buttonStart = findViewById(R.id.buttonStart);

        String[] breakActivities = {"Dance", "Stretch", "Walk", "Meditate"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, breakActivities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBreakActivities.setAdapter(adapter);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFocusSession();
            }
        });
    }

    private void startFocusSession() {
        String focusTimeStr = editTextFocusTime.getText().toString();
        String focusTimeSecondsStr = editTextFocusTimeSeconds.getText().toString(); // Get the seconds input
        String breakIntervalStr = editTextBreakInterval.getText().toString();
        String breakDurationStr = editTextBreakDuration.getText().toString();

        if (focusTimeSecondsStr.isEmpty() || breakIntervalStr.isEmpty() || breakDurationStr.isEmpty()) {
            Toast.makeText(this, "Please enter all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int focusTime = focusTimeStr.isEmpty() ? 0 : Integer.parseInt(focusTimeStr);
        int focusTimeSeconds = Integer.parseInt(focusTimeSecondsStr);
        int totalFocusTime = focusTime * 60 + focusTimeSeconds;
        int breakInterval = Integer.parseInt(breakIntervalStr);
        int breakDuration = Integer.parseInt(breakDurationStr);
        String breakActivity = spinnerBreakActivities.getSelectedItem().toString();

        Intent intent = new Intent(FocusSessionActivity.this, SessionStatusActivity.class);
        intent.putExtra("totalFocusTime", totalFocusTime); // Pass total focus time in seconds
        intent.putExtra("breakInterval", breakInterval);
        intent.putExtra("breakDuration", breakDuration);
        intent.putExtra("breakActivity", breakActivity);
        startActivity(intent);
    }
}
