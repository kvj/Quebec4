package org.kvj.quebec4.ui;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kvj.bravo7.SuperActivity;
import org.kvj.quebec4.R;
import org.kvj.quebec4.service.Q4App;
import org.kvj.quebec4.service.Q4Controller;
import org.kvj.quebec4.service.Q4Service;
import org.kvj.quebec4.service.data.TaskBean;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class NewTask extends SuperActivity<Q4App, Q4Controller, Q4Service> {

	protected static final String TAG = "NewTask";
	EditText title = null, interval = null, time = null, points = null;
	Button calcInterval = null;
	RadioGroup locationType = null;
	ViewGroup pathOptions = null;
	private static Pattern timePattern = Pattern
			.compile("^(\\d{1,2})(\\:(\\d{1,2}))?$");

	private static final int TAKE_PHOTO = Activity.RESULT_FIRST_USER + 1;
	private static final int SELECT_PHOTO = Activity.RESULT_FIRST_USER + 2;
	private String photoFile = null;
	private String media = null;

	public NewTask() {
		super(Q4Service.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_task);
		title = (EditText) findViewById(R.id.new_title);
		interval = (EditText) findViewById(R.id.new_interval);
		time = (EditText) findViewById(R.id.helper_time);
		points = (EditText) findViewById(R.id.helper_points);
		calcInterval = (Button) findViewById(R.id.helper_calc);
		calcInterval.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				calcInterval();
			}
		});
		locationType = (RadioGroup) findViewById(R.id.new_location);
		locationType.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(RadioGroup group, int checkedId) {
				locationChanged(checkedId);
			}
		});
		pathOptions = (ViewGroup) findViewById(R.id.path_options);
		int locationID = R.id.point_location;
		Intent intent = getIntent();
		if (null != intent && null != intent.getExtras()) {
			if ("path".equals(intent.getStringExtra("type"))) {
				locationID = R.id.path_location;
			}
			if ("camera".equals(intent.getStringExtra("type"))) {
				startCamera();
			}
		}
		locationType.check(locationID);

	}

	private void calcInterval() {
		String timeStr = time.getText().toString().trim();
		String pointsStr = points.getText().toString().trim();
		if (TextUtils.isEmpty(timeStr) || TextUtils.isEmpty(pointsStr)) {
			notifyUser("Data not entered");
			return;
		}
		Matcher m = timePattern.matcher(timeStr);
		if (null == m || !m.find()) {
			notifyUser("Invalid time entered");
			return;
		}
		int minutes = Integer.parseInt(m.group(1));
		if (null != m.group(3)) {
			minutes = minutes * 60 + Integer.parseInt(m.group(3));
		}
		int points = Integer.parseInt(pointsStr);
		if (minutes < 1 || minutes < points) {
			notifyUser("Time too small");
			return;
		}
		if (points < 1) {
			notifyUser("Points too few");
			return;
		}
		interval.setText(Integer.toString((int) Math.floor(minutes
				/ (double) points)));
	}

	private void locationChanged(int id) {
		boolean helperVisible = id == R.id.path_location;
		if (helperVisible) {
			pathOptions.setVisibility(View.VISIBLE);
		} else {
			pathOptions.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.new_menu, menu);
		return true;
	}

	private void openGallery() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, SELECT_PHOTO);
	}

	private void startCamera() {
		ContentValues values = new ContentValues();
		values.put(Media.TITLE, "Take photo:");

		values.put(Images.Media.MIME_TYPE, "image/jpeg");
		File file = new File(getExternalCacheDir(), Long.toString(System
				.currentTimeMillis()) + ".jpg");
		photoFile = file.getAbsolutePath();
		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
		startActivityForResult(i, TAKE_PHOTO);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent dt) {
		// Log.i(TAG, "onActivityResult: " + requestCode + ", " + resultCode);
		super.onActivityResult(requestCode, resultCode, dt);
		if (resultCode == Activity.RESULT_OK) {
			try {
				String photoPath = null;
				switch (requestCode) {
				case TAKE_PHOTO:
					photoPath = photoFile;
					break;
				case SELECT_PHOTO:
					Uri selectedImage = dt.getData();
					String[] filePathColumn = { MediaStore.Images.Media.DATA };

					Cursor cursor = getContentResolver().query(selectedImage,
							filePathColumn, null, null, null);
					cursor.moveToFirst();

					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
					photoPath = cursor.getString(columnIndex);
					cursor.close();
					break;
				}
				if (null != photoPath) {
					notifyUser("Photo will be uploaded");
					media = photoPath;
					Log.i(TAG, "Upload photo: " + media);
				} else {
					notifyUser("No photo was selected");
					return;
				}
			} catch (Exception e) {
				Log.e(TAG, "Error processing image", e);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_camera:
			startCamera();
			break;
		case R.id.menu_gallery:
			openGallery();
			break;
		case R.id.menu_create:
			save();
			break;
		}
		return true;
	}

	private void save() {
		String titleStr = title.getText().toString().trim();
		if (TextUtils.isEmpty(titleStr)) {
			notifyUser("Title is empty");
			return;
		}
		TaskBean task = new TaskBean();
		task.title = titleStr;
		task.media = media;
		switch (locationType.getCheckedRadioButtonId()) {
		case R.id.no_location:
			task.type = TaskBean.TYPE_NO_GEO;
			task.status = TaskBean.STATUS_READY;
			break;
		case R.id.point_location:
			task.type = TaskBean.TYPE_POINT;
			task.status = TaskBean.STATUS_CONSUME;
			break;
		case R.id.path_location:
			String intervalStr = interval.getText().toString();
			if (TextUtils.isEmpty(intervalStr)) {
				notifyUser("Interval is not set");
				return;
			}
			int intervalNum = Integer.parseInt(intervalStr);
			if (intervalNum < 1) {
				notifyUser("Interval is too small");
				return;
			}
			task.type = TaskBean.TYPE_PATH;
			task.status = TaskBean.STATUS_CONSUME;
			task.interval = intervalNum;
			break;
		}
		synchronized (controller) {
			Integer id = controller.createTask(task);
			if (null == id) {
				notifyUser("Task is not created");
				return;
			}
			controller.sendTasks();
			if (task.status == TaskBean.STATUS_CONSUME) {
				controller.wantLocation();
			}

		}
		finish();
	}

}
