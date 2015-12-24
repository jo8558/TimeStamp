package johannesprueller.timestamp;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AsyncTimeLoaderTask extends AsyncTask<Void, Void, List<TimeStampItem>> {

    public interface AsyncResponse{
        void processFinish(List<TimeStampItem> output);
    }

    public AsyncTimeLoaderTask(Context context, AsyncResponse delegate) {
        this.context = context;
        this.delegate = delegate;
    }

    @Override
    protected List<TimeStampItem> doInBackground(Void... params) {

        timeStampItemList = new ArrayList<>();

        String argument = filtersdf.format(new Date()) + "%";

        String[] args = new String[]{argument};
        Cursor cursor = context.getContentResolver()
                .query(TimeStampContentProvider.CONTENT_URI, null, TimeStampContentProvider.START + " LIKE ?", args, null);

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                TimeStampItem item = cursorToTimeStampItem(cursor);
                timeStampItemList.add(item);
                cursor.moveToNext();
            }

            return timeStampItemList;
        } else {
            throw new AssertionError();
        }
    }

    @Override
    protected void onPostExecute(List<TimeStampItem> timeStampItems) {
        delegate.processFinish(timeStampItems);
    }

    private TimeStampItem cursorToTimeStampItem(Cursor cursor) {
        TimeStampItem item = new TimeStampItem();
        try {
            item.setId(cursor.getInt(0));
            item.setStartTime(sdf.parse(cursor.getString(1)));

            String endTime = cursor.getString(2);
            if (!(endTime == null) && !(endTime.isEmpty())) {
                item.setStopTime(sdf.parse(endTime));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return item;
    }

    public AsyncResponse delegate = null;

    private Context context;
    private SimpleDateFormat filtersdf = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private List<TimeStampItem> timeStampItemList;
}
