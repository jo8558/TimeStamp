package johannesprueller.timestamp;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class NfcSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.activity_nfc_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        if(bar != null)
        {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);

        nfcInfo = (TextView) findViewById(R.id.nfcInfo);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.alertTitle);
                builder.setMessage(R.string.alertMessage);
                builder.setNegativeButton(R.string.alertCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        listenToNfc = false;
                    }
                });
                builder.setCancelable(false);
                alertDialog = builder.create();
                listenToNfc = true;
                alertDialog.show();
            }
        });

        listview = (ListView) findViewById(R.id.nfcList);
        nfcTags = LoadNfcTags();
        nfcTagAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nfcTags);
        listview.setAdapter(nfcTagAdapter);

        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                if (message.what == 0) {

                    if (listenToNfc) {
                        String tag = message.obj.toString();

                        switch (dbHelper.SaveTag(tag)) {
                            case Successful:
                                Toast.makeText(context, R.string.nfcRegisterSuccessful, Toast.LENGTH_LONG).show();
                                nfcTags.clear();
                                nfcTags.addAll(dbHelper.LoadNfcTags());
                                nfcTagAdapter.notifyDataSetChanged();
                                nfcInfo.setVisibility(View.GONE);
                                listview.setVisibility(View.VISIBLE);
                                break;
                            case Failed:
                                Toast.makeText(context, R.string.nfcRegisterFailed, Toast.LENGTH_LONG).show();
                                break;
                            case AlreadyExisiting:
                                Toast.makeText(context, R.string.nfcRegisterAlreadyExisiting, Toast.LENGTH_LONG).show();
                                break;
                        }
                        alertDialog.dismiss();
                    }

                }
            }
        };

    NfcHelper helper = new NfcHelper(this, handler);
    helper.InitializeNfc();
}

    private List<String> LoadNfcTags() {
        List<String> nfctags = dbHelper.LoadNfcTags();
        if (nfctags.size() == 0) {
            listview.setVisibility(View.GONE);
            nfcInfo.setVisibility(View.VISIBLE);
        }
        return nfctags;
    }

private TextView nfcInfo;
private ListView listview;
private Context context;
private AlertDialog alertDialog;
private DatabaseHelper dbHelper;
private boolean listenToNfc;
private List<String> nfcTags;
private ArrayAdapter nfcTagAdapter;
}
