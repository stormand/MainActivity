package meet.you.data;

import android.R.integer;
import android.net.Uri;

public interface MeetData {
	
	public static Uri URI = MeetProvider.CONTENT_URI;
	public static Uri URI_ID = MeetProvider.CONTENT_URI_ID;
	
	
	public static String KEY_ID = MeetProvider.ID;
	public static String KEY_TOPIC = MeetProvider.KEY_TOPIC;
	public static String KEY_WHEN = MeetProvider.KEY_DATE;
	public static String KEY_WHERE = MeetProvider.KEY_WHERE;
	public static String KEY_PRE_TIME = MeetProvider.KEY_PRE_TIME;
	public static String KEY_LAST_TIME = MeetProvider.KEY_LAST_TIME;
	
	public static String ACTION_MEET_VIEW = "meet.action.VIEW";
	public static String ACTION_MEET_EDIT = "meet.action.EDIT";
	
	public static  String EXTRA_FOCUS_DATE = "focus_day";
	
	public class Meet{
		public String topic;
		public String location;
		public long dateMillis;
		public int  preMinute;
		public float lastTime;
		public Uri dbUri;
	}
	
	public interface XmlItem{
		public static String TAG_MEET = "meet";
		public static String ATTR_TOPIC = "topic";
		public static String ATTR_WHEN = "when";
		public static String ATTR_LOCATION = "location";
		public static String ATTR_PRE_TIME = "pretime";
		public static String ATTR_LAST_TIME = "pretime";
	}
	

	public static final String MET_SUFIX = ".xml";
	
}
