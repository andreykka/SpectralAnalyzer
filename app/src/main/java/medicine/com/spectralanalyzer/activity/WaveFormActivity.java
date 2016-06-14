package medicine.com.spectralanalyzer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.Toast;

import com.musicg.wave.Wave;
import medicine.com.spectralanalyzer.AudioProcessor;
import medicine.com.spectralanalyzer.R;
import medicine.com.spectralanalyzer.fragment.SpectralAnalyzerWaveFormFragment;

import java.io.File;
import java.util.List;

import static medicine.com.spectralanalyzer.pojo.ActivityConstants.FILE_TO_PROCESS;

public class WaveFormActivity extends FragmentActivity {

    private static final String TAG = "WaveFormActivity";

    private File audioFile;

    private SpectralAnalyzerWaveFormFragment waveformFragment;
    private FragmentManager fragmentManager = getSupportFragmentManager();

    private Button btnZoomIn;
    private Button btnZoomOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waveform_layout);

        btnZoomIn = (Button) findViewById(R.id.btn_zoom_in);
        btnZoomOut = (Button) findViewById(R.id.btn_zoom_out);
        Button findPeriodsBtn = (Button) findViewById(R.id.find_periods_btn);
        findPeriodsBtn.setOnClickListener(new FindPeriodsBtnClickListener());

        Intent requestedIntent = getIntent();
        if (requestedIntent != null) {
            audioFile = (File) requestedIntent.getSerializableExtra(FILE_TO_PROCESS);
        } else {
            Intent resultIntent = new Intent();
            setResult(RESULT_CANCELED, resultIntent);
            finish();
        }
        processAudioFile();
        findPeriodsBtn.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.configuration_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.settings_item: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            }
        }
        return true;
    }

    public void zoomIn(View view) {
        waveformFragment.waveformZoomIn();
    }

    public void zoomOut(View view) {
        waveformFragment.waveformZoomOut();
    }

    private class FindPeriodsBtnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            processAudioFile();
        }
    }

    private void processAudioFile() {
        if (!audioFile.exists()) {
            return;
        }
        Wave wave = new Wave(audioFile.getAbsolutePath());
        AudioProcessor audioProcessor = new AudioProcessor(AudioProcessor.getSingleChannelData(wave),
                wave.getWaveHeader().getSampleRate());

        waveformFragment = new SpectralAnalyzerWaveFormFragment();
        waveformFragment.setFileName(audioFile.getAbsolutePath());

        List<Pair<Double, Double>> periodsOfSound = audioProcessor.getInSecondPeriods();
        waveformFragment.setPeriods(periodsOfSound);
        audioProcessor.destroy();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.attach(waveformFragment);
        fragmentTransaction.replace(R.id.container, waveformFragment);
        fragmentTransaction.commit();

        btnZoomIn.setEnabled(true);
        btnZoomOut.setEnabled(true);

        String foundPeriods = "Found " + periodsOfSound.size() + " periods";
        Toast toast = Toast.makeText(getApplicationContext(), foundPeriods, Toast.LENGTH_LONG);
        toast.show();

        Log.i(TAG, foundPeriods + " of sounds");
    }


}
