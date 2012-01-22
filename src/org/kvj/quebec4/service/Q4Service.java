package org.kvj.quebec4.service;

import java.util.Calendar;
import java.util.Date;

import org.kvj.bravo7.SuperService;
import org.kvj.quebec4.R;
import org.kvj.quebec4.service.Q4Controller.ControllerListener;
import org.kvj.quebec4.service.data.TaskBean;
import org.kvj.quebec4.ui.TaskList;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class Q4Service extends SuperService<Q4Controller, Q4App> implements
		ControllerListener {

	protected static final String TAG = "Q4Service";
	protected static String LOCK_NAME = "Q4Service";
	private BroadcastReceiver callbackReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "Item created: " + intent);
			try {
				synchronized (controller) {
					controller.updateStatus(intent.getIntExtra("callback", -1),
							TaskBean.STATUS_SENT);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	};

	public Q4Service() {
		super(Q4Controller.class, "Quebec4");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		setAlarmBroadcastReceiverClass(DataAlarmReceiver.class);
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.matburt.mobileorg.ng.CREATED");
		registerReceiver(callbackReceiver, filter);
		controller.setListener(this);
		controller.rescheduleTasks();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Got intent: " + intent);
		if (null != intent && null != intent.getExtras()) {
			if (intent.hasExtra("id")) {
				synchronized (controller) {
					try {
						int taskID = intent.getIntExtra("id", -1);
						Log.i(TAG, "Task: " + taskID);
						TaskBean task = controller.getTask(taskID);
						if (null != task
								&& task.status == TaskBean.STATUS_SLEEP) {
							if (controller.updateStatus(taskID,
									TaskBean.STATUS_CONSUME)) {
								controller.wantLocation();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					powerUnlock(this);
				}
			}
		}
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		controller.setListener(null);
		unregisterReceiver(callbackReceiver);
	}

	public void searching() {
		raiseNotification(R.drawable.s_searching, "Searching for location...",
				TaskList.class);
	}

	public void waiting() {
		raiseNotification(R.drawable.s_sleep, "Timeout in path", TaskList.class);
	}

	public void done() {
		hideNotification();
	}

	public void schedule(int taskID, int mins) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, mins);
		Bundle bundle = new Bundle();
		bundle.putInt("id", taskID);
		Log.i(TAG,
				"Scheduling: " + taskID + ", " + new Date(c.getTimeInMillis()));
		runAtTime(c.getTimeInMillis(), taskID, bundle);
	}
}
