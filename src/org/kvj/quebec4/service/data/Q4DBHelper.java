package org.kvj.quebec4.service.data;

import org.kvj.bravo7.DBHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class Q4DBHelper extends DBHelper {

	public Q4DBHelper(Context context) {
		super(context, "q4", 1);
	}

	@Override
	public void migrate(SQLiteDatabase db, int version) {
		switch (version) {
		case 1:
			db.execSQL("create table tasks (id integer primary key autoincrement, "
					+ "title text, "
					+ "type integer, "
					+ "status integer, "
					+ "created integer, "
					+ "interval integer, "
					+ "accuracy integer, " + "media text)");
			db.execSQL("create table points (id integer primary key autoincrement, "
					+ "task_id integer, "
					+ "created integer, "
					+ "lon real, "
					+ "lat real, "
					+ "speed real, "
					+ "altitude real, "
					+ "accuracy real)");
			break;
		}
	}

}
