package org.kvj.quebec4.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class TaskBean implements Parcelable {

	public static final int TYPE_NO_GEO = 0;
	public static final int TYPE_POINT = 1;
	public static final int TYPE_PATH = 2;

	public static final int STATUS_READY = 0;
	public static final int STATUS_SENT = 1;
	public static final int STATUS_SLEEP = 2;
	public static final int STATUS_CONSUME = 3;
	public static final int STATUS_CONSUME_AND_FINISH = 4;

	public Integer id = null;
	public String title = null;
	public int type = TYPE_NO_GEO;
	public int status = STATUS_READY;
	public long created = new Date().getTime();
	public int interval = 0;
	public int accuracy = 0;
	public String media = null;
	public String info = "";

	public PointBean point = null;
	public List<PointBean> points = null;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(title);
		dest.writeInt(status);
		dest.writeLong(created);
		dest.writeString(null != media ? media : "");
		dest.writeInt(type);
		if (type == TYPE_POINT && null != point) { // Have point
			dest.writeInt(1); // Have point
			dest.writeParcelable(point, flags);
		} else if (type == TYPE_PATH && null != points) { // Have path
			dest.writeInt(1); // Have points
			dest.writeList(points);
		} else { // Defult - no geo
			dest.writeInt(0); // No additional data
		}
	}

	public static final Parcelable.Creator<TaskBean> CREATOR = new Creator<TaskBean>() {

		@Override
		public TaskBean[] newArray(int size) {
			return new TaskBean[size];
		}

		@Override
		public TaskBean createFromParcel(Parcel source) {
			TaskBean bean = new TaskBean();
			bean.id = source.readInt();
			bean.title = source.readString();
			bean.status = source.readInt();
			bean.created = source.readLong();
			bean.media = source.readString();
			bean.type = source.readInt();
			boolean haveData = 1 == source.readInt();
			if (haveData) { // Have data - read point(s)
				if (bean.type == TYPE_POINT) { // Read point
					bean.point = source.readParcelable(TaskBean.class
							.getClassLoader());
				} else if (bean.type == TYPE_PATH) { // Read points
					bean.points = new ArrayList<PointBean>();
					source.readList(bean.points,
							TaskBean.class.getClassLoader());
				}
			}
			return bean;
		}
	};
}
