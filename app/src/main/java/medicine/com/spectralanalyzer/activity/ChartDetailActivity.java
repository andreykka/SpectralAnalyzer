package medicine.com.spectralanalyzer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import medicine.com.spectralanalyzer.R;
import medicine.com.spectralanalyzer.pojo.ProcessorResult;

import static medicine.com.spectralanalyzer.pojo.ActivityConstants.PROCESS_RESULT_PARAM;

public class ChartDetailActivity extends Activity {

    private static final String COUNT_WAVES_NAME = "Count Waves:";

    private static final String AVERAGE_LENGTH_OF_PERISTALTIC_PERIOD_NAME =
            "Average Length Of Peristaltic Period:";

    private static final String AVERAGE_MAX_AMPLITUDE_OF_PERISTALTIC_WAVES_NAME =
            "Average Max Amplitude Of Peristaltic Waves";

    private static final String AVERAGE_AMPLITUDE_OF_PERISTALTIC_WAVES_NAME =
            "Average Amplitude Of Peristaltic Waves";

    private static final String MAX_AMPLITUDE_CONTRACTIONS_DURING_NON_PERISTALTIC_PERIOD_NAME =
            "Max Amplitude Of Contractions During Non Peristaltic Period";

    private static final String AVERAGE_AMPLITUDE_CONTRACTIONS_DURING_NON_PERISTALTIC_PERIOD_NAME =
            "Average Amplitude Of Contractions During Non Peristaltic Period";

    private static final String AVERAGE_AMPLITUDE_RISE_TIME_NAME =
            "Average Amplitude Of Rise Time";

    private static final String AVERAGE_TIME_OF_REDUCING_AMPLITUDE_NAME =
            "Average Time Of Reducing Amplitude";

    private static final String INDEX_OF_POWER_OF_PERISTALTIC_WAVES_NAME =
            "Index Of Power Of Peristaltic Waves";

    private static final String VALUE_FORMAT = "%1$,.2f";

    private LayoutInflater inflater;
    private LinearLayout rootContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transcription_layout);
        rootContainer = (LinearLayout) findViewById(R.id.root_container);

        inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Intent requestedIntent = getIntent();
        ProcessorResult processorResult = (ProcessorResult) requestedIntent.getSerializableExtra(PROCESS_RESULT_PARAM);
        populateItems(processorResult);
    }

    private void populateItems(ProcessorResult processorResult) {
        Integer itemNumber = 0;

        populateItem(++itemNumber, COUNT_WAVES_NAME, (double) processorResult.getCountWaves());

        populateItem(++itemNumber, AVERAGE_LENGTH_OF_PERISTALTIC_PERIOD_NAME,
                processorResult.getAverageLengthOfPeristalticPeriod());
        populateItem(++itemNumber, AVERAGE_MAX_AMPLITUDE_OF_PERISTALTIC_WAVES_NAME,
                processorResult.getAverageMaxAmplitudeOfPeristalticWaves());
        populateItem(++itemNumber, AVERAGE_AMPLITUDE_OF_PERISTALTIC_WAVES_NAME,
                processorResult.getAverageAmplitudeOfPeristalticWaves());
        populateItem(++itemNumber, MAX_AMPLITUDE_CONTRACTIONS_DURING_NON_PERISTALTIC_PERIOD_NAME,
                processorResult.getMaxAmplitudeContractionsDuringNonPeristalticPeriod());
        populateItem(++itemNumber, AVERAGE_AMPLITUDE_CONTRACTIONS_DURING_NON_PERISTALTIC_PERIOD_NAME,
                processorResult.getAverageAmplitudeContractionsDuringNonPeristalticPeriod());
        populateItem(++itemNumber, AVERAGE_AMPLITUDE_RISE_TIME_NAME,
                processorResult.getAverageAmplitudeRiseTime());
        populateItem(++itemNumber, AVERAGE_TIME_OF_REDUCING_AMPLITUDE_NAME,
                processorResult.getAverageTimeReducingAmplitude());
        populateItem(++itemNumber, INDEX_OF_POWER_OF_PERISTALTIC_WAVES_NAME,
                processorResult.getIndexOfPeristalticWave());
    }

    private void populateItem(Integer itemNumber, String varName, Double varValue) {
        View view = inflater.inflate(R.layout.transcription_layout_item, null);

        ((TextView) view.findViewById(R.id.numberTextView)).setText(String.format("%1s.", itemNumber));
        ((TextView) view.findViewById(R.id.variableNameTextView)).setText(varName);

        ((TextView) view.findViewById(R.id.variableValueTextView)).setText(
                String.format(VALUE_FORMAT, varValue));

        rootContainer.addView(view);
    }

}
