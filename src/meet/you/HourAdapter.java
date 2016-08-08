package meet.you;

import java.util.List;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


public class HourAdapter extends BaseAdapter {
	
	private List<ResolveInfo> mApps;
	
    public HourAdapter(List<ResolveInfo> data) {
    	mApps = data;
    	
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView i = new ImageView(parent.getContext());

        if (convertView == null) {

        } else {
            i = (ImageView) convertView;
        }

        ResolveInfo info = mApps.get(position);

        return i;
    }


    public final int getCount() {
        return mApps.size();
    }

    public final Object getItem(int position) {
        return mApps.get(position);
    }

    public final long getItemId(int position) {
        return position;
    }
}
