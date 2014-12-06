package edu.tamu.csce462.etchasketcher;

import java.io.PrintWriter;
import java.util.ArrayList;

import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

public class LiveDrawActivity extends Activity {

	private boolean isRealTime;
	private ArrayList<Integer> realtimeList = new ArrayList<Integer>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.live_draw_activity);
		
		ToggleButton realtimeToggle = (ToggleButton) findViewById(R.id.realtimeToggle);
		realtimeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if (isChecked) {
		           isRealTime = true;
		           realtimeList.clear();
		           realtimeList.trimToSize();
		        } else {
		           isRealTime = false;
		        }
		    }
		});
		
		Button submitButton = (Button) findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isRealTime == false) {
					PrintWriter writer = new PrintWriter(MainActivity.outputStream);
					String submit_array = "[";
					for(int i = 0; i < realtimeList.size(); i++) {
						submit_array = submit_array + String.valueOf(realtimeList.get(i)) + ",";
					}
					if(submit_array.length() > 2) {
						submit_array = submit_array.substring(0, submit_array.length()-1);
					}
					submit_array = submit_array + "]";
					writer.write(submit_array);
					writer.flush();
					realtimeList.clear();
					realtimeList.trimToSize();
				}
				else {
					Context context = getApplicationContext();
					CharSequence text = "Real Time mode must be OFF!";
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}
			}
		});	

	    final RelativeLayout parent = (RelativeLayout) findViewById(R.id.RelativeLayout);
	    parent.setOnTouchListener(new OnTouchListener() {
	        public boolean onTouch(View v, MotionEvent ev) {
	        	if (isRealTime == true) {
					PrintWriter writer = new PrintWriter(MainActivity.outputStream);
					writer.write("[" + ev.getX() + "," + ev.getY() + "]");
					writer.flush();
	        	}
	        	else {
	        		realtimeList.add((int) ev.getX());
	        		realtimeList.add((int) ev.getY());
	        	}
	            return true;
	            
	        }
	    });
	}
	
	
}
