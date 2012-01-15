package org.kvj.quebec4.service;

import org.kvj.bravo7.SuperService;
import org.kvj.quebec4.service.data.TaskBean;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class Q4Service extends SuperService<Q4Controller, Q4App> {

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
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(callbackReceiver);
	}
}
