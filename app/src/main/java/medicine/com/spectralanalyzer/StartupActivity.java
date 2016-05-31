package medicine.com.spectralanalyzer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import com.musicg.wave.Wave;
import medicine.com.spectralanalyzer.pojo.PreferenceConstants;
import medicine.com.spectralanalyzer.pojo.ProcessorResult;
import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static medicine.com.spectralanalyzer.ActivityConstants.DATE_TIME_FORMATTER;
import static medicine.com.spectralanalyzer.ActivityConstants.PATH_NAME;
import static medicine.com.spectralanalyzer.pojo.PreferenceConstants.*;

public class StartupActivity extends Activity {

    private static final String DIR_NAME = "AnalyzerData";
    private static final int RECORD_AUDIO_REQUEST_CODE = 123;

    private AudioProcessor audioProcessor;

    private String sessionPath;

    private Button btnRecord1;
    private Button btnRecord2;
    private Button btnRecord3;
    private Button btnRecord4;
    private Button btnRecord5;

    private CheckBox checkBox1;
    private CheckBox checkBox2;
    private CheckBox checkBox3;
    private CheckBox checkBox4;
    private CheckBox checkBox5;

    private Button buttonToDisable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_input);

        initializeSessionPath();

        PreferenceManager.setDefaultValues(this, R.xml.audio_processor_configuration, false);

        btnRecord1 = (Button) findViewById(R.id.btnStartRecord1);
        btnRecord2 = (Button) findViewById(R.id.btnStartRecord2);
        btnRecord3 = (Button) findViewById(R.id.btnStartRecord3);
        btnRecord4 = (Button) findViewById(R.id.btnStartRecord4);
        btnRecord5 = (Button) findViewById(R.id.btnStartRecord5);

        checkBox1 = (CheckBox) findViewById(R.id.checkBoxStatusRecord1);
        checkBox2 = (CheckBox) findViewById(R.id.checkBoxStatusRecord2);
        checkBox3 = (CheckBox) findViewById(R.id.checkBoxStatusRecord3);
        checkBox4 = (CheckBox) findViewById(R.id.checkBoxStatusRecord4);
        checkBox5 = (CheckBox) findViewById(R.id.checkBoxStatusRecord5);
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

    private void setUpAudioProcessorConfiguration() {
        SharedPreferences audioProcessorPreferences = getSharedPreferences(AUDIO_PROCESSOR_CONFIGURATION, MODE_PRIVATE);
        float silentDuration = audioProcessorPreferences.getFloat(SILENCE_DURATION, DEFAULT_SILENCE_DURATION);
        int noiseValue = audioProcessorPreferences.getInt(NOISE_VALUE, DEFAULT_NOISE_VALUE);
        AudioProcessor.setUpConfiguration(silentDuration, noiseValue);
    }

    public void handleRecordAction(View view) {
        if (! (view instanceof Button)) {
            return;
        }

        buttonToDisable = (Button) view;

        Intent intent = new Intent(this, AudioRecorder3.class);
        intent.putExtra(PATH_NAME, sessionPath);

        startActivityForResult(intent, RECORD_AUDIO_REQUEST_CODE);
    }

    public void submitRequest(View view) {
        File dir = new File(sessionPath);
        File[] files = dir.listFiles();

        // refresh configuration of AudioProcessor
        setUpAudioProcessorConfiguration();

        List<ProcessorResult> resultList = new ArrayList<>();

        for (File file : files) {
            if (file.exists()) {
                audioProcessor = new AudioProcessor(new Wave(file.getAbsolutePath()));
                resultList.add(audioProcessor.processAudio());
                audioProcessor = null;
            }
        }

        int size = resultList.size();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (buttonToDisable == null) {
                return;
            }
            if (resultCode == RESULT_OK) {
                buttonToDisable.setEnabled(false);
                buttonToDisable.setBackgroundColor(ContextCompat.getColor(this, R.color.actionSuccess));
                buttonToDisable = null;
            } else if (resultCode == RESULT_CANCELED) {
                buttonToDisable.setEnabled(false);
                buttonToDisable.setBackgroundColor(ContextCompat.getColor(this, R.color.actionFail));
                buttonToDisable = null;
            }
        }
    }

    private void initializeSessionPath() {
        sessionPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        sessionPath += "/" + DIR_NAME + "/" + DATE_TIME_FORMATTER.print(DateTime.now());

        File f = new File(sessionPath);
        if (! f.exists()) {
            f.mkdirs();
        }
    }
}
