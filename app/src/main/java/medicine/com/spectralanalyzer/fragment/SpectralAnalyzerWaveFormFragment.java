package medicine.com.spectralanalyzer.fragment;

import android.graphics.Color;
import android.util.Pair;

import com.semantive.waveformandroid.waveform.Segment;
import com.semantive.waveformandroid.waveform.WaveformFragment;

import java.util.ArrayList;
import java.util.List;

public class SpectralAnalyzerWaveFormFragment extends WaveformFragment {

    private String fileName = "notFound";
    private List<Pair<Double, Double>> periods = new ArrayList<>();

    public SpectralAnalyzerWaveFormFragment() {
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setPeriods(List<Pair<Double, Double>> periods) {
        this.periods = periods;
    }

    @Override
    protected String getFileName() {
        return fileName;
    }

    @Override
    protected List<Segment> getSegments() {
        List<Segment> segmentList = new ArrayList<>(periods.size());

        for (Pair<Double, Double> period : periods) {
            Segment segment = new Segment(period.first, period.second, Color.rgb(238, 23, 104));
            segmentList.add(segment);
        }

        return segmentList;
    }
}
