package medicine.com.spectralanalyzer.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.musicg.wave.Wave;

import medicine.com.spectralanalyzer.AudioProcessor;
import medicine.com.spectralanalyzer.R;
import medicine.com.spectralanalyzer.adapter.ArrayAdapterItem;
import medicine.com.spectralanalyzer.pojo.ItemData;
import medicine.com.spectralanalyzer.pojo.ProcessorResult;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static medicine.com.spectralanalyzer.pojo.ActivityConstants.DATE_TIME_FORMATTER;
import static medicine.com.spectralanalyzer.pojo.ActivityConstants.FILE_TO_PROCESS;
import static medicine.com.spectralanalyzer.pojo.ActivityConstants.PATH_NAME;
import static medicine.com.spectralanalyzer.pojo.ActivityConstants.PROCESS_RESULT_PARAM;
import static medicine.com.spectralanalyzer.pojo.SettingConstants.DEFAULT_MAX_SILENT_LENGTH;
import static medicine.com.spectralanalyzer.pojo.SettingConstants.DEFAULT_MIN_SOUND_DURATION;
import static medicine.com.spectralanalyzer.pojo.SettingConstants.DEFAULT_NOISE_VALUE;
import static medicine.com.spectralanalyzer.pojo.SettingConstants.MAX_SILENT_LENGTH;
import static medicine.com.spectralanalyzer.pojo.SettingConstants.MIN_SOUND_DURATION;
import static medicine.com.spectralanalyzer.pojo.SettingConstants.NOISE_VALUE;
import static medicine.com.spectralanalyzer.pojo.SettingConstants.SETTINGS;

public class MainManageActivity extends Activity {

    private static final String PROJECT_DIR_NAME = "AnalyzerData";
    private static final int RECORD_AUDIO_REQUEST_CODE = 9379992;

    private File sessionDir;

    private ListView listView;
    private List<ItemData> listViewItems;
    private ArrayAdapterItem adapter;

