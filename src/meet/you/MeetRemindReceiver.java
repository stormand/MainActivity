package meet.you;

import java.io.IOException;
import java.util.Date;

import meet.you.data.MeetData;
import meet.you.data.MeetData.Meet;
import meet.you.R;
import android.R.integer;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class MeetRemindReceiver extends BroadcastReceiver {
	private final String TAG = "MeetRec";

	public static String ACTION_REMIND = "meet.intent.action.REMIND";

	private Vibrator vibrator;
	private MediaPlayer player;
	private AlertDialog ad=null;
	private  int ringerMode;
	private Handler handler = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		final String action = intent.getAction();
		Log.v(TAG, "action=" + action);

		if (ACTION_REMIND.equals(action)) {
			handleRemindAction(context, intent);
		}
	}

	private void handleRemindAction(final Context ctx, Intent intent) {
		Uri uri = intent.getData();

		ContentResolver cr = ctx.getContentResolver();

		Cursor cs = cr.query(uri, new String[] {
				MeetData.KEY_TOPIC,
				MeetData.KEY_WHERE,
				MeetData.KEY_WHEN }, null,
				null, null);

		if (cs != null) {
			if (cs.moveToFirst()) {

				final Meet meet = new Meet();
				
				meet.topic = cs.getString(0);
				meet.location = cs.getString(1);
				meet.dateMillis = cs.getLong(2);
				
				if(handler == null){handler = new Handler(Looper.getMainLooper());} 
				handler .post(new Runnable() {
				                        @Override
				                        public void run() {
				                        	Log.i("一番","yifan");
				                        	showRemind(ctx, meet);
				                        }
				                });
				try {
					ring(ctx);
				} catch (IOException e) {
					// TODO
					// Auto-generated
					// catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO
					// Auto-generated
					// catch block
					e.printStackTrace();
				}

			}
			cs.close();
		}

	}

	public void showRemind(Context ctx, Meet meet) {
//		View panel = View.inflate(ctx,
//				R.layout.meet_remind_lyt, null);
//
//		TextView tvTopic = (TextView) panel
//				.findViewById(R.id.topic);
//		TextView tvDate = (TextView) panel
//				.findViewById(R.id.date);
//		TextView tvLoc = (TextView) panel
//				.findViewById(R.id.location);

		int flags = DateUtils.FORMAT_SHOW_TIME
				| DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_SHOW_WEEKDAY;
		String time = DateUtils.formatDateTime(ctx,
				meet.dateMillis, flags);

//		tvTopic.setText(meet.topic);
//		tvDate.setText(time);
//		tvLoc.setText(meet.location);

		AlertDialog.Builder adb = new AlertDialog.Builder(ctx);
		adb.setTitle(R.string.meet_remind);
//		adb.setView(panel);
		adb.setMessage(meet.topic+time+meet.location);
		
		adb.setPositiveButton(R.string.remind_ok,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						// TODO
						// Auto-generated
						// method
						// stub
						switch (ringerMode) {
						case AudioManager.RINGER_MODE_NORMAL:
							player.setLooping(false);
							break;
						case AudioManager.RINGER_MODE_VIBRATE:
							vibrator.cancel();
							break;
						default:
							break;
						}
					}
				});

		ad = adb.create();
		ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		ad.show();
//		ad.setOnCancelListener(new OnCancelListener() {
//			@Override
//			public void onCancel(DialogInterface dialog) {
//				// TODO Auto-generated method
//				// stub
//				switch (ringerMode) {
//				case AudioManager.RINGER_MODE_NORMAL:
//					player.setLooping(false);
//					break;
//				case AudioManager.RINGER_MODE_VIBRATE:
//					vibrator.cancel();
//					break;
//				default:
//					break;
//				}
//			}
//		}
//		);

	}

	private MediaPlayer ring(Context ctx) throws Exception, IOException {

		Uri uri = Uri.parse("android.resource://"
				+ ctx.getApplicationContext()
						.getPackageName()
				+ "/" + R.raw.ring);
		player = new MediaPlayer();
		player.setDataSource(ctx, uri);
		final AudioManager audioManager = (AudioManager) ctx
				.getSystemService(Context.AUDIO_SERVICE);

		ringerMode = audioManager.getRingerMode();
		switch (ringerMode) {
		case AudioManager.RINGER_MODE_NORMAL:
			
			player.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
			player.setLooping(true);
			player.prepare();
			player.start();
			break;
		case AudioManager.RINGER_MODE_VIBRATE:
			vibrator = (Vibrator) ctx
					.getSystemService(Context.VIBRATOR_SERVICE);
			long[] pattern = { 100, 400, 100, 400}; // 停止 开启 停止开启
			vibrator.vibrate(pattern, 2); 
			break;
		default:
			break;
		}
		return player;
	}

}
