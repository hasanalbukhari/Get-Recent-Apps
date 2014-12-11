package com.example.recentapplicationslist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	boolean running = false;
	int period, interval, originalPeriod;
	HashSet<App> newApps = new HashSet<App>();
	ArrayList<App> previousRecentApps = null, veryFirstList = null;
	
	ScheduledExecutorService scheduleTaskExecutor;
	
	Button startButton;
	EditText periodEditText, intervalEditText;
	ListView applicationsListView;
	
	AppsAdapter appsAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		startButton = (Button)findViewById(R.id.startButton);
		periodEditText = (EditText)findViewById(R.id.periodEditText);
		intervalEditText = (EditText)findViewById(R.id.intervalEditText);
		applicationsListView = (ListView)findViewById(R.id.applicationsListView);
		
		setDefaultValues(false);
		appsAdapter = new AppsAdapter(MainActivity.this, R.layout.application_item, new ArrayList<App>());
		applicationsListView.setAdapter(appsAdapter);
		
		startButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!running) {
					try {
						period = originalPeriod = Integer.parseInt(periodEditText.getText().toString());
						interval = Integer.parseInt(intervalEditText.getText().toString());
						if (interval>period || interval == 0 || period == 0)
							Toast.makeText(MainActivity.this, "period can't be less than period!!", Toast.LENGTH_SHORT).show();
						else {
							startSchedule();
							setDefaultValues(running = !running);
						}
					} catch (Exception e) {
						Toast.makeText(MainActivity.this, "Invalid Input!", Toast.LENGTH_SHORT).show();
					}
				} else {
					stopSchedule();
					setDefaultValues(running = !running);
				}
			}
		});
	}
	
	void setDefaultValues(boolean running) {
		periodEditText.setEnabled(!running);
		intervalEditText.setEnabled(!running);
		startButton.setText(running?R.string.stop:R.string.start);
		startButton.setTextColor(running?0xFFFF0000:0xFFFFFFFF);
		
		if (!running) {
			intervalEditText.setText("");
			periodEditText.setText("");
		}
	}
	
	void startSchedule() {
		if (scheduleTaskExecutor == null) {
			newApps = new LinkedHashSet<App>();
			previousRecentApps = veryFirstList = null;
			scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
			scheduleTaskExecutor.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					runOnUiThread(new Runnable() {
						public void run() {
							if (scheduleTaskExecutor!=null && !scheduleTaskExecutor.isShutdown() && period >= 0) {
								if ((originalPeriod - period) % interval == 0) {
									getRecentApplications();
								}
								periodEditText.setText(""+period);
								period --;
							} else {
								stopSchedule();
								setDefaultValues(running = !running);
							}
						}
					});
				}
			}, 0, 1, TimeUnit.SECONDS);
		}
	}
	
	void stopSchedule() {
		if (scheduleTaskExecutor!=null) {
			scheduleTaskExecutor.shutdown();
			scheduleTaskExecutor = null;
		}
	}
	
	void getRecentApplications() {
		appsAdapter.clear();
		ArrayList<App> recentApps = new ArrayList<App>(); 
		
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RecentTaskInfo> activitys = activityManager.getRecentTasks(Integer.MAX_VALUE, ActivityManager.RECENT_IGNORE_UNAVAILABLE); //RECENT_WITH_EXCLUDED
		for (int i = 0; i < activitys.size(); i++) {
			RecentTaskInfo activity = activitys.get(i);
			if (activity.baseIntent != null && activity.baseIntent.getCategories() != null) {
				if (activity.baseIntent.getCategories().contains(Intent.CATEGORY_LAUNCHER)) {
					App app = new App();
			
					app.setApplication_package_name(activity.baseIntent.getComponent().getPackageName());
					
					try {
						app.setApplication_name(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(app.getApplication_package_name(), PackageManager.GET_META_DATA)).toString());
					} catch (NameNotFoundException e) {
						app.setApplication_name(app.getApplication_package_name().substring(app.getApplication_package_name().lastIndexOf(".")+1));
						e.printStackTrace();
					}
				
					recentApps.add(app);
				}
			}
		}
		
		if (previousRecentApps != null) {
			
			int oldCount = newApps.size();
			// detecting new apps. apps that does not appear in the very first fetched list.
			for (int i=0; i<recentApps.size(); i++)
				if (!veryFirstList.contains(recentApps.get(i)))
					newApps.add(recentApps.get(i));
			
			int index = getIndexOfFirstAppBeforeFOSL(recentApps);
			for (int i=index; i>=0; i--) {
				newApps.add(recentApps.get(i));
			}
			
			appsAdapter.addAll(newApps);
			appsAdapter.notifyDataSetChanged();
			
			Toast.makeText(this, appsAdapter.getCount() - oldCount + " Apps launched in the last interval!", Toast.LENGTH_SHORT).show();
		}
		
		if (veryFirstList == null)
			veryFirstList = recentApps;
		previousRecentApps = recentApps;
	}
	
	public int getIndexOfFirstAppBeforeFOSL(ArrayList<App> recentApps) {
		int i=previousRecentApps.size()-1, j = recentApps.size()-1;
		for (; i>=0 && j>=0 ; i--) {
			App app = previousRecentApps.get(i);
			if (app.equals(recentApps.get(j))) {
				j--;
			} else {
				// this application got re-launched and therefore it changed it place in list.
				// or removed manually by user.
			}
		}
		
		return j;
	}
	
	@Override
	protected void onDestroy() {
		super.onPause();
		stopSchedule();
	}
}
