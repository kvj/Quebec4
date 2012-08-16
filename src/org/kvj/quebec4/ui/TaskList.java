package org.kvj.quebec4.ui;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.kvj.bravo7.ControllerConnector;
import org.kvj.bravo7.ControllerConnector.ControllerReceiver;
import org.kvj.quebec4.R;
import org.kvj.quebec4.data.PointBean;
import org.kvj.quebec4.data.TaskBean;
import org.kvj.quebec4.service.Q4App;
import org.kvj.quebec4.service.Q4Controller;
import org.kvj.quebec4.service.Q4Controller.LocationStatusListener;
import org.kvj.quebec4.service.Q4Service;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

public class TaskList extends Activity implements
		ControllerReceiver<Q4Controller>, LocationStatusListener {

	private static final String TAG = "TaskList";
	private static final int CREATE_TASK = Activity.RESULT_FIRST_USER + 1;
	Q4Controller controller = null;
	ControllerConnector<Q4App, Q4Controller, Q4Service> cc = null;
	TaskListAdapter adapter = null;
	Integer menuTaskID = null;
	ListView listView = null;
	TextView statusView = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new TaskListAdapter();
		setContentView(R.layout.task_list);
		listView = (ListView) findViewById(R.id.task_list_list);
		statusView = (TextView) findViewById(R.id.task_list_status);
		listView.setAdapter(adapter);
		Intent serviceIntent = new Intent(this, Q4Service.class);
		startService(serviceIntent);
		registerForContextMenu(listView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.tasks_menu, menu);
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo contextMenuInfo = (AdapterContextMenuInfo) menuInfo;
		getMenuInflater().inflate(R.menu.task_context, menu);
		TaskBean task = adapter.getItem(contextMenuInfo.position);
		menuTaskID = task.id;
		menu.findItem(R.id.menu_finish_at).setEnabled(
				task.type == TaskBean.TYPE_PATH);
		menu.findItem(R.id.menu_preview).setEnabled(null != task.media);
		menu.findItem(R.id.menu_point_and_finish).setEnabled(
				task.type == TaskBean.TYPE_PATH);
		List<PointBean> points = controller.getPoints(task.id);
		Menu subMenu = menu.findItem(R.id.menu_finish_at).getSubMenu();
		subMenu.clear();
		int j = 0;
		for (int i = points.size() - 1; i >= 0; i--, j++) {
			PointBean point = points.get(i);
			subMenu.add(R.id.menu_finish_at, point.id, j, DateFormat
					.getTimeFormat(this).format(new Date(point.created)));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Log.i(TAG,
				"Context menu: " + item.getItemId() + ", " + item.getGroupId());
		try {
			TaskBean task = controller.getTask(menuTaskID);
			if (null == task) {
				return true;
			}
			if (item.getGroupId() == 0) {
				switch (item.getItemId()) {
				case R.id.menu_point_and_finish:
					if (task.type == TaskBean.TYPE_PATH
							&& (task.status == TaskBean.STATUS_SLEEP || task.status == TaskBean.STATUS_CONSUME)) {
						synchronized (controller) {
							controller.updateStatus(task.id,
									TaskBean.STATUS_CONSUME_AND_FINISH);
							controller.wantLocation();
						}
					}
					break;
				case R.id.menu_cancel:
					synchronized (controller) {
						controller.removeTask(task.id);
					}
					break;
				case R.id.menu_preview:
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setDataAndType(Uri.fromFile(new File(task.media)),
							"image/jpeg");
					// i.setDataAndType(Uri.parse(task.media), "image/jpeg");
					startActivity(i);
					break;
				}
			} else {
				synchronized (controller) {
					List<PointBean> points = controller.getPoints(task.id);
					boolean foundPoint = false;
					for (PointBean point : points) {
						if (foundPoint) {
							controller.removePoint(point.id);
						} else {
							if (point.id.intValue() == item.getItemId()) {
								foundPoint = true;
							}
						}
					}
					controller.updateStatus(task.id, TaskBean.STATUS_READY);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		controller.refreshStatus();
		adapter.setController(this, controller);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent newIntent = new Intent(this, NewTask.class);
		switch (item.getItemId()) {
		case R.id.menu_new_text:
			newIntent.putExtra("type", "point");
			startActivityForResult(newIntent, CREATE_TASK);
			break;
		case R.id.menu_new_note:
			newIntent.putExtra("type", "none");
			startActivityForResult(newIntent, CREATE_TASK);
			break;
		case R.id.menu_new_camera:
			newIntent.putExtra("type", "camera");
			startActivityForResult(newIntent, CREATE_TASK);
			break;
		case R.id.menu_new_path:
			newIntent.putExtra("type", "path");
			startActivityForResult(newIntent, CREATE_TASK);
			break;
		case R.id.menu_reload:
			if (null != controller) {
				adapter.setController(this, controller);
			}
			break;
		case R.id.menu_settings:
			Intent configIntent = new Intent(this, ConfigActivity.class);
			startActivity(configIntent);
			break;
		case R.id.menu_new_drawing:
			startActivity(new Intent(this, DrawingPane.class));
			break;
		}
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		cc = new ControllerConnector<Q4App, Q4Controller, Q4Service>(this, this);
		cc.connectController(Q4Service.class);
	}

	public void onController(Q4Controller controller) {
		controller.addLocationStatusListener(this);
		changed(controller.getLocationStatus());
		if (null != this.controller) {
			return;
		}
		this.controller = controller;
		adapter.setController(this, controller);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (null != controller) { // Disconnect listener
			controller.removeLocationStatusListener(this);
		}
		cc.disconnectController();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (null != controller) {
				adapter.setController(this, controller);
			}
		}
	}

	@Override
	public void changed(final String status) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (null != statusView) { // Have status
					statusView.setText(status);
				}
			}
		});
	}
}