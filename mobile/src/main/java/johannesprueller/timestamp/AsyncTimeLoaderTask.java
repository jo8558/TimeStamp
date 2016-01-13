package johannesprueller.timestamp;

import android.content.Context;
import android.os.AsyncTask;


import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class AsyncTimeLoaderTask extends AsyncTask<Void, Void, List<TimeStampItem>> {

    public interface AsyncResponse {
        void processFinish(List<TimeStampItem> output);
    }

    public AsyncTimeLoaderTask(Context context, AsyncResponse delegate) {
        this.context = context;
        this.delegate = delegate;
        this.dbHelper = new DatabaseHelper(context);
    }

    @Override
    protected List<TimeStampItem> doInBackground(Void... params) {
        timeStampItemList = new ArrayList<>();
        timeStampItemList = dbHelper.LoadTimeStampItemsForDate(DateTime.now());
        return timeStampItemList;
    }

    @Override
    protected void onPostExecute(List<TimeStampItem> timeStampItems) {
        delegate.processFinish(timeStampItems);
    }

    public AsyncResponse delegate = null;

    private Context context;

    private List<TimeStampItem> timeStampItemList;
    private DatabaseHelper dbHelper;
}
