package org.kvj.quebec4.ui.widget;

import org.kvj.quebec4.R;
import org.kvj.quebec4.service.DrawingController;
import org.kvj.quebec4.service.Q4App;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class LinesBackground extends View {

	private static final String TAG = "Lines";
	static int[] widths = { 30, 50, 80 };

	public LinesBackground(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Log.i(TAG, "Draw: " + getWidth() + ", " + getHeight());
		Paint paint = new Paint();
		paint.setStrokeWidth(2);
		paint.setARGB(0x33, 255, 255, 255);
		paint.setStyle(Style.STROKE);
		boolean landScape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		paint.setPathEffect(new DashPathEffect(new float[] {
				DrawingController.dp2px(getContext(), 2),
				DrawingController.dp2px(getContext(), 4) }, 2));
		int step = 0;
		int size = Q4App.getInstance().getIntPreference(R.string.gridSize,
				R.string.gridSizeDefault);
		if (size <= 0 || size > widths.length) {
			return;
		}
		step = (int) DrawingController.dp2px(getContext(), widths[size - 1]);
		for (int x = step; x < getWidth(); x += step) {
			canvas.drawLine(x, landScape ? getHeight() : 0, x, landScape ? 0
					: getHeight(), paint);
		}
		for (int y = step; y < getHeight(); y += step) {
			canvas.drawLine(0, landScape ? getHeight() - y : y, getWidth(),
					landScape ? getHeight() - y : y, paint);
		}
	}

}
