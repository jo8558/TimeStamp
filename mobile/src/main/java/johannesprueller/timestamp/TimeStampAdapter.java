package johannesprueller.timestamp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TimeStampAdapter extends BaseAdapter{

    public TimeStampAdapter(Context context, List<TimeStampItem> itemList)
    {
        this.context = context;
        this.itemList = itemList;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.timestamp_itemview, null);

        TimeStampItem item = itemList.get(position);

        TextView startTime = (TextView)convertView.findViewById(R.id.startTime);
        startTime.setText(dtf.print(item.getStartTime()));

        TextView endTime = (TextView)convertView.findViewById(R.id.endTime);
        if(item.getStopTime() == null) {
            endTime.setText("--:--:--");
        }
        else
        {
            endTime.setText(dtf.print(item.getStopTime()));
        }

        return convertView;
    }

    private Context context;
    private LayoutInflater layoutInflater;
    private List<TimeStampItem> itemList;
    private DateTimeFormatter dtf = DateTimeFormat.forPattern("HH:mm:ss");
}
