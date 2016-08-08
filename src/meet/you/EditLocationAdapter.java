package meet.you;

import java.util.ArrayList;

import android.R.layout;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EditLocationAdapter extends BaseAdapter {

	private LayoutInflater infater = null;
	private ArrayList<String> locationList;
	private static Context mContext;
	private EditLocationActivity editLocationActivity;

	public EditLocationAdapter(Context context,EditLocationActivity Activity, ArrayList<String> l) {
		
		infater = (LayoutInflater) Activity.
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		locationList = l;
		editLocationActivity=Activity;
		mContext=context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return locationList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		//init
		View view=null;
		ViewHolder viewHolder=new ViewHolder();
		if (convertView == null || convertView.getTag() == null) {
		view =  infater.inflate(R.layout.edit_location_item, null);
		viewHolder.locationInfo=(TextView) view.findViewById(R.id.locationInfo);
		viewHolder.deleteBtn=(Button) view.findViewById(R.id.deleteBtn);
		viewHolder.mLayout=(RelativeLayout)view.findViewById(R.id.itemLayout);
		view.setTag(viewHolder);
	} 
	else{
		view = convertView ;
		viewHolder = (ViewHolder) convertView.getTag() ;
	}	
		viewHolder.locationInfo.setText(locationList.get(position));
		viewHolder.deleteBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				editLocationActivity.deleteDialog(position);
			}
		});
		 
		if (position==0) {
			viewHolder.mLayout.setBackground(viewHolder.topBackground);
		}else if (position==getCount()-1) {
			viewHolder.mLayout.setBackground(viewHolder.bottomBackground);
		}else {
			viewHolder.mLayout.setBackground(viewHolder.middleBackground);
		}
		return view;

	}
	
	static class ViewHolder {  
		        TextView locationInfo;
		        Button deleteBtn;
		        RelativeLayout mLayout;
		        Resources resources = mContext.getResources();  
		        Drawable topBackground = resources.getDrawable(R.drawable.sub_item_back_top_selector); 
		        Drawable middleBackground = resources.getDrawable(R.drawable.sub_item_back_middle_selector); 
		        Drawable bottomBackground = resources.getDrawable(R.drawable.sub_item_back_bottom_selector); 
		    }  
}
