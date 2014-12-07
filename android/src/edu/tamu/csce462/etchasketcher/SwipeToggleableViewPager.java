package edu.tamu.csce462.etchasketcher;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class SwipeToggleableViewPager extends ViewPager {
	public SwipeToggleableViewPager(Context context) {
		super(context);
	}

	public SwipeToggleableViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		return (MainActivity.swipeable) ? super.onInterceptTouchEvent(arg0) : false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return (MainActivity.swipeable) ? super.onTouchEvent(event) : false;
	}
}
