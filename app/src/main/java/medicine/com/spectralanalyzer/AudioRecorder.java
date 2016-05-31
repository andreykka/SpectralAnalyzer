package medicine.com.spectralanalyzer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import android.widget.TextView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static medicine.com.spectralanalyzer.ActivityConstants.DATE_TIME_FORMATTER;

public class AudioRecorder extends Activity {

    private Handler handler = new Handler(Looper.getMainLooper());

    private static final String EXTENSION = ".3gp";
    private static final String LOG_TAG = "AudioRecorder";

    private static String mFileName = null;
    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;
    private TextView timeText;
    boolean recording = false;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private static int PERIOD = 100;

    private Runnable updateTimerThread = new Runnable() {
        int min = 0;
        int sec = 0;
        int ms = 0;

        @Override
        public void run() {
            ms += PERIOD;
            if (ms >= 1000) {
                ms = 0;
                sec++;
                if (sec >= 60) {
                    sec = 0;
                    min++;
                }
            }
            String time = (min < 10 ? "0" + min : min) + ":" + (sec < 10 ? "0" + sec : sec) + ":" + ms;
            timeText.setText(time);
            handler.postDelayed(this, PERIOD);
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent requestedIntent = getIntent();

        if (requestedIntent != null) {
            String pathName = requestedIntent.getStringExtra(ActivityConstants.PATH_NAME);
            mFileName = pathName.concat("/").concat(DATE_TIME_FORMATTER.print(DateTime.now())).concat(EXTENSION);
            Log.i(LOG_TAG, mFileName);
        } else {
            Intent resultIntent = new Intent();
            setResult(RESULT_CANCELED, resultIntent);
            finish();
        }

        LinearLayout ll = new LinearLayout(this);
        mRecordButton = new RecordButton(this);
        timeText = new TextView(this);
        timeText.setText("00:00:000");
        ll.addView(mRecordButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));

        ll.addView(timeText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));

        setContentView(ll);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void onRecord(boolean start) {
        if (start) {
            recording = true;
            startRecording();
            handler.post(updateTimerThread);
        } else {
            recording = false;
            stopRecording();
            handler.removeCallbacks(updateTimerThread);
        }
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mRecorder.setAudioEncodingBitRate(44100);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }
}
