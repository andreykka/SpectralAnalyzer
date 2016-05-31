package medicine.com.spectralanalyzer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import medicine.com.spectralanalyzer.pojo.ProcessorResult;

import java.util.ArrayList;
import java.util.List;

public class ChartActivity extends FragmentActivity {

    private RadarChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.chart_layout);

        mChart = (RadarChart) findViewById(R.id.chart1);

        mChart.setWebLineWidth(1.5f);
        mChart.setWebLineWidthInner(0.75f);
        mChart.setWebAlpha(100);
        Intent requestedIntent = getIntent();

        if (requestedIntent != null) {
            ProcessorResult processorResult1 = (ProcessorResult) requestedIntent.getSerializableExtra("processorResult1");
            ProcessorResult processorResult2 = (ProcessorResult) requestedIntent.getSerializableExtra("processorResult2");
            setData(processorResult1, processorResult2);
        }

    }

    public void setData(ProcessorResult... datas) {
        int i = 0;

        List<IRadarDataSet> radarDataSets = new ArrayList<>();

        for (ProcessorResult data : datas) {
            ArrayList<Entry> yVals = new ArrayList<>();

            yVals.add(new Entry((float) data.getCountWaves(), 0));
            yVals.add(new Entry((float) data.getAvrgAmplitudeDecreasingTime(), 1));
            yVals.add(new Entry((float) data.getAvrgAmplitudeIncreasingTime(), 2));
            yVals.add(new Entry((float) data.getAvrgReductionAmplitudeInNotPeristalticPeriod(), 3));
            yVals.add(new Entry((float) data.getAvrgAmplitudePeristalticWaves(), 4));
            yVals.add(new Entry((float) data.getAvrgWaveDuration(), 5));
            yVals.add(new Entry((float) data.getMaxReductionAmplitudeInNotPeristalticPeriod(), 6));
            yVals.add(new Entry((float) data.getAvrgMaxAmplitudePeristalticWaves(), 7));
            yVals.add(new Entry((float) data.getAvrgIndexOfPeristalticWave(), 8));

            RadarDataSet dataSet = new RadarDataSet(yVals, "Set " + ++i);
            dataSet.setColor(ColorTemplate.VORDIPLOM_COLORS[i]);
            dataSet.setFillColor(ColorTemplate.COLOR_NONE);
            radarDataSets.add(dataSet);
        }

        RadarData radarData = new RadarData(xVal, radarDataSets);
        radarData.setValueTextSize(8f);
        radarData.setDrawValues(false);
        mChart.setData(radarData);
    }


    private List<String> xVal = new ArrayList<String>() {{
        add("1");
        add("2");
        add("3");
        add("4");
        add("5");
        add("6");
        add("7");
        add("8");
        add("9");
    }};

}
