package medicine.com.spectralanalyzer.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.*;
import android.os.*;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import ca.uol.aig.fftpack.RealDoubleFFT;
import medicine.com.spectralanalyzer.R;
import org.joda.time.DateTime;

import java.io.*;

import static medicine.com.spectralanalyzer.pojo.ActivityConstants.*;
import static medicine.com.spectralanalyzer.pojo.ActivityConstants.DATE_TIME_FORMATTER;

public class AudioRecorder extends Activity implements View.OnClickListener {

    private static final String LOG_TAG = AudioRecorder.class.getSimpleName();

    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";

    private static final int SAMPLE_RATE_8000 = 8000;
    private static final int SAMPLE_RATE_22050 = 22050;

    private static int RECORDER_SAMPLE_RATE = SAMPLE_RATE_8000;

    private static final int RECORDER_BPP = 16;
    private static final int BYTE_RATE = 8;

    private int recorderBufferSize;

    private int blockSize = 256;

    private RealDoubleFFT transformer;

    private Button startStopButton;
    private RadioButton radioButton8000;
    private RadioButton radioButton22050;

    private ImageView imageView;
    private Canvas canvas;
    private Paint paint;

    private boolean isRecording = false;

    private File sessionDir;

    private RecordAudio recordTask;
    private AudioRecord audioRecord;
    private AudioTrack track;

