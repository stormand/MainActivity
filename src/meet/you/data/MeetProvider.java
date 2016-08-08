package meet.you.data;

import java.util.HashMap;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class MeetProvider extends ContentProvider {

	private static final String DATABASE_NAME = "Meet";
	private static final String TABLE_NAME = "met";
	private static final String AUTHORITY = "meet.you.data.meetprovider";

	private static final int DATABASE_VERSION = 3;

	static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_NAME);
	static final Uri CONTENT_URI_ID = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_NAME + "/");

	private static final UriMatcher uriMatcher;

	static final String KEY_TOPIC = "meet_topic";
	static final String KEY_DATE = "meet_date";
	public static final String KEY_WHERE = "meet_room";
	static final String KEY_PRE_TIME = "meet_pre_time";
	static final String KEY_LAST_TIME = "last_time";

	public static final String TABLE_ROOM_NAME = "location";
	public static final Uri CONTENT_ROOM_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_ROOM_NAME);

	public static final String DESCRIPTION = "description";
	public static final String END = "end";
	public static final String ID = "_id";
	public static final String CALENDAR_ID = "calendar_id";
	public static final String EVENT_ID = "event_id";
	public static final String START_DAY = "start_day";
	public static final String END_DAY = "end_day";
	public static final String END_TIME = "end_time";
	public static final String START_TIME = "start_time";

	private static final HashMap<String, String> mMap;
	private static final HashMap<String, String> lMAP;
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;
	
	private boolean isCreateTable;

	private  class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null,
					DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			createTables(db);
			isCreateTable=true;
		}

		@Override
		public void onUpgrade(SQLiteDatabase db,
				int oldVersion, int newVersion) {
			if (oldVersion==1) {
				db.execSQL("CREATE TABLE "
						+ TABLE_ROOM_NAME
						+ "("
						+ ID
						+ " integer primary key autoincrement, "
						+ KEY_WHERE
						+ " TEXT);");
				isCreateTable=true;
			}
			db.execSQL("alter table "+TABLE_NAME+" add "+KEY_LAST_TIME+" FLOAT;");
		}

		private void createTables(SQLiteDatabase db) {

			db.execSQL("CREATE TABLE "
					+ TABLE_ROOM_NAME
					+ "("
					+ ID
					+ " integer primary key autoincrement, "
					+ KEY_WHERE
					+ " TEXT);");
			db.execSQL("CREATE TABLE "
					+ TABLE_NAME
					+ "("
					+ ID
					+ " integer primary key autoincrement, "
					+ KEY_TOPIC
					+ " TEXT, "
					+ KEY_WHERE
					+ " TEXT, "
					+ KEY_DATE
					+ " INTEGER, "
					+ KEY_PRE_TIME
					+ " INTEGER, "
					+ KEY_LAST_TIME
					+ " FLOAT);");


		}

	}

	@Override
	public boolean onCreate() {
		
		isCreateTable=false;
		Context context = getContext();
		DBHelper = new DatabaseHelper(context);
		db = DBHelper.getWritableDatabase();
		
		if (isCreateTable) {
			ContentValues cv = new ContentValues();
			
			cv.put(KEY_WHERE, "待定");
			insert(CONTENT_ROOM_URI, cv);
			cv.put(KEY_WHERE, "东海32楼");
			insert(CONTENT_ROOM_URI, cv);
			cv.put(KEY_WHERE, "东海33楼");
			insert(CONTENT_ROOM_URI, cv);
			cv.put(KEY_WHERE, "时代19楼");
			insert(CONTENT_ROOM_URI, cv);
			cv.put(KEY_WHERE, "时代21楼");
			insert(CONTENT_ROOM_URI, cv);
			cv.put(KEY_WHERE, "东海12楼");
			insert(CONTENT_ROOM_URI, cv);
			
		}

		return (db == null) ? false : true;
	}


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		
		int count = 0;
		String id = null;
		int match = uriMatcher.match(uri);
		
		switch (match) {
			case MATCH_TABLE:
				count = db.delete(TABLE_NAME, selection, selectionArgs);
				break;
			case MATCH_RECORD:
				id = uri.getPathSegments().get(1);
				count = db.delete(TABLE_NAME, ID + " = " + id
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
				break;
			case MATCH_ROOM_TABLE:
				count = db.delete(TABLE_ROOM_NAME, selection, selectionArgs);
				break;
			case MATCH_ROOM_RECORD:
				id = uri.getPathSegments().get(1);
				count = db.delete(TABLE_ROOM_NAME, ID + " = " + id
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
				break;
			default:
				break;
		}
		
		/*
		if (match == 1) {
			count = db.delete(MEET_TABLE, selection, selectionArgs);
		} else if (match == 2) {
			String id = uri.getPathSegments().get(1);
			count = db.delete(MEET_TABLE, ID + " = " + id
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
		}
		*/
		
		//getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		long rowID = 0;
		final int match = uriMatcher.match(uri);
		switch (match) {
		case MATCH_CONTENT:
		case MATCH_RECORD:
		case MATCH_TABLE:
			rowID = db.insert(TABLE_NAME, null, values);
			break;
		case MATCH_ROOM_CONTENT:
		case MATCH_ROOM_RECORD:
		case MATCH_ROOM_TABLE:
			rowID = db.insert(TABLE_ROOM_NAME, null,
					values);
			break;
		default:
			break;
		}

		// to edit
		Uri insertedUri = null;
		if (rowID > 0) {
			insertedUri = ContentUris.withAppendedId(
					CONTENT_URI_ID,
					rowID);
			// getContext().getContentResolver().notifyChange(insertedUri,
			// null);
		} else {
			throw new SQLException(
					"Failed to insert row into "
							+ insertedUri);
		}

		return insertedUri;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

		final int match = uriMatcher.match(uri);
		Cursor c = null;
		switch (match) {
		case MATCH_RECORD:
			sqlBuilder.appendWhere(ID + "=?");
			selectionArgs = DatabaseUtils
					.appendSelectionArgs(selectionArgs,
							new String[] { uri.getLastPathSegment() });
		case MATCH_TABLE:
			sqlBuilder.setTables(TABLE_NAME);
			sqlBuilder.setProjectionMap(mMap);

			if (TextUtils.isEmpty(sortOrder)) {
				sortOrder = KEY_DATE
						+ " COLLATE LOCALIZED ASC";
			}
			c = sqlBuilder.query(db, projection,
					selection,
					selectionArgs,
					null, null,
					sortOrder);
			break;
		case MATCH_ROOM_RECORD:
			sqlBuilder.appendWhere(ID + "=?");
			selectionArgs = DatabaseUtils
					.appendSelectionArgs(selectionArgs,
							new String[] { uri.getLastPathSegment() });
		case MATCH_ROOM_TABLE:
			sqlBuilder.setTables(TABLE_ROOM_NAME);
			sqlBuilder.setProjectionMap(lMAP);
			if (TextUtils.isEmpty(sortOrder)) {
				sortOrder = ID
						+ " COLLATE LOCALIZED ASC";
			}
			c = sqlBuilder.query(db, projection,
					selection,
					selectionArgs,
					null, null,
					sortOrder);
			break;
		default:
			break;
		}

		/*
		 * if (uriMatcher.match(uri) == 1) {
		 * sqlBuilder.setProjectionMap(mMap); } else if
		 * (uriMatcher.match(uri) == 2) {
		 * sqlBuilder.setProjectionMap(mMap);
		 * sqlBuilder.appendWhere(ID + "=?"); selectionArgs =
		 * DatabaseUtils.appendSelectionArgs(selectionArgs, new
		 * String[] {uri.getLastPathSegment()}); } else if
		 * (uriMatcher.match(uri) == 3) {
		 * sqlBuilder.setProjectionMap(mMap);
		 * sqlBuilder.appendWhere(WHEN + ">=? OR ");
		 * sqlBuilder.appendWhere(END + "<=?"); List<String>
		 * list = uri.getPathSegments(); String start =
		 * list.get(1); String end = list.get(2); selectionArgs
		 * = DatabaseUtils.appendSelectionArgs(selectionArgs,
		 * new String[] {start, end}); }
		 */

		// c.setNotificationUri(getContext().getContentResolver(),
		// uri);

		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		int count = 0;
		int match = uriMatcher.match(uri);

		switch (match) {
		case MATCH_TABLE:
			count = db.update(TABLE_NAME, values,
					selection,
					selectionArgs);
			break;
		case MATCH_RECORD:
			count = db.update(TABLE_NAME,
					values,
					ID
							+ " = "
							+ uri.getPathSegments()
									.get(1)
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection
									+ ')'
									: ""),
					selectionArgs);
			break;
		default:
			break;
		}

		/*
		 * if (match == 1) { count = db.update(MEET_TABLE,
		 * values, selection, selectionArgs); } else if (match
		 * == 2) { count = db.update(MEET_TABLE, values, ID +
		 * " = " + uri.getPathSegments().get(1) +
		 * (!TextUtils.isEmpty(selection) ? " AND (" + selection
		 * + ')' : ""), selectionArgs); } else { throw new
		 * IllegalArgumentException("Unknown URI " + uri); }
		 */

		// getContext().getContentResolver().notifyChange(uri,
		// null);
		return count;
	}

	private static final int MATCH_TABLE = 1;
	private static final int MATCH_RECORD = 2;
	private static final int MATCH_CONTENT = 3;
	private static final int MATCH_ROOM_TABLE = 4;
	private static final int MATCH_ROOM_RECORD = 5;
	private static final int MATCH_ROOM_CONTENT = 6;

	static {

		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, TABLE_NAME, MATCH_TABLE);
		uriMatcher.addURI(AUTHORITY, TABLE_NAME + "/#",
				MATCH_RECORD);
		uriMatcher.addURI(AUTHORITY, TABLE_NAME + "/#/#",
				MATCH_CONTENT);
		uriMatcher.addURI(AUTHORITY, TABLE_ROOM_NAME,
				MATCH_ROOM_TABLE);
		uriMatcher.addURI(AUTHORITY, TABLE_ROOM_NAME + "/#",
				MATCH_ROOM_RECORD);
		uriMatcher.addURI(AUTHORITY, TABLE_ROOM_NAME + "/#/#",
				MATCH_ROOM_CONTENT);

		mMap = new HashMap<String, String>();

		mMap.put(ID, ID);
		mMap.put(KEY_TOPIC, KEY_TOPIC);
		mMap.put(KEY_DATE, KEY_DATE);
		mMap.put(KEY_WHERE, KEY_WHERE);
		mMap.put(KEY_PRE_TIME, KEY_PRE_TIME);
		mMap.put(KEY_LAST_TIME, KEY_LAST_TIME);

		lMAP = new HashMap<String, String>();

		lMAP.put(ID, ID);
		lMAP.put(KEY_WHERE, KEY_WHERE);

		/*
		 * mMap.put(DESCRIPTION, DESCRIPTION); mMap.put(END,
		 * END); mMap.put(CALENDAR_ID, CALENDAR_ID);
		 * mMap.put(EVENT_ID, EVENT_ID); mMap.put(START_DAY,
		 * START_DAY); mMap.put(END_DAY, END_DAY);
		 * mMap.put(START_TIME, START_TIME); mMap.put(END_TIME,
		 * END_TIME);
		 */
	}

}
