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
import com.musicg.wave.Wave;
import medicine.com.spectralanalyzer.AudioProcessor;
import medicine.com.spectralanalyzer.R;
import medicine.com.spectralanalyzer.fragment.CustomWaveFormFragment;
import medicine.com.spectralanalyzer.pojo.ProcessorResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static medicine.com.spectralanalyzer.pojo.ActivityConstants.FILE_TO_PROCESS;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";

    private File audioFile;

    private AudioProcessor audioProcessor;
    private CustomWaveFormFragment waveformFragment;
    private FragmentManager fragmentManager = getSupportFragmentManager();

    private Button btnZoomIn;
    private Button btnZoomOut;
    private Button start_process_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnZoomIn = (Button) findViewById(R.id.btn_zoom_in);
        btnZoomOut = (Button) findViewById(R.id.btn_zoom_out);
        start_process_btn = (Button) findViewById(R.id.start_process_btn);

        Intent requestedIntent = getIntent();

        if (requestedIntent != null) {
            audioFile = (File) requestedIntent.getSerializableExtra(FILE_TO_PROCESS);
        } else {
            Intent resultIntent = new Intent();
            setResult(RESULT_CANCELED, resultIntent);
            finish();
        }
        processAudioFile();
        start_process_btn.setEnabled(true);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
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

    public void onBtnClick(View view) throws FileNotFoundException {
        processAudioFile();
    }

    public void showChart(View v) {
        ProcessorResult processorResult1 = new ProcessorResult();
        processorResult1.setAverageLengthOfPeristalticPeriod(5);
        processorResult1.setAverageMaxAmplitudeOfPeristalticWaves(50);
        processorResult1.setAverageAmplitudeOfPeristalticWaves(50);
        processorResult1.setAverageTimeReducingAmplitude(12.0);
        processorResult1.setAverageAmplitudeRiseTime(25.2);
        processorResult1.setAverageAmplitudeContractionsDuringNonPeristalticPeriod(15);
        processorResult1.setMaxAmplitudeContractionsDuringNonPeristalticPeriod(50.27);
        processorResult1.setIndexOfPeristalticWave(1.9);
        processorResult1.setCountWaves(25);

        ProcessorResult processorResult2 = new ProcessorResult();
        processorResult2.setAverageLengthOfPeristalticPeriod(10);
        processorResult2.setAverageMaxAmplitudeOfPeristalticWaves(80);
        processorResult2.setAverageAmplitudeOfPeristalticWaves(70);
        processorResult2.setAverageTimeReducingAmplitude(20);
        processorResult2.setAverageAmplitudeRiseTime(35.3);
        processorResult2.setAverageAmplitudeContractionsDuringNonPeristalticPeriod(20);
        processorResult2.setMaxAmplitudeContractionsDuringNonPeristalticPeriod(59.2);
        processorResult2.setIndexOfPeristalticWave(3.98);
        processorResult2.setCountWaves(35);


        Intent chartIntent = new Intent(this, ChartActivity.class);
        chartIntent.putExtra("processorResult1", processorResult1);
        chartIntent.putExtra("processorResult2", processorResult2);
        startActivity(chartIntent);
    }

    public void zoomIn(View view) {
        waveformFragment.waveformZoomIn();
    }

    public void zoomOut(View view) {
        waveformFragment.waveformZoomOut();
    }

    private void processAudioFile() {
        if (!audioFile.exists()) {
            return;
        }
        audioProcessor = new AudioProcessor(new Wave(audioFile.getAbsolutePath()));

        waveformFragment = new CustomWaveFormFragment();
        waveformFragment.setFileName(audioFile.getAbsolutePath());

//        ProcessorResult processorResult = audioProcessor.processAudio();

        List<Pair<Double, Double>> periodsOfSound = audioProcessor.getInSecondPeriods();

        waveformFragment.setPeriods(periodsOfSound);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, waveformFragment);
        fragmentTransaction.attach(waveformFragment);
        fragmentTransaction.commit();

        btnZoomIn.setEnabled(true);
        btnZoomOut.setEnabled(true);
        Log.i(TAG, "Found " + periodsOfSound.size() + " periods of sounds");
    }


}
