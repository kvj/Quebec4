package org.kvj.quebec4.service;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.view.View;

public class DrawingController {

	private static final String TAG = "DrawingController";
	int width = 0;
	int height = 0;
	Bitmap currentPage = null;
	List<List<Bitmap>> pages = new ArrayList<List<Bitmap>>();
	int columns = 0;
	int selectedColumn = -1;
	int selectedRow = -1;

	public DrawingController(Context context) {
		width = context.getResources().getDisplayMetrics().widthPixels;
		height = context.getResources().getDisplayMetrics().heightPixels;
		if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			int t = width;
			width = height;
			height = t;
		}
		// Log.i(TAG, "New drawing!: " + width + ", " + height);
		currentPage = Bitmap.createBitmap(width, height, Config.ARGB_4444);
	}

	public Bitmap getPage() {
		return currentPage;
	}

	public Matrix getMatrix(View view) {
		Matrix matrix = new Matrix();
		if (view.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			matrix.setRotate(-90);
			matrix.postTranslate(0, view.getHeight());
		}
		return matrix;
	}

	public int getRowCount() {
		return pages.size();
	}

	public int getColCount() {
		return columns;
	}

}
