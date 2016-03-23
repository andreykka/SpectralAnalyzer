package medicine.com.spectralanalyzer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;

import com.musicg.wave.Wave;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {


    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);
    }

    // TODO: 23.03.16 replace using this method onto log to console
    public void write(String str) {
        editText.append(str.concat("\r\n"));
    }


    public void onBtnClick(View view) throws FileNotFoundException {
        File audioWavFile = new File(Environment.getExternalStorageDirectory(), "Download/android_shared/file1.wav");
        write("Red file".concat(audioWavFile.getAbsolutePath()));

        if (! audioWavFile.exists()) {
            write("file:".concat(audioWavFile.getAbsolutePath()).concat(" doesn't exist"));
            return;
        }

        // TODO: 23.03.16 extract this segment of code to external class
        Wave wave = new Wave(audioWavFile.getAbsolutePath());

        short[] sampleAmplitudes = wave.getSampleAmplitudes();
        int channels = wave.getWaveHeader().getChannels();

        // length of audio file
        float duration = wave.length();

        // if stereo audio. Obtain for processing only first channel.
        // doesn't matter which one.
        int oneChannelLength = (sampleAmplitudes.length % 2 == 0) ? sampleAmplitudes.length / 2 : sampleAmplitudes.length / 2 + 1;
        short[] oneChanelAudio = new short[oneChannelLength];
        if (channels == 2)  {
            for (int i=1, j=0; i < sampleAmplitudes.length; i += 2, j++ ) {
                oneChanelAudio[j] = sampleAmplitudes[i];
            }
        }

        write("Red file".concat(audioWavFile.getAbsolutePath()));

    }

    // TODO: 23.03.16 extract this method to external class
    private void findPlace(short[] waves) {
        int start, end;
        List<Pair<Integer, Integer>> periods = new ArrayList<>();
        List<Object> foundedWaves;
        ShortBuffer sb = ShortBuffer.allocate(waves.length);

        for (int i = 0; i < waves.length; i++) {
            //
        }

    }

    /**
     * Verify is length of audio segment is bigger than 0.5 sec
     *
     * @param shortBuffer segment of audio file
     * @return <b>true</b> if length > 0.5 sec, <b>false</b> otherwise.
     */
    // TODO: 23.03.16 extract this method to external class
    private boolean isLengthEnough(ShortBuffer shortBuffer, Integer sampleRate) {
        if (shortBuffer != null) {
            int capacity = shortBuffer.capacity();
            return  capacity > sampleRate;

        }
        return false;
    }

}
