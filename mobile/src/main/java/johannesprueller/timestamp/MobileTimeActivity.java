package johannesprueller.timestamp;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import static android.R.color.black;
import static android.R.color.transparent;

public class MobileTimeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.context = this;

        setContentView(R.layout.activity_mobile_time);

        timeStampItemList = new ArrayList<>();
        timeStampAdapter = new TimeStampAdapter(this, timeStampItemList);

        mainLayout = (LinearLayout) this.findViewById(R.id.mainLayout);
        timeInfo = (TextView) this.findViewById(R.id.timeInfo);
        textClock = (TextClock) this.findViewById(R.id.textclock);
        chronometer = (Chronometer) this.findViewById(R.id.timerview);
        listView = (ListView) this.findViewById(R.id.timelist);
        listView.setAdapter(timeStampAdapter);

//        if(textClock.is24HourModeEnabled())
//        {
//            textClock.setFormat24Hour(textClock.getFormat24Hour());
//        }
//        else
//        {
//            textClock.setFormat12Hour(textClock.getFormat12Hour());
//        }


        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                if (message.what == 0) {
                    String msg = message.obj.toString();
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            }
        };

        NfcHelper helper = new NfcHelper(this, handler);
        helper.InitializeNfc();

        new AsyncTimeLoaderTask(this, new AsyncTimeLoaderTask.AsyncResponse() {
            @Override
            public void processFinish(List<TimeStampItem> output) {
                if (output != null && output.size() > 0) {
                    if (timeStampItemList.size() == 0) {
                        CreateHeader();
                    }

                    timeStampItemList.addAll(output);
                    timeStampAdapter.notifyDataSetChanged();

                    TimeStampItem newItem = timeStampItemList.get(timeStampItemList.size() - 1);
                    if (newItem.getStopTime() == null) {
                        currentItem = newItem;
                        long diff = newItem.getStartTime().getTime();

                        long base;
                        SharedPreferences prefs = getSharedPreferences(LASTPAUSE, MODE_PRIVATE);
                        lastPause = prefs.getLong(PAUSE, 0);
                        base = prefs.getLong(BASE, 0);

                        chronometer.setBase(base + SystemClock.elapsedRealtime() - lastPause);
                        timeInfo.setText(R.string.workedTime);
                        textClock.setVisibility(View.GONE);
                        chronometer.setVisibility(View.VISIBLE);
                        chronometer.start();
                    }
                }
            }
        }).execute();
    }

    public void onCurrentTimeClick(View view) {
        timeInfo.setText(R.string.workedTime);
        textClock.setVisibility(View.GONE);
        if (!alreadyStarted) {
            chronometer.setBase(SystemClock.elapsedRealtime());
            alreadyStarted = true;
        } else {
            chronometer.setBase(chronometer.getBase() + SystemClock.elapsedRealtime() - lastPause);
        }

        chronometer.setVisibility(View.VISIBLE);
        chronometer.start();

        currentItem = new TimeStampItem();
        currentItem.setStartTime(new Date());

        if (timeStampItemList.size() == 0) {
            CreateHeader();
            timeStampItemList.add(currentItem);
        } else {
            timeStampItemList.add(currentItem);
        }
        timeStampAdapter.notifyDataSetChanged();

        ContentValues values = new ContentValues();
        values.put(TimeStampContentProvider.START, sdf.format(currentItem.getStartTime()));

        Uri uri = getContentResolver().insert(TimeStampContentProvider.CONTENT_URI, values);
        if (uri != null) {
            String id = uri.getPathSegments().get(1);
            currentItem.setId(Integer.parseInt(id));
        }
    }

    public void onWorkingTimeClick(View view) {
        String workedTime = chronometer.getText().toString();
        lastPause = SystemClock.elapsedRealtime();
        chronometer.stop();
        timeInfo.setText(R.string.currentTime);
        chronometer.setVisibility(View.GONE);
        textClock.setVisibility(View.VISIBLE);

        currentItem.setStopTime(new Date());
        timeStampAdapter.notifyDataSetChanged();

        ContentValues values = new ContentValues();
        values.put(TimeStampContentProvider.END, sdf.format(currentItem.getStopTime()));

        String[] args = new String[]{String.valueOf(currentItem.getId())};
        int result = getContentResolver().update(TimeStampContentProvider.CONTENT_URI, values, "_id=?", args);
    }

    private void CreateHeader() {
        View headerView = LayoutInflater.from(this).inflate(R.layout.timestamp_itemview, null);

        TextView headerStart = (TextView) headerView.findViewById(R.id.startTime);
        headerStart.setText(R.string.headerStart);

        TextView headerEnd = (TextView) headerView.findViewById(R.id.endTime);
        headerEnd.setText(R.string.headerEnd);

        listView.addHeaderView(headerView);
    }

    @Override
    protected void onStop() {
        super.onStop();
        chronometer.stop();
        SharedPreferences prefs = getSharedPreferences(LASTPAUSE, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(PAUSE, SystemClock.elapsedRealtime());
        editor.putLong(BASE, chronometer.getBase());
        editor.apply();
    }

    private Context context;
    private Handler handler;

    private LinearLayout mainLayout;
    private TextView timeInfo;
    private TextClock textClock;
    private ListView listView;
    private Chronometer chronometer;

    private List<TimeStampItem> timeStampItemList;
    private TimeStampItem currentItem;
    private TimeStampAdapter timeStampAdapter;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private long lastPause = 0;
    private boolean alreadyStarted;

    private final String LASTPAUSE = "LastPause";
    private final String PAUSE = "pause";
    private final String BASE = "base";
}
