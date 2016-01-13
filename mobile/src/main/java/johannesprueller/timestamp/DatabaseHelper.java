package johannesprueller.timestamp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    public DatabaseHelper(Context context) {
        this.context = context;
    }

    public void SaveStartTime(TimeStampItem currentItem) {
        ContentValues values = new ContentValues();
        values.put(TimeStampContentProvider.START, dtf.print(currentItem.getStartTime()));

        Uri uri = context.getContentResolver().insert(TimeStampContentProvider.CONTENT_TIMESTAMP_URI, values);
        if (uri != null) {
            String id = uri.getPathSegments().get(1);
            currentItem.setId(Integer.parseInt(id));
        }
    }

    public void SaveEndTime(TimeStampItem currentItem) {
        ContentValues values = new ContentValues();
        values.put(TimeStampContentProvider.END, dtf.print(currentItem.getStopTime()));

        String[] args = new String[]{String.valueOf(currentItem.getId())};
        int result = context.getContentResolver().update(TimeStampContentProvider.CONTENT_TIMESTAMP_URI, values, "_id=?", args);
        if (result != 1) {
            Toast.makeText(context, R.string.updateDatabaseError, Toast.LENGTH_SHORT).show();
        }
    }

    public DBResult SaveTag(String tag) {
        List<String> existingTags = LoadNfcTags();

        if (existingTags.contains(tag)) {
            return DBResult.AlreadyExisiting;
        }

        ContentValues values = new ContentValues();
        values.put(TimeStampContentProvider.NFCUID, tag);

        Uri uri = context.getContentResolver().insert(TimeStampContentProvider.CONTENT_NFC_URI, values);
        return uri != null ? DBResult.Successful : DBResult.Failed;
    }

    public List<String> LoadNfcTags() {
        List<String> nfcTagList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(TimeStampContentProvider.CONTENT_NFC_URI, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                nfcTagList.add(cursor.getString(1));
                cursor.moveToNext();
            }
            cursor.close();
        }

        return nfcTagList;
    }

    public List<TimeStampItem> LoadTimeStampItemsForDate(DateTime dateTime) {
        List<TimeStampItem> timeStampItemList = new ArrayList<>();

        String argument = filterdtf.print(dateTime) + "%";

        String[] args = new String[]{argument};
        Cursor cursor = context.getContentResolver()
                .query(TimeStampContentProvider.CONTENT_TIMESTAMP_URI, null, TimeStampContentProvider.START + " LIKE ?", args, null);

        HandleTimeStampCursor(cursor, timeStampItemList);

        return timeStampItemList;
    }

    public List<List<TimeStampItem>> LoadTimeStampItems() {
        List<TimeStampItem> items = new ArrayList<>();
        List<DateTime> dates = new ArrayList<>();
        String[] columns = new String[]{"date("+TimeStampContentProvider.START+")"};
        Cursor cursor = context.getContentResolver().query(TimeStampContentProvider.CONTENT_TIMESTAMP_URI, columns, null, null, TimeStampContentProvider.START + " DESC");
        if(cursor != null)
        {
            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                dates.add(filterdtf.parseDateTime(cursor.getString(0)));
                cursor.moveToNext();
            }
            cursor.close();
        }

        List<List<TimeStampItem>> dateItems = new ArrayList<>();

        for (DateTime date : dates) {
            List<TimeStampItem> timeStamps = LoadTimeStampItemsForDate(date);
            dateItems.add(timeStamps);
        }

        return dateItems;
    }

    private void HandleTimeStampCursor(Cursor cursor, List<TimeStampItem> timeStampItemList) {
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                TimeStampItem item = new TimeStampItem();
                item.setId(cursor.getInt(0));
                item.setStartTime(dtf.parseDateTime(cursor.getString(1)));

                String endTime = cursor.getString(2);
                if (!(endTime == null) && !(endTime.isEmpty())) {
                    item.setStopTime(dtf.parseDateTime(endTime));
                }

                timeStampItemList.add(item);
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    private Context context;
    private DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private DateTimeFormatter filterdtf = DateTimeFormat.forPattern("yyyy-MM-dd");
}
