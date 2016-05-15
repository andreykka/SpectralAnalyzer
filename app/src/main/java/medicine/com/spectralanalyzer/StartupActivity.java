package medicine.com.spectralanalyzer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import java.io.File;
import java.io.IOException;

import static medicine.com.spectralanalyzer.ActivityConstants.PATH_NAME;

public class StartupActivity extends Activity {

    private static final String DIR_NAME = "AnalyzerData";
    private static final int RECORD_AUDIO_REQUEST_CODE = 123;

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

    public void handleRecordAction(View view) {
        if (! (view instanceof Button)) {
            return;
        }

        buttonToDisable = (Button) view;

        Intent intent = new Intent(this, AudioRecorder.class);
        intent.putExtra(PATH_NAME, sessionPath);

        startActivityForResult(intent, RECORD_AUDIO_REQUEST_CODE);
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
        sessionPath += "/" + DIR_NAME;

        File f = new File(sessionPath);
        if (! f.exists()) {
            f.mkdir();
        }
    }
}