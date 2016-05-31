package medicine.com.spectralanalyzer;

import android.util.Log;
import android.util.Pair;

import com.musicg.wave.Wave;
import medicine.com.spectralanalyzer.pojo.ProcessorResult;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AudioProcessor {
    /**
     * Logger for AudioProcessor class
     */
    private static final String TAG = AudioProcessor.class.getSimpleName();

    /**
     *10 In seconds value, indicate allowed length of silent inside sound
     */
    private static float ALLOWED_SILENT_RATIO = 0.5f;

    /**
     * Minimal value of silence.
     */
    private static int SILENT = 0;

    private Wave wave;

    private short[] singleChannelData;

    /**
     * Indicate how much element contains in 1 sec of sound
     */
    private int sampleRate;

    /**
     *10 Step to navigating = 1 millisecond
     */
    private int period;

    public AudioProcessor(Wave wave) {
        this.wave = wave;
        singleChannelData = getSingleChannelData(wave);
        sampleRate = wave.getWaveHeader().getSampleRate();
        period = sampleRate / 1000; // 44100/1000 = 1 ms
    }

    public static void setUpConfiguration(float allowedSilentRatio, int silentValue) {
        ALLOWED_SILENT_RATIO = allowedSilentRatio;
        SILENT = silentValue;
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

    public static void setAllowedSilentDuration(float allowedSilentRatio) {
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
        ShortBuffer buff = ShortBuffer.allocate(singleChannelData.length / 2);

        int silenceCounter = 0;
        int allowedSilenceLength = 4; // milliseconds

        for (int i = 0; i < singleChannelData.length; i += period) {
            if (singleChannelData[i] > SILENT || singleChannelData[i] < -SILENT) {
                buff.put(singleChannelData[i]);
                silenceCounter = 0;
            } else {
                if (++silenceCounter < allowedSilenceLength) {
                    buff.put(singleChannelData[i]);
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
            first = getInSecondValueOfPosition(period.first);
            second = getInSecondValueOfPosition(period.second);
            inSecondPeriods.add(Pair.create(first, second));
        }
        return inSecondPeriods;
    }

    private Double getInSecondValueOfPosition(Integer position) {
        return position / (double) sampleRate;
    }

    public List<Short> getDataForPeriod(int start, int end) {
        int length = end - start;
        if (length <= 0)
            throw  new IllegalArgumentException("End period can't be bigger than start");
        short[] out = new short[length];
        System.arraycopy(singleChannelData, start, out, 0, length);

        return Arrays.asList(ArrayUtils.toObject(out));
    }

    /**
     * Find max element.
     *
     * @param list in which will be searching
     * @return Pair of vales (first->max value) (second->max index)
     */
    private Pair<Integer, Integer> getMaxValueAndIndex(List<Short> list) {
        int longPeriod = period;
        Pair<Integer, Integer> fastMax = getNearToMaxValueAndIndex(list, longPeriod);

        // get index of supposed max element
        int fIndex = fastMax.second ;

        int start = (fIndex-longPeriod > 0) ? fIndex-longPeriod : 0;
        int end = (fIndex+longPeriod <= list.size()) ? fIndex+longPeriod : list.size();

        List<Short> listToDetail = list.subList(start, end);

        return getNearToMaxValueAndIndex(listToDetail, 1);
    }

    private Pair<Integer, Integer> getNearToMaxValueAndIndex(List<Short> items, Integer period) {
        int maxValue = Integer.MIN_VALUE;
        int index = 0;

        for (int i = 0; i < items.size(); i+= period){
            if (items.get(i) > maxValue) {
                index = i;
                maxValue = items.get(i);
            }
        }

        return Pair.create(maxValue, index);
    }

    public ProcessorResult processAudio() {
        ProcessorResult processorResult = new ProcessorResult();

        List<Pair<Integer, Integer>> periodsOfSound = getPeriodsOfSound();
        List<Pair<Integer, Integer>> periodsBetweenSounds = getPeriodsBetweenSounds(periodsOfSound);
        // 1
        processorResult.setCountWaves(periodsOfSound.size());
        // 2
        processorResult.setAvrgWaveDuration(getAvrgDurationByPeriods(periodsOfSound));
        // 3 max + max + ... / count
        processorResult.setAvrgMaxAmplitudePeristalticWaves(getMaxAndAvrgMaxAmplitude(periodsOfSound).second / Short.MAX_VALUE);

        // 4 ???
        Pair<Integer, Double> maxAndAvrgMaxNonPeristalticPeriod = getMaxAndAvrgMaxAmplitude(periodsBetweenSounds);
        // 5
        processorResult.setMaxReductionAmplitudeInNotPeristalticPeriod(maxAndAvrgMaxNonPeristalticPeriod.first / Short.MAX_VALUE);

        // 6
        processorResult.setAvrgReductionAmplitudeInNotPeristalticPeriod(maxAndAvrgMaxNonPeristalticPeriod.second / Short.MAX_VALUE);
        Pair<Double, Double> durationToMaxAndFromMax = calculateDurationToMaxAndFromMax(periodsOfSound);

        // 7
        processorResult.setAvrgAmplitudeIncreasingTime(durationToMaxAndFromMax.first);

        // 8
        processorResult.setAvrgAmplitudeDecreasingTime(durationToMaxAndFromMax.second / Short.MAX_VALUE);

        return processorResult;
    }

    private double getInSecondsDurationBySamples(Integer sampleCount) {
        return (double) sampleCount / sampleRate;
    }

    /**
     * Get in seconds duration of peristaltic period
     * @param periods
     * @return
     */
    private double getAvrgDurationByPeriods(List<Pair<Integer, Integer>> periods) {
        int sampleCount = 0;

        for (Pair<Integer, Integer> period: periods) {
            // calculate duration of each wave
            sampleCount += period.second - period.first;
        }
        return getInSecondsDurationBySamples(sampleCount) / (double) periods.size();
    }

    /**
     * Returns pair of values
     * <br> First - pair of [Max value] and [Index]
     * <br> Second - Average of max values
     *
     * @param periods list of periods.
     * @return Pair of max and average max values.
     */
    private Pair<Integer, Double> getMaxAndAvrgMaxAmplitude(List<Pair<Integer, Integer>> periods) {
        List<Integer> maxAmplitudes = new ArrayList<>(periods.size());

        List<Short> wavesForPeriod;
        for (Pair<Integer, Integer> period: periods) {
            wavesForPeriod = getDataForPeriod(period.first, period.second);
            Pair<Integer, Integer> maxValueAndIndex = getMaxValueAndIndex(wavesForPeriod);

            maxAmplitudes.add(maxValueAndIndex.first);
        }

        int sumOfMaxAmplitudeValues = 0;
        int max = 0;

        for (Integer amplitude : maxAmplitudes) {
            if (max > amplitude) {
                max = amplitude;
            }
            sumOfMaxAmplitudeValues += amplitude;
        }

        double avrgMax = (double) sumOfMaxAmplitudeValues / (double) periods.size();

        return Pair.create(max, avrgMax);
    }


    /**
     * Get Pair of values that indicates.
     * <br>First - duration to max element.
     * <br>Second - duration from max element to the end.
     *
     * @param periods
     * @return
     */
    private Pair<Double, Double> calculateDurationToMaxAndFromMax(List<Pair<Integer, Integer>> periods) {
        List<Short> wavesWithMaxValue = new ArrayList<>();
        Pair<Integer, Integer> maxValueWithIndex = Pair.create(0, 0);

        int max = 0;

        List<Short> wavesForPeriod;
        for (Pair<Integer, Integer> period : periods) {
            wavesForPeriod = getDataForPeriod(period.first, period.second);
            Pair<Integer, Integer> pair = getMaxValueAndIndex(wavesForPeriod);
            if (pair.first > max) {
                max = pair.first;
                maxValueWithIndex = pair;
                wavesWithMaxValue = wavesForPeriod;
            }
        }

        double toMaxDuration = getInSecondsDurationBySamples(maxValueWithIndex.second - 1);
        double fromMaxDuration = getInSecondsDurationBySamples(wavesWithMaxValue.size() - 1 - maxValueWithIndex.second);

        return Pair.create(toMaxDuration, fromMaxDuration);
    }


    public List<Pair<Integer, Integer>> getPeriodsBetweenSounds(List<Pair<Integer, Integer>> periodsOfSound) {
        List<Pair<Integer, Integer>> nonPeristalticPeriods = new ArrayList<>();

        int start = 0;
        int end;

        for (int i = 0; i < periodsOfSound.size(); i++) {
            if (periodsOfSound.get(i).first == 0) {
                start = periodsOfSound.get(i).second;
                continue;
            }

            end = periodsOfSound.get(i).first;
            nonPeristalticPeriods.add(Pair.create(start, end));

            start = periodsOfSound.get(i).second;
            if (i == periodsOfSound.size() - 1 && start < singleChannelData.length - 1) {
                end = singleChannelData.length - 1;
                nonPeristalticPeriods.add(Pair.create(start, end));
            }
        }
        return nonPeristalticPeriods;

    }

}
