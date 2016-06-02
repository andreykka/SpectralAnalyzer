package medicine.com.spectralanalyzer.pojo;


import java.io.Serializable;

public class ProcessorResult implements Serializable {


    private int countWaves;

    /**
     * Average peristaltic period time duration. In seconds.
     */
    private double averageLengthOfPeristalticPeriod;

    /**
     * Average value of max amplitudes peristaltic waves. In %.
     * The mean square of all max limits of peristaltic period
     */
    private double averageMaxAmplitudeOfPeristalticWaves;

    /**
     * Average amplitude of peristaltic waves. In %.
     */
    private double averageAmplitudeOfPeristalticWaves;

    /**
     * Max amplitude of contractions during non peristaltic period. In %.
     * <br> Max limit value of amplitudes of non peristaltic period
     */
    private double maxAmplitudeContractionsDuringNonPeristalticPeriod;

    /**
     * Average amplitude of not peristaltic waves. In %.
     */
    private double averageAmplitudeContractionsDuringNonPeristalticPeriod;

    /**
     * Average time of increasing amplitudes. In seconds.
     */
    private double averageAmplitudeRiseTime;

    /**
     * Average time of decreasing amplitudes. In seconds.
     */
    private double averageTimeReducingAmplitude;

    private double indexOfPeristalticWave;


    public ProcessorResult() {
    }

    public int getCountWaves() {
        return countWaves;
    }

    public void setCountWaves(int countWaves) {
        this.countWaves = countWaves;
    }

    public double getAverageLengthOfPeristalticPeriod() {
        return averageLengthOfPeristalticPeriod;
    }

    public void setAverageLengthOfPeristalticPeriod(double averageLengthOfPeristalticPeriod) {
        this.averageLengthOfPeristalticPeriod = averageLengthOfPeristalticPeriod;
    }

    public double getAverageMaxAmplitudeOfPeristalticWaves() {
        return averageMaxAmplitudeOfPeristalticWaves;
    }

    public void setAverageMaxAmplitudeOfPeristalticWaves(double averageMaxAmplitudeOfPeristalticWaves) {
        this.averageMaxAmplitudeOfPeristalticWaves = averageMaxAmplitudeOfPeristalticWaves;
    }

    public double getAverageAmplitudeOfPeristalticWaves() {
        return averageAmplitudeOfPeristalticWaves;
    }

    public void setAverageAmplitudeOfPeristalticWaves(double averageAmplitudeOfPeristalticWaves) {
        this.averageAmplitudeOfPeristalticWaves = averageAmplitudeOfPeristalticWaves;
    }

    public double getMaxAmplitudeContractionsDuringNonPeristalticPeriod() {
        return maxAmplitudeContractionsDuringNonPeristalticPeriod;
    }

    public void setMaxAmplitudeContractionsDuringNonPeristalticPeriod(double maxAmplitudeContractionsDuringNonPeristalticPeriod) {
        this.maxAmplitudeContractionsDuringNonPeristalticPeriod = maxAmplitudeContractionsDuringNonPeristalticPeriod;
    }

    public double getAverageAmplitudeContractionsDuringNonPeristalticPeriod() {
        return averageAmplitudeContractionsDuringNonPeristalticPeriod;
    }

    public void setAverageAmplitudeContractionsDuringNonPeristalticPeriod(double averageAmplitudeContractionsDuringNonPeristalticPeriod) {
        this.averageAmplitudeContractionsDuringNonPeristalticPeriod = averageAmplitudeContractionsDuringNonPeristalticPeriod;
    }

    public double getAverageAmplitudeRiseTime() {
        return averageAmplitudeRiseTime;
    }

    public void setAverageAmplitudeRiseTime(double averageAmplitudeRiseTime) {
        this.averageAmplitudeRiseTime = averageAmplitudeRiseTime;
    }

    public double getAverageTimeReducingAmplitude() {
        return averageTimeReducingAmplitude;
    }

    public void setAverageTimeReducingAmplitude(double averageTimeReducingAmplitude) {
        this.averageTimeReducingAmplitude = averageTimeReducingAmplitude;
    }

    public double getIndexOfPeristalticWave() {
        return indexOfPeristalticWave;
    }

    public void setIndexOfPeristalticWave(double indexOfPeristalticWave) {
        this.indexOfPeristalticWave = indexOfPeristalticWave;
    }
}
