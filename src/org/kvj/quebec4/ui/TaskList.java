package org.kvj.quebec4.ui;

import org.kvj.bravo7.ControllerConnector;
import org.kvj.bravo7.ControllerConnector.ControllerReceiver;
import org.kvj.quebec4.R;
import org.kvj.quebec4.service.Q4App;
import org.kvj.quebec4.service.Q4Controller;
import org.kvj.quebec4.service.Q4Service;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class TaskList extends ListActivity implements
		ControllerReceiver<Q4Controller> {

	Q4Controller controller = null;
	ControllerConnector<Q4App, Q4Controller, Q4Service> cc = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.tasks_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent newIntent = new Intent(this, NewTask.class);
		switch (item.getItemId()) {
		case R.id.menu_new_text:
			newIntent.putExtra("type", "text");
			startActivity(newIntent);
			break;
		case R.id.menu_new_camera:
			newIntent.putExtra("type", "camera");
			startActivity(newIntent);
			break;
		case R.id.menu_new_path:
			newIntent.putExtra("type", "path");
			startActivity(newIntent);
			break;
		}
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		cc = new ControllerConnector<Q4App, Q4Controller, Q4Service>(this, this);
		cc.connectController(Q4Service.class);
	}

	public void onController(Q4Controller controller) {
		this.controller = controller;
	}

	@Override
	protected void onStop() {
		super.onStop();
		cc.disconnectController();
	}
}