package com.example.recentapplicationslist;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AppsAdapter extends ArrayAdapter<App> {
	
	private int resource;
	
	public AppsAdapter(Context context, int resource, ArrayList<App> teams) {
		super(context, resource, teams);
		this.resource = resource;
	}
    
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if (view == null)
		{
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = vi.inflate(resource, null);
		}
		
		App app = getItem(position);
		
		TextView appNameTV = (TextView)view.findViewById(R.id.applicationNameTextView);
		TextView packageTV = (TextView)view.findViewById(R.id.packageTextView);
		
		appNameTV.setText(app.getApplication_name());
		packageTV.setText(app.getApplication_package_name());
		
		return view;
	}
}
