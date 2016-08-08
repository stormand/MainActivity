package meet.you;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import meet.you.data.MeetData;
import meet.you.data.MeetProvider;
import meet.you.wxapi.WXEntryActivity;
import android.R.animator;
import android.R.integer;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FxService extends Service {

	// 定义浮动窗口布局
	private LinearLayout mFloatLayout,mFloatDetailCloseLayout;
	private RelativeLayout mFloatDetailLayout;
	private WindowManager.LayoutParams wmParams;
	private WindowManager.LayoutParams wmDetailParams;
	// 创建浮动窗口设置布局参数的对象
	private WindowManager mWindowManager;

//	private ListView mFloatView;
	private Button mFloatButton;
	private ListView mFloatView;
	private Button mClosebButton;
	private Button mCloseBtn;
	
	private SimpleAdapter adapter;
	private ArrayList<Map<String, Object>> list;
	
	//线的动画
	private Animation translateAnimation;
	private RelativeLayout lineLayout;

	private static final String TAG = "FxService";
	

	public static int viewWidth;
	public static int viewHeight;
	 private static int statusBarHeight;
	private float xInScreen;
	private float yInScreen;
	private float xDownInScreen;
	private float yDownInScreen;
	private float xInView;
	private float yInView;
	
	protected int activityCloseEnterAnimation;
	protected int activityCloseExitAnimation;
	
	 private boolean isEdgeEnable=true;
	
	
	ViewGroup container = null;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i(TAG, "oncreat");
		initWindowManager();
		createFloatView();
		
		TypedArray activityStyle = getTheme().obtainStyledAttributes(new int[] {android.R.attr.windowAnimationStyle});

		int windowAnimationStyleResId = activityStyle.getResourceId(0, 0);      

		activityStyle.recycle();
		activityStyle = getTheme().obtainStyledAttributes(windowAnimationStyleResId, new int[] {android.R.attr.activityCloseEnterAnimation, android.R.attr.activityCloseExitAnimation});
		activityCloseEnterAnimation = activityStyle.getResourceId(0, 0);
		activityCloseExitAnimation = activityStyle.getResourceId(1, 0);

		activityStyle.recycle();
		
	}
	
	public void initWindowManager(){
		wmParams = new WindowManager.LayoutParams();
		// 获取的是WindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager) getApplication()
				.getSystemService(getApplication().WINDOW_SERVICE);
		// 设置window type
		wmParams.type = LayoutParams.TYPE_PHONE;
		// 设置图片格式，效果为背景透明
		wmParams.format = PixelFormat.RGBA_8888;
		// 设置浮动窗口不可聚焦,不影响背后操作（实现操作除浮动窗口外的其他可见窗口的操作）
		wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
		// 调整悬浮窗显示的停靠位置为左侧置顶
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		
		wmParams.x=0;
		wmParams.y=mWindowManager.getDefaultDisplay().getHeight()/2;

		// 设置悬浮窗口长宽数据
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private void createFloatView() {
	
		LayoutInflater inflater = LayoutInflater
				.from(getApplication());
		// 获取浮动窗口视图所在布局
		mFloatLayout = (LinearLayout) inflater.inflate(
				R.layout.float_layout, null);
		// 添加mFloatLayout
		mWindowManager.addView(mFloatLayout, wmParams);
		// 浮动窗口
		mFloatButton=(Button) mFloatLayout.findViewById(R.id.floatButton);

		getDBData();

		mFloatLayout.measure(View.MeasureSpec
				.makeMeasureSpec(0,
						View.MeasureSpec.UNSPECIFIED),
				View.MeasureSpec.makeMeasureSpec(
						0,
						View.MeasureSpec.UNSPECIFIED));
		Animation animation=AnimationUtils.loadAnimation(getApplicationContext(), R.anim.float_anim);
		mFloatButton.setAnimation(animation);

		// 设置监听浮动窗口的触摸移动
			mFloatButton.setOnTouchListener(new OnTouchListener()   
		        {  
		              
		            @Override  
		            public boolean onTouch(View v, MotionEvent event)   
		            {  
		                // TODO Auto-generated method stub  
		                //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标  
		            	switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
					xInView = event.getX();
					yInView = event.getY();
					xDownInScreen = event.getRawX();
					yDownInScreen = event.getRawY()- getStatusBarHeight();
					xInScreen = event.getRawX();
					yInScreen = event.getRawY() - getStatusBarHeight();
					break;
				case MotionEvent.ACTION_MOVE:
					xInScreen = event.getRawX();
					yInScreen = event.getRawY() - getStatusBarHeight();
					// 手指移动的时候更新小悬浮窗的位置
					 wmParams.x =  (int) (xInScreen - xInView);
				                wmParams.y =  (int) (yInScreen - yInView);
				                mWindowManager.updateViewLayout(mFloatLayout,wmParams);
					break;
				case MotionEvent.ACTION_UP:
					// 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
					if (xDownInScreen == xInScreen && yDownInScreen == yInScreen) {
						mWindowManager.removeView(mFloatLayout);
						createTimeAxisView();
						closeTimeAxisView();
					}else {
						SharedPreferences sharedPreferences = getSharedPreferences("isEdgeEnable",
								Activity.MODE_PRIVATE);
						isEdgeEnable = sharedPreferences.getBoolean("isEdgeEnable", false);
						
						if (isEdgeEnable) {
							edgeAdsorptionHandler.sendEmptyMessage(0);
						}
					}

					break;
				default:
					break;
				}
				return true;
		            }  
		        });   

	}
	
	public  Handler edgeAdsorptionHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if (wmParams.x<mWindowManager.getDefaultDisplay().getWidth()/2&&wmParams.x>0) {
					wmParams.x=wmParams.x-15;
					   mWindowManager.updateViewLayout(mFloatLayout,wmParams);
					edgeAdsorptionHandler.sendEmptyMessageDelayed(0, 1);
				}else if(wmParams.x>=mWindowManager.getDefaultDisplay().getWidth()/2&&wmParams.x<mWindowManager.getDefaultDisplay().getWidth()){
					wmParams.x=wmParams.x+15;
					   mWindowManager.updateViewLayout(mFloatLayout,wmParams);
						edgeAdsorptionHandler.sendEmptyMessageDelayed(0, 1);
				}
				break;

			default:
				break;
			}
		};
	};
	
	public void setEdgeAdsorption(boolean isEnable) {
		isEdgeEnable=isEnable;
	}
	
	private void createTimeAxisView() {

		wmDetailParams = new WindowManager.LayoutParams();
		// 获取的是WindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager) getApplication()
				.getSystemService(getApplication().WINDOW_SERVICE);
		Log.i(TAG, "mWindowManager--->" + mWindowManager);
		// 设置window type
		wmDetailParams.type = LayoutParams.TYPE_PHONE;
		// 设置图片格式，效果为背景透明
		wmDetailParams.format = PixelFormat.RGBA_8888;
		// 设置浮动窗口不可聚焦,不影响背后操作（实现操作除浮动窗口外的其他可见窗口的操作）
		wmDetailParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
		// 调整悬浮窗显示的停靠位置为左侧置顶
		wmDetailParams.gravity = Gravity.LEFT|Gravity.TOP;
		// 以屏幕左上角为原点，设置x、y初始值，相对于gravity
		wmDetailParams.x = 0;
		wmDetailParams.y = 0;

		// 设置悬浮窗口长宽数据
		wmDetailParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmDetailParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

		LayoutInflater inflater = LayoutInflater
				.from(getApplication());
		// 获取浮动窗口视图所在布局
		mFloatDetailLayout = (RelativeLayout) inflater.inflate(
				R.layout.time_axis_layout, null);
		
		// 添加mFloatLayout
		mWindowManager.addView(mFloatDetailLayout, wmDetailParams);
		// 浮动窗口
		mFloatView = (ListView) mFloatDetailLayout
				.findViewById(R.id.timeAxisListView);
		
		getDBData();
		
		  translateAnimation=new TranslateAnimation(0,0,-900,0);
		  translateAnimation.setDuration(list.size()*50);
		  translateAnimation.setFillAfter(true);
		  
			lineLayout=(RelativeLayout) mFloatDetailLayout.findViewById(R.id.lineLayout);
			lineLayout.setAnimation(translateAnimation);

		if (!list.isEmpty()) {
			ServiceApdapter mApdapter=new ServiceApdapter(FxService.this, list);
			
			adapter = new SimpleAdapter(
					getApplicationContext(),
					list,
					R.layout.time_axis,
					new String[] { "topic","time", "room" },
					new int[] {
							R.id.timeAxisTitle,
							R.id.show_time,
							R.id.timeAxisRoom});
			mFloatView.setAdapter(mApdapter);
			
			mFloatView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent,
						View view,
						int position,
						long id) {
					// TODO Auto-generated method stub
	
					int idTemp=(Integer) list.get(position).get("id");
					Uri uri = ContentUris.withAppendedId(MeetData.URI_ID, idTemp);
					
					Intent viewMeet = new Intent(MeetData.ACTION_MEET_VIEW);
					viewMeet.setData(uri);
					viewMeet.setClass(FxService.this, WXEntryActivity.class);
					viewMeet.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(viewMeet);
					
					bigToSmall();
				}
			});
		}

		mFloatDetailLayout.measure(View.MeasureSpec
				.makeMeasureSpec(0,
						View.MeasureSpec.UNSPECIFIED),
				View.MeasureSpec.makeMeasureSpec(
						0,
						View.MeasureSpec.UNSPECIFIED));

	}
	
	public void bigToSmall(){
		
		createFloatView();
		mWindowManager.removeView(mFloatDetailLayout);
		mWindowManager.removeView(mFloatDetailCloseLayout);	
		
	}


	public void getDBData() {
		Map<String, Object> map = new HashMap<String, Object>();
		list = new ArrayList<Map<String, Object>>();
		Cursor meetCursor = meetToday();
		long meet_time;
		String room;
		String topic;
		int id;
		
		if (meetCursor != null && meetCursor.moveToFirst()) {
			list.clear();
			do {
				map = new HashMap<String, Object>();
				meet_time = meetCursor.getLong(meetCursor
						.getColumnIndex(MeetData.KEY_WHEN));
				room = meetCursor.getString(meetCursor
						.getColumnIndex(MeetData.KEY_WHERE));
				id = meetCursor.getInt(meetCursor
						.getColumnIndex(MeetData.KEY_ID));
				topic=meetCursor.getString(meetCursor
						.getColumnIndex(MeetData.KEY_TOPIC));
				GregorianCalendar mTime = new GregorianCalendar();
				mTime.setTimeInMillis(meet_time);
				final int hour = mTime.get(Calendar.HOUR_OF_DAY);
				String timeLabel = DateUtils
						.formatDateTime(this,
								mTime.getTimeInMillis(),
								DateUtils.FORMAT_SHOW_TIME);
				map.put("time", timeLabel);
				map.put("room", "地点："+room);
				map.put("id", id);
				map.put("topic", topic);
				list.add(map);

			} while (meetCursor.moveToNext());
		}
	}
	
	/**
	 * 用于获取状态栏的高度。
	 * 
	 * @return 返回状态栏高度的像素值。
	 */
	private int getStatusBarHeight() {
		if (statusBarHeight == 0) {
			try {
				Class<?> c = Class.forName("com.android.internal.R$dimen");
				Object o = c.newInstance();
				Field field = c.getField("status_bar_height");
				int x = (Integer) field.get(o);
				statusBarHeight = getResources().getDimensionPixelSize(x);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusBarHeight;
	}


	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mFloatLayout != null) {
			// 移除悬浮窗口
			mWindowManager.removeView(mFloatLayout);
		}
	}

	public Cursor meetToday() {
		Time today = new Time();
		today.setToNow();
		long time = System.currentTimeMillis();
		GregorianCalendar focusDate = new GregorianCalendar(
				today.year, today.month,
				today.monthDay);
		long dayStart = focusDate.getTimeInMillis();
		focusDate.roll(Calendar.DAY_OF_MONTH, true);

		long dayEnd = focusDate.getTimeInMillis();

		ContentResolver cr = getContentResolver();

		Cursor cs = cr.query(MeetData.URI,
				MeetListAdapter.DATA_COL,

				MeetData.KEY_WHEN
						+ ">=? "
						+ " AND "
						+ MeetData.KEY_WHEN
						+ "< ?",

				new String[] {
						String.valueOf(time),
						String.valueOf(dayEnd) },
				null);
		return cs;

	}

	public void closeTimeAxisView(){
		LayoutInflater inflater = LayoutInflater
				.from(getApplication());
		// 获取浮动窗口视图所在布局
		mFloatDetailCloseLayout =  (LinearLayout) inflater.inflate(
				R.layout.float_exit, null);
		// 添加mFloatLayout
		mWindowManager.addView(mFloatDetailCloseLayout , wmParams);
		mCloseBtn=(Button) mFloatDetailCloseLayout.findViewById(R.id.float_close_btn);
		mCloseBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				bigToSmall();
				
			}
		});
		//添加动画
		Animation animation=AnimationUtils.loadAnimation(getApplicationContext(), R.anim.float_anim);
		mCloseBtn.setAnimation(animation);
	}
}