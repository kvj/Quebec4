package org.kvj.quebec4.service.data;

import java.util.Date;

public class PointBean {
	public Integer id = null;
	public int taskID = 0;
	public long created = new Date().getTime();
	public double lon = 0;
	public double lat = 0;
	public double speed = 0;
	public double altitude = 0;
	public double accuracy = 0;
}