    private Chronometer chronometer;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_layout);

        startStopButton = (Button) this.findViewById(R.id.StartStopButton);

        radioButton8000 = (RadioButton) this.findViewById(R.id.radioButton8000);
        radioButton22050 = (RadioButton) this.findViewById(R.id.radioButton22050);
        selectAppropriateSampleRateRadioButton();

        chronometer = (Chronometer) findViewById(R.id.chronometer);

        startStopButton.setOnClickListener(this);
        transformer = new RealDoubleFFT(blockSize);

        imageView = (ImageView) this.findViewById(R.id.ImageView01);
        Bitmap bitmap = Bitmap.createBitmap(512, 400, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(ContextCompat.getColor(this, R.color.specterColor));

        imageView.setImageBitmap(bitmap);

        Intent requestedIntent = getIntent();
        if (requestedIntent != null) {
            sessionDir = (File) requestedIntent.getSerializableExtra(PATH_NAME);
        } else {
            Intent resultIntent = new Intent();
            setResult(RESULT_CANCELED, resultIntent);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.settings_item: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            }
        }
        return true;
    }

    private void selectAppropriateSampleRateRadioButton() {
        if (RECORDER_SAMPLE_RATE == SAMPLE_RATE_8000) {
            radioButton8000.setChecked(true);
        } else {
            radioButton22050.setChecked(true);
        }
    }

    public class RecordAudio extends AsyncTask<Void, double[], Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                recorderBufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);

                int trackerBufferSize = AudioTrack.getMinBufferSize(RECORDER_SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);

                audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, RECORDER_SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, recorderBufferSize);

                track = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION,
                        RECORDER_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, trackerBufferSize, AudioTrack.MODE_STREAM);

                if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                    audioRecord.startRecording();
                    if (track.getState() == AudioTrack.STATE_INITIALIZED) {
                        track.play();
                    }

                    recordAudioDataToTempFile();
                } else {
                    Log.e(LOG_TAG, "AudioRecord created with error State: " + audioRecord.getState());
                }

            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(double[]... toTransform) {
            canvas.drawColor(Color.argb(100, 243, 243, 243));

            int high = 400;

            int scale = 2;
            int downy, x;

            for (int i = 0, j = 1; i < toTransform[0].length; i++, j += scale) {
                for (int k = 0; k < scale; k++) {
                    x = j + k;
                    downy = (int) (high - (toTransform[0][i] * 300));
                    downy = downy > high ? high : downy;

                    canvas.drawLine(x, downy, x, high, paint);
                }
            }

            imageView.invalidate();
        }

        private void recordAudioDataToTempFile() throws IOException {
            FileOutputStream os = getTempFileOutputStream();

            if (null != os) {
                // while loop of recording
                startRecording(os);

                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void startRecording(FileOutputStream fileOutputStream) {
            int byteBlockSize = blockSize * 2;
            byte data[] = new byte[byteBlockSize];
            double[] toTransform = new double[blockSize];

            int read;
            while (isRecording) {
                read = audioRecord.read(data, 0, byteBlockSize);

                track.write(data, 0, read);

                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        fileOutputStream.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                for (int i = 0, j = 0; i < byteBlockSize && i < read; i += 2, j++) {
                    toTransform[j] = (double) (data[i + 1] << 8 | data[i]) / Short.MAX_VALUE;
                }
                transformer.ft(toTransform);
                publishProgress(toTransform);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            stopRecording();
            progressDialog.dismiss();
            finish();
        }
    }

    private FileOutputStream getWavFileOutputSteam() {
        String wavFileName = DATE_TIME_FORMATTER.print(DateTime.now()) + AUDIO_RECORDER_FILE_EXT_WAV;
        FileOutputStream wavFileOutputStream = null;

        try {
            File wavFile = new File(sessionDir, wavFileName);
            boolean isCreated = false;
            if (!wavFile.exists()) {
                isCreated = wavFile.createNewFile();
            }
            if (isCreated) {
                wavFileOutputStream = new FileOutputStream(wavFile);
            } else {
                Log.e(LOG_TAG, ".WAV File Wasn't created on getWavFileOutputSteam operation");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return wavFileOutputStream;
    }

    private FileOutputStream getTempFileOutputStream() {
        FileOutputStream tempFileOutputStream = null;

        try {
            File tempFile = new File(sessionDir, AUDIO_RECORDER_TEMP_FILE);
            boolean isCreated = false;
            if (!tempFile.exists()) {
                isCreated = tempFile.createNewFile();
            }
            if (isCreated) {
                tempFileOutputStream = new FileOutputStream(tempFile);
            } else {
                Log.e(LOG_TAG, "Temp File Wasn't created on getTempFileOutputStream operation");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFileOutputStream;
    }

    private FileInputStream getTempFileInputStream() {
        FileInputStream tempFileInputStream = null;

        try {
            File tempFile = new File(sessionDir, AUDIO_RECORDER_TEMP_FILE);
            boolean isCreated = true;
            if (!tempFile.exists()) {
                isCreated = tempFile.createNewFile();
            }

            if (isCreated) {
                tempFileInputStream = new FileInputStream(tempFile);
            } else {
                Log.e(LOG_TAG, "Temp File Wasn't created on getTempFileInputStream operation");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFileInputStream;
    }

    public void onClick(View arg0) {
        if (isRecording) {
            setUpInterruptRecordingConfiguration();
        } else {
            setUpStartRecordingConfiguration();
        }
    }

    private void stopRecording() {
        if (null != audioRecord) {
            isRecording = false;

            int recorderState = audioRecord.getState();
            if (recorderState == AudioRecord.STATE_INITIALIZED) {
                audioRecord.stop();
            }
            audioRecord.release();
            audioRecord = null;
        }

        if (null != track) {
            int trackerState = track.getState();
            if (trackerState == AudioTrack.STATE_INITIALIZED) {
                try {
                    track.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                    track.pause();
                    track.flush();
                }
            }
            track.release();
            track = null;
        }

        copyFromTempWaveFile();
        deleteTempFile();
    }

    private void deleteTempFile() {
        boolean isDeleted;

        File tempFile = new File(sessionDir, AUDIO_RECORDER_TEMP_FILE);
        isDeleted = tempFile.delete();

        if (isDeleted) {
            Log.i(LOG_TAG, "Temp file successfully deleted");
        } else {
            Log.i(LOG_TAG, "Temp file wasn't deleted");
        }
    }

    private void copyFromTempWaveFile() {
        FileInputStream tempFileInputStream = getTempFileInputStream();
        FileOutputStream wavFileOutputStream = getWavFileOutputSteam();
        long totalAudioLen;
        long totalDataLen;
        long longSampleRate = RECORDER_SAMPLE_RATE;
        int channels = 1;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLE_RATE * channels / BYTE_RATE;

        byte[] data = new byte[recorderBufferSize];

        try {
            totalAudioLen = tempFileInputStream.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            WriteWaveFileHeader(wavFileOutputStream, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);

            while (tempFileInputStream.read(data) != -1) {
                wavFileOutputStream.write(data);
            }

            tempFileInputStream.close();
            wavFileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen,
                                     long longSampleRate, int channels, long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    private void setSampleRateDisabilityStatus(boolean isEnabled) {
        radioButton8000.setEnabled(isEnabled);
        radioButton22050.setEnabled(isEnabled);
    }

    private void setUpStartRecordingConfiguration() {
        isRecording = true;
        startStopButton.setText(R.string.stop_recording);
        // disallow any updates after start recording
        setSampleRateDisabilityStatus(false);
        recordTask = new RecordAudio();
        recordTask.execute();
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    private void setUpInterruptRecordingConfiguration() {
        progressDialog = ProgressDialog.show(AudioRecorder.this, "Wait...", "Saving audio file...", true);
        isRecording = false;
        recordTask.cancel(false);
        chronometer.stop();
        startStopButton.setText(R.string.start_recording);
        setSampleRateDisabilityStatus(true);
        startStopButton.setEnabled(false);

        new Thread(new Runnable(){
            @Override
            public void run(){
                while (progressDialog.isShowing()){
                    continue;
                };
            }
        }).start();
    }

    public void onRadioButtonSelect(View v) {
        RadioButton selectedButton = (RadioButton) v;
        if (selectedButton == radioButton8000) {
            RECORDER_SAMPLE_RATE = SAMPLE_RATE_8000;
        } else {
            RECORDER_SAMPLE_RATE = SAMPLE_RATE_22050;
        }
    }

}