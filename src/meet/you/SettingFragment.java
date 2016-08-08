package meet.you;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

import meet.you.data.MeetData;

/**
 * Created by 一凡 on 2016/2/4.
 */
public class SettingFragment extends android.app.Fragment {

    private Switch floatWindow;
    private Button editLocation, shareQQ,aboutBtn;
    private Tencent mTencent;
    private Switch edgeSwitch;

    private static final String APP_ID = "1105125960";

    public static String FloatWindow="floatWindow";
    private Boolean floatWhether;
    private boolean isEdgeEnable;
    private View rootView;

    SharedPreferences mySharedPreferencesTrue;
    SharedPreferences.Editor editorFloat;

    SharedPreferences mSharedPreferencesEdge;
    SharedPreferences.Editor editorFloatEdge;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView=inflater.inflate(R.layout.setup,container,false);

        floatWindow = (Switch)rootView.findViewById(R.id.setup_switch);
        edgeSwitch=(Switch) rootView.findViewById(R.id.edgeSwith);
        editLocation = (Button) rootView.findViewById(R.id.setup_editLocation);
        shareQQ = (Button) rootView.findViewById(R.id.setup_qqshare);
        aboutBtn=(Button)rootView.findViewById(R.id.about_btn);

        mTencent = Tencent.createInstance(APP_ID, getActivity().getApplicationContext());

        mySharedPreferencesTrue = getActivity().getSharedPreferences(FloatWindow,
                Activity.MODE_PRIVATE);
        mSharedPreferencesEdge=getActivity().getSharedPreferences("isEdgeEnable",
                Activity.MODE_PRIVATE);
        editorFloat=mySharedPreferencesTrue.edit();
        editorFloatEdge=mSharedPreferencesEdge.edit();

        editorFloatEdge.putBoolean("isEdgeEnable", true);
        editorFloatEdge.commit();

        floatWhether = mySharedPreferencesTrue.getBoolean(FloatWindow, true);
        isEdgeEnable=mySharedPreferencesTrue.getBoolean("isEdgeEnable", true);

        floatWindow.setChecked(floatWhether);
        if (!floatWhether) {
            edgeSwitch.setEnabled(false);
        }
        edgeSwitch.setChecked(isEdgeEnable);

        edgeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    editorFloatEdge.putBoolean("isEdgeEnable", true);
                    editorFloatEdge.commit();

                }else {
                    editorFloatEdge.putBoolean("isEdgeEnable", false);
                    editorFloatEdge.commit();
                }
            }
        });

        floatWindow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if(isChecked){
                    edgeSwitch.setEnabled(true);
                    editorFloat.putBoolean(FloatWindow, true);
                    editorFloat.commit();
                    floatWindowService();
                }else{
                    edgeSwitch.setEnabled(false);
                    editorFloat.putBoolean(FloatWindow, false);
                    editorFloat.commit();
                    Intent intent=new Intent(getActivity(), FxService.class);
                    getActivity().stopService(intent);
                }
            }
        });

        editLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),
                        EditLocationActivity.class);
                startActivity(intent);
            }
        });

        shareQQ.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Bundle params = new Bundle();
                params.putString("title", "议员");
                params.putString("summary", "应用分享");
                params.putString("targetUrl",  "http://fusion.qq.com/cgi-bin/qzapps/unified_jump?appid=12260123&from=mqq&actionFlag=0&params=pname%3Dmeet.you%26versioncode%3D1%26channelid%3D%26actionflag%3D0");
                params.putString("imageUrl","http://pp.myapp.com/ma_icon/0/icon_12260123_1453360535/96");
                params.putString("appName",  "议员");
                mTencent.shareToQQ(getActivity(), params,
                        new BaseUiListener());
            }
        });

        aboutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        rootView.getContext());
                builder.setTitle("关于");
                builder.setMessage("软件名称：议员"+"\n"+
                        "作者：陈一凡" + "\n"+
                        "版本：1.1");

                builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            }
        });

        return rootView;
    }

    private class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(JSONObject response) {
            // mBaseMessageText.setText("onComplete:");
            // mMessageText.setText(response.toString());
            doComplete(response);
        }

        protected void doComplete(JSONObject values) {

        }

        @Override
        public void onCancel() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onError(UiError arg0) {
            // TODO Auto-generated method stub

        }
    }
    public void floatWindowService() {

        Time today = new Time();
        today.setToNow();
        long time = System.currentTimeMillis();
        GregorianCalendar focusDate = new GregorianCalendar(today.year,
                today.month, today.monthDay);
        long dayStart = focusDate.getTimeInMillis();
        focusDate.roll(Calendar.DAY_OF_MONTH, true);

        long dayEnd = focusDate.getTimeInMillis();

        ContentResolver cr = getActivity().getContentResolver();

        Cursor cs = cr.query(MeetData.URI, MeetListAdapter.DATA_COL,

                MeetData.KEY_WHEN + ">=? " + " AND " + MeetData.KEY_WHEN + "< ?",

                new String[] { String.valueOf(time), String.valueOf(dayEnd) }, null);

        if (cs != null) {

            final int N = cs.getCount();
            if (N > 0) {
                Intent intent = new Intent(getActivity(), FxService.class);
                getActivity().startService(intent);
            }else {
                Toast.makeText(getActivity().getApplicationContext(),
                        "今日无会议",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}
