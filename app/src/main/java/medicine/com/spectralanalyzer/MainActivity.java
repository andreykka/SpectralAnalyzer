package medicine.com.spectralanalyzer;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.musicg.wave.Wave;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";

    private AudioProcessor audioProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onBtnClick(View view) throws FileNotFoundException {
        File audioWavFile = new File(Environment.getExternalStorageDirectory(), "Download/android_shared/file1.wav");

        if (!audioWavFile.exists()) {
            return;
        }

        audioProcessor = new AudioProcessor(new Wave(audioWavFile.getAbsolutePath()));

        List<Pair<Integer, Integer>> periodsOfSound = audioProcessor.getPeriodsOfSound();
        Log.i(TAG, "Founded " + periodsOfSound.size() + " periods of sounds");
    }

}
