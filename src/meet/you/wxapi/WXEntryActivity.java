package meet.you.wxapi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import meet.you.data.Constants;
import meet.you.data.MeetData;
import meet.you.data.MeetProvider;
import meet.you.data.MeetUtil;
import meet.you.data.Util;
import meet.you.data.MeetData.Meet;
import android.R.integer;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Layout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import meet.you.EditLocationActivity;
import meet.you.MeetRemindReceiver;
import meet.you.R;

import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.ShowMessageFromWX;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXAppExtendObject;
import com.tencent.mm.sdk.openapi.WXFileObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;

public class WXEntryActivity extends AppCompatActivity implements IWXAPIEventHandler,
		OnClickListener {

	// add by allen liu
	private Button mCreateBtn;

	private EditText mTopic;
	private Spinner mLocation;
	private Button mLocationEditBtnButton;
	private Button mDateSelBtn;
	private Button mTimeSelBtn;
	private Spinner mRemindSpin;
	private Spinner mLastDaySpin;
	private Spinner mLastTimeSpin;
	
	private TextView mTopicTextView;
	private TextView mContextTextView;

	private ArrayAdapter<String> mLocationAdapter;
	private ArrayList<String> mAddrs;

	private GregorianCalendar mDate;

	private final String TAG = "MeetEdit";

	private Meet mOrigMeet;
	private Meet mNewMeet;

	private IWXAPI mWXapi;

	private boolean isItemExist;
	private boolean isTextChanged;//监听修改
	private boolean isAddMeet; // 区分是添加还是查看
	private boolean isLocationExist;

	private int mMeetRoomInt = 0;
	private int mPreTimeInt = 0;
	private int mLastDayPos = 0;
	private int mLastTimePos = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.new_meet);

		Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar_new_meet);
		setSupportActionBar(toolbar);
		
