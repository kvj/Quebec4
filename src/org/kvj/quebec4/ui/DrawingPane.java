package org.kvj.quebec4.ui;

import org.kvj.bravo7.SuperActivity;
import org.kvj.quebec4.R;
import org.kvj.quebec4.service.Q4App;
import org.kvj.quebec4.service.Q4Controller;
import org.kvj.quebec4.service.Q4Service;
import org.kvj.quebec4.ui.widget.LinesBackground;
import org.kvj.quebec4.ui.widget.PenCatcher;

import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

public class DrawingPane extends SuperActivity<Q4App, Q4Controller, Q4Service> {

	private static final String TAG = "Drawing";
	private boolean menuShown = true;
	int[] itemsToHide = new int[] { R.id.drawing_bottom, R.id.drawing_left,
			R.id.drawing_right, R.id.drawing_top };
	LinesBackground lines = null;
	PenCatcher pen = null;

	public DrawingPane() {
		super(Q4Service.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drawing);
		lines = (LinesBackground) findViewById(R.id.drawing_bg);
		pen = (PenCatcher) findViewById(R.id.drawing_pen);
		Button toggle = (Button) findViewById(R.id.drawing_toggle);
		toggle.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				toggleMenu();
			}
		});
		toggleMenu();
	}

	private void toggleMenu() {
		menuShown = !menuShown;
		if (menuShown) {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			if (VERSION.SDK_INT >= 11) {
				getActionBar().show();
			}
			for (int i = 0; i < itemsToHide.length; i++) {
				findViewById(itemsToHide[i]).setVisibility(View.VISIBLE);
			}
		} else {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			if (VERSION.SDK_INT >= 11) {
				getActionBar().hide();
			}
			for (int i = 0; i < itemsToHide.length; i++) {
				findViewById(itemsToHide[i]).setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onController(Q4Controller controller) {
		if (null == this.controller) {
			pen.setController(controller);
		}
		this.controller = controller;
	}

	@Override
	public void onBackPressed() {
		if (null != controller) {
			controller.clearDrawing();
		}
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.drawing_menu, menu);
		return true;
	}
}
