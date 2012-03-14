package org.kvj.quebec4.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.kvj.bravo7.SuperActivity;
import org.kvj.quebec4.R;
import org.kvj.quebec4.service.DrawingController.PageDirection;
import org.kvj.quebec4.service.Q4App;
import org.kvj.quebec4.service.Q4Controller;
import org.kvj.quebec4.service.Q4Service;
import org.kvj.quebec4.service.data.TaskBean;
import org.kvj.quebec4.ui.widget.LinesBackground;
import org.kvj.quebec4.ui.widget.PageNavigator;
import org.kvj.quebec4.ui.widget.PenCatcher;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.LightingColorFilter;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;

public class DrawingPane extends SuperActivity<Q4App, Q4Controller, Q4Service> {

	private static final String TAG = "Drawing";
	private boolean menuShown = true;
	int[] itemsToHide = new int[] { R.id.drawing_bottom, R.id.drawing_left,
			R.id.drawing_right, R.id.drawing_top };
	int[] itemsToDarken = new int[] { R.id.drawing_bottom, R.id.drawing_left,
			R.id.drawing_right, R.id.drawing_top, R.id.drawing_erase,
			R.id.drawing_toggle };
	LinesBackground lines = null;
	PenCatcher pen = null;
	PageNavigator navigator = null;
	ImageButton eraseButton = null;
	ImageButton toggleButton = null;

