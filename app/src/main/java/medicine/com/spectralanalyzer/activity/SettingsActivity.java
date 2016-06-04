package medicine.com.spectralanalyzer.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import medicine.com.spectralanalyzer.AudioProcessor;
import medicine.com.spectralanalyzer.R;

import static medicine.com.spectralanalyzer.pojo.SettingConstants.*;

public class SettingsActivity extends Activity {

    private static final int SOUND_DURATION_RATIO = 20;
    private static final String MIN_DURATION_TEXT_FORMAT = "%1$,.2f sec";
    private static final String MAX_SILENCE_LENGTH_TEXT_FORMAT = "%1s ms";
    private static final String NOISE_VALUE_TEXT_FORMAT = "%1s";

    private SeekBar minSoundDuration;
    private SeekBar maxSilenceLength;
    private SeekBar noiseValueSeekBar;

    private TextView minSoundDurationView;
    private TextView maxSilenceLengthView;
    private TextView noiseValueView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        Button saveSettingsButton = (Button) findViewById(R.id.saveSettingsButton);
        Button resetSettingsButton = (Button) findViewById(R.id.resetSettingsButton);

        minSoundDuration = (SeekBar) findViewById(R.id.minSoundDurationSeekBar);
        maxSilenceLength = (SeekBar) findViewById(R.id.MaxSilenceLengthSeekBar);
        noiseValueSeekBar = (SeekBar) findViewById(R.id.noiseValueSeekBar);

        minSoundDurationView = (TextView) findViewById(R.id.minSoundDurationView);
        maxSilenceLengthView = (TextView) findViewById(R.id.maxSilenceLengthView);
        noiseValueView = (TextView) findViewById(R.id.noiseValueView);

        // add listeners on components
        minSoundDuration.setOnSeekBarChangeListener(new OnSeekBarChangeListener());
        maxSilenceLength.setOnSeekBarChangeListener(new OnSeekBarChangeListener());
        noiseValueSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener());

        saveSettingsButton.setOnClickListener(new SavePreferencesButtonClickListener());
        resetSettingsButton.setOnClickListener(new ResetPreferencesButtonClickListener());
        populateSeekBarsData();
    }

    private void populateSeekBarsData() {
        SharedPreferences audioProcessorSettings = getSharedPreferences(SETTINGS, MODE_PRIVATE);

        float minSoundDurationValue = audioProcessorSettings.getFloat(MIN_SOUND_DURATION, DEFAULT_MIN_SOUND_DURATION);
        int maxSilenceLengthValue = audioProcessorSettings.getInt(MAX_SILENT_LENGTH, DEFAULT_MAX_SILENT_LENGTH);
        int noiseValue = audioProcessorSettings.getInt(NOISE_VALUE, DEFAULT_NOISE_VALUE);

        minSoundDuration.setProgress(0);
        maxSilenceLength.setProgress(0);
        noiseValueSeekBar.setProgress(0);

        minSoundDuration.setProgress((int) (minSoundDurationValue * SOUND_DURATION_RATIO));
        maxSilenceLength.setProgress(maxSilenceLengthValue);
        noiseValueSeekBar.setProgress(noiseValue);
    }

    private class OnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (seekBar == minSoundDuration) {
                minSoundDurationView.setText(String.format(MIN_DURATION_TEXT_FORMAT, (float) progress / SOUND_DURATION_RATIO));
            } else if (seekBar == noiseValueSeekBar) {
                noiseValueView.setText(String.format(NOISE_VALUE_TEXT_FORMAT, progress));
            } else if (seekBar == maxSilenceLength) {
                maxSilenceLengthView.setText(String.format(MAX_SILENCE_LENGTH_TEXT_FORMAT, progress));
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
            minSoundDuration.setProgress((int) (DEFAULT_MIN_SOUND_DURATION * SOUND_DURATION_RATIO));
            maxSilenceLength.setProgress(DEFAULT_MAX_SILENT_LENGTH);
            noiseValueSeekBar.setProgress(DEFAULT_NOISE_VALUE);
        }
    }

    private class SavePreferencesButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            SharedPreferences.Editor edit = getSharedPreferences(SETTINGS, MODE_PRIVATE).edit();

            float minSoundDurationValue = minSoundDuration.getProgress() / (float) SOUND_DURATION_RATIO;
            int maxSilenceLengthValue = maxSilenceLength.getProgress();
            int noiseValue = noiseValueSeekBar.getProgress();

            edit.putFloat(MIN_SOUND_DURATION, minSoundDurationValue);
            edit.putInt(MAX_SILENT_LENGTH, maxSilenceLengthValue);
            edit.putInt(NOISE_VALUE, noiseValue);

            edit.apply();

            AudioProcessor.setUpConfiguration(minSoundDurationValue, maxSilenceLengthValue, noiseValue);
            finish();
        }
    }

}
