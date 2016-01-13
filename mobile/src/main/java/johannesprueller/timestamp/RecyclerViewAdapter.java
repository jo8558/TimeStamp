package johannesprueller.timestamp;

import android.app.ActionBar;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Text;

import java.util.Date;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.TimeStampViewHolder> {

    public RecyclerViewAdapter(Context context)
    {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        timestamps = dbHelper.LoadTimeStampItems();
        this.context = context;
    }

    @Override
    public TimeStampViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.timeview_recylceritem, parent, false);
        return new TimeStampViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TimeStampViewHolder holder, int position) {
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoParams.setMargins(8, 0, 0, 8);

        SpannableString content = new SpannableString(dtf.print(timestamps.get(position).get(0).getStartTime()));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        holder.info.setText(content);
        holder.info.setLayoutParams(infoParams);

        DateTime today = DateTime.now();
        DateTime sum = new DateTime(today.getYear(), today.getMonthOfYear(), today.getDayOfMonth(),0,0,0);

        for(TimeStampItem timeStamp : timestamps.get(position))
        {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
            layoutParams.setLayoutDirection(LinearLayout.HORIZONTAL);
            LinearLayout layout = new LinearLayout(context);
            layout.setLayoutParams(layoutParams);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            params.setMargins(8,0,0,8);

            TextView start = new TextView(context);
            start.setText( context.getString(R.string.headerStart) + "  " + timedtf.print(timeStamp.getStartTime()));
            start.setLayoutParams(params);
            start.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            layout.addView(start);

            TextView end = new TextView(context);
            if(timeStamp.getStopTime() != null) {
                end.setText(context.getString(R.string.headerEnd) + "  " + timedtf.print(timeStamp.getStopTime()));
                Period diff = new Period(timeStamp.getStartTime(), timeStamp.getStopTime());
                sum = sum.plus(diff);
            }
            else
            {
                Period diff = new Period(timeStamp.getStartTime(), DateTime.now());
                sum = sum.plus(diff);
                end.setText( context.getString(R.string.headerEnd) + "  " + "--:--:--");
            }
            end.setLayoutParams(params);
            end.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            layout.addView(end);

            holder.listLayout.addView(layout);
        }

        LinearLayout.LayoutParams totallayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        totallayoutParams.setLayoutDirection(LinearLayout.HORIZONTAL);

        LinearLayout totalLayout = new LinearLayout(context);
        totalLayout.setLayoutParams(totallayoutParams);

        LinearLayout.LayoutParams totalParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        totalParams.setMargins(8, 15, 0, 8);

        TextView totalText = new TextView(context);
        totalText.setLayoutParams(totalParams);
        totalText.setText(R.string.total);
        totalText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        totalLayout.addView(totalText);

        TextView calcuatedTotal = new TextView(context);
        calcuatedTotal.setLayoutParams(totalParams);
        calcuatedTotal.setText(timedtf.print(sum));
        calcuatedTotal.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        totalLayout.addView(calcuatedTotal);

        holder.listLayout.addView(totalLayout);
    }

    @Override
    public int getItemCount() {
        return timestamps.size();
    }

    public static class TimeStampViewHolder extends RecyclerView.ViewHolder {

        public TimeStampViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cardviewItem);
            info = (TextView)itemView.findViewById(R.id.cardviewInfo);
            listLayout = (LinearLayout)itemView.findViewById(R.id.timerlistlayout);
        }

        CardView cv;
        TextView info;
        LinearLayout listLayout;
    }

    private List<List<TimeStampItem>> timestamps;
    private Context context;
    private DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
    private DateTimeFormatter timedtf = DateTimeFormat.forPattern("HH:mm");
}