	public DrawingPane() {
		super(Q4Service.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (VERSION.SDK_INT < 11) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
		setContentView(R.layout.drawing);
		if (VERSION.SDK_INT < 11) {
			for (int id : itemsToDarken) {
				findViewById(id).getBackground().setColorFilter(
						new LightingColorFilter(0xFFFFFFFF, 0xFF000000));
			}
		}
		lines = (LinesBackground) findViewById(R.id.drawing_bg);
		pen = (PenCatcher) findViewById(R.id.drawing_pen);
		navigator = (PageNavigator) findViewById(R.id.drawing_navigator);
		toggleButton = (ImageButton) findViewById(R.id.drawing_toggle);
		toggleButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				toggleMenu();
			}
		});
		eraseButton = (ImageButton) findViewById(R.id.drawing_erase);
		eraseButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				toggleErase();
			}
		});
		toggleMenu();
		((ImageButton) findViewById(R.id.drawing_top))
				.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						if (null == controller) {
							return;
						}
						movePageUp();
					}
				});
		((ImageButton) findViewById(R.id.drawing_bottom))
				.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						if (null == controller) {
							return;
						}
						movePageDown();
					}
				});
		((ImageButton) findViewById(R.id.drawing_left))
				.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						if (null == controller) {
							return;
						}
						movePageLeft();
					}
				});
		((ImageButton) findViewById(R.id.drawing_right))
				.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						if (null == controller) {
							return;
						}
						movePageRight();
					}
				});
	}

	private void question(String title, String message,
			final OnClickListener okClick) {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								okClick.onClick(null);
							}

						}).setNegativeButton("No", null).show();

	}

	private void pageChanged() {
		pen.pageChanged();
		navigator.showNavigator(this);
	}

	protected void movePageRight() {
		if (controller.getDrawing().getCol(this) < controller.getDrawing()
				.getColCount(this) - 1) {
			controller.getDrawing().selectColumn(this,
					controller.getDrawing().getCol(this) + 1);
			pageChanged();
		} else {
			question("Create column?", "Are sure want to create column?",
					new OnClickListener() {

						public void onClick(View v) {
							controller.getDrawing().addPage(DrawingPane.this,
									PageDirection.PageRight);
							pageChanged();
						}
					});
		}
	}

	protected void movePageLeft() {
		if (controller.getDrawing().getCol(this) > 0) {
			controller.getDrawing().selectColumn(this,
					controller.getDrawing().getCol(this) - 1);
			pageChanged();
		} else {
			question("Create column?", "Are sure want to create column?",
					new OnClickListener() {

						public void onClick(View v) {
							controller.getDrawing().addPage(DrawingPane.this,
									PageDirection.PageLeft);
							pageChanged();
						}
					});
		}
	}

	protected void movePageDown() {
		if (controller.getDrawing().getRow(this) < controller.getDrawing()
				.getRowCount(this) - 1) {
			controller.getDrawing().selectRow(this,
					controller.getDrawing().getRow(this) + 1);
			pageChanged();
		} else {
			question("Create row?", "Are sure want to create row?",
					new OnClickListener() {

						public void onClick(View v) {
							controller.getDrawing().addPage(DrawingPane.this,
									PageDirection.PageDown);
							pageChanged();
						}
					});
		}
	}

	protected void movePageUp() {
		if (controller.getDrawing().getRow(this) > 0) {
			controller.getDrawing().selectRow(this,
					controller.getDrawing().getRow(this) - 1);
			pageChanged();
		} else {
			question("Create row?", "Are sure want to create row?",
					new OnClickListener() {

						public void onClick(View v) {
							controller.getDrawing().addPage(DrawingPane.this,
									PageDirection.PageUp);
							pageChanged();
						}
					});
		}
	}

	private void toggleErase() {
		pen.setErasing(!pen.isErasing());
		eraseButton.setImageResource(pen.isErasing() ? R.drawable.a_d_drawing
				: R.drawable.a_d_erase);
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
		navigator.showNavigator(this);
	}

	@Override
	public void onController(Q4Controller controller) {
		if (null == this.controller) {
			pen.setController(controller);
			navigator.setController(this, controller);
		}
		this.controller = controller;
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Discard changes?")
				.setMessage("Are you sure want to discard changes?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								if (null != controller) {
									controller.clearDrawing();
								}
								finish();
							}

						}).setNegativeButton("No", null).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.drawing_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (null == controller) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.menu_d_clear:
			controller.getDrawing().clearPage();
			pageChanged();
			break;
		case R.id.menu_d_grid_0:
			Q4App.getInstance().setIntPreference(R.string.gridSize, 0);
			lines.invalidate();
			break;
		case R.id.menu_d_grid_1:
			Q4App.getInstance().setIntPreference(R.string.gridSize, 1);
			lines.invalidate();
			break;
		case R.id.menu_d_grid_2:
			Q4App.getInstance().setIntPreference(R.string.gridSize, 2);
			lines.invalidate();
			break;
		case R.id.menu_d_grid_3:
			Q4App.getInstance().setIntPreference(R.string.gridSize, 3);
			lines.invalidate();
			break;
		case R.id.menu_d_r_col:
			if (controller.getDrawing().getColCount(this) > 1) {
				question("Remove column?",
						"Are you sure want to remove column?",
						new OnClickListener() {

							public void onClick(View v) {
								controller.getDrawing().removeColumn(
										DrawingPane.this);
								pageChanged();
							}
						});
			}
			break;
		case R.id.menu_d_r_row:
			if (controller.getDrawing().getRowCount(this) > 1) {
				question("Remove row?", "Are you sure want to remove row?",
						new OnClickListener() {

							public void onClick(View v) {
								controller.getDrawing().removeRow(
										DrawingPane.this);
								pageChanged();
							}
						});
			}
			break;
		case R.id.menu_d_width_0:
			Q4App.getInstance().setIntPreference(R.string.penSize, 0);
			lines.invalidate();
			break;
		case R.id.menu_d_width_1:
			Q4App.getInstance().setIntPreference(R.string.penSize, 1);
			lines.invalidate();
			break;
		case R.id.menu_d_width_2:
			Q4App.getInstance().setIntPreference(R.string.penSize, 2);
			lines.invalidate();
			break;
		case R.id.menu_d_save:
			getDrawingName();
			break;
		}
		return true;
	}

	private void getDrawingName() {
		final EditText textView = new EditText(this);
		textView.setSingleLine();
		textView.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
		AlertDialog.Builder dialog = new Builder(this);
		dialog.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle("Drawing name").setView(textView);
		dialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				save(textView.getText().toString().trim());
			}

		}).setNegativeButton("Cancel", null).show();

	}

	private void save(String title) {
		if (TextUtils.isEmpty(title)) {
			title = "Drawing ["
					+ (new SimpleDateFormat("yyyy-MM-dd EEE HH:mm",
							Locale.ENGLISH)).format(new Date()) + "]";
		}
		final TaskBean task = new TaskBean();
		task.title = title;
		task.type = TaskBean.TYPE_NO_GEO;
		task.status = TaskBean.STATUS_READY;
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage("Saving...");
		progressDialog.setCancelable(false);
		progressDialog.setIndeterminate(true);
		progressDialog.show();
		final File file = new File(SuperActivity.getExternalCacheFolder(this),
				"" + System.currentTimeMillis() + ".png");
		// Log.i(TAG, "Saving drawing to " + file.getAbsolutePath());
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				if (!controller.getDrawing().saveToFile(DrawingPane.this, file)) {
					return "Error saving drawing";
				}
				return null;
			}

			@Override
			protected void onPostExecute(String result) {
				progressDialog.dismiss();
				if (null != result) {
					notifyUser(result);
				} else {
					task.media = file.getAbsolutePath();
					synchronized (controller) {
						Integer id = controller.createTask(task, null);
						if (null == id) {
							notifyUser("Task is not created");
							return;
						}
					}
					controller.clearDrawing();
					setResult(RESULT_OK);
					finish();
				}
			};

		}.execute();
	}
}
