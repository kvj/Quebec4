package org.kvj.quebec4.service.data;

import java.util.Date;

public class TaskBean {

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
}