//		//设置actionbar
//		ActionBar actionBar=getActionBar();
//		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//		actionBar.setDisplayShowCustomEnabled(true);
//		LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		mActionBarView= inflator.inflate(R.layout.actionbar_new_meeting, null);
//		actionBar.setCustomView(mActionBarView);
//		//添加监听
		
		mWXapi = WXAPIFactory.createWXAPI(this,
				Constants.APP_ID);
		mWXapi.registerApp(Constants.APP_ID);    
		
		mTopicTextView=(TextView) findViewById(R.id.topicTextView);
		mContextTextView=(TextView)findViewById(R.id.contextTextView);

		mTopic = (EditText) findViewById(R.id.topic);
		mTopic.selectAll();

		mLocation = (Spinner) findViewById(R.id.location);
		mLocation.setSelection(0);

		mLocationEditBtnButton = (Button) findViewById(R.id.editLocation);
		mLocationEditBtnButton.setOnClickListener(this);

		mDateSelBtn = (Button) findViewById(R.id.date);
		mDateSelBtn.setOnClickListener(this);

		mTimeSelBtn = (Button) findViewById(R.id.time);
		mTimeSelBtn.setOnClickListener(this);

		mRemindSpin = (Spinner) findViewById(R.id.remind_time);
		mRemindSpin.setSelection(0);

		mLastDaySpin = (Spinner) findViewById(R.id.end_date);
		mLastDaySpin.setSelection(0);

		mLastTimeSpin = (Spinner) findViewById(R.id.end_time);
		mLastTimeSpin.setSelection(0);

		initLocationSpinner();

		isAddMeet = false;
		isTextChanged = false;
		isLocationExist=true;

        OnClickListener toolbarOnclickListener =new OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.newMeetSaveBtn:
                    case R.id.newMeetSaveRightBtn:
                        mOrigMeet = saveAsFile();
                        saveInDB(mOrigMeet);
                        WXEntryActivity.this.finish();
                        break;
                    case R.id.NewMeetSendBtn:
                        mNewMeet = saveAsFile();
                        if (mNewMeet != null) {
                            sendToWeixin(mNewMeet);
                        }
                        break;
                    case R.id.newMeetDeleteBtn:
                        deleteMeet();
                        break;
                    default:
                        break;
                }
            }
        };
        toolbar.findViewById(R.id.newMeetDeleteBtn).setOnClickListener(toolbarOnclickListener);
        toolbar.findViewById(R.id.newMeetSaveBtn).setOnClickListener(toolbarOnclickListener);
        toolbar.findViewById(R.id.NewMeetSendBtn).setOnClickListener(toolbarOnclickListener);
        toolbar.findViewById(R.id.newMeetSaveRightBtn).setOnClickListener(toolbarOnclickListener);

		String action = getIntent().getAction();
		if (MeetData.ACTION_MEET_VIEW.equals(action)) {
			// may be we should check pre-remind
			// minute,any way,no one cares,set it as
			// default
			Uri data = getIntent().getData();
			initByMeetRecord(data);
            toolbar.findViewById(R.id.newMeetSaveBtn).setVisibility(View.GONE);
            toolbar.findViewById(R.id.newMeetSaveRightBtn).setVisibility(View.GONE);
		} else {
			Uri data = getIntent().getData();
			if (data != null) {
				initByMeetFile(data);
                toolbar.findViewById(R.id.newMeetDeleteBtn).setVisibility(View.GONE);
                toolbar.findViewById(R.id.newMeetSaveBtn).setVisibility(View.GONE);
                toolbar.findViewById(R.id.NewMeetSendBtn).setVisibility(View.GONE);
			} else {
				initDefault();
                toolbar.findViewById(R.id.newMeetDeleteBtn).setVisibility(View.GONE);
                toolbar.findViewById(R.id.newMeetSaveRightBtn).setVisibility(View.GONE);
			}
		}
		initChangeListener();
		mWXapi.handleIntent(getIntent(), this);

	}

	private void initChangeListener() {

		mTopic.addTextChangedListener(mTextWatcher);
		mDateSelBtn.addTextChangedListener(mTextWatcher);
		mTimeSelBtn.addTextChangedListener(mTextWatcher);
		mLocation.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(
					AdapterView<?> parent,
					View view,
					int position,
					long id) {
				// TODO Auto-generated method
				// stub
				if (position != mMeetRoomInt) {
					isTextChanged = true;

				}
			}

			@Override
			public void onNothingSelected(
					AdapterView<?> parent) {
				// TODO Auto-generated method
				// stub

			}
		});
		mRemindSpin.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(
					AdapterView<?> parent,
					View view,
					int position,
					long id) {
				// TODO Auto-generated method
				// stub
				if (position != mPreTimeInt) {
					isTextChanged = true;

				}
			}

			@Override
			public void onNothingSelected(
					AdapterView<?> parent) {
				// TODO Auto-generated method
				// stub

			}
		});
		mLastDaySpin.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(
					AdapterView<?> parent,
					View view,
					int position,
					long id) {
				// TODO Auto-generated method
				// stub
				if (position != mLastDayPos) {
					isTextChanged = true;
					mLastDayPos=position;
				}
			}

			@Override
			public void onNothingSelected(
					AdapterView<?> parent) {
				// TODO Auto-generated method
				// stub

			}
		});
		mLastTimeSpin.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(
					AdapterView<?> parent,
					View view,
					int position,
					long id) {
				// TODO Auto-generated method
				// stub
				if (position != mLastTimePos) {
					isTextChanged = true;
					mLastTimePos=position;
				}
			}

			@Override
			public void onNothingSelected(
					AdapterView<?> parent) {
				// TODO Auto-generated method
				// stub

			}
		});
	}

	TextWatcher mTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start,
				int before, int count) {
			// TODO Auto-generated method stub
			isTextChanged = true;

		}

		@Override
		public void beforeTextChanged(CharSequence s,
				int start, int count, int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			isTextChanged = true;

		}
	};

	private void initLocationSpinner() {
		String as[] = new String[1];
		as[0] = "meet_room";
		mAddrs = new ArrayList<String>();
		Cursor cursorAddrs = getContentResolver().query(
				MeetProvider.CONTENT_ROOM_URI,
				as, null, null, null);
		while (cursorAddrs != null && cursorAddrs.moveToNext()) {
			mAddrs.add(cursorAddrs.getString(cursorAddrs
					.getColumnIndex(as[0])));
		}
		mLocationAdapter = new ArrayAdapter<String>(
				this,
				R.layout.location_spinner_item,
				mAddrs);
		mLocationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mLocation.setAdapter(mLocationAdapter);
	}

	private void initLastDaySpinner() {

		ArrayList<String> lastDay = new ArrayList<String>();

		int flags = DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_SHOW_WEEKDAY;
		String dateLabel;

		for (int i = 0; i < 6; i++) {
			dateLabel = DateUtils.formatDateTime(
					this,
					mDate.getTimeInMillis()
							+ 86400000
							* i,
					flags);
			lastDay.add(dateLabel);
		}
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				this,
				android.R.layout.simple_spinner_item,
				lastDay);
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mLastDaySpin.setAdapter(arrayAdapter);
	}

	private void initLastTimeSpinner() {

		ArrayList<String> lastTime = new ArrayList<String>();

		String timeLabel;
		for (int i = 0; i < 14; i++) {
			timeLabel = DateUtils.formatDateTime(
					this,
					mDate.getTimeInMillis()
							+ 1800000
							* (i + 1),
					DateUtils.FORMAT_SHOW_TIME);
			lastTime.add(timeLabel + "  (" + 0.5
					* (i + 1) + "小时)");
		}

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				this,
				android.R.layout.simple_spinner_item,
				lastTime);
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mLastTimeSpin.setAdapter(arrayAdapter);
		mLastTimeSpin.setSelection(1);
	}

	private void initByMeetRecord(Uri uri) {
		// TODO Auto-generated method stub
		
		mTopicTextView.setVisibility(View.GONE);
		mContextTextView.setVisibility(View.GONE);
		
		String as[] = new String[5];
		as[0] = "meet_topic";
		as[1] = "meet_date";
		as[2] = "meet_room";
		as[3] = "meet_pre_time";
		as[4] = "last_time";
		Cursor cursor = getContentResolver().query(uri, as,
				null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			int topicIndex = cursor.getColumnIndex("meet_topic");
			int dateIndex = cursor.getColumnIndex("meet_date");
			int roomIndex = cursor.getColumnIndex("meet_room");
			int preTimeIndex = cursor
					.getColumnIndex("meet_pre_time");
			int lastTimeIndex = cursor
					.getColumnIndex("last_time");
			// topic
			mTopic.setText(cursor.getString(topicIndex));
			// location
			mMeetRoomInt = findLocation(cursor
					.getString(roomIndex));
			mLocation.setSelection(mMeetRoomInt);
			// pre-time
			mPreTimeInt = findPreTime(cursor
					.getString(preTimeIndex));
			mRemindSpin.setSelection(mPreTimeInt);
			// date and time
			mDate = new GregorianCalendar();
			mDate.setTimeInMillis(cursor
					.getLong(dateIndex));

			int flags = DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_SHOW_WEEKDAY;
			String dateLabel = DateUtils
					.formatDateTime(this,
							mDate.getTimeInMillis(),
							flags);
			mDateSelBtn.setText(dateLabel);

			// incre one hour from now
			String timeLabel = DateUtils
					.formatDateTime(this,
							mDate.getTimeInMillis(),
							DateUtils.FORMAT_SHOW_TIME);
			mTimeSelBtn.setText(timeLabel);

			// last time
			initLastDaySpinner();
			initLastTimeSpinner();
			float lastTime = cursor.getFloat(lastTimeIndex);
			mLastDayPos = (int) lastTime / 24;
			mLastTimePos = (int) ((lastTime - 24 * mLastDayPos) * 2);
			mLastDaySpin.setSelection(mLastDayPos);
			mLastTimeSpin.setSelection(mLastTimePos);

			mOrigMeet = saveAsFile();
			saveInDB(mOrigMeet);
			mOrigMeet.dbUri = uri;

			if (cursor != null) {
				cursor.close();
			}

		}
		initChangeListener();

	}

	/**
	 * if has data,it may be in edit or view mode
	 * 
	 * @param data
	 */
	private void initByMeetFile(Uri data) {
		Meet meetReq = MeetUtil.readMeetFromFile(data.getPath());

		if (meetReq == null) {
			showFailDlg();
			return;
		}
		hideAll();
	
		//topic
		mTopicTextView.setText("主题："+meetReq.topic);
		mTopic.setText(meetReq.topic);
		
		//start date & time
		mDate = new GregorianCalendar();
		mDate.setTimeInMillis(meetReq.dateMillis);
		int flags = DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_SHOW_WEEKDAY;
		String dateLabel = DateUtils.formatDateTime(this,
				mDate.getTimeInMillis(), flags);
		
		final int hour = mDate.get(Calendar.HOUR_OF_DAY);
		mDate.set(Calendar.HOUR_OF_DAY, hour);
		String timeLabel = DateUtils.formatDateTime(this,
				mDate.getTimeInMillis(),
				DateUtils.FORMAT_SHOW_TIME);
		String dateLabelEnd = null, timeLabelEnd = null;
		//location
		mLocation.setSelection(findLocation(meetReq.location));
		// last time
		initLastDaySpinner();
		initLastTimeSpinner();
		float lastTimeTemp = meetReq.lastTime;
		mLastDayPos = (int) lastTimeTemp / 24;
		mLastTimePos = (int) ((lastTimeTemp - 24 * mLastDayPos) * 2);
		if (mLastTimePos<=14) {
			dateLabelEnd = DateUtils.formatDateTime(this,
					mDate.getTimeInMillis()+86400000*mLastDayPos, flags);
			timeLabelEnd = DateUtils.formatDateTime(this,
					mDate.getTimeInMillis()+1800000*mLastTimePos,
					DateUtils.FORMAT_SHOW_TIME);
		}else {
			mLastDayPos=0;
			mLastTimePos=1;
			dateLabelEnd=dateLabel;
			timeLabelEnd = DateUtils.formatDateTime(this,
					mDate.getTimeInMillis()+3600000,
					DateUtils.FORMAT_SHOW_TIME);
		}
		mContextTextView.setText("地点："+meetReq.location+"\n"
				+"开始时间："+dateLabel+","+timeLabel+"\n"
				+"结束时间："+dateLabelEnd+","+timeLabelEnd);
		
		if (!isLocationExist) {
			addLocationDialog();
		}
	}
	
	private void hideAll(){
		
		LinearLayout mLinearLayout1=(LinearLayout) findViewById(R.id.linearLayout3);
		LinearLayout mLinearLayout2=(LinearLayout) findViewById(R.id.linearLayout4);
		TextView locationTextView=(TextView) findViewById(R.id.locationTextView);
		ImageView locationPic=(ImageView) findViewById(R.id.location_pic);
		
		locationPic.setVisibility(View.GONE);
		locationTextView.setVisibility(View.GONE);
		mLocationEditBtnButton.setVisibility(View.GONE);
		mLocation.setVisibility(View.GONE);
		mLinearLayout1.setVisibility(View.GONE);
		mLinearLayout2.setVisibility(View.GONE);
		mTopic.setVisibility(View.GONE);

	}

	private void showFailDlg() {
		// TODO Auto-generated method stub
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setMessage(R.string.meet_req_failed);
		adb.setPositiveButton(R.string.remind_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						// TODO
						// Auto-generated
						// method
						// stub
						finish();
					}
				});
	}

	@SuppressWarnings("deprecation")
	private void initDefault() {
		
		mTopicTextView.setVisibility(View.GONE);
		mContextTextView.setVisibility(View.GONE);

		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		// clipboard.setText("some thing");
		if (clipboard.getText() != null) {
			mTopic.setText(clipboard.getText());
		}

		long now = getIntent().getLongExtra(
				MeetData.EXTRA_FOCUS_DATE,
				System.currentTimeMillis());

		mDate = new GregorianCalendar();
		mDate.setTimeInMillis(now);
		mDate.set(Calendar.MINUTE, 0);

		int flags = DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_SHOW_WEEKDAY;
		String dateLabel = DateUtils.formatDateTime(this,
				mDate.getTimeInMillis(), flags);
		mDateSelBtn.setText(dateLabel);

		// incre one hour from now
		final int hour = mDate.get(Calendar.HOUR_OF_DAY);
		mDate.set(Calendar.HOUR_OF_DAY, hour + 1);
		String timeLabel = DateUtils.formatDateTime(this,
				mDate.getTimeInMillis(),
				DateUtils.FORMAT_SHOW_TIME);
		mTimeSelBtn.setText(timeLabel);

		setTitle(R.string.meeting_create);
		isAddMeet = true;

		initLastDaySpinner();
		initLastTimeSpinner();
		initChangeListener();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar
		// if it is present.
		getMenuInflater().inflate(R.menu.edit_menu, menu);
		return true;
	}


	public void onClick(View v) {
		// TODO Auto-generated method stub
		Calendar cal = Calendar.getInstance();
		final int id = v.getId();

		switch (id) {
		case R.id.date:
			// TimeZone tz = TimeZone.getDefault();
			DatePickerDialog dpd = new DatePickerDialog(
					this,
					mDateSetLsn,
					mDate.get(Calendar.YEAR),
					mDate.get(Calendar.MONTH),
					mDate.get(Calendar.DAY_OF_MONTH));
			dpd.show();
			break;
		case R.id.time:
			TimePickerDialog tpd = new TimePickerDialog(
					this,
					mTimeSetLsn,
					mDate.get(Calendar.HOUR_OF_DAY),
					0, true);
			tpd.show();
			break;
		case R.id.editLocation:
			Intent intent = new Intent();
			intent.setClass(this,EditLocationActivity.class);
			intent.setAction("android.intent.action.ADDROOM");
			startActivityForResult(intent,0);

			
		default:
			break;
		}

	}

	private OnDateSetListener mDateSetLsn = new OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year,
				int monthOfYear, int dayOfMonth) {
			// TODO Auto-generated method stub
			mDate.set(year, monthOfYear, dayOfMonth);

			int flags = DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_SHOW_WEEKDAY;
			String dateLabel = DateUtils
					.formatDateTime(WXEntryActivity.this,
							mDate.getTimeInMillis(),
							flags);
			mDateSelBtn.setText(dateLabel);
			initLastDaySpinner();
		}
	};

	private OnTimeSetListener mTimeSetLsn = new OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay,
				int minute) {
			// TODO Auto-generated method stub
			mDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
			mDate.set(Calendar.MINUTE, minute);

			String dateLabel = DateUtils
					.formatDateTime(WXEntryActivity.this,
							mDate.getTimeInMillis(),
							DateUtils.FORMAT_SHOW_TIME);
			mTimeSelBtn.setText(dateLabel);
			initLastTimeSpinner();
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	private void deleteMeet() {
		// TODO Auto-generated method stub
		ContentResolver cr = getContentResolver();
		cr.delete(mOrigMeet.dbUri, null, null);
		if (!isTextChanged) {
			finish();
		}
	}

	private void sendToWeixin(Meet meet) {

		String path = MeetUtil.genMeetFile(meet);

		Log.v(TAG, "file path=" + path);
		final WXFileObject appdata = new WXFileObject();

		appdata.filePath = path;
		appdata.fileData = Util.readFromFile(path, 0, -1);

		final WXMediaMessage msg = new WXMediaMessage();

		Bitmap icon = BitmapFactory.decodeResource(
				getResources(),
				R.drawable.ic_launcher);
		msg.setThumbImage(icon);

		String time = DateUtils.formatDateTime(
				WXEntryActivity.this,
				meet.dateMillis,
				DateUtils.FORMAT_SHOW_TIME
						| DateUtils.FORMAT_SHOW_DATE);
		msg.title = meet.topic + "\n" + time + " "
				+ meet.location;

		msg.description = getResources().getString(
				R.string.meeting_request);
		msg.mediaObject = appdata;

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = Util.buildTransaction("appdata");
		req.message = msg;
		req.scene = /*
			 * isTimelineCb.isChecked() ?
			 * SendMessageToWX.Req.WXSceneTimeline :
			 */SendMessageToWX.Req.WXSceneSession;

		mWXapi.sendReq(req);
	}

	private static int[] PRE_REMIND_MINUTE = new int[] { 15, 30 };

	private Meet generateMeet() {

		Meet meet = new Meet();

		meet.topic = mTopic.getText().toString();
		if (TextUtils.isEmpty(meet.topic)) {
			meet.topic = mTopic.getHint().toString();
		}

		meet.location = (String) mLocation.getSelectedItem();
		meet.dateMillis = mDate.getTimeInMillis();
		meet.preMinute = PRE_REMIND_MINUTE[mRemindSpin
				.getSelectedItemPosition()];

		ContentValues cv = new ContentValues();

		String as[] = new String[3];
		as[0] = "meet_topic";
		as[1] = "meet_date";
		as[2] = "meet_room";
		isItemExist = false;
		Cursor cursor = getContentResolver().query(
				MeetData.URI, as, null, null,
				null);
		if (cursor != null && cursor.moveToFirst()) {
			int topicIndex = cursor.getColumnIndex("meet_topic");
			int dateIndex = cursor.getColumnIndex("meet_date");
			int roomIndex = cursor.getColumnIndex("meet_room");

			do {

				GregorianCalendar mCursorDate = new GregorianCalendar();
				mCursorDate.setTimeInMillis(cursor
						.getLong(dateIndex));
				int flags = DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_SHOW_WEEKDAY;
				String mCursorDateLabel = DateUtils
						.formatDateTime(this,
								mCursorDate.getTimeInMillis(),
								flags);
				String mCursorTimeLabel = DateUtils
						.formatDateTime(this,
								mCursorDate.getTimeInMillis(),
								DateUtils.FORMAT_SHOW_TIME);
				String mDateLabel = DateUtils
						.formatDateTime(this,
								mDate.getTimeInMillis(),
								flags);
				String mTimeLabel = DateUtils
						.formatDateTime(this,
								mDate.getTimeInMillis(),
								DateUtils.FORMAT_SHOW_TIME);

				if (mCursorDateLabel.equals(mDateLabel)) {
					if (mCursorTimeLabel.equals(mTimeLabel)) {
						if (meet.topic.equals(cursor
								.getString(topicIndex))) {
							if (meet.location.equals(cursor
									.getString(roomIndex))) {
								isItemExist = true;
							}
						}
					}
				}
			} while (cursor.moveToNext());
			if (cursor != null) {
				cursor.close();
			}
		}
		if (!isItemExist) {
			cv.put(MeetData.KEY_TOPIC, meet.topic);
			cv.put(MeetData.KEY_WHEN, meet.dateMillis);
			cv.put(MeetData.KEY_WHERE, meet.location);
			cv.put(MeetData.KEY_PRE_TIME,
					meet.preMinute);

			ContentResolver cr = getContentResolver();

			meet.dbUri = cr.insert(MeetData.URI, cv);
		}
		return meet;

	}

	// 保存为Meet，用于发送
	private Meet saveAsFile() {
		Meet meet = new Meet();

		meet.topic = mTopic.getText().toString();
		if (TextUtils.isEmpty(meet.topic)) {
			meet.topic = "开会";
		}

		meet.location = (String) mLocation.getSelectedItem();
		meet.dateMillis = mDate.getTimeInMillis();
		meet.preMinute = PRE_REMIND_MINUTE[mRemindSpin
				.getSelectedItemPosition()];
		meet.lastTime = (float) (mLastDayPos * 24 + mLastTimePos * 0.5);
		return meet;

	}

	// 储存到数据库
	private void saveInDB(Meet meet) {

		String as[] = new String[3];
		as[0] = "meet_topic";
		as[1] = "meet_date";
		as[2] = "meet_room";
		isItemExist = false;
		Cursor cursor = getContentResolver().query(
				MeetData.URI, as, null, null,
				null);
		if (cursor != null && cursor.moveToFirst()) {
			int topicIndex = cursor.getColumnIndex("meet_topic");
			int dateIndex = cursor.getColumnIndex("meet_date");
			int roomIndex = cursor.getColumnIndex("meet_room");

			do {

				GregorianCalendar mCursorDate = new GregorianCalendar();
				mCursorDate.setTimeInMillis(cursor
						.getLong(dateIndex));
				int flags = DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_SHOW_WEEKDAY;
				String mCursorDateLabel = DateUtils
						.formatDateTime(this,
								mCursorDate.getTimeInMillis(),
								flags);
				String mCursorTimeLabel = DateUtils
						.formatDateTime(this,
								mCursorDate.getTimeInMillis(),
								DateUtils.FORMAT_SHOW_TIME);
				String mDateLabel = DateUtils
						.formatDateTime(this,
								mDate.getTimeInMillis(),
								flags);
				String mTimeLabel = DateUtils
						.formatDateTime(this,
								mDate.getTimeInMillis(),
								DateUtils.FORMAT_SHOW_TIME);

				if (mCursorDateLabel.equals(mDateLabel)) {
					if (mCursorTimeLabel.equals(mTimeLabel)) {
						if (meet.topic.equals(cursor
								.getString(topicIndex))) {
							if (meet.location.equals(cursor
									.getString(roomIndex))) {
								return;
							}
						}
					}
				}
			} while (cursor.moveToNext());
			if (cursor != null) {
				cursor.close();
			}

		}

		ContentValues cv = new ContentValues();
		cv.put(MeetData.KEY_TOPIC, meet.topic);
		cv.put(MeetData.KEY_WHEN, meet.dateMillis);
		cv.put(MeetData.KEY_WHERE, meet.location);
		cv.put(MeetData.KEY_PRE_TIME, meet.preMinute);
		cv.put(MeetData.KEY_LAST_TIME, meet.lastTime);

		ContentResolver cr = getContentResolver();

		meet.dbUri = cr.insert(MeetData.URI, cv);
		setupMeetRemind(meet);
	}

	private void setupMeetRemind(Meet meet) {

		if (meet == null) {
			Log.e(TAG, "meet can not be null");
			return;
		}

		long triggerTime = meet.dateMillis - meet.preMinute
				* 60 * 1000;

		Intent trigAction = new Intent();
		trigAction.setClass(this, MeetRemindReceiver.class);
		trigAction.setAction(MeetRemindReceiver.ACTION_REMIND);
		trigAction.setData(meet.dbUri);

		PendingIntent pi = PendingIntent.getBroadcast(this, 0,
				trigAction,
				PendingIntent.FLAG_ONE_SHOT);

		AlarmManager alm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alm.set(AlarmManager.RTC, triggerTime, pi);
	}

	public int findLocation(String location) {
		int i;
		for (i = 0; i < mAddrs.size(); ++i) {
			String iString = mAddrs.get(i);
			if (mAddrs.get(i).equals(location)) {
				return i;
			}
		}

		mAddrs.add(location);
		mLocationAdapter.setNotifyOnChange(true);
		i = mAddrs.size() - 1;
		isLocationExist=false;
		return i;
	}

	public int findPreTime(String remindTime) {
		int i;
		String[] remindTimeArray = getResources()
				.getStringArray(R.array.reminder_minutes_opt);
		for (i = 0; i < remindTimeArray.length; i++) {
			if (remindTimeArray[i] == remindTime) {
				return i;
			}
		}
		return 0;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		// TODO Auto-generated method stub
		switch (requestCode) {
		case 0:
			if (data.getExtras().getBoolean("rtnIsAdd")) {
				String as[] = new String[1];
				as[0] = "meet_room";
				mAddrs.clear();

				Cursor cursorAddrs = getContentResolver()
						.query(MeetProvider.CONTENT_ROOM_URI,
								as,
								null,
								null,
								null);
				while (cursorAddrs != null
						&& cursorAddrs.moveToNext()) {
					mAddrs.add(cursorAddrs.getString(cursorAddrs
							.getColumnIndex(as[0])));
				}
				mLocationAdapter.setNotifyOnChange(true);
				mLocation.setSelection(cursorAddrs
						.getCount() - 1);
			}

			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		mWXapi.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onResp(BaseResp resp) {
		// TODO Auto-generated method stub
//	Log.i("微信返回",resp.errStr);
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			if (isAddMeet) {
				saveInDB(mNewMeet);
			} else {
				deleteMeet();
				saveInDB(mNewMeet);
			}
			saveInDB(mNewMeet);
			this.finish();
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			break;
		default:
			break;
		}
	}

	protected void exitDialog() {
		AlertDialog.Builder builder = new Builder(
				WXEntryActivity.this);
		builder.setMessage("是否保存到本地?");
		builder.setTitle("提示");
		builder.setPositiveButton(
				"确认",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						if (!isAddMeet) {
							deleteMeet();
						}
						mOrigMeet = saveAsFile();
						saveInDB(mOrigMeet);
						dialog.dismiss();
						WXEntryActivity.this.finish();
					}
				});
		builder.setNegativeButton(
				"取消",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
						WXEntryActivity.this.finish();
					}
				});
		builder.create().show();
	}
	
	protected void addLocationDialog() {
		AlertDialog.Builder builder = new Builder(
				WXEntryActivity.this);
		builder.setMessage("将地址："+mLocation.getSelectedItem().toString()+"添加到常用地址列表？");
		builder.setTitle("提示");
		builder.setPositiveButton(
				"确认",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						ContentValues cValues = new ContentValues();
						cValues.put(MeetProvider.KEY_WHERE,
								mLocation.getSelectedItem().toString());
						getContentResolver().insert(
								MeetProvider.CONTENT_ROOM_URI,
								cValues);
					}
				});
		builder.setNegativeButton(
				"取消",null);
		builder.create().show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {	
			if (isTextChanged) {
				exitDialog();
			}else {
				WXEntryActivity.this.finish();
			}	
		}
		return false;

	}

}
