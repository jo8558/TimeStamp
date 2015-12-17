package johannesprueller.timestamp;

import android.content.Context;
import android.content.pm.PackageManager;
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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import org.w3c.dom.Text;

import java.util.Date;

import static android.R.color.black;
import static android.R.color.transparent;

public class MobileTimeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.context = this;

        setContentView(R.layout.activity_mobile_time);

        mainLayout = (LinearLayout) this.findViewById(R.id.mainLayout);
        timeInfo = (TextView)this.findViewById(R.id.timeInfo);
        textClock = (TextClock)this.findViewById(R.id.textclock);

        chronometer = (Chronometer)this.findViewById(R.id.timerview);

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
                if(message.what == 0) {
                    String msg = message.obj.toString();
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            }
        };

        NfcHelper helper = new NfcHelper(this, handler);
        helper.InitializeNfc();
    }

    public void onCurrentTimeClick(View view) {
        timeInfo.setText(R.string.workedTime);
        textClock.setVisibility(View.GONE);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.setVisibility(View.VISIBLE);
        chronometer.start();
    }

    public void onWorkingTimeClick(View view){
        String workedTime = chronometer.getText().toString();
        chronometer.stop();
        timeInfo.setText(R.string.currentTime);
        chronometer.setVisibility(View.GONE);
        textClock.setVisibility(View.VISIBLE);
    }

    private Context context;
    private Handler handler;

    private LinearLayout mainLayout;
    private TextView timeInfo;
    private TextClock textClock;
    private Chronometer chronometer;
}
