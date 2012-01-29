package org.kvj.quebec4.ui.widget;

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

	public LinesBackground(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Log.i(TAG, "Draw: " + getWidth() + ", " + getHeight());
		Paint paint = new Paint();
		paint.setStrokeWidth(1);
		paint.setARGB(0x33, 255, 255, 255);
		paint.setStyle(Style.STROKE);
		boolean landScape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		paint.setPathEffect(new DashPathEffect(new float[] { 3, 9 }, 2));
		for (int x = 75; x < getWidth(); x += 75) {
			canvas.drawLine(x, landScape ? getHeight() : 0, x, landScape ? 0
					: getHeight(), paint);
		}
		for (int y = 75; y < getHeight(); y += 75) {
			canvas.drawLine(0, landScape ? getHeight() - y : y, getWidth(),
					landScape ? getHeight() - y : y, paint);
		}
	}

}
