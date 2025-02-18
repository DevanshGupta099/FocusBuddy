package com.example.focusbuddy;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Spinner spinnerThemes;
    private Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load the selected theme before calling super.onCreate
        SharedPreferences preferences = getSharedPreferences("themePrefs", MODE_PRIVATE);
        String theme = preferences.getString("theme", "Default");
        if (theme.equals("Dark")) {
            setTheme(R.style.AppTheme_Dark);
        } else if (theme.equals("Light")) {
            setTheme(R.style.AppTheme_Light);
        } else {
            setTheme(R.style.AppTheme_Default);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        spinnerThemes = findViewById(R.id.spinnerThemes);
        buttonSave = findViewById(R.id.buttonSave);

        String[] themes = {"Default", "Dark", "Light"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, themes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerThemes.setAdapter(adapter);

        // Load the currently selected theme into the spinner
        int selectedPosition = adapter.getPosition(theme);
        spinnerThemes.setSelection(selectedPosition);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTheme();
            }
        });
    }

    private void saveTheme() {
        String selectedTheme = spinnerThemes.getSelectedItem().toString();
        SharedPreferences preferences = getSharedPreferences("themePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("theme", selectedTheme);
        editor.apply();

        // Apply the selected theme and recreate the activity
        recreate();
    }
}
