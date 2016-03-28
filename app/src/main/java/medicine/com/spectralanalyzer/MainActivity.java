package medicine.com.spectralanalyzer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Pair;
import android.view.View;

import com.musicg.wave.Wave;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onBtnClick(View view) throws FileNotFoundException {
        File audioWavFile = new File(Environment.getExternalStorageDirectory(), "Download/android_shared/file1.wav");

        if (! audioWavFile.exists()) {
            return;
        }

        // TODO: 23.03.16 extract this segment of code to external class
        Wave wave = new Wave(audioWavFile.getAbsolutePath());

        short[] sampleAmplitudes = wave.getSampleAmplitudes();
        int channels = wave.getWaveHeader().getChannels();

        // if stereo audio. Obtain for processing only first channel.
        // doesn't matter which one.
        int oneChannelLength = (sampleAmplitudes.length % 2 == 0) ? sampleAmplitudes.length / 2 : sampleAmplitudes.length / 2 + 1;
        short[] oneChanelAudio = new short[oneChannelLength];
        if (channels == 2)  {
            for (int i=1, j=0; i < sampleAmplitudes.length; i += 2, j++ ) {
                oneChanelAudio[j] = sampleAmplitudes[i];
            }
        }
        findPlace(wave, oneChanelAudio);

    }

    // TODO: 23.03.16 extract this method to external class
    private void findPlace(Wave wave, short[] waves) {
        int start, end;
        List<Pair<Integer, Integer>> periods = new ArrayList<>();
        ShortBuffer sb = ShortBuffer.allocate(waves.length / 2);

        int silence = 50;

        double halfSecondRate = 0.5;

        Integer allowedSilenceLength = (int) (0.03 * wave.getWaveHeader().getSampleRate());
        Integer silenceCounter = 0;

        int max = 0;
        for (int i = 0; i < waves.length; i++) {
            max = max < waves[i] ? waves[i] : max;
            if (waves[i] > silence || waves[i] < -silence) {
                sb.put(waves[i]);
                silenceCounter = 0;
            } else {
                if (++silenceCounter < allowedSilenceLength) {
                    sb.put(waves[i]);
                    continue;
                }
                silenceCounter = 0;
                // current i is silence
                if (isLengthEnough(sb.position(), wave.getWaveHeader().getSampleRate(), halfSecondRate)) {
                    start = i - sb.position();
                    end = sb.position()-1;
                    Pair<Integer, Integer> period = Pair.create(start, end);
                    periods.add(period);
                }
                sb = (ShortBuffer) sb.clear();
            }
        }

    }

    /**
     * Verify is length of audio segment is bigger than 0.5 sec
     *
     * @return <b>true</b> if length > 0.5 sec, <b>false</b> otherwise.
     */
    // TODO: 23.03.16 extract this method to external class
    private boolean isLengthEnough(Integer sampleCount, Integer sampleRate, Double targetSeconds) {
        double targetSampleCountForTime = sampleRate * targetSeconds;
        return  sampleCount > targetSampleCountForTime;
    }

}
