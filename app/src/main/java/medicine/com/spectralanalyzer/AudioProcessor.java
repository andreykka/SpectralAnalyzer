package medicine.com.spectralanalyzer;

import android.util.Log;
import android.util.Pair;
import android.util.TimingLogger;

import com.musicg.wave.Wave;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.nio.ShortBuffer;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;

public class AudioProcessor {
    /**
     * Logger for AudioProcessor class
     */
    private static final String TAG = AudioProcessor.class.getSimpleName();

    /**
     * In seconds value, indicate allowed length of silent inside sound
     */
    private static double ALLOWED_SILENT_RATIO = 0.5;

    /**
     * Minimal value of silence.
     */
    private static int SILENT = 3;

    private Wave wave;
    private short[] oneChannelData;
    private int period;

    public AudioProcessor(Wave wave) {
        this.wave = wave;
        oneChannelData = getSingleChannelData(wave);
        period = wave.getWaveHeader().getSampleRate() / 1000; // 44100   1000 1 ms
    }

    private short[] getSingleChannelData(Wave wave) {
        short[] sampleAmplitudes = wave.getSampleAmplitudes();
        int channelsCount = wave.getWaveHeader().getChannels();

        int oneChannelLength = (sampleAmplitudes.length % channelsCount == 0)
                ? sampleAmplitudes.length / channelsCount
                : sampleAmplitudes.length / channelsCount + 1;

        short[] singleWayChannel = new short[oneChannelLength];
        for (int i=1, j=0; i < sampleAmplitudes.length; i += channelsCount, j++ ) {
            singleWayChannel[j] = sampleAmplitudes[i];
        }
    
        return singleWayChannel;
    }

    public Wave getWave() {
        return wave;
    }

    public short[] getOneChannelData() {
        return oneChannelData;
    }

    public static void setAllowedSilentRatio(double allowedSilentRatio) {
        ALLOWED_SILENT_RATIO = allowedSilentRatio;
    }

    /**
     * Finds the place in audio wave that contains sound.
     *
     * @return List of pairs, first element indicate start sample,
     * <br>second element indicate count of frames, that contain sound
     */
    public List<Pair<Integer, Integer>> getPeriodsOfSound() {
        long before = System.currentTimeMillis();
        int start, end;
        List<Pair<Integer, Integer>> periods = new ArrayList<>();
        ShortBuffer sb = ShortBuffer.allocate(oneChannelData.length / 2);

        // (int) (0.03 * wave.getWaveHeader().getSampleRate());
        int silenceCounter = 0, allowedSilenceLength = 3; // ms

        for (int i = 0; i < oneChannelData.length; i += period) {
            if (oneChannelData[i] > SILENT || oneChannelData[i] < -SILENT) {
                sb.put(oneChannelData[i]);
                silenceCounter = 0;
            } else {
                if (++silenceCounter < allowedSilenceLength) {
                    sb.put(oneChannelData[i]);
                    continue;
                }
                silenceCounter = 0;
                // current i is silence
                if (isLengthEnough(sb.position() * period, wave.getWaveHeader().getSampleRate())) {
                    start = (i - 1) - sb.position() * period;
                    end = sb.position() * period;
                    Pair<Integer, Integer> range = Pair.create(start, end);
                    periods.add(range);
                }
                sb = (ShortBuffer) sb.clear();
            }
        }
        long after = System.currentTimeMillis();
        Log.d(TAG, "Spent time: " + (after-before) + " ms");
        return periods;
    }


    /**
     * Verify is length of audio segment bigger than specified in {@link #ALLOWED_SILENT_RATIO}
     * <br>0.5 sec by default.
     *
     * @return <b>true</b> if length > 0.5sec, <b>false</b> otherwise.
     */
    private boolean isLengthEnough(Integer sampleCount, Integer sampleRate) {
        double targetSampleCountForTime = sampleRate * ALLOWED_SILENT_RATIO;
        return  sampleCount > targetSampleCountForTime;
    }
    
}
