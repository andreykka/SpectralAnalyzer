package medicine.com.spectralanalyzer.activity;

import android.content.Intent;
import android.graphics.Color;
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
import medicine.com.spectralanalyzer.pojo.ProcessorResult;

import java.util.ArrayList;
import java.util.List;

import static medicine.com.spectralanalyzer.pojo.ActivityConstants.PROCESS_RESULT_PARAM;

public class ChartActivity extends FragmentActivity {

    private RadarChart radarChart;
    private ProcessorResult processorResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.chart_layout);

        radarChart = (RadarChart) findViewById(R.id.chart1);

        radarChart.setWebLineWidth(1.75f);
        radarChart.setWebLineWidthInner(1.75f);
        radarChart.setWebAlpha(100);
        drawChartAxises(getAxises());

        Intent intent = getIntent();
        if (intent != null) {
            processorResult = (ProcessorResult) intent.getSerializableExtra(PROCESS_RESULT_PARAM);
            if (processorResult != null) {
                drawChart(processorResult);
            }
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
                Intent intent = new Intent(this, ChartDetailActivity.class);
                intent.putExtra(PROCESS_RESULT_PARAM, processorResult);
                startActivity(intent);
            }
        }
        return true;
    }

    public void drawChart(ProcessorResult data) {
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry((float) data.getCountWaves(), 0));
        entries.add(new Entry((float) data.getAverageLengthOfPeristalticPeriod(), 5));
        entries.add(new Entry((float) data.getAverageMaxAmplitudeOfPeristalticWaves(), 7));
        entries.add(new Entry((float) data.getAverageAmplitudeOfPeristalticWaves(), 4));
        entries.add(new Entry((float) data.getMaxAmplitudeContractionsDuringNonPeristalticPeriod(), 6));
        entries.add(new Entry((float) data.getAverageAmplitudeContractionsDuringNonPeristalticPeriod(), 3));
        entries.add(new Entry((float) data.getAverageAmplitudeRiseTime(), 2));
        entries.add(new Entry((float) data.getAverageTimeReducingAmplitude(), 1));
        entries.add(new Entry((float) data.getIndexOfPeristalticWave(), 8));

        RadarDataSet dataSet = new RadarDataSet(entries, "Processor Result" );
        dataSet.setColor(Color.rgb(3, 160, 22));
        dataSet.setFillColor(ColorTemplate.COLOR_NONE);
        dataSet.setLineWidth(2f);
        radarChart.getData().getDataSets().add(dataSet);
    }

    public void drawChartAxises(List<ProcessorResult> axises) {
        int i = 0, j=0;

        List<IRadarDataSet> radarDataSets = new ArrayList<>();

        for (ProcessorResult data : axises) {
            ArrayList<Entry> yVals = new ArrayList<>();

            yVals.add(new Entry((float) data.getCountWaves(), 0));
            yVals.add(new Entry((float) data.getAverageLengthOfPeristalticPeriod(), 5));
            yVals.add(new Entry((float) data.getAverageMaxAmplitudeOfPeristalticWaves(), 7));
            yVals.add(new Entry((float) data.getAverageAmplitudeOfPeristalticWaves(), 4));
            yVals.add(new Entry((float) data.getMaxAmplitudeContractionsDuringNonPeristalticPeriod(), 6));
            yVals.add(new Entry((float) data.getAverageAmplitudeContractionsDuringNonPeristalticPeriod(), 3));
            yVals.add(new Entry((float) data.getAverageAmplitudeRiseTime(), 2));
            yVals.add(new Entry((float) data.getAverageTimeReducingAmplitude(), 1));
            yVals.add(new Entry((float) data.getIndexOfPeristalticWave(), 8));

            RadarDataSet dataSet = new RadarDataSet(yVals, j++ == 0 ? "Min Axis" : "Max Axis" );
            dataSet.setColor(Color.rgb(215, 4, 15));
            dataSet.setFillColor(ColorTemplate.COLOR_NONE);
            dataSet.setLineWidth(2.75f);
            radarDataSets.add(dataSet);
        }

        RadarData radarData = new RadarData(xVal, radarDataSets);
        radarData.setValueTextSize(8f);
        radarData.setDrawValues(false);
        radarChart.setData(radarData);
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
        processorResult1.setAverageMaxAmplitudeOfPeristalticWaves(30);
        processorResult1.setAverageAmplitudeOfPeristalticWaves(40);
        processorResult1.setAverageTimeReducingAmplitude(10);
        processorResult1.setAverageAmplitudeRiseTime(25);
        processorResult1.setAverageAmplitudeContractionsDuringNonPeristalticPeriod(18);
        processorResult1.setMaxAmplitudeContractionsDuringNonPeristalticPeriod(31);
        processorResult1.setIndexOfPeristalticWave(2);
        processorResult1.setCountWaves(25);

        axises.add(processorResult1);

        ProcessorResult processorResult2 = new ProcessorResult();
        processorResult2.setAverageLengthOfPeristalticPeriod(30);
        processorResult2.setAverageMaxAmplitudeOfPeristalticWaves(80);
        processorResult2.setAverageAmplitudeOfPeristalticWaves(70);
        processorResult2.setAverageTimeReducingAmplitude(45);
        processorResult2.setAverageAmplitudeRiseTime(48);
        processorResult2.setAverageAmplitudeContractionsDuringNonPeristalticPeriod(68);
        processorResult2.setMaxAmplitudeContractionsDuringNonPeristalticPeriod(59.2);
        processorResult2.setIndexOfPeristalticWave(9);
        processorResult2.setCountWaves(55);

        axises.add(processorResult2);

        return axises;
    }

/*
    private ProcessorResult getProcessorResult() {
        ProcessorResult processorResult1 = new ProcessorResult();
        processorResult1.setAverageLengthOfPeristalticPeriod(20);
        processorResult1.setAverageMaxAmplitudeOfPeristalticWaves(50);
        processorResult1.setAverageAmplitudeOfPeristalticWaves(50);
        processorResult1.setAverageTimeReducingAmplitude(10);
        processorResult1.setAverageAmplitudeRiseTime(40);
        processorResult1.setAverageAmplitudeContractionsDuringNonPeristalticPeriod(30);
        processorResult1.setMaxAmplitudeContractionsDuringNonPeristalticPeriod(50);
        processorResult1.setIndexOfPeristalticWave(10);
        processorResult1.setCountWaves(30);
        return processorResult1;

    }
*/

}
