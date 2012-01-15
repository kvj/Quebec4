package org.kvj.quebec4.service;

import java.util.ArrayList;
import java.util.List;

import org.kvj.quebec4.R;
import org.kvj.quebec4.service.data.PointBean;
import org.kvj.quebec4.service.data.Q4DBHelper;
import org.kvj.quebec4.service.data.TaskBean;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;

public class Q4Controller {

	private Q4DBHelper db = null;

	public Q4Controller() {
		db = new Q4DBHelper(Q4App.getInstance());
		if (!db.open()) {
			db = null;
		}
	}

	public Integer createTask(TaskBean task) {
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

	List<TaskBean> getTasks(int... statuses) {
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

	public Integer createPoint(PointBean point) {
		if (null == db) {
			return null;
		}
		try {
			db.getDatabase().beginTransaction();
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
			db.getDatabase().setTransactionSuccessful();
			return point.id;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.getDatabase().endTransaction();
		}
		return null;
	}

	List<PointBean> getPoints(Integer taskID) {
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

	public void sendTasks() {
		try {
			List<TaskBean> readyTasks = getTasks(TaskBean.STATUS_READY);
			for (int i = 0; i < readyTasks.size(); i++) {
				TaskBean task = readyTasks.get(i);
				Intent createIntent = new Intent(
						"com.matburt.mobileorg.ng.CREATE");
				createIntent.putExtra("text", task.title);
				createIntent.putExtra(
						"todo",
						Q4App.getInstance()
								.getStringPreference(R.string.todoConfig,
										R.string.todoConfigDefault));
				createIntent.putExtra(
						"tags",
						Q4App.getInstance()
								.getStringPreference(R.string.tagsConfig,
										R.string.tagsConfigDefault));
				List<PointBean> points = getPoints(task.id);
				ArrayList<String> paramNames = new ArrayList<String>();
				ArrayList<String> paramValues = new ArrayList<String>();
				if (task.type == TaskBean.TYPE_POINT && points.size() > 0) {
					paramNames.add("COORDINATES");
					paramValues.add(pointToCoordinates(points.get(0)));
					paramNames.add("COORDINATE_DETAILS");
					paramValues.add(pointToPointDetails(points.get(0)));
				}
				createIntent.putStringArrayListExtra("properties_names",
						paramNames);
				createIntent.putStringArrayListExtra("properties_values",
						paramValues);
				createIntent.putExtra("callback", task.id);
				Q4App.getInstance().startService(createIntent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void wantLocation() {
		// TODO Auto-generated method stub

	}

}
