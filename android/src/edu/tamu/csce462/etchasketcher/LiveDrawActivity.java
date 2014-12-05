package edu.tamu.csce462.etchasketcher;

import java.io.PrintWriter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

public class LiveDrawActivity extends Activity {

	private boolean isRealTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.live_draw_activity);
		
		ToggleButton realtimeToggle = (ToggleButton) findViewById(R.id.realtimeToggle);
		realtimeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if (isChecked) {
		           isRealTime = true;
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
					writer.write("Live Draw: submit test");
					writer.flush();
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
	}
	
	
}
