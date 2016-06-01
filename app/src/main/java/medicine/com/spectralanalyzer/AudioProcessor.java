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

    /**
     * Finds the place in audio wave that contains sound.
     *
     * @return List of pairs, first element indicate start sample,
     * <br>second element indicate end sample.
     */
    public List<Pair<Integer, Integer>> getPeristalticPeriods() {
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
        List<Pair<Integer, Integer>> periodsOfSound = getPeristalticPeriods();
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

    public List<List<Short>> getDataForPeriods(List<Pair<Integer, Integer>> periods) {
        List<List<Short>> periodsData = new ArrayList<>(periods.size());

        List<Short> wavesForPeriod;
        for (Pair<Integer, Integer> period : periods) {
            wavesForPeriod = getDataForPeriod(period.first, period.second);
            periodsData.add(wavesForPeriod);
        }
        return periodsData;
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

        List<Pair<Integer, Integer>> peristalticPeriod = getPeristalticPeriods();
        List<Pair<Integer, Integer>> nonPeristalticPeriod = getPeriodsBetweenSounds(peristalticPeriod);

        List<List<Short>> peristalticPeriodsData = getDataForPeriods(peristalticPeriod);

        // 1
        processorResult.setCountWaves(peristalticPeriod.size());

        // 2
        processorResult.setAverageLengthOfPeristalticPeriod(getAverageLengthOfPeristalticPeriod(peristalticPeriod));

        // 3 max + max + ... / count
        int limit100PercentValue = Short.MAX_VALUE / 2;
        processorResult.setAverageMaxAmplitudeOfPeristalticWaves(
                getAverageMaxAmplitudesOfPeriods(peristalticPeriodsData) / limit100PercentValue);

        // 4
        processorResult.setAverageAmplitudeOfPeristalticWaves(
                getAverageAmplitudeOfPeristalticWaves(peristalticPeriodsData) / limit100PercentValue);

        Pair<Integer, Double> maxAndAvrgMaxNonPeristalticPeriod = getMaxAndAvrgMaxAmplitude(nonPeristalticPeriod);

        // 5
        processorResult.setMaxAmplitudeContractionsDuringNonPeristalticPeriod(maxAndAvrgMaxNonPeristalticPeriod.first / limit100PercentValue);

        // 6 ??????
        processorResult.setAverageAmplitudeContractionsDuringNonPeristalticPeriod(maxAndAvrgMaxNonPeristalticPeriod.second / limit100PercentValue);
        Pair<Double, Double> durationToMaxAndFromMax = calculateDurationToMaxAndFromMax(peristalticPeriod);

        // 7
        Pair<Double, Double> averageAmplitudeRiseAndReduceTime = getAverageAmplitudeRiseAndReduceTime(peristalticPeriodsData);
        processorResult.setAverageAmplitudeRiseTime(averageAmplitudeRiseAndReduceTime.first / limit100PercentValue);

        // 8
        processorResult.setAverageTimeReducingAmplitude(averageAmplitudeRiseAndReduceTime.second / limit100PercentValue);

        // 9
        processorResult.setIndexOfPeristalticWave(
                processorResult.getAverageMaxAmplitudeOfPeristalticWaves() / processorResult.getAverageLengthOfPeristalticPeriod());

        return processorResult;
    }

    private double getInSecondsDurationBySamples(long sampleCount) {
        return (double) sampleCount / sampleRate;
    }

    /**
     * Get in seconds duration of peristaltic period
     * @param periods
     * @return
     */
    private double getAverageLengthOfPeristalticPeriod(List<Pair<Integer, Integer>> periods) {
        int sampleCount = 0;

        for (Pair<Integer, Integer> period: periods) {
            // calculate duration of each wave
            sampleCount += period.second - period.first;
        }
        return getInSecondsDurationBySamples(sampleCount) / (double) periods.size();
    }

    private Double getAverageMaxAmplitudesOfPeriods(List<List<Short>> periodsAmplitudes) {
        long sumOfMaxAmplitudes = 0;
        double countPeriods = periodsAmplitudes.size();

        for (List<Short> periodAmplitudes : periodsAmplitudes) {
            Pair<Integer, Integer> maxValueAndIndex = getMaxValueAndIndex(periodAmplitudes);
            sumOfMaxAmplitudes += maxValueAndIndex.first;
        }

        return sumOfMaxAmplitudes / countPeriods;
    }

    /**
     * Return pair of value that represents
     * <p/>
     * <b>First:</b> average amplitudes Rise time.
     * <b>Second:</b> average amplitudes Reduce time.
     *
     * @param periods list of Period's data
     * @return average amplitudes Rise and Reduce time
     */
    private Pair<Double, Double> getAverageAmplitudeRiseAndReduceTime(List<List<Short>> periods) {
        long sumOfRiseSamples = 0;
        long sumOfReduceSamples = 0;

        Pair<Integer, Integer> maxValueAndIndex;
        for (List<Short> periodData : periods) {
            maxValueAndIndex = getMaxValueAndIndex(periodData);

            sumOfRiseSamples += maxValueAndIndex.second;
            sumOfReduceSamples += periodData.size() - sumOfRiseSamples;
        }

        double averageRiseTime = getInSecondsDurationBySamples(sumOfRiseSamples / periods.size());
        double averageReduceTime = getInSecondsDurationBySamples(sumOfReduceSamples / periods.size());

        return Pair.create(averageRiseTime, averageReduceTime);

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

    public Double getAverageAmplitudeOfPeristalticWaves(List<List<Short>> peristalticPeriodsData) {
        // максимальні піки амплітуд всіх перестальтичних хвиль
        List<List<Short>> limitValuesOfAmplitudes = new ArrayList<>();

        for (List<Short> peristalticPeriod: peristalticPeriodsData) {
            List<Short> limitValuesPerSinglePeriod = new ArrayList<>();

            boolean isGrowsUp = false;
            // suppose first value is limit value
            short limitValue = peristalticPeriod.get(0);
            for (int i = 1; i < peristalticPeriod.size(); i++) {
                // if values grows up
                if (peristalticPeriod.get(i) >= limitValue) {
                    isGrowsUp = true;
                    limitValue = peristalticPeriod.get(i);
                } else {
                    // when direction of wave changes, save limit value.
                    if (isGrowsUp) {
                        limitValuesPerSinglePeriod.add(limitValue);
                        isGrowsUp = false;
                    }
                }
            }
            limitValuesOfAmplitudes.add(limitValuesPerSinglePeriod);
        }

        List<Double> meanSquares = new ArrayList<>();
        for(List<Short> limitValuesOfSinglePeriod: limitValuesOfAmplitudes) {
            double meanSquareForSinglePeriod = calculateMeanSquare(limitValuesOfSinglePeriod);
            meanSquares.add(meanSquareForSinglePeriod);
        }

        Double sumOfMeanSquares = .0;
        for (Double meanSquare: meanSquares) {
            sumOfMeanSquares += meanSquare;
        }

        // mean arithmetic of all (mean square values)
        return sumOfMeanSquares / meanSquares.size();

    }

    private double calculateMeanSquare(List<Short> maxAmplitudes) {
        long sum = 0;
        long sumOfSquares = 0;

        // count of limit values
        int n = maxAmplitudes.size();

        for (Short limitValue : maxAmplitudes) {
            sum = +limitValue;
            sumOfSquares += limitValue * limitValue;
        }
        double arithmeticMean = sum / (double) n;

        return sumOfSquares - Math.pow(arithmeticMean, 2) * n;
    }
}
