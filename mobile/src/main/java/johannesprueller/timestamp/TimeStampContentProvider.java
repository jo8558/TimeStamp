package johannesprueller.timestamp;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;

public class TimeStampContentProvider extends ContentProvider {

    static final String PROVIDER_NAME = "at.jo.provider.TimeStamp";
    static final String URL = "content://" + PROVIDER_NAME + "/timestamp";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String _ID = "_id";
    static final String START = "startTime";
    static final String END = "endTime";

    private static HashMap<String, String> TIMESTAMP_PROJECTION_MAP;

    static final int TIMESTAMP = 1;

    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "timestamp", TIMESTAMP);
    }

    @Override
    public boolean onCreate() {
        context = getContext();
        if (context != null) {
            resolver = context.getContentResolver();
        }
        TimeStampDatabaseHelper dbHelper = new TimeStampDatabaseHelper(context);

        db = dbHelper.getWritableDatabase();
        return (db != null);

    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(TIMESTAMP_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case TIMESTAMP:
                queryBuilder.setProjectionMap(TIMESTAMP_PROJECTION_MAP);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if ((sortOrder == null) || sortOrder.equals("")) {
            sortOrder = START;
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        if (resolver != null) {
            cursor.setNotificationUri(resolver, uri);
            return cursor;
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case TIMESTAMP:
                return "vnd.android.cursor.dir/vnd.timestamp";
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        long rowID = 0;
        try {
            rowID = db.insertOrThrow(TIMESTAMP_TABLE_NAME, "", values);
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        }

        if (rowID > 0) {
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI, rowID);

            if (resolver != null) {
                resolver.notifyChange(newUri, null);
                return newUri;
            }
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int count;

        switch (uriMatcher.match(uri)) {
            case TIMESTAMP:
                count = db.delete(TIMESTAMP_TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (resolver != null) {
            resolver.notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count;

        switch (uriMatcher.match(uri)) {
            case TIMESTAMP:
                count = db.update(TIMESTAMP_TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI ", null);
        }

        if (resolver != null) {
            resolver.notifyChange(uri, null);
        }

        return count;
    }

    private Context context;
    private ContentResolver resolver;

    private SQLiteDatabase db;
    static final String DATABASE_NAME = "TimeStamp";
    static final String TIMESTAMP_TABLE_NAME = "Timestamp";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE = "CREATE TABLE " + TIMESTAMP_TABLE_NAME +
            "( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " startTime TEXT, " +
            " endTime TEXT);";

    private static class TimeStampDatabaseHelper extends SQLiteOpenHelper {

        public TimeStampDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            int upgradeTo = oldVersion + 1;
//            while (upgradeTo <= newVersion) {
//                switch (upgradeTo) {
//                    // TODO: 23.12.2015 implement case for each new updateversion! source: http://blog.adamsbros.org/2012/02/28/upgrade-android-sqlite-database/
//                }
//            }
        }
    }
}
