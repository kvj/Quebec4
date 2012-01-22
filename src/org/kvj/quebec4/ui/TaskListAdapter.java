package org.kvj.quebec4.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kvj.quebec4.R;
import org.kvj.quebec4.service.Q4Controller;
import org.kvj.quebec4.service.data.PointBean;
import org.kvj.quebec4.service.data.TaskBean;

import android.content.Context;
import android.database.DataSetObserver;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class TaskListAdapter implements ListAdapter {

	private static final String TAG = "TaskList";
	List<TaskBean> data = new ArrayList<TaskBean>();
	DataSetObserver observer = null;
	Q4Controller controller = null;

	public int getCount() {
		return data.size();
	}

	public TaskBean getItem(int position) {
		return data.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) parent.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.task_item, parent, false);
		}
		TaskBean task = getItem(position);
		TextView titleView = (TextView) convertView
				.findViewById(R.id.task_item_title);
		TextView infoView = (TextView) convertView
				.findViewById(R.id.task_item_info);
		ImageView iconView = (ImageView) convertView
				.findViewById(R.id.task_item_icon);
		titleView.setText(task.title);
		switch (task.type) {
		case TaskBean.TYPE_NO_GEO:
		case TaskBean.TYPE_POINT:
			iconView.setImageResource(null == task.media ? R.drawable.a_point
					: R.drawable.a_gallery);
			infoView.setText("");
			break;
		case TaskBean.TYPE_PATH:
			iconView.setImageResource(R.drawable.a_path);
			infoView.setText(task.info);
			break;
		}
		return convertView;
	}

	public int getViewTypeCount() {
		return 1;
	}

	public boolean hasStableIds() {
		return false;
	}

	public boolean isEmpty() {
		return getCount() == 0;
	}

	public void registerDataSetObserver(DataSetObserver observer) {
		this.observer = observer;
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		this.observer = null;
	}

	public void setController(Context context, Q4Controller controller) {
		this.controller = controller;
		List<TaskBean> tasks = controller.getTasks(TaskBean.STATUS_CONSUME,
				TaskBean.STATUS_SLEEP, TaskBean.STATUS_CONSUME_AND_FINISH);
		synchronized (data) {
			data.clear();
			data.addAll(tasks);
			for (TaskBean task : data) {
				if (TaskBean.TYPE_PATH == task.type) {
					StringBuilder info = new StringBuilder();
					info.append("Every " + task.interval + " min. ");
					List<PointBean> points = controller.getPoints(task.id);
					if (points.size() > 0) {
						PointBean last = points.get(points.size() - 1);
						info.append("points: " + points.size());
						info.append(" last: "
								+ DateFormat.getTimeFormat(context).format(
										new Date(last.created)));
					}
					task.info = info.toString();
				}
			}
		}
		if (null != observer) {
			observer.onChanged();
		}
	}

	public boolean areAllItemsEnabled() {
		return true;
	}

	public boolean isEnabled(int position) {
		return true;
	}

}
