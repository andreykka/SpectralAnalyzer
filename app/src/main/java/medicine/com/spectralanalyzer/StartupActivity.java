package medicine.com.spectralanalyzer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class StartupActivity extends Activity {

    private Button btnRecord1;
    private Button btnRecord2;
    private Button btnRecord3;
    private Button btnRecord4;
    private Button btnRecord5;

    private CheckBox checkBox1;
    private CheckBox checkBox2;
    private CheckBox checkBox3;
    private CheckBox checkBox4;
    private CheckBox checkBox5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        btnRecord1 = (Button) findViewById(R.id.btnStartRecord1);
        btnRecord2 = (Button) findViewById(R.id.btnStartRecord2);
        btnRecord3 = (Button) findViewById(R.id.btnStartRecord3);
        btnRecord4 = (Button) findViewById(R.id.btnStartRecord4);
        btnRecord5 = (Button) findViewById(R.id.btnStartRecord5);

        checkBox1 = (CheckBox) findViewById(R.id.checkBoxStatusRecord1);
        checkBox2 = (CheckBox) findViewById(R.id.checkBoxStatusRecord2);
        checkBox3 = (CheckBox) findViewById(R.id.checkBoxStatusRecord3);
        checkBox4 = (CheckBox) findViewById(R.id.checkBoxStatusRecord4);
        checkBox5 = (CheckBox) findViewById(R.id.checkBoxStatusRecord5);
    }

    public void handleRecordAction(View view) {



    }


}
