package medicine.com.spectralanalyzer;

import android.util.Log;
import android.util.Pair;

import com.musicg.wave.Wave;

import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private static int SILENT = 2;

    private Wave wave;
    private short[] oneChannelData;
    private int sampleRate;
    private int period;

    public AudioProcessor(Wave wave) {
        this.wave = wave;
        oneChannelData = getSingleChannelData(wave);
        sampleRate = wave.getWaveHeader().getSampleRate();
        period = sampleRate / 1000; // 44100/1000 = 1 ms
    }

    private short[] getSingleChannelData(Wave wave) {
        short[] sampleAmplitudes = wave.getSampleAmplitudes();
        int channelsCount = wave.getWaveHeader().getChannels();

        int oneChannelLength = (sampleAmplitudes.length % channelsCount == 0)
                ? sampleAmplitudes.length / channelsCount
                : sampleAmplitudes.length / channelsCount + 1;

        short[] singleWayChannel = new short[oneChannelLength];
        for (int i = 1, j = 0; i < sampleAmplitudes.length; i += channelsCount, j++) {
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

    public static void setSILENT(int SILENT) {
        AudioProcessor.SILENT = SILENT;
    }

    /**
     * Finds the place in audio wave that contains sound.
     *
     * @return List of pairs, first element indicate start sample,
     * <br>second element indicate end sample.
     */
    public List<Pair<Integer, Integer>> getPeriodsOfSound() {
        long before = System.currentTimeMillis();
        int start, end;
        List<Pair<Integer, Integer>> periods = new ArrayList<>();
        ShortBuffer buff = ShortBuffer.allocate(oneChannelData.length / 2);

        int silenceCounter = 0;
        int allowedSilenceLength = 4; // ms

        for (int i = 0; i < oneChannelData.length; i += period) {
            if (oneChannelData[i] > SILENT || oneChannelData[i] < -SILENT) {
                buff.put(oneChannelData[i]);
                silenceCounter = 0;
            } else {
                if (++silenceCounter < allowedSilenceLength) {
                    buff.put(oneChannelData[i]);
                    continue;
                }
                silenceCounter = 0;
                // current i is silence
                if (isLengthEnough(buff.position() * period)) {
                    end = i - 1;
                    start = end - (buff.position() * period);
                    start = start < 0 ? 0 : start;
                    Pair<Integer, Integer> range = Pair.create(start, end);
                    periods.add(range);
                }
                buff = (ShortBuffer) buff.clear();
            }
        }
        long after = System.currentTimeMillis();
        Log.d(TAG, "Spent time: " + (after - before) + " ms");
        Log.d(TAG, "Found periods: " + periods.size());
        return periods;
    }


    /**
     * Verify is length of audio segment bigger than specified in {@link #ALLOWED_SILENT_RATIO}
     * <br>0.5 sec by default.
     *
     * @return <b>true</b> if length > 0.5sec, <b>false</b> otherwise.
     */
    private boolean isLengthEnough(Integer sampleCount) {
        double targetSampleCountForTime = sampleRate * ALLOWED_SILENT_RATIO;
        return sampleCount > targetSampleCountForTime;
    }

    public List<Pair<Double, Double>> getInSecondPeriods() {
        List<Pair<Integer, Integer>> periodsOfSound = getPeriodsOfSound();
        List<Pair<Double, Double>> inSecondPeriods = new ArrayList<>(periodsOfSound.size());
        double first, second;
        for (Pair<Integer, Integer> period: periodsOfSound) {
            first = period.first / 44100d;
            second = period.second / 44100d;
            inSecondPeriods.add(Pair.create(first, second));
        }
        return inSecondPeriods;
    }

    public List<Short> getDataForPeriod(int start, int end) {
        int length = end - start;
        if (length <= 0)
            throw  new IllegalArgumentException("End period can't be bigger than start");
        List<Short> data = new ArrayList<>(length);
        short[] out = new short[length];
        System.arraycopy(oneChannelData, start, out, 0, length);

        for (short s: out) {
            data.add(s);
        }

        return data;
    }



}
