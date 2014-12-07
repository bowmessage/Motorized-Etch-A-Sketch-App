package edu.tamu.csce462.etchasketcher;

import java.io.PrintWriter;
import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

public class LiveDrawFragment extends Fragment {

	private boolean isRealTime;
	private ArrayList<Float> realtimeList = new ArrayList<Float>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.live_draw_fragment, container,
				false);

		ToggleButton realtimeToggle = (ToggleButton) view
				.findViewById(R.id.realtimeToggle);
		realtimeToggle
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							isRealTime = true;
							realtimeList.clear();
							realtimeList.trimToSize();
						} else {
							isRealTime = false;
						}
					}
				});

		Button submitButton = (Button) view.findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (MainActivity.isConnected) {

					if (isRealTime == false) {
						PrintWriter writer = new PrintWriter(
								MainActivity.outputStream);
						String submit_array = "[";
						for (int i = 0; i < realtimeList.size(); i++) {
							submit_array = submit_array
									+ String.valueOf(realtimeList.get(i)) + ",";
						}
						if (submit_array.length() > 2) {
							submit_array = submit_array.substring(0,
									submit_array.length() - 1);
						}
						submit_array = submit_array + "]";
						writer.write(submit_array);
						writer.flush();
						realtimeList.clear();
						realtimeList.trimToSize();
					} else {
						Context context = getActivity();
						CharSequence text = "Real Time mode must be OFF!";
						int duration = Toast.LENGTH_SHORT;

						Toast toast = Toast.makeText(context, text, duration);
						toast.show();
					}
				} else {
					Context context = getActivity();
					CharSequence text = "Must connect to device first!";
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}
			}
		});

		final View drawField = (View) view.findViewById(R.id.drawView);
		drawField.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent ev) {
				switch (ev.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// disable swiping when the button is touched
					MainActivity.swipeable = false;
					// the rest of the code...
					Log.d("", "Disable Swipe");
					break;
				case MotionEvent.ACTION_MOVE:

					break;
				case MotionEvent.ACTION_UP:
					// re enable swipping when the touch is stopped
					// the rest of the code...
					MainActivity.swipeable = true;
					Log.d("", "Enable Swipe");
					break;
				}

				v.performClick();

				if (MainActivity.isConnected) {
					if (isRealTime == true) {
						PrintWriter writer = new PrintWriter(
								MainActivity.outputStream);
						writer.write("[" + ev.getX() + "," + ev.getY() + "]");
						writer.flush();
					} else {
						realtimeList.add(ev.getX());
						realtimeList.add(ev.getY());
					}
					return true;
				} else {
					Context context = getActivity();
					CharSequence text = "Must connect to device first!";
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
					return true;
				}

			}
		});
		/*
		 * drawField.setOnDragListener(new OnDragListener(){
		 * 
		 * @Override public boolean onDrag(View v, DragEvent event) {
		 * 
		 * ViewParent parent = v.getParent(); // or get a reference to the
		 * ViewPager and cast it to ViewParent
		 * 
		 * parent.requestDisallowInterceptTouchEvent(true); return false; }
		 * 
		 * });
		 */

		return view;
	}
}
