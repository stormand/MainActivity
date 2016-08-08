package meet.you;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ServiceApdapter extends BaseAdapter {

	private LayoutInflater infater = null;
	private ArrayList<Map<String, Object>> mList;
	private FxService mFxService;

	public ServiceApdapter(FxService service,
			ArrayList<Map<String, Object>> list) {

		mList = list;
		mFxService=service;
		infater = (LayoutInflater) mFxService.
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view=null;
		ViewHolder viewHolder=new ViewHolder();
		if (convertView == null || convertView.getTag() == null) {
		view =  infater.inflate(R.layout.time_axis, null);
		viewHolder.time=(TextView) view.findViewById(R.id.show_time);
		viewHolder.title=(TextView) view.findViewById(R.id.timeAxisTitle);
		viewHolder.detail=(TextView) view.findViewById(R.id.timeAxisRoom);
		viewHolder.detailItem=(RelativeLayout)view.findViewById(R.id.timeAxisItem);
		viewHolder.itemaAnimation=AnimationUtils.loadAnimation(mFxService, R.anim.float_anim);
		viewHolder.timeaAnimation=AnimationUtils.loadAnimation(mFxService, R.anim.float_anim_time);
		view.setTag(viewHolder);
	} 
	else{
		view = convertView ;
		viewHolder = (ViewHolder) convertView.getTag() ;
	}	
		viewHolder.time.setText((String) mList.get(position).get("time"));
		viewHolder.title.setText((String) mList.get(position).get("topic"));
		viewHolder.detail.setText((String) mList.get(position).get("room"));
		
		viewHolder.timeaAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis()+position*75);
		viewHolder.itemaAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis()+position*75);
		viewHolder.time.setAnimation(viewHolder.timeaAnimation);
		viewHolder.detailItem.setAnimation(viewHolder.itemaAnimation);

		return view;
	}

	static class ViewHolder {
		TextView time;
		TextView title;
		TextView detail;

		RelativeLayout detailItem;
		
		Animation itemaAnimation;
		Animation timeaAnimation;

	}

}
