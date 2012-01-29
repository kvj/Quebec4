package org.kvj.quebec4.ui.widget;

import org.kvj.quebec4.service.Q4Controller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class PenCatcher extends View implements OnTouchListener {

	private static final String TAG = "Pen";
	Canvas c = null;
	int x = -1, y = -1;
	int strokeWidth = 5;

	enum DrawState {
		Wait, Draw, Erase
	};

	DrawState drawState = DrawState.Wait;
	private Q4Controller controller = null;

	public PenCatcher(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnTouchListener(this);
		Log.i(TAG, "Created: " + getWidth() + ", " + getHeight());
	}

	public boolean onTouch(View view, MotionEvent event) {
		final int pointerCount = event.getPointerCount();
		if (MotionEvent.ACTION_DOWN == event.getAction() && pointerCount == 1) {
			drawState = DrawState.Draw;
			x = (int) event.getX();
			y = (int) event.getY();
			return true;
		}
		if (MotionEvent.ACTION_MOVE == event.getAction()
				&& drawState == DrawState.Draw) {
			Paint p = new Paint();
			p.setStyle(Style.STROKE);
			p.setARGB(255, 0xdd, 0xdd, 0xdd);
			p.setStrokeWidth(strokeWidth);
			p.setStrokeCap(Cap.SQUARE);
			int toX = (int) event.getX();
			int toY = (int) event.getY();
			float[] points = new float[] { x, y, toX, toY };
			Matrix m = controller.getDrawing().getMatrix(this);
			Matrix inv = new Matrix();
			if (m.invert(inv)) {
				inv.mapPoints(points);
			}
			c.drawLine(points[0], points[1], points[2], points[3], p);
			Rect rect = new Rect();
			rect.left = Math.min(x, toX) - strokeWidth;
			rect.top = Math.min(y, toY) - strokeWidth;
			rect.right = Math.max(x, toX) + strokeWidth;
			rect.bottom = Math.max(y, toY) + strokeWidth;
			invalidate(rect);
			x = toX;
			y = toY;
			return true;
		}
		if (MotionEvent.ACTION_UP == event.getAction()) {
			drawState = DrawState.Wait;
			return true;
		}
		// Log.i(TAG, "Touch: " + event.getAction() + ", " + pointerCount);
		// Log.i(TAG, "At time: " + event.getEventTime());
		// for (int p = 0; p < pointerCount; p++) {
		// Log.i(TAG, String.format("  pointer %d: (%1.2f,%1.2f)",
		// event.getPointerId(p), event.getX(p), event.getY(p)));
		// }
		return true;
	}

	@Override
	protected void onAttachedToWindow() {
		Log.i(TAG, "Attached: " + getWidth() + ", " + getHeight());
		super.onAttachedToWindow();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (null != controller) {
			canvas.drawBitmap(controller.getDrawing().getPage(), controller
					.getDrawing().getMatrix(this), new Paint());
		}
	}

	public void setController(Q4Controller controller) {
		this.controller = controller;
		c = new Canvas();
		c.setBitmap(controller.getDrawing().getPage());
		invalidate();
	}

}
