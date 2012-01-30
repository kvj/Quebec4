package org.kvj.quebec4.ui.widget;

import org.kvj.quebec4.R;
import org.kvj.quebec4.service.Q4Controller;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class PageNavigator extends View {

	private Q4Controller controller = null;
	private static final float lineWidth = 2;
	Animation animation = null;

	public PageNavigator(Context context, AttributeSet attrs) {
		super(context, attrs);
		animation = AnimationUtils
				.loadAnimation(context, R.anim.navigator_fade);
		animation.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub

			}

			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub

			}

			public void onAnimationEnd(Animation arg0) {
				setVisibility(GONE);
			}
		});
	}

	public void setController(Activity activity, Q4Controller controller) {
		this.controller = controller;
		showNavigator(activity);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (null == controller) {
			return;
		}
		float width = getWidth() / 3;
		float height = getHeight() / 3;
		int cols = controller.getDrawing().getColCount(getContext());
		float pwidth = width / cols;
		int rows = controller.getDrawing().getRowCount(getContext());
		float pheight = height / rows;
		Paint pagePaint = new Paint();
		pagePaint.setARGB(0x88, 0xff, 0xff, 0xff);
		pagePaint.setStyle(Style.FILL);
		int selectedCol = controller.getDrawing().getCol(getContext());
		int selectedRow = controller.getDrawing().getRow(getContext());
		canvas.drawRect(width + selectedCol * pwidth, height + selectedRow
				* pheight, width + (selectedCol + 1) * pwidth, height
				+ (selectedRow + 1) * pheight, pagePaint);
		Paint linePaint = new Paint();
		linePaint.setARGB(0xff, 0xee, 0xee, 0xee);
		linePaint.setStyle(Style.STROKE);
		linePaint.setStrokeWidth(lineWidth);
		for (int i = 0; i < cols; i++) {
			canvas.drawLine(width + i * pwidth, height, width + i * pwidth,
					2 * height, linePaint);
		}
		canvas.drawLine(2 * width, height, 2 * width, 2 * height, linePaint);
		for (int i = 0; i < rows; i++) {
			canvas.drawLine(width, height + i * pheight, 2 * width, height + i
					* pheight, linePaint);
		}
		canvas.drawLine(width, 2 * height, 2 * width, 2 * height, linePaint);
	}

	public void showNavigator(final Activity activity) {
		if (getVisibility() != GONE) {
			animation.reset();
		} else {
			setVisibility(VISIBLE);
		}
		invalidate();
		startAnimation(animation);
	}
}
