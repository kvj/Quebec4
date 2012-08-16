package org.kvj.quebec4.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kvj.quebec4.R;
import org.kvj.quebec4.data.PointBean;
import org.kvj.quebec4.data.Quebec4Service;
import org.kvj.quebec4.data.Quebec4Service.Stub;
import org.kvj.quebec4.data.TaskBean;
import org.kvj.quebec4.service.data.Q4DBHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.os.Binder;
import android.os.RemoteException;

public class Q4Controller {

	public static interface ControllerListener {

		public void searching();

		public void waiting();

		public void done();

		public void schedule(int taskID, int mins);
	}

	public static interface LocationStatusListener {
		public void changed(String status);
	}

	private static final String TAG = "Q4";

	private Q4DBHelper db = null;
	private LocationController locationController = null;
	private ControllerListener listener = null;
	private DrawingController drawing = null;
	private List<LocationStatusListener> locationStatusListeners = new ArrayList<LocationStatusListener>();
	private String locationStatus = "Idle";

	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	private String getTime() {
		return timeFormat.format(new Date());
	}

	private String locationStatusToString(Location location) {
		if (null == location) { // not found
			return "not found";
		}
		StringBuilder sb = new StringBuilder(String.format("%s: acc:%d",
				location.getProvider(), (int) location.getAccuracy()));
		if (0 != location.getSpeed()) { // Have speed
			sb.append(String.format("/sp:%d", (int) location.getSpeed()));
		}
		if (0 != location.getAltitude()) { // Have speed
			sb.append(String.format("/el:%d", (int) location.getAltitude()));
		}
		return sb.toString();
	}

	public Q4Controller() {
		db = new Q4DBHelper(Q4App.getInstance());
		if (!db.open()) {
			db = null;
		}
		locationController = new LocationController(Q4App.getInstance()) {

			@Override
			public void locationStarted() {
				setLocationStatus(getTime() + ": "
						+ "searching for geo data...");
			}

			@Override
			public boolean locationFound(Location location) {
				PointBean point = null;
				if (null != location) {
					int accuracy = Q4App.getInstance().getIntPreference(
							R.string.accuracyConfig,
							R.string.accuracyConfigDefault);
					setLocationStatus(getTime() + ": "
							+ locationStatusToString(location));
					if (location.getAccuracy() > accuracy) {
						return false;
					}
					point = new PointBean();
					point.accuracy = location.getAccuracy();
					point.altitude = location.getAltitude();
					point.lat = location.getLatitude();
					point.lon = location.getLongitude();
					point.speed = location.getSpeed();
					// Log.i(TAG, "Found point: " + pointToCoordinates(point)
					// + ", " + pointToPointDetails(point));
				}
				List<TaskBean> tasks = getTasks(TaskBean.STATUS_CONSUME,
						TaskBean.STATUS_CONSUME_AND_FINISH);
				for (int i = 0; i < tasks.size(); i++) {
					TaskBean task = tasks.get(i);
					if (null != point) {
						point.taskID = task.id;
						createPoint(point, true);
					}
					if (TaskBean.TYPE_POINT == task.type) {
						updateStatus(task.id, TaskBean.STATUS_READY);
					}
					if (TaskBean.TYPE_PATH == task.type) {
						if (TaskBean.STATUS_CONSUME_AND_FINISH == task.status) {
							updateStatus(task.id, TaskBean.STATUS_READY);
						} else {
							updateStatus(task.id, TaskBean.STATUS_SLEEP);
							listener.schedule(task.id, task.interval);
						}
					}
				}
				// sendTasks();
				List<TaskBean> sleepTasks = getTasks(TaskBean.STATUS_SLEEP);
				if (sleepTasks.size() > 0) {
					listener.waiting();
				} else {
					listener.done();
				}
				return true;
			}

			@Override
			public void locationFinished(Location location) {
				setLocationStatus(getTime() + ": last: "
						+ locationStatusToString(location));
			}
		};
	}

	public Integer createTask(TaskBean task, PointBean point) {
		if (null == db) {
			return null;
		}
		try {
			db.getDatabase().beginTransaction();
			ContentValues values = new ContentValues();
			values.put("title", task.title);
			values.put("type", task.type);
			values.put("status", task.status);
			values.put("created", task.created);
			values.put("interval", task.interval);
			values.put("accuracy", task.accuracy);
			values.put("media", task.media);
			long id = db.getDatabase().insert("tasks", null, values);
			task.id = new Integer(new Long(id).intValue());
			if (null != point) {
				point.taskID = task.id;
				createPoint(point, false);
			}
			db.getDatabase().setTransactionSuccessful();
			return task.id;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.getDatabase().endTransaction();
		}
		return null;
	}

