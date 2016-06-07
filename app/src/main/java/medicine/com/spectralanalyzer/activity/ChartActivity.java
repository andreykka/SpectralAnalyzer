package medicine.com.spectralanalyzer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import medicine.com.spectralanalyzer.R;
import medicine.com.spectralanalyzer.pojo.ActivityConstants;
import medicine.com.spectralanalyzer.pojo.ProcessorResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static medicine.com.spectralanalyzer.pojo.ActivityConstants.PROCESS_RESULT_PARAM;

public class ChartActivity extends FragmentActivity {

    private RadarChart mChart;
    private ProcessorResult processorResult;

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

        Intent intent = getIntent();
        if (intent != null) {
            processorResult = (ProcessorResult) intent.getSerializableExtra(PROCESS_RESULT_PARAM);
            setData(getAxises().toArray(new ProcessorResult[getAxises().size()]));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chart_layout_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.showTranscription: {
                Intent intent = new Intent(this, TranscriptionActivity.class);
                intent.putExtra(PROCESS_RESULT_PARAM, processorResult);
                startActivity(intent);
            }
        }
        return true;
    }


    public void setData(ProcessorResult... datas) {
        int i = 0;

        List<IRadarDataSet> radarDataSets = new ArrayList<>();

        for (ProcessorResult data : datas) {
            ArrayList<Entry> yVals = new ArrayList<>();

            yVals.add(new Entry((float) data.getCountWaves(), 0));
            yVals.add(new Entry((float) data.getAverageTimeReducingAmplitude(), 1));
            yVals.add(new Entry((float) data.getAverageAmplitudeRiseTime(), 2));
            yVals.add(new Entry((float) data.getAverageAmplitudeContractionsDuringNonPeristalticPeriod(), 3));
            yVals.add(new Entry((float) data.getAverageAmplitudeOfPeristalticWaves(), 4));
            yVals.add(new Entry((float) data.getAverageLengthOfPeristalticPeriod(), 5));
            yVals.add(new Entry((float) data.getMaxAmplitudeContractionsDuringNonPeristalticPeriod(), 6));
            yVals.add(new Entry((float) data.getAverageMaxAmplitudeOfPeristalticWaves(), 7));
            yVals.add(new Entry((float) data.getIndexOfPeristalticWave(), 8));

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


    public List<ProcessorResult> getAxises() {
        List<ProcessorResult> axises = new ArrayList<>();

        ProcessorResult processorResult1 = new ProcessorResult();
        processorResult1.setAverageLengthOfPeristalticPeriod(5);
        processorResult1.setAverageMaxAmplitudeOfPeristalticWaves(50);
        processorResult1.setAverageAmplitudeOfPeristalticWaves(50);
        processorResult1.setAverageTimeReducingAmplitude(12.0);
        processorResult1.setAverageAmplitudeRiseTime(25.2);
        processorResult1.setAverageAmplitudeContractionsDuringNonPeristalticPeriod(15);
        processorResult1.setMaxAmplitudeContractionsDuringNonPeristalticPeriod(50.27);
        processorResult1.setIndexOfPeristalticWave(1.9);
        processorResult1.setCountWaves(25);

        axises.add(processorResult1);

        ProcessorResult processorResult2 = new ProcessorResult();
        processorResult2.setAverageLengthOfPeristalticPeriod(10);
        processorResult2.setAverageMaxAmplitudeOfPeristalticWaves(80);
        processorResult2.setAverageAmplitudeOfPeristalticWaves(70);
        processorResult2.setAverageTimeReducingAmplitude(20);
        processorResult2.setAverageAmplitudeRiseTime(35.3);
        processorResult2.setAverageAmplitudeContractionsDuringNonPeristalticPeriod(20);
        processorResult2.setMaxAmplitudeContractionsDuringNonPeristalticPeriod(59.2);
        processorResult2.setIndexOfPeristalticWave(3.98);
        processorResult2.setCountWaves(35);

        axises.add(processorResult2);

        return axises;
    }

}
