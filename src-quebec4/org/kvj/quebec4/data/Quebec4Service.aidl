package org.kvj.quebec4.data;

import org.kvj.quebec4.data.TaskBean;

interface Quebec4Service {
	List<TaskBean> getTasks();
	boolean removeTask(in TaskBean task);
	TaskBean getTask(int id);	
}