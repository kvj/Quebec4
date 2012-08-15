package org.kvj.quebec4.data;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class PointBean implements Parcelable {

	public Integer id = null;
	public int taskID = 0;
	public long created = new Date().getTime();
	public double lon = 0;
	public double lat = 0;
	public double speed = 0;
	public double altitude = 0;
	public double accuracy = 0;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(created);
		dest.writeDouble(lon);
		dest.writeDouble(lat);
		dest.writeDouble(speed);
		dest.writeDouble(altitude);
		dest.writeDouble(accuracy);
	}

	public static final Parcelable.Creator<PointBean> CREATOR = new Creator<PointBean>() {

		@Override
		public PointBean createFromParcel(Parcel source) {
			PointBean bean = new PointBean();
			bean.created = source.readLong();
			bean.lon = source.readDouble();
			bean.lat = source.readDouble();
			bean.speed = source.readDouble();
			bean.altitude = source.readDouble();
			bean.accuracy = source.readDouble();
			return bean;
		}

		@Override
		public PointBean[] newArray(int size) {
			return new PointBean[size];
		}
	};
}
