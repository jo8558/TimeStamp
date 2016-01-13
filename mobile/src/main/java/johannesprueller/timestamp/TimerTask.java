package johannesprueller.timestamp;

import android.os.Handler;
import android.widget.TextView;

import org.joda.time.DateTime;

public class TimerTask implements Runnable {

    public TimerTask(TextView view, DateTime workedTime, Handler timerHandler) {
        this.timerView = view;
        this.workedTime = workedTime;
        this.timerHandler = timerHandler;
    }

    public Runnable setFirstRun() {
        isFirst = true;
        return this;
    }

    @Override
    public void run() {
        DateTime nextstep = workedTime;
        if (!isFirst) {
            nextstep = workedTime.plusSeconds(1);
        } else {
            isFirst = false;
        }
        String hours;
        String minutes;
        String seconds;

        if (nextstep.getHourOfDay() <= 9) {
            hours = "0" + nextstep.getHourOfDay();
        } else {
            hours = String.valueOf(nextstep.getHourOfDay());
        }

        if (nextstep.getMinuteOfHour() <= 9) {
            minutes = "0" + nextstep.getMinuteOfHour();
        } else {
            minutes = String.valueOf(nextstep.getMinuteOfHour());
        }

        if (nextstep.getSecondOfMinute() <= 9) {
            seconds = "0" + nextstep.getSecondOfMinute();
        } else {
            seconds = String.valueOf(nextstep.getSecondOfMinute());
        }

        timerView.setText(hours + ":" + minutes + ":" + seconds);

        workedTime = nextstep;

        DateTime now = DateTime.now();
        if (!(now.getHourOfDay() == 23) &&
                !(now.getMinuteOfHour() == 59) &&
                !(now.getSecondOfMinute() == 59)) {
            timerHandler.postDelayed(this, 1000);
        }
    }


    private TextView timerView;
    private DateTime workedTime;
    private Handler timerHandler;
    private boolean isFirst;
}
