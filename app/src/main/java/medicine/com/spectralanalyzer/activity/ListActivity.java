package medicine.com.spectralanalyzer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import medicine.com.spectralanalyzer.R;
import medicine.com.spectralanalyzer.adapter.ArrayAdapterItem;
import medicine.com.spectralanalyzer.pojo.ItemData;
import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static medicine.com.spectralanalyzer.pojo.ActivityConstants.DATE_TIME_FORMATTER;
import static medicine.com.spectralanalyzer.pojo.ActivityConstants.FILE_TO_PROCESS;
import static medicine.com.spectralanalyzer.pojo.ActivityConstants.PATH_NAME;

public class ListActivity extends Activity {

    private static final String PROJECT_DIR_NAME = "AnalyzerData";
    private static final int RECORD_AUDIO_REQUEST_CODE = 9379992;

    private File sessionDir;

    private ListView listView;
    private List<ItemData> listViewItems;
    private ArrayAdapterItem adapter;

    private Button processAudioBtn;
    private Button addRecordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);

        initializeSessionPath();
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        addRecordBtn = (Button) findViewById(R.id.addRecordBtn);
        processAudioBtn = (Button) findViewById(R.id.processAudioBtn);

        addRecordBtn.setOnClickListener(new AddRecordOnClickListener());

        listViewItems = getLatestData();
        listView = (ListView) findViewById(R.id.listView);
        adapter = new ArrayAdapterItem(this, R.layout.list_view_row_item, listViewItems);
        listView.setAdapter(adapter);
        listView.setVisibility(adapter.isEmpty() ? View.GONE : View.VISIBLE);
        listView.setOnItemClickListener(new OnListViewItemClickListener());
        refreshEnabilityOfProcessBtn();

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

    private List<ItemData> getLatestData() {
        List<ItemData> itemDataList = new ArrayList<>();

        File[] files = sessionDir.listFiles();
        int tagGenerator = 0;
        for (File file : files) {
            itemDataList.add(new ItemData(++tagGenerator, file));
        }

        return itemDataList;
    }

    private void refreshListViewData() {
        List<ItemData> latestData = getLatestData();
        for (ItemData data : latestData) {
            if (!listViewItems.contains(data)) {
                listViewItems.add(data);
            }
        }
        adapter.notifyDataSetChanged();
        listView.setVisibility(adapter.isEmpty() ? View.GONE : View.VISIBLE);
        refreshEnabilityOfProcessBtn();
    }

    private void refreshEnabilityOfProcessBtn() {
        processAudioBtn.setEnabled(!adapter.isEmpty());
    }

    private void initializeSessionPath() {
        String sessionDirName = DATE_TIME_FORMATTER.print(DateTime.now());

        if (isExternalStorageAvailable()) {
            File externalStorage = Environment.getExternalStorageDirectory();
            String externalStorageSessionDirName = PROJECT_DIR_NAME + "/" + sessionDirName;
            sessionDir = new File(externalStorage, externalStorageSessionDirName);
        } else {
            sessionDir = new File(getFilesDir(), sessionDirName);
        }

        if (!sessionDir.exists()) {
            sessionDir.mkdirs();
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshListViewData();
    }

    private class AddRecordOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), AudioRecorder.class);
            intent.putExtra(PATH_NAME, sessionDir);
            startActivityForResult(intent, RECORD_AUDIO_REQUEST_CODE);
        }
    }

    private class OnListViewItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ItemData selectedItem = adapter.getItem(position);
            File selectedFileToProcess = selectedItem.getFile();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra(FILE_TO_PROCESS, selectedFileToProcess);
            startActivity(intent);
        }
    }

}
