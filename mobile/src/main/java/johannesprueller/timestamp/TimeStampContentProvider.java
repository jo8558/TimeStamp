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

import java.util.HashMap;

public class TimeStampContentProvider extends ContentProvider {

    static final String PROVIDER_NAME = "at.jo.provider.TimeStamp";
    static final String TIMESTAMP_URL = "content://" + PROVIDER_NAME + "/timestamp";
    static final String NFC_URL = "content://" + PROVIDER_NAME + "/nfc";
    static final Uri CONTENT_TIMESTAMP_URI = Uri.parse(TIMESTAMP_URL);
    static final Uri CONTENT_NFC_URI = Uri.parse(NFC_URL);

    static final String _ID = "_id";
    static final String START = "startTime";
    static final String END = "endTime";

    static final String NFCUID = "nfcUid";

    private static HashMap<String, String> TIMESTAMP_PROJECTION_MAP;

    static final int TIMESTAMP = 1;
    static final int NFC = 2;

    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "timestamp", TIMESTAMP);
        uriMatcher.addURI(PROVIDER_NAME, "nfc", NFC);
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
        queryBuilder.setDistinct(true);
        switch (uriMatcher.match(uri))
        {
            case TIMESTAMP:
                queryBuilder.setTables(TIMESTAMP_TABLE_NAME);
                if ((sortOrder == null) || sortOrder.equals("")) {
                    sortOrder = START;
                }
                break;
            case NFC:
                queryBuilder.setTables(NFC_TABLE_NAME);
                break;
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
            case NFC:
                return "vnd.android.cursor.dir/vnd.nfc";
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        long rowID = 0;
        try {
            switch (uriMatcher.match(uri))
            {
                case TIMESTAMP:
                    rowID = db.insertOrThrow(TIMESTAMP_TABLE_NAME, "", values);
                    break;
                case NFC:
                    rowID = db.insertOrThrow(NFC_TABLE_NAME, "", values);
                    break;
            }

        } catch (android.database.SQLException e) {
            e.printStackTrace();
        }

        if (rowID > 0) {
            Uri newUri = ContentUris.withAppendedId(CONTENT_TIMESTAMP_URI, rowID);

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
            case NFC:
                count = db.delete(NFC_TABLE_NAME, selection, selectionArgs);
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
            case NFC:
                count = db.update(NFC_TABLE_NAME, values, selection, selectionArgs);
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
    static final String NFC_TABLE_NAME = "NFCTAG";
    static final int DATABASE_VERSION = 2;
    static final String CREATE_DB_TABLE = "CREATE TABLE " + TIMESTAMP_TABLE_NAME +
            "( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " startTime TEXT, " +
            " endTime TEXT);";
    static final String CREATE_NFC_TABLE = "CREATE TABLE " + NFC_TABLE_NAME +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " nfcUid TEXT);";

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
            while (upgradeTo <= newVersion) {
                switch (upgradeTo) {
                    case 2:
                        db.execSQL(CREATE_NFC_TABLE);
                        break;
                }
                upgradeTo++;
            }
        }
    }
}
