package meet.you.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import meet.you.data.MeetData.Meet;
import meet.you.data.MeetData.XmlItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.os.Environment;
import android.util.Log;
import android.util.Xml;

public class MeetUtil {

	private final static String TAG = "MeetUtil";

	public static String genMeetFile(Meet meet) {

		if (meet == null) {
			Log.e(TAG, "meet is null");
			return "";
		}

		File dataDir = Environment.getExternalStorageDirectory();
		if (!dataDir.exists()) {
			Log.e(TAG, "ExternalStorageDirectory not exists");
			return "";
		}

		File meetFile = null;

		try {
			meetFile = new File(dataDir, System.currentTimeMillis() + MeetData.MET_SUFIX);
			meetFile.createNewFile();

			final FileOutputStream fstr = new FileOutputStream(meetFile);
			final BufferedOutputStream str = new BufferedOutputStream(fstr);

			final XmlSerializer serializer = Xml.newSerializer();
			serializer.setOutput(str, "utf-8");
			serializer.startDocument(null, true);
			serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

			serializer.startTag(null, XmlItem.TAG_MEET);

			serializer.attribute(null, XmlItem.ATTR_TOPIC, meet.topic);
			serializer.attribute(null, XmlItem.ATTR_LOCATION, meet.location);
			serializer.attribute(null, XmlItem.ATTR_PRE_TIME, String.valueOf(meet.preMinute));
			serializer.attribute(null, XmlItem.ATTR_WHEN, String.valueOf(meet.dateMillis));
			serializer.attribute(null, XmlItem.ATTR_LAST_TIME, String.valueOf(meet.lastTime));

			serializer.endTag(null, XmlItem.TAG_MEET);

			serializer.endDocument();

			str.flush();
			fstr.getFD().sync();

			fstr.close();
			str.close();

			// New settings successfully written, old ones are no longer
			// needed.
			return meetFile.getAbsolutePath();
		} catch (java.io.IOException e) {
			Log.e(TAG, "generate meet file failed !!!", e);
		}

		return "";
	}

	public static Meet readMeetFromFile(String path) {

		Log.v (TAG, "path=" + path);
		
		Meet meet = null;
		FileInputStream fis = null;

		try {

			File meetFile = new File(path);

			if (!meetFile.exists()) {
				Log.e(TAG, "invaild meet file path");
				return null;
			}

			fis = new FileInputStream(meetFile);

			final XmlPullParser xmlParser = Xml.newPullParser();
			xmlParser.setInput(fis, null);

			int type = xmlParser.next();
			while (type != XmlPullParser.END_TAG && type != XmlPullParser.END_DOCUMENT) {

				String tagName = xmlParser.getName();
				if (XmlItem.TAG_MEET.equals(tagName)) {

					meet = new Meet();

					meet.topic = xmlParser.getAttributeValue(null, XmlItem.ATTR_TOPIC);
					meet.location = xmlParser.getAttributeValue(null, XmlItem.ATTR_LOCATION);
					String lastTimeString=xmlParser.getAttributeValue(null, XmlItem.ATTR_LAST_TIME);
					if (lastTimeString!=null&&lastTimeString.length()!=0) {
						meet.lastTime=Float.parseFloat(lastTimeString);
					}		
					String when = xmlParser.getAttributeValue(null, XmlItem.ATTR_WHEN);
					meet.dateMillis = Long.parseLong(when);

					break;
				}
			}

			fis.close();

		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}

		return meet;
	}
}