	private static String[] tasksFields = { "id", "title", "type", "status",
			"created", "interval", "accuracy", "media" };
	private static String[] pointsFields = { "id", "task_id", "created", "lon",
			"lat", "speed", "altitude", "accuracy" };

	private static TaskBean cursorToTask(Cursor c) {
		TaskBean task = new TaskBean();
		task.id = c.getInt(0);
		task.title = c.getString(1);
		task.type = c.getInt(2);
		task.status = c.getInt(3);
		task.created = c.getLong(4);
		task.interval = c.getInt(5);
		task.accuracy = c.getInt(6);
		task.media = c.getString(7);
		return task;
	}

	private static PointBean cursorToPoint(Cursor c) {
		PointBean point = new PointBean();
		point.id = c.getInt(0);
		point.taskID = c.getInt(1);
		point.created = c.getLong(2);
		point.lon = c.getDouble(3);
		point.lat = c.getDouble(4);
		point.speed = c.getDouble(5);
		point.altitude = c.getDouble(6);
		point.accuracy = c.getDouble(7);
		return point;
	}

	public List<TaskBean> getTasks(int... statuses) {
		List<TaskBean> result = new ArrayList<TaskBean>();
		if (null == db || null == statuses || 0 == statuses.length) {
			return result;
		}
		try {
			StringBuilder where = new StringBuilder();
			List<String> whereArgs = new ArrayList<String>();
			for (int i = 0; i < statuses.length; i++) {
				if (i > 0) {
					where.append(" or ");
				}
				where.append("status=?");
				whereArgs.add(Integer.toString(statuses[i]));
			}
			Cursor c = db.getDatabase().query("tasks", tasksFields,
					where.toString(), whereArgs.toArray(new String[0]), null,
					null, "created");
			if (c.moveToFirst()) {
				do {
					result.add(cursorToTask(c));
				} while (c.moveToNext());
			}
			c.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean updateStatus(Integer taskID, int status) {
		if (null == db) {
			return false;
		}
		try {
			db.getDatabase().beginTransaction();
			ContentValues values = new ContentValues();
			values.put("status", status);
			db.getDatabase().update("tasks", values, "id=?",
					new String[] { taskID.toString() });
			db.getDatabase().setTransactionSuccessful();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.getDatabase().endTransaction();
		}
		return false;
	}

	public Integer createPoint(PointBean point, boolean createtrans) {
		if (null == db) {
			return null;
		}
		try {
			if (createtrans) {
				db.getDatabase().beginTransaction();
			}
			ContentValues values = new ContentValues();
			values.put("task_id", point.taskID);
			values.put("created", point.created);
			values.put("lon", point.lon);
			values.put("lat", point.lat);
			values.put("speed", point.speed);
			values.put("altitude", point.altitude);
			values.put("accuracy", point.accuracy);
			long id = db.getDatabase().insert("points", null, values);
			point.id = new Integer(new Long(id).intValue());
			if (createtrans) {
				db.getDatabase().setTransactionSuccessful();
			}
			return point.id;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (createtrans) {
				db.getDatabase().endTransaction();
			}
		}
		return null;
	}

	public List<PointBean> getPoints(Integer taskID) {
		List<PointBean> result = new ArrayList<PointBean>();
		if (null == db) {
			return result;
		}
		try {
			Cursor c = db.getDatabase().query("points", pointsFields,
					"task_id=?", new String[] { taskID.toString() }, null,
					null, "created");
			if (c.moveToFirst()) {
				do {
					result.add(cursorToPoint(c));
				} while (c.moveToNext());
			}
			c.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private String pointToCoordinates(PointBean point) {
		return String.format("%1.6f,%1.6f", point.lat, point.lon);
	}

	private String pointToPointDetails(PointBean point) {
		return String.format("acc:%1.3f,sp:%1.3f,alt:%1.3f", point.accuracy,
				point.speed, point.altitude);
	}

	private JSONObject pointToJSON(PointBean point) throws JSONException {
		JSONObject result = new JSONObject();
		result.put("lat", point.lat);
		result.put("lon", point.lon);
		result.put("acc", point.accuracy);
		result.put("alt", point.altitude);
		result.put("created", point.created);
		result.put("speed", point.speed);
		result.put("id", point.id);
		return result;
	}

	public JSONObject taskToJSON(TaskBean task, boolean addData)
			throws JSONException {
		JSONObject result = new JSONObject();
		result.put("id", task.id);
		result.put("title", task.title);
		result.put("created", task.created);
		if (addData) {
			if (null != task.media) {
				result.put("media", task.media);
			}
			List<PointBean> points = getPoints(task.id);
			if (TaskBean.TYPE_POINT == task.type && points.size() > 0) {
				result.put("point", pointToJSON(points.get(0)));
			}
			if (TaskBean.TYPE_PATH == task.type && points.size() > 0) {
				JSONArray arr = new JSONArray();
				for (int i = 0; i < points.size(); i++) {
					arr.put(pointToJSON(points.get(i)));
				}
				result.put("points", arr);
			}
		}
		return result;
	}

	//
	// public void sendTasks() {
	// try {
	// List<TaskBean> readyTasks = getTasks(TaskBean.STATUS_READY);
	// for (int i = 0; i < readyTasks.size(); i++) {
	// TaskBean task = readyTasks.get(i);
	// Intent createIntent = new Intent(
	// "com.matburt.mobileorg.ng.CREATE");
	// createIntent.putExtra("text", task.title);
	// createIntent.putExtra(
	// "todo",
	// Q4App.getInstance()
	// .getStringPreference(R.string.todoConfig,
	// R.string.todoConfigDefault));
	// createIntent.putExtra(
	// "tags",
	// Q4App.getInstance()
	// .getStringPreference(R.string.tagsConfig,
	// R.string.tagsConfigDefault));
	// if (null != task.media) {
	// createIntent.putExtra("attachment", task.media);
	// }
	// List<PointBean> points = getPoints(task.id);
	// ArrayList<String> paramNames = new ArrayList<String>();
	// ArrayList<String> paramValues = new ArrayList<String>();
	// if (task.type == TaskBean.TYPE_POINT && points.size() > 0) {
	// paramNames.add("COORDINATES");
	// paramValues.add(pointToCoordinates(points.get(0)));
	// paramNames.add("COORDINATES_DETAILS");
	// paramValues.add(pointToPointDetails(points.get(0)));
	// }
	// if (task.type == TaskBean.TYPE_PATH) {
	// ArrayList<String> childrenTypes = new ArrayList<String>();
	// ArrayList<String> childrenValues = new ArrayList<String>();
	// StringBuilder buffer = new StringBuilder(":PATH:\n");
	// DateFormat dateFormat = new SimpleDateFormat(
	// "yyyy-MM-dd EEE HH:mm", Locale.ENGLISH);
	// for (PointBean point : points) {
	// buffer.append(String.format("%s,%s,%s\n",
	// dateFormat.format(new Date(point.created)),
	// pointToCoordinates(point),
	// pointToPointDetails(point)));
	// }
	// buffer.append(":END:");
	// childrenTypes.add("drawer");
	// childrenValues.add(buffer.toString());
	// createIntent.putStringArrayListExtra("children_types",
	// childrenTypes);
	// createIntent.putStringArrayListExtra("children_values",
	// childrenValues);
	// }
	// createIntent.putStringArrayListExtra("properties_names",
	// paramNames);
	// createIntent.putStringArrayListExtra("properties_values",
	// paramValues);
	// createIntent.putExtra("callback", task.id);
	// Q4App.getInstance().startService(createIntent);
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	public void wantLocation() {
		locationController.enableLocation(Q4App.getInstance().getIntPreference(
				R.string.timeoutConfig, R.string.timeoutConfigDefault));
		if (null != listener) {
			listener.searching();
		}
	}

	public void setListener(ControllerListener listener) {
		this.listener = listener;
	}

	public TaskBean getTask(int id) {
		if (null == db) {
			return null;
		}
		try {
			Cursor c = db.getDatabase().query("tasks", tasksFields, "id=?",
					new String[] { Integer.toString(id) }, null, null,
					"created");
			if (c.moveToFirst()) {
				TaskBean task = cursorToTask(c);
				c.close();
				return task;
			}
			c.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void refreshStatus() {
		List<TaskBean> consumingTasks = getTasks(TaskBean.STATUS_CONSUME,
				TaskBean.STATUS_CONSUME_AND_FINISH);
		if (consumingTasks.size() > 0) {
			listener.searching();
		} else {
			locationController.disableLocation(null);
			List<TaskBean> sleepTasks = getTasks(TaskBean.STATUS_SLEEP);
			if (sleepTasks.size() > 0) {
				listener.waiting();
			} else {
				listener.done();
			}
		}
	}

	public synchronized void rescheduleTasks() {
		List<TaskBean> sleepTasks = getTasks(TaskBean.STATUS_SLEEP);
		for (TaskBean task : sleepTasks) {
			listener.schedule(task.id, task.interval);
		}
		if (sleepTasks.size() > 0) {
			listener.waiting();
		}
		List<TaskBean> consumingTasks = getTasks(TaskBean.STATUS_CONSUME,
				TaskBean.STATUS_CONSUME_AND_FINISH);
		if (consumingTasks.size() > 0) {
			wantLocation();
		}
	}

	public boolean removePoint(Integer pointID) {
		if (null == db) {
			return false;
		}
		try {
			db.getDatabase().beginTransaction();
			db.getDatabase().delete("points", "id=?",
					new String[] { pointID.toString() });
			db.getDatabase().setTransactionSuccessful();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.getDatabase().endTransaction();
		}
		return false;
	}

	public boolean removeTask(Integer taskID) {
		if (null == db) {
			return false;
		}
		try {
			if (null != taskID) {
				TaskBean task = getTask(taskID);
				if (null != task && null != task.media) {
					// Log.i(TAG, "Remove media:" + task.media + " = "
					// + Q4App.getInstance().getExternalCacheDir());
					if (task.media.startsWith(Q4App.getInstance()
							.getExternalCacheDir().toString())) {
						try {
							File file = new File(task.media);
							if (file.exists() && file.isFile()) {
								file.delete();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			db.getDatabase().beginTransaction();
			db.getDatabase().delete("points", "task_id=?",
					new String[] { taskID.toString() });
			db.getDatabase().delete("tasks", "id=?",
					new String[] { taskID.toString() });
			db.getDatabase().setTransactionSuccessful();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.getDatabase().endTransaction();
		}
		return false;
	}

	public DrawingController getDrawing() {
		if (null == drawing) {
			drawing = new DrawingController(Q4App.getInstance());
		}
		return drawing;
	}

	public void clearDrawing() {
		drawing = null;
	}

	private Quebec4Service.Stub stub = new Stub() {

		@Override
		public boolean removeTask(TaskBean task) throws RemoteException {
			return Q4Controller.this.removeTask(task.id);
		}

		@Override
		public List<TaskBean> getTasks() throws RemoteException {
			List<TaskBean> tasks = Q4Controller.this.getTasks(
					TaskBean.STATUS_CONSUME, TaskBean.STATUS_SLEEP,
					TaskBean.STATUS_CONSUME_AND_FINISH, TaskBean.STATUS_READY);
			return tasks;
		}

		@Override
		public TaskBean getTask(int id) throws RemoteException {
			TaskBean task = Q4Controller.this.getTask(id);
			if (null == task) { // Not found
				return task;
			}
			List<PointBean> points = getPoints(task.id);
			if (task.type == TaskBean.TYPE_POINT && points.size() == 1) {
				// Have point
				task.point = points.get(0);
			}
			if (task.type == TaskBean.TYPE_PATH && points.size() > 0) {
				// Have points
				task.points = points;
			}
			return task;
		}
	};

	public Binder getService() {
		return stub;
	}

	private void setLocationStatus(String locationStatus) {
		this.locationStatus = locationStatus;
		synchronized (locationStatusListeners) { // Lock
			for (LocationStatusListener listener : locationStatusListeners) {
				// Report status
				listener.changed(locationStatus);
			}
		}
	}

	public void addLocationStatusListener(LocationStatusListener listener) {
		synchronized (locationStatusListeners) { // Lock
			if (!locationStatusListeners.contains(listener)) { // Don't have
				locationStatusListeners.add(listener);
			}
		}
	}

	public void removeLocationStatusListener(LocationStatusListener listener) {
		synchronized (locationStatusListeners) { // Lock
			locationStatusListeners.remove(listener);
		}
	}

	public String getLocationStatus() {
		return locationStatus;
	}
}