    private Button processAudioBtn;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);

        initializeSessionPath();
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        Button addRecordBtn = (Button) findViewById(R.id.addRecordBtn);
        processAudioBtn = (Button) findViewById(R.id.processAudioBtn);

        addRecordBtn.setOnClickListener(new AddRecordOnClickListener());
        processAudioBtn.setOnClickListener(new ProcessRecordsOnClickListener());

        listViewItems = getLatestData();
        listView = (ListView) findViewById(R.id.listView);
        adapter = new ArrayAdapterItem(this, R.layout.list_view_row_item, listViewItems);
        listView.setAdapter(adapter);
        listView.setVisibility(adapter.isEmpty() ? View.GONE : View.VISIBLE);
        listView.setOnItemClickListener(new OnListViewItemClickListener());
        refreshEnabilityOfProcessBtn();
        setUpAudioProcessorInitialSetting();
    }

    private void setUpAudioProcessorInitialSetting(){
        SharedPreferences audioProcessorSettings = getSharedPreferences(SETTINGS, MODE_PRIVATE);

        float minSoundDurationValue = audioProcessorSettings.getFloat(MIN_SOUND_DURATION, DEFAULT_MIN_SOUND_DURATION);
        int maxSilenceLengthValue = audioProcessorSettings.getInt(MAX_SILENT_LENGTH, DEFAULT_MAX_SILENT_LENGTH);
        int noiseValue = audioProcessorSettings.getInt(NOISE_VALUE, DEFAULT_NOISE_VALUE);

        AudioProcessor.setUpConfiguration(minSoundDurationValue, maxSilenceLengthValue, noiseValue);
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

    private List<ItemData> getLatestData() {
        List<ItemData> itemDataList = new ArrayList<>();

        File[] files = sessionDir.listFiles();
        int tagGenerator = 0;
        for (File file : files) {
            itemDataList.add(new ItemData(++tagGenerator, file));
        }

        return itemDataList;
    }

    private void refreshListViewData() {
        List<ItemData> latestData = getLatestData();
        for (ItemData data : latestData) {
            if (!listViewItems.contains(data)) {
                listViewItems.add(data);
            }
        }
        adapter.notifyDataSetChanged();
        listView.setVisibility(adapter.isEmpty() ? View.GONE : View.VISIBLE);
        refreshEnabilityOfProcessBtn();
    }

    private void refreshEnabilityOfProcessBtn() {
        processAudioBtn.setEnabled(!adapter.isEmpty());
    }

    private void initializeSessionPath() {
        String sessionDirName = DATE_TIME_FORMATTER.print(DateTime.now());

        if (isExternalStorageAvailable()) {
            File externalStorage = Environment.getExternalStorageDirectory();
            String externalStorageSessionDirName = PROJECT_DIR_NAME + "/" + sessionDirName;
            sessionDir = new File(externalStorage, externalStorageSessionDirName);
        } else {
            sessionDir = new File(getFilesDir(), sessionDirName);
        }

        if (!sessionDir.exists()) {
            sessionDir.mkdirs();
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshListViewData();
    }

    private class AddRecordOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), AudioRecorder.class);
            intent.putExtra(PATH_NAME, sessionDir);
            startActivityForResult(intent, RECORD_AUDIO_REQUEST_CODE);
        }
    }

    private class ProcessRecordsOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            progressDialog = ProgressDialog.show(MainManageActivity.this, "Wait...",
                    "Audio files processing...", true);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final List<ProcessorResult> resultList = new ArrayList<>();

                    File[] files = sessionDir.listFiles();
                    for (File file : files) {
                        if (file.exists()) {
                            Wave wave = new Wave(file.getAbsolutePath());
                            AudioProcessor audioProcessor = new AudioProcessor(AudioProcessor.getSingleChannelData(wave),
                                    wave.getWaveHeader().getSampleRate());
                            resultList.add(audioProcessor.processAudio());
                            audioProcessor.destroy();
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(), ChartActivity.class);
                            intent.putExtra(PROCESS_RESULT_PARAM, getAverageValuesByAllProcessorResults(resultList));
                            startActivity(intent);
                        }
                    });
                }
            }).start();

        }
    }

    private ProcessorResult getAverageValuesByAllProcessorResults(List<ProcessorResult> results) {
        if (results == null || results.size() <= 0) {
            return null;
        }
        int count = results.size();
        ProcessorResult processorResult = new ProcessorResult();
        if (count == 1) {
            return results.get(0);
        }
        int countWaves = 0;
        double averageLengthOfPeristalticPeriod = .0;
        double averageMaxAmplitudeOfPeristalticWaves = .0;
        double averageAmplitudeOfPeristalticWaves = .0;
        double maxAmplitudeContractionsDuringNonPeristalticPeriod = .0;
        double averageAmplitudeContractionsDuringNonPeristalticPeriod = .0;
        double averageAmplitudeRiseTime = .0;
        double averageTimeReducingAmplitude = .0;
        double indexOfPeristalticWave = .0;

        for (ProcessorResult result: results) {
            countWaves += result.getCountWaves();
            averageLengthOfPeristalticPeriod += result.getAverageLengthOfPeristalticPeriod();
            averageMaxAmplitudeOfPeristalticWaves += result.getAverageMaxAmplitudeOfPeristalticWaves();
            averageAmplitudeOfPeristalticWaves += result.getAverageAmplitudeOfPeristalticWaves();
            maxAmplitudeContractionsDuringNonPeristalticPeriod += result.getMaxAmplitudeContractionsDuringNonPeristalticPeriod();
            averageAmplitudeContractionsDuringNonPeristalticPeriod += result.getAverageAmplitudeContractionsDuringNonPeristalticPeriod();
            averageAmplitudeRiseTime += result.getAverageAmplitudeRiseTime();
            averageTimeReducingAmplitude += result.getAverageTimeReducingAmplitude();
            indexOfPeristalticWave += result.getIndexOfPeristalticWave();
        }

        processorResult.setCountWaves(countWaves / count);
        processorResult.setAverageLengthOfPeristalticPeriod(averageLengthOfPeristalticPeriod / count);
        processorResult.setAverageMaxAmplitudeOfPeristalticWaves(averageMaxAmplitudeOfPeristalticWaves / count);
        processorResult.setAverageAmplitudeOfPeristalticWaves(averageAmplitudeOfPeristalticWaves / count);
        processorResult.setMaxAmplitudeContractionsDuringNonPeristalticPeriod(
                maxAmplitudeContractionsDuringNonPeristalticPeriod / count);
        processorResult.setAverageAmplitudeContractionsDuringNonPeristalticPeriod(
                averageAmplitudeContractionsDuringNonPeristalticPeriod / count);
        processorResult.setAverageAmplitudeRiseTime(averageAmplitudeRiseTime / count);
        processorResult.setAverageTimeReducingAmplitude(averageTimeReducingAmplitude / count);
        processorResult.setIndexOfPeristalticWave(indexOfPeristalticWave / count);

        return processorResult;
    }

    private class OnListViewItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ItemData selectedItem = adapter.getItem(position);
            File selectedFileToProcess = selectedItem.getFile();
            Intent intent = new Intent(getApplicationContext(), WaveFormActivity.class);
            intent.putExtra(FILE_TO_PROCESS, selectedFileToProcess);
            startActivity(intent);
        }
    }

}
