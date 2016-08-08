package meet.you;

import meet.you.data.MeetData;
import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class MeetListAdapter extends CursorAdapter {

	public static String [] DATA_COL = new String [] {MeetData.KEY_ID,MeetData.KEY_TOPIC, MeetData.KEY_WHEN,
		MeetData.KEY_WHERE};
	
	private final int INDEX_ID = 0;
	private final int INDEX_TOPIC = 1;
	private final int INDEX_WHEN = 2;
	private final int INDEX_WHERE = 3;
	
	public MeetListAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		View v = View.inflate(context, R.layout.meet_list_item_lyt,null);
		return v;
	}

	@Override
	public void bindView(View panel, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		
		TextView tvTopic = (TextView) panel.findViewById(R.id.topic);
		TextView tvDate = (TextView) panel.findViewById(R.id.date);
		TextView tvLoc = (TextView) panel.findViewById(R.id.location);
		
		tvTopic.setText(cursor.getString(INDEX_TOPIC));
		tvLoc.setText(cursor.getString(INDEX_WHERE)); 
		
		long timeMillis = cursor.getLong(INDEX_WHEN);
		String time = DateUtils.formatDateTime(context,timeMillis,DateUtils.FORMAT_SHOW_TIME);
		
		tvDate.setText(time);
		
		//
		int id = cursor.getInt(INDEX_ID);
		panel.setTag(id);
		
	}

}
