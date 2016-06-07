package medicine.com.spectralanalyzer;

import android.util.Log;
import android.util.Pair;

import com.musicg.wave.Wave;
import medicine.com.spectralanalyzer.pojo.ProcessorResult;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AudioProcessor {

    private static final int PERCENT_100 = 100;

    /**
     * Logger for AudioProcessor class
     */
    private static final String TAG = AudioProcessor.class.getSimpleName();

    /**
     * In seconds value, indicate minimum allowed length of sound
     */

    private static float MIN_SOUND_DURATION = 0.5f;

    /**
     * In seconds value, indicate minimum allowed length of sound
     */
    private static int MAX_SILENCE_LENGTH = 4;

    /**
     * Minimal value of silence.
     */
    private static int NOISE = 2;


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
        singleChannelData = getSingleChannelData(wave);
        sampleRate = wave.getWaveHeader().getSampleRate();
        period = sampleRate / 1000; // 44100/1000 = 1 ms
    }

    public static void setUpConfiguration(float minSoundDuration, int maxSilenceLength, int noiseValue) {
        MIN_SOUND_DURATION = minSoundDuration;
        MAX_SILENCE_LENGTH = maxSilenceLength;
        NOISE = noiseValue;
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

    /**
     * Finds the place in audio wave that contains sound.
     *
     * @return List of pairs, first element indicate start sample,
     * <br>second element indicate end sample.
     */
    public List<Pair<Integer, Integer>> getPeristalticPeriods() {
        showCurrentConfiguration();

        long before = System.currentTimeMillis();
        int start, end;
        List<Pair<Integer, Integer>> periods = new ArrayList<>();

        long readData = 0;

        int silenceCounter = 0;

        for (int i = 0; i < singleChannelData.length; i += period) {
            if (singleChannelData[i] > NOISE || singleChannelData[i] < -NOISE) {
                readData++;
                silenceCounter = 0;
            } else {
                if (++silenceCounter <= MAX_SILENCE_LENGTH) {
                    readData++;
                    continue;
                }
                silenceCounter = 0;
                // current i is silence
                if (isLengthEnough(readData * period)) {
                    end = i - 1;
                    start = (int) (end - (readData * period));
                    start = start < 0 ? 0 : start;
                    Pair<Integer, Integer> range = Pair.create(start, end);
                    periods.add(range);
                }
                readData = 0;
            }
        }
        long after = System.currentTimeMillis();
        Log.d(TAG, "Spent time: " + (after - before) + " ms");
        Log.d(TAG, "Found periods: " + periods.size());
        return periods;
    }


    /**
     * Verify is length of audio segment bigger than specified in {@link #MIN_SOUND_DURATION}
     * <br>0.5 sec by default.
     *
     * @return <b>true</b> if length > 0.5sec, <b>false</b> otherwise.
     */
    private boolean isLengthEnough(Long sampleCount) {
        double targetSampleCountForTime = sampleRate * MIN_SOUND_DURATION;
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

    public List<List<Short>> getDataForPeristalticPeriods(List<Pair<Integer, Integer>> periods) {
        List<List<Short>> periodsData = new ArrayList<>(periods.size());

        List<Short> wavesForPeriod;
        for (Pair<Integer, Integer> period : periods) {
            wavesForPeriod = getDataForPeriod(period.first, period.second);
            periodsData.add(wavesForPeriod);
        }
        return periodsData;
    }

    public List<Short> getDataForNonPeristalticPeriods(List<Pair<Integer, Integer>> periods) {
        List<Short> nonPeristalticPeriod = new ArrayList<>();

        List<Short> wavesForPeriod;
        for (Pair<Integer, Integer> period : periods) {
            wavesForPeriod = getDataForPeriod(period.first, period.second);
            nonPeristalticPeriod.addAll(wavesForPeriod);
        }
        return nonPeristalticPeriod;
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
        List<Pair<Integer, Integer>> nonPeristalticPeriod = getNonPeristalticPeriod(peristalticPeriod);
        if (peristalticPeriod == null ||  peristalticPeriod.size() <= 0) {

            return processorResult;
        }

        List<List<Short>> peristalticPeriodsData = getDataForPeristalticPeriods(peristalticPeriod);
        List<Short> nonPeristalticPeriodData = getDataForNonPeristalticPeriods(nonPeristalticPeriod);

        // 1
        processorResult.setCountWaves(peristalticPeriod.size());

        // 2
        processorResult.setAverageLengthOfPeristalticPeriod(getAverageLengthOfPeristalticPeriod(peristalticPeriod));

        // 3 max + max + ... / count
        int limit100PercentValue = Short.MAX_VALUE;
        processorResult.setAverageMaxAmplitudeOfPeristalticWaves(
                getAverageMaxAmplitudesOfPeriods(peristalticPeriodsData) / limit100PercentValue * PERCENT_100);

        // 4
        processorResult.setAverageAmplitudeOfPeristalticWaves(
                getAverageAmplitudeOfPeristalticWaves(peristalticPeriodsData) / limit100PercentValue * PERCENT_100);

        // 5
        // IMPORTANT COULD BE REPLACED INTO SINGLE CALCULATION WITH
        // 6. AverageAmplitudeContractionsDuringNonPeristalticPeriod
        processorResult.setMaxAmplitudeContractionsDuringNonPeristalticPeriod(
                getMaxAmplitudeOfNonPeristalticWaves(nonPeristalticPeriodData) / limit100PercentValue * PERCENT_100);

        // 6 mean square of non peristaltic period
        processorResult.setAverageAmplitudeContractionsDuringNonPeristalticPeriod(
                getAverageAmplitudeOfNonPeristalticWaves(nonPeristalticPeriodData) / limit100PercentValue * PERCENT_100);

        // 7
        Pair<Double, Double> averageAmplitudeRiseAndReduceTime = getAverageAmplitudeRiseAndReduceTime(peristalticPeriodsData);
        processorResult.setAverageAmplitudeRiseTime(averageAmplitudeRiseAndReduceTime.first / limit100PercentValue * PERCENT_100);

        // 8
        processorResult.setAverageTimeReducingAmplitude(averageAmplitudeRiseAndReduceTime.second / limit100PercentValue * PERCENT_100);

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
     *
     * @param peristalticPeriods peristaltic periods.
     * @return average length of all peristaltic waves.
     */
    private double getAverageLengthOfPeristalticPeriod(List<Pair<Integer, Integer>> peristalticPeriods) {
        int sampleCount = 0;

        for (Pair<Integer, Integer> period : peristalticPeriods) {
            // calculate duration of each wave
            sampleCount += period.second - period.first;
        }
        return getInSecondsDurationBySamples(sampleCount) / (double) peristalticPeriods.size();
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
     * Find the non peristaltic period based on information about peristaltic period location.
     *
     * @param peristalticPeriod peristaltic period.
     * @return non peristaltic periods.
     */
    public List<Pair<Integer, Integer>> getNonPeristalticPeriod(List<Pair<Integer, Integer>> peristalticPeriod) {
        List<Pair<Integer, Integer>> nonPeristalticPeriods = new ArrayList<>();

        int start = 0;
        int end;

        for (int i = 0; i < peristalticPeriod.size(); i++) {
            if (peristalticPeriod.get(i).first == 0) {
                start = peristalticPeriod.get(i).second;
                continue;
            }

            end = peristalticPeriod.get(i).first;
            nonPeristalticPeriods.add(Pair.create(start, end));

            start = peristalticPeriod.get(i).second;
            if (i == peristalticPeriod.size() - 1 && start < singleChannelData.length - 1) {
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
            limitValuesOfAmplitudes.add(getLimitValuesPerSinglePeriod(peristalticPeriod));
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

    /**
     * Get Mean Square of Non peristaltic period.
     *
     * @param nonPeristalticPeriodData non Peristaltic period data
     * @return mean square of peristaltic period
     */
    public Double getAverageAmplitudeOfNonPeristalticWaves(List<Short> nonPeristalticPeriodData) {
        List<Short> limitValuesNonPeristalticPeriod = getLimitValuesPerSinglePeriod(nonPeristalticPeriodData);
        return calculateMeanSquare(limitValuesNonPeristalticPeriod);
    }

    /**
     * Get Max Amplitude of non Peristaltic period.
     *
     * @param nonPeristalticPeriodData non Peristaltic period data
     * @return mean square of non peristaltic waves
     */
    public Double getMaxAmplitudeOfNonPeristalticWaves(List<Short> nonPeristalticPeriodData) {
        List<Short> limitValuesNonPeristalticPeriod = getLimitValuesPerSinglePeriod(nonPeristalticPeriodData);
        short max = limitValuesNonPeristalticPeriod.get(0);

        for (short value : limitValuesNonPeristalticPeriod) {
            if (value >= max) {
                max = value;
            }
        }
        return (double) max;
    }

    private List<Short> getLimitValuesPerSinglePeriod(List<Short> period) {
        List<Short> limitValuesPerSinglePeriod = new ArrayList<>();

        boolean isGrowsUp = false;
        // suppose first value is limit value
        short limitValue = 0;
        for (int i = 1; i < period.size(); i++) {
            // if values grows up
            if (period.get(i) >= limitValue) {
                isGrowsUp = true;
                limitValue = period.get(i);
            } else {
                // when direction of wave changes, save limit value.
                if (isGrowsUp) {
                    limitValuesPerSinglePeriod.add(limitValue);
                    isGrowsUp = false;
                }
            }
        }
        return limitValuesPerSinglePeriod;
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

    private void showCurrentConfiguration() {
        Log.i(TAG, "MAX_SILENCE_LENGTH= " + MAX_SILENCE_LENGTH);
        Log.i(TAG, "MIN_SOUND_DURATION= " + MIN_SOUND_DURATION);
        Log.i(TAG, "NOISE= " + NOISE);
        Log.i(TAG, "sampleRate= " + sampleRate);
        Log.i(TAG, "period= " + period);
    }

}
