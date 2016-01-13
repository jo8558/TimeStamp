package johannesprueller.timestamp;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextClock;
import android.widget.TextView;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

public class MobileTimeActivity extends AppCompatActivity {

    private Handler handler;
    private Handler timerHandler;
    private TimerTask timerRunnable;
    private DateTime workedTime;
    private TextView timeInfo;
    private TextClock textClock;
    private ListView listView;
    private TextView timerView;
    private List<TimeStampItem> timeStampItemList;
    private TimeStampItem currentItem;
    private TimeStampAdapter timeStampAdapter;
    private DateTime today;
    private DatabaseHelper dbHelper;
    private boolean working;
    private DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this);
        setContentView(R.layout.activity_mobile_time);

        timeInfo = (TextView) this.findViewById(R.id.timeInfo);
        textClock = (TextClock) this.findViewById(R.id.textclock);
        listView = (ListView) this.findViewById(R.id.timelist);
        timerView = (TextView) this.findViewById(R.id.timerview);

        timeStampItemList = new ArrayList<>();
        timeStampAdapter = new TimeStampAdapter(this, timeStampItemList);
        listView.setAdapter(timeStampAdapter);

        dbHelper = new DatabaseHelper(this);
        dbHelper.LoadTimeStampItems();
        today = DateTime.now();
        workedTime = new DateTime(today.getYear(), today.getMonthOfYear(), today.getDayOfMonth(), 0, 0, 0);

        timerHandler = new Handler();
        timerRunnable = new TimerTask(timerView, workedTime, timerHandler);

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                if (message.what == 0) {
                    List<String> nfcTags = dbHelper.LoadNfcTags();
                    if(nfcTags != null) {
                        if (nfcTags.contains(message.obj.toString())) {
                            if (working) {
                                HandleWorkingTimeClicked();
                            } else {
                                HandleCurrentTimeClicked();
                            }
                        }
                    }
                } else if (message.what == 1) {
                    GetNewTimerLoaderTask().execute();
                }
            }
        };


        NfcHelper helper = new NfcHelper(this, handler);
        helper.InitializeNfc();
    }

    @Override
    protected void onStart() {
        super.onStart();
        GetNewTimerLoaderTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.timestamp_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nfcsetting:
                startActivity(new Intent(this, NfcSettingActivity.class));
                break;
            case R.id.timeoverview:
                startActivity(new Intent(this, TimeOverviewActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCurrentTimeClick(View view) {
        HandleCurrentTimeClicked();
    }

    public void onWorkingTimeClick(View view) {
        HandleWorkingTimeClicked();
    }

    private void HandleCurrentTimeClicked() {
        working = true;
        revertTextViewsToWorkingTime();

        currentItem = new TimeStampItem();
        currentItem.setStartTime(new DateTime());

        if (timeStampItemList.size() == 0) {
            createHeader();
            timeStampItemList.add(currentItem);
        } else {
            timeStampItemList.add(currentItem);
        }
        timeStampAdapter.notifyDataSetChanged();

        dbHelper.SaveStartTime(currentItem);

        timerHandler.post(timerRunnable.setFirstRun());
    }

    private void HandleWorkingTimeClicked() {
        working = false;
        timerHandler.removeCallbacks(timerRunnable);

        revertTextViewsToCurrentTime();

        currentItem.setStopTime(new DateTime());
        timeStampAdapter.notifyDataSetChanged();

        dbHelper.SaveEndTime(currentItem);
    }

    private void createHeader() {
        View headerView = LayoutInflater.from(this).inflate(R.layout.timestamp_itemview, null);

        TextView headerStart = (TextView) headerView.findViewById(R.id.startTime);
        headerStart.setText(R.string.headerStart);

        TextView headerEnd = (TextView) headerView.findViewById(R.id.endTime);
        headerEnd.setText(R.string.headerEnd);

        listView.addHeaderView(headerView);
    }

    private void revertTextViewsToWorkingTime() {
        timeInfo.setText(R.string.workedTime);
        textClock.setVisibility(View.GONE);
        timerView.setVisibility(View.VISIBLE);
    }

    private void revertTextViewsToCurrentTime() {
        timeInfo.setText(R.string.currentTime);
        timerView.setVisibility(View.GONE);
        textClock.setVisibility(View.VISIBLE);
    }

    private void handleAsyncTimeLoaderResult(List<TimeStampItem> output) {
        if (output != null && output.size() > 0) {
            if (timeStampItemList.size() == 0) {
                createHeader();
            }

            timeStampItemList.clear();
            timeStampItemList.addAll(output);
            timeStampAdapter.notifyDataSetChanged();

            workedTime = new DateTime(today.getYear(), today.getMonthOfYear(), today.getDayOfMonth(),0,0,0);

            for(TimeStampItem item : output)
            {
                if(item.getStartTime() != null && item.getStopTime() != null) {
                    Period diff = new Period(item.getStartTime(), item.getStopTime());
                    workedTime = workedTime.plus(diff);
                }
                else
                {
                    if(item.getStartTime() != null)
                    {
                        Period diff = new Period(item.getStartTime(), DateTime.now());
                        workedTime = workedTime.plus(diff);
                    }
                }
            }

            TimeStampItem lastItem = timeStampItemList.get(timeStampItemList.size() - 1);
            currentItem = lastItem;

            timerRunnable = new TimerTask(timerView, workedTime, timerHandler);

            if (lastItem.getStopTime() == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        revertTextViewsToWorkingTime();
                    }
                });
                working = true;
                timerHandler.post(timerRunnable);
            } else {
                timerHandler.removeCallbacks(timerRunnable);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        revertTextViewsToCurrentTime();
                    }
                });
                working = false;
            }
        } else {
            timeStampItemList.clear();
            timeStampAdapter.notifyDataSetChanged();
            revertTextViewsToCurrentTime();
            working = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        timerHandler.removeCallbacks(timerRunnable);
    }

    private AsyncTimeLoaderTask GetNewTimerLoaderTask() {
        return new AsyncTimeLoaderTask(this, new AsyncTimeLoaderTask.AsyncResponse() {
            @Override
            public void processFinish(List<TimeStampItem> output) {
                handleAsyncTimeLoaderResult(output);
            }
        });
    }
}
