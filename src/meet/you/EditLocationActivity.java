package meet.you;

import java.util.ArrayList;

import meet.you.data.MeetData;
import meet.you.data.MeetProvider;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class EditLocationActivity extends AppCompatActivity {

	private ArrayList<String> mLocationList;
	private ArrayList<Integer> mLocationIdList = new ArrayList<Integer>();

	private SQLiteDatabase db;

	private ListView mLocationListView;

	private EditLocationActivity activity;
	
	private String newRoomName=null;
	
	private Intent intent;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_location_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_location_toolbar);
        setSupportActionBar(toolbar);
		
		intent=getIntent();
		intent.putExtra("rtnIsAdd", false);
		  this.setResult(0, intent);
		activity = this;
		mLocationListView = (ListView) findViewById(R.id.locationListView);
		mLocationList = new ArrayList<String>();
		initListView();
		
		//auto create new location
		if (intent.getAction()=="android.intent.action.ADDROOM") {
			addDialog(EditLocationActivity.this);
		}
		
	}

	public void initListView() {
		Cursor cursor = null;
		try {
			// read the database
			mLocationList.clear();
			mLocationIdList.clear();
			String as[] = new String[2];
			as[0] = MeetProvider.ID;
			as[1] = MeetProvider.KEY_WHERE;

			cursor = getContentResolver()
					.query(MeetProvider.CONTENT_ROOM_URI,
							as,
							null,
							null,
							null);
			if (cursor != null && cursor.moveToFirst()) {

				int locationIndex = cursor.getColumnIndex(MeetProvider.KEY_WHERE);
				int idIndex = cursor.getColumnIndex(MeetProvider.ID);

				do {
					int id = cursor.getInt(idIndex);
					String locationTemp = cursor.getString(locationIndex);

					mLocationList.add(locationTemp);
					mLocationIdList.add(id);

				} while (cursor.moveToNext());

				if (cursor != null) {
					cursor.close();
				}
			}
		} catch (Exception e) {
			if (cursor != null) {
				cursor.close();
			}
			e.printStackTrace();
		}

		// set listview
		EditLocationAdapter mEditLocationAdapter = new EditLocationAdapter(this,
				activity, mLocationList);
		mLocationListView.setAdapter(mEditLocationAdapter);
	}
	
	public Boolean insertItem(String location) {
		boolean ITEMEXIST=false;
		for (int i = 0; i < mLocationList.size(); i++) {
			if (mLocationList.get(i).equals(location)) {
				ITEMEXIST=true;
			}
		}
		if (!ITEMEXIST) {
			ContentValues cValues = new ContentValues();
			cValues.put(MeetProvider.KEY_WHERE,
					location);
			getContentResolver().insert(
					MeetProvider.CONTENT_ROOM_URI,
					cValues);

			intent.putExtra("rtnIsAdd", true);
			this.setResult(0,intent);
			mLocationIdList.clear();
			mLocationList.clear();
		}
		
		return !ITEMEXIST;
	}
	
	public void deleteDialog(final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				this);
		builder.setMessage("确认删除？");
		builder.setPositiveButton("确认", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog,
					int which) {
				//to delete the item
				int id  = mLocationIdList.get(position);
				
				ContentResolver cResolver = getContentResolver();	
				Uri uri = Uri.withAppendedPath(MeetProvider.CONTENT_ROOM_URI, String.valueOf(id));
				
				cResolver.delete(uri, null, null);
				initListView();
			}
		});
		
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,
					int which) {
				dialog.cancel();
			}
		});

		builder.create().show();
	}

	private void addDialog(Context context) {
		LayoutInflater inflater = LayoutInflater.from(this);
		final View textEntryView = inflater.inflate(
				R.layout.add_dialog, null);
		final EditText edtInput = (EditText) textEntryView
				.findViewById(R.id.addRoomText);
		final AlertDialog.Builder builder = new AlertDialog.Builder(
				context);
		builder.setCancelable(false);
		builder.setMessage("添加会议地点：");
		builder.setView(textEntryView);
		builder.setPositiveButton(
				"确认",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						// 判断输入是否为空
						if (edtInput.getText()
								.toString()
								.equals("")) {
							Toast.makeText(getApplicationContext(),
									"please enter a location",
									Toast.LENGTH_SHORT)
									.show();
							addDialog(EditLocationActivity.this);
							return;
						}
						//插入数据库
						if (!insertItem(edtInput.getText().toString())) {
							Toast.makeText(getApplicationContext(),
									"该会议地点已存在",
									Toast.LENGTH_SHORT)
									.show();
							addDialog(EditLocationActivity.this);
							return;
						}else {
							initListView();
						}
				
						
					}
				});
		builder.setNegativeButton("取消", null);
		builder.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar
		// if it is present.
		getMenuInflater().inflate(R.menu.edit_location_menu,
				menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		final int menuId = item.getItemId();
		switch (menuId) {
		case R.id.add_location:
			// todo
			addDialog(EditLocationActivity.this);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	

}
