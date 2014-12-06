package edu.tamu.csce462.etchasketcher;

import java.io.PrintWriter;
import java.util.ArrayList;

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
					PrintWriter writer = new PrintWriter(MainActivity.outputStream);
					writer.write("Live Draw: RealTime on test");
					writer.flush();
		        } else {
		           isRealTime = false;
					PrintWriter writer = new PrintWriter(MainActivity.outputStream);
					writer.write("Live Draw: RealTime off test");
					writer.flush();
		        }
		    }
		});
		
		Button submitButton = (Button) findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isRealTime == false) {
					PrintWriter writer = new PrintWriter(MainActivity.outputStream);
					writer.write("Live Draw: submit button test");
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
	        		writer.write("Live Draw: Touch at " + ev.getX() + "," + ev.getY());
	        		writer.flush();
	        	}
	        	else {
	        		
	        	}
	            return true;
	            
	        }
	    });
	}
	
	
}
