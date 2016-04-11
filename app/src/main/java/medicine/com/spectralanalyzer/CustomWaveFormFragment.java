package medicine.com.spectralanalyzer;

import android.graphics.Color;
import android.util.Pair;

import com.semantive.waveformandroid.waveform.Segment;
import com.semantive.waveformandroid.waveform.WaveformFragment;

import java.util.ArrayList;
import java.util.List;

public class CustomWaveFormFragment extends WaveformFragment {

    private String fileName = "notFound";
    private List<Pair<Integer, Integer>> periods = new ArrayList<>();

    public CustomWaveFormFragment() {
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setPeriods(List<Pair<Integer, Integer>> periods) {
        this.periods = periods;
    }

    @Override
    protected String getFileName() {
        return fileName;
    }

    @Override
    protected List<Segment> getSegments() {
        List<Segment> segmentList = new ArrayList<>(periods.size());

        for (Pair<Integer, Integer> period: periods) {
            double start = period.first / 44100;
            double end = (period.first + period.second) / 44100;
            Segment segment = new Segment(start, end, Color.rgb(238, 23, 104));
            segmentList.add(segment);
        }

        return segmentList;
    }
}
