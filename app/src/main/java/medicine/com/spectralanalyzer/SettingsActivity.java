package medicine.com.spectralanalyzer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import static medicine.com.spectralanalyzer.pojo.PreferenceConstants.*;

public class SettingsActivity extends Activity {

    private static final int SILENT_DURATION_RATIO = 10;

    private Button saveSettingsButton;
    private Button resetSettingsButton;

    private SeekBar silenceDurationSeekBar;
    private SeekBar noiseValueSeekBar;

    private TextView silenceDurationView;
    private TextView noiseValueView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        saveSettingsButton = (Button) findViewById(R.id.saveSettingsButton);
        resetSettingsButton = (Button) findViewById(R.id.resetSettingsButton);

        silenceDurationSeekBar = (SeekBar) findViewById(R.id.silenceDurationSeekBar);
        noiseValueSeekBar = (SeekBar) findViewById(R.id.noiseValueSeekBar);

        silenceDurationView = (TextView) findViewById(R.id.silenceDurationView);
        noiseValueView = (TextView) findViewById(R.id.noiseValueView);

        // add listeners on components
        silenceDurationSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener());
        noiseValueSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener());

        saveSettingsButton.setOnClickListener(new SavePreferencesButtonClickListener());
        resetSettingsButton.setOnClickListener(new ResetPreferencesButtonClickListener());

        SharedPreferences audioProcessorSettings = getSharedPreferences(SETTINGS, MODE_PRIVATE);

        float silenceDuration = audioProcessorSettings.getFloat(SILENCE_DURATION, DEFAULT_SILENCE_DURATION);
        int noiseValue = audioProcessorSettings.getInt(NOISE_VALUE, DEFAULT_NOISE_VALUE);

        silenceDurationSeekBar.setProgress((int) (silenceDuration * SILENT_DURATION_RATIO));
        noiseValueSeekBar.setProgress(noiseValue);

    }

    private class OnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (seekBar == silenceDurationSeekBar) {
                String silenceValue = ((float) progress / SILENT_DURATION_RATIO) + " sec";
                silenceDurationView.setText(silenceValue);
            } else if (seekBar == noiseValueSeekBar) {
                String noiseTextValue = progress + "";
                noiseValueView.setText(noiseTextValue);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    private class ResetPreferencesButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            silenceDurationSeekBar.setProgress((int) (DEFAULT_SILENCE_DURATION * SILENT_DURATION_RATIO));
            noiseValueSeekBar.setProgress(DEFAULT_NOISE_VALUE);
        }
    }

    private class SavePreferencesButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            SharedPreferences.Editor edit = getSharedPreferences(SETTINGS, MODE_PRIVATE).edit();

            float silentDuration = silenceDurationSeekBar.getProgress() / (float) SILENT_DURATION_RATIO;
            int noiseValue = noiseValueSeekBar.getProgress();

            edit.putFloat(SILENCE_DURATION, silentDuration);
            edit.putInt(NOISE_VALUE, noiseValue);

            edit.commit();

            AudioProcessor.setUpConfiguration(silentDuration, noiseValue);
            finish();
        }
    }

}
