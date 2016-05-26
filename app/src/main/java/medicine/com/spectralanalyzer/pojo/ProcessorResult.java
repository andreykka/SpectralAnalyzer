package medicine.com.spectralanalyzer.pojo;


public class ProcessorResult {


    private int countWaves;

    /**
     * Average peristaltic wave duration. In seconds.
     */
    private double avrgWaveDuration;

    /**
     * Average value of max amplitudes peristaltic waves. In %.
     */
    private double avrgMaxAmplitudePeristalticWaves;

    /**
     * Average amplitude of peristaltic waves. In %.
     */
    private double avrgAmplitudePeristalticWaves;

    /**
     * Max amplitude of reduction during between peristaltic period. In %.
     * <br> Max amplitude value.
     */
    private double reductionAmplitudeInPeristalticPeriod;

    /**
     * Average amplitude of not peristaltic waves. In %.
     */
    private double avrgAmplitudeOrReductionInNotPeristalticWaves;

    /**
     * Average time of increasing amplitudes. In seconds.
     */
    private double avrgAmplitudeIncreasingTime;

    /**
     * Average time of decreasing amplitudes. In seconds.
     */
    private double avrgAmplitudeDecreasingTime;


    private double avrgIndexOfPeristalticWave;

    public ProcessorResult() {
    }

    public int getCountWaves() {
        return countWaves;
    }

    public void setCountWaves(int countWaves) {
        this.countWaves = countWaves;
    }

    public double getAvrgWaveDuration() {
        return avrgWaveDuration;
    }

    public void setAvrgWaveDuration(double avrgWaveDuration) {
        this.avrgWaveDuration = avrgWaveDuration;
    }

    public double getAvrgMaxAmplitudePeristalticWaves() {
        return avrgMaxAmplitudePeristalticWaves;
    }

    public void setAvrgMaxAmplitudePeristalticWaves(double avrgMaxAmplitudePeristalticWaves) {
        this.avrgMaxAmplitudePeristalticWaves = avrgMaxAmplitudePeristalticWaves;
    }

    public double getAvrgAmplitudePeristalticWaves() {
        return avrgAmplitudePeristalticWaves;
    }

    public void setAvrgAmplitudePeristalticWaves(double avrgAmplitudePeristalticWaves) {
        this.avrgAmplitudePeristalticWaves = avrgAmplitudePeristalticWaves;
    }

    public double getReductionAmplitudeInPeristalticPeriod() {
        return reductionAmplitudeInPeristalticPeriod;
    }

    public void setReductionAmplitudeInPeristalticPeriod(double reductionAmplitudeInPeristalticPeriod) {
        this.reductionAmplitudeInPeristalticPeriod = reductionAmplitudeInPeristalticPeriod;
    }

    public double getAvrgAmplitudeOrReductionInNotPeristalticWaves() {
        return avrgAmplitudeOrReductionInNotPeristalticWaves;
    }

    public void setAvrgAmplitudeOrReductionInNotPeristalticWaves(double avrgAmplitudeOrReductionInNotPeristalticWaves) {
        this.avrgAmplitudeOrReductionInNotPeristalticWaves = avrgAmplitudeOrReductionInNotPeristalticWaves;
    }

    public double getAvrgAmplitudeIncreasingTime() {
        return avrgAmplitudeIncreasingTime;
    }

    public void setAvrgAmplitudeIncreasingTime(double avrgAmplitudeIncreasingTime) {
        this.avrgAmplitudeIncreasingTime = avrgAmplitudeIncreasingTime;
    }

    public double getAvrgAmplitudeDecreasingTime() {
        return avrgAmplitudeDecreasingTime;
    }

    public void setAvrgAmplitudeDecreasingTime(double avrgAmplitudeDecreasingTime) {
        this.avrgAmplitudeDecreasingTime = avrgAmplitudeDecreasingTime;
    }

    public double getAvrgIndexOfPeristalticWave() {
        return avrgIndexOfPeristalticWave;
    }

    public void setAvrgIndexOfPeristalticWave(double avrgIndexOfPeristalticWave) {
        this.avrgIndexOfPeristalticWave = avrgIndexOfPeristalticWave;
    }
}
