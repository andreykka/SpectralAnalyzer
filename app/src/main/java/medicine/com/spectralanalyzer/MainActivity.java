package medicine.com.spectralanalyzer;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import android.widget.Button;
import com.musicg.wave.Wave;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";

    private AudioProcessor audioProcessor;
    private CustomWaveFormFragment waveformFragment;
    private FragmentManager fragmentManager = getSupportFragmentManager();

    private Button btnZoomIn;
    private Button btnZoomOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnZoomIn = (Button) findViewById(R.id.btn_zoom_in);
        btnZoomOut = (Button) findViewById(R.id.btn_zoom_out);
    }

    public void onBtnClick(View view) throws FileNotFoundException {
        File audioWavFile = new File(Environment.getExternalStorageDirectory(), "Download/android_shared/file1.wav");

        if (!audioWavFile.exists()) {
            return;
        }

        audioProcessor = new AudioProcessor(new Wave(audioWavFile.getAbsolutePath()));

        waveformFragment = new CustomWaveFormFragment();
        waveformFragment.setFileName(audioWavFile.getAbsolutePath());

        List<Pair<Double, Double>> periodsOfSound = audioProcessor.getInSecondPeriods();
        waveformFragment.setPeriods(periodsOfSound);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.container, waveformFragment);
        fragmentTransaction.commit();
        btnZoomIn.setEnabled(true);
        btnZoomOut.setEnabled(true);
        Log.i(TAG, "Found " + periodsOfSound.size() + " periods of sounds");
    }

    public void zoomIn(View view) {
        waveformFragment.waveformZoomIn();
    }

    public void zoomOut(View view) {
        waveformFragment.waveformZoomOut();
    }

}
