package medicine.com.spectralanalyzer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import ca.uol.aig.fftpack.RealDoubleFFT;
import org.joda.time.DateTime;

import java.io.*;

import static medicine.com.spectralanalyzer.ActivityConstants.DATE_TIME_FORMATTER;

public class AudioRecorder3 extends Activity implements View.OnClickListener {

    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";

    private int RECORDER_SAMPLE_RATE = 44100;
    private static final int RECORDER_BPP = 16;

    private RealDoubleFFT transformer;
    private int blockSize = 256;

    private Button startStopButton;
    private boolean isRecording = false;

    private RecordAudio recordTask;

    private ImageView imageView;
    private Canvas canvas;
    private Paint paint;
    private String sessionPath;
    private int recorderBufferSize;
    private int trackerBufferSize;

    private AudioRecord audioRecord;
    private AudioTrack track;

    private TextView timeText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_layout_3);

        startStopButton = (Button) this.findViewById(R.id.StartStopButton);
        timeText = (TextView) findViewById(R.id.timeText);

        startStopButton.setOnClickListener(this);

        transformer = new RealDoubleFFT(blockSize);

        imageView = (ImageView) this.findViewById(R.id.ImageView01);
        Bitmap bitmap = Bitmap.createBitmap(512, 400, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.GREEN);

        imageView.setImageBitmap(bitmap);

        Intent requestedIntent = getIntent();
        if (requestedIntent != null) {
            sessionPath = requestedIntent.getStringExtra(ActivityConstants.PATH_NAME);
        } else {
            Intent resultIntent = new Intent();
            setResult(RESULT_CANCELED, resultIntent);
            finish();
        }

        recorderBufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        trackerBufferSize = AudioTrack.getMinBufferSize(RECORDER_SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

    }

    public class RecordAudio extends AsyncTask<Void, double[], Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, RECORDER_SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, recorderBufferSize);

                track = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION,
                        RECORDER_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, trackerBufferSize, AudioTrack.MODE_STREAM);

                if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                    audioRecord.startRecording();
                }

                if (track.getState() == AudioTrack.STATE_INITIALIZED) {
                    track.play();
                }

                handler.post(updateTimerThread);
                writeAudioDataToFile();
                track.stop();
                track.release();

            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(double[]... toTransform) {
            canvas.drawColor(Color.BLACK);

            int i1 = 400;

            int scale = 2;
            for (int i = 0, j = 1; i < toTransform[0].length; i++, j += scale) {
                for (int k = 0; k < scale; k++) {
                    int x = j + k;
                    int downy = (int) (i1 - (toTransform[0][i] * 400));

                    canvas.drawLine(x, downy, x, i1, paint);
                }
            }

            imageView.invalidate();
        }

        private void writeAudioDataToFile() {
            int byteBlockSize = blockSize * 2;
            byte data[] = new byte[byteBlockSize];
            double[] toTransform = new double[blockSize];

            String filename = getTempFilename();
            FileOutputStream os = null;
            try {
                os = new FileOutputStream(filename);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            int read;

            if (null != os) {
                while (isRecording) {
                    read = audioRecord.read(data, 0, byteBlockSize);

                    track.write(data, 0, read);

                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            os.write(data);
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

                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                stopRecording();
            }
        }

    }

    private String getFilename() {
        File file = new File(sessionPath);

        if (!file.exists()) {
            file.mkdirs();
        }

        return file.getAbsolutePath().concat("/").concat(DATE_TIME_FORMATTER.print(DateTime.now())) + AUDIO_RECORDER_FILE_EXT_WAV;
    }

    private String getTempFilename() {
        File file = new File(sessionPath);

        if (!file.exists()) {
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    public void onClick(View arg0) {
        if (isRecording) {
            isRecording = false;
            startStopButton.setText("Start");
            recordTask.cancel(true);
        } else {
            isRecording = true;
            startStopButton.setText("Stop");
            recordTask = new RecordAudio();
            recordTask.execute();
        }
    }


    private void stopRecording() {

        handler.removeCallbacks(updateTimerThread);

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
                track.stop();
            }
            track.release();
            track = null;
        }

        copyWaveFile(getTempFilename(), getFilename());
        deleteTempFile();

    }

    private void deleteTempFile() {
        File file = new File(getTempFilename());
        file.delete();

        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLE_RATE;
        int channels = 1;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLE_RATE * channels / 8;

        byte[] data = new byte[recorderBufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;


            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while (in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        deleteTempFile();
    }

    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

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

    Handler handler = new Handler(Looper.getMainLooper());

    private Runnable updateTimerThread = new Runnable() {
        int min = 0;
        int sec = 0;
        int ms = 0;
        int PERIOD = 100;

        @Override
        public void run() {
            ms += PERIOD + 10;
            if (ms >= 1000) {
                ms = 0;
                sec++;
                if (sec >= 60) {
                    sec = 0;
                    min++;
                }
            }
            String time = (min < 10 ? "0" + min : min) + ":" + (sec < 10 ? "0" + sec : sec);
            timeText.setText(time);
            handler.postDelayed(this, PERIOD);
        }
    };

}