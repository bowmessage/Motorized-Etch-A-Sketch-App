package edu.tamu.csce462.etchasketcher;

import java.io.PrintWriter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class PresetFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.preset_fragment, container,
				false);

		Spinner spinner = (Spinner) view.findViewById(R.id.spinner1);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				getActivity(), R.array.drawable_array,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);

		Button drawButton = (Button) view.findViewById(R.id.drawButton);
		drawButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (MainActivity.isConnected) {
					PrintWriter writer = new PrintWriter(
							MainActivity.outputStream);
					Spinner spinner = (Spinner) view
							.findViewById(R.id.spinner1);
					String selected = (String) spinner.getSelectedItem();

					String toWrite = "";
					if (selected.equals("Circle")) {
						toWrite = pointsStringForCircleOfRadius(100);
					} else if (selected.equals("Square")) {
						toWrite = "[100.0,100.0,100.0,500.0,500.0,500.0,500.0,100.0,100.0,100.0]";
					} else if (selected.equals("Triangle")) {
						toWrite = "[500,100,50,500,950,500,500,100";
					} else if (selected.equals("Puppy")) {
						toWrite = "puppy test";
					} else if (selected.equals("TAMU")) {
						toWrite = "[200,100,200,150,300,150,300,125,500,125,500,500,300,500,300,550,750,550,750,500,700,500,700,125,900,125,900,150,950,150,950,100,200,100]";
					} else if (selected.equals("Info")) {
						toWrite = "info test";
					} else if (selected.equals("Frame")) {
						toWrite = "[0,0,0,650,1000,650,1000,0,0,0]";
					}
					Log.d("", toWrite);
					writer.write(toWrite);
					writer.flush();
				} else {
					Context context = getActivity();
					CharSequence text = "Must connect to device first!";
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}
			}
		});
		/*
		 * Button clearButton = (Button) view.findViewById(R.id.clearButton);
		 * clearButton.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { if (MainActivity.isConnected
		 * == true) { PrintWriter writer = new PrintWriter(
		 * MainActivity.outputStream); writer.write("[0,0]"); writer.flush(); }
		 * else { Context context = getActivity(); CharSequence text =
		 * "Must connect to device first!"; int duration = Toast.LENGTH_SHORT;
		 * 
		 * Toast toast = Toast.makeText(context, text, duration); toast.show();
		 * } } });
		 */

		return view;
	}

	protected String pointsStringForCircleOfRadius(int radius) {
		StringBuilder ret = new StringBuilder();
		ret.append("[");
		for (float i = 0; i < 2 * Math.PI; i += Math.PI / 100) {
			String x = "" + (double) Math.round(Math.cos(i) * radius * 100000)
					/ 100000;
			String y = "" + (double) Math.round(Math.sin(i) * radius * 100000)
					/ 100000;

			ret.append(x + "," + y + ",");
		}
		ret.deleteCharAt(ret.length() - 1);// Get rid of that last comma
		ret.append("]");
		return ret.toString();
	}
}
