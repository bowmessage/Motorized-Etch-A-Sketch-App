package edu.tamu.csce462.etchasketcher;

import java.io.PrintWriter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class PresetFragment extends Fragment {
	Toast toast;

	

	public class PresetCanvasView extends View {

		private Paint paint;
		public String pathString;

		public PresetCanvasView(Context context) {
			super(context);
			paint = new Paint();
			pathString = "[]";
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			int x = getWidth();
			int y = getHeight();

			// Bg
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(Color.GRAY);
			canvas.drawPaint(paint);

			// Path
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(5);
			paint.setColor(Color.RED);
			canvas.drawPath(MainActivity.pathFromString(pathString), paint);

			// Use Color.parseColor to define HTML colors
			// paint.setColor(Color.parseColor("#CD5C5C"));
			// canvas.drawCircle(x / 2, y / 2, radius, paint);
		}
	}

	public String pathStringFromItemName(String item) {
		if (item.equals("Circle")) {
			return pointsStringForCircleOfRadius(1000);
		} else if (item.equals("Spiral")) {
			return pointsStringForSpiralOfRadius(1000);
		} else if (item.equals("Square")) {
			return "[100.0,100.0,100.0,500.0,500.0,500.0,500.0,100.0,100.0,100.0]";
		} else if (item.equals("Triangle")) {
			return "[500,100,50,500,950,500,500,100]";
		} else if (item.equals("Puppy")) {
			return "puppy test";
		} else if (item.equals("TAMU")) {
			return "[200,100,200,150,300,150,300,125,500,125,500,500,300,500,300,550,750,550,750,500,700,500,700,125,900,125,900,150,950,150,950,100,200,100]";
		} else if (item.equals("Info")) {
			return "info test";
		} else if (item.equals("Frame")) {
			return "[0,0,0,650,1000,650,1000,0,0,0]";
		} else
			return "[]";
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.preset_fragment, container,
				false);

		LinearLayout parentLayout = (LinearLayout) view
				.findViewById(R.id.preset_parent_layout);
		final PresetCanvasView canvasView = new PresetCanvasView(getActivity());
		parentLayout.addView(canvasView);

		final Spinner spinner = (Spinner) view.findViewById(R.id.spinner1);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				getActivity(), R.array.drawable_array,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				String itemName = (String) spinner.getItemAtPosition(position);
				canvasView.pathString = pathStringFromItemName(itemName);
				canvasView.invalidate();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}

		});

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

					String toWrite = pathStringFromItemName(selected);

					Log.d("", toWrite);
					writer.write(toWrite);
					writer.flush();
				} else {
					Context context = getActivity();
					CharSequence text = "Must connect to device first!";
					int duration = Toast.LENGTH_SHORT;
					if(toast != null) {
						toast.cancel();
					}
					toast = Toast.makeText(context, text, duration);
					toast.show();
				}
			}
		});

		return view;
	}

	public static final int numLoops = 10;

	protected String pointsStringForSpiralOfRadius(int radius) {
		StringBuilder ret = new StringBuilder();
		ret.append("[");
		double limit = numLoops * (2 * Math.PI + .3);
		double incr = Math.PI / 75;
		int numPts = (int) (limit / incr);
		int curPt = 0;
		for (float i = 0; i < limit; i += incr) {
			String x = ""
					+ (radius + (double) Math.round(Math.cos(i) * radius
							* curPt / numPts * 100000) / 100000);
			String y = ""
					+ (radius + (double) Math.round(Math.sin(i) * radius
							* curPt / numPts * 100000) / 100000);

			ret.append(x + "," + y + ",");
			curPt++;
		}
		ret.deleteCharAt(ret.length() - 1);// Get rid of that last comma
		ret.append("]");
		return ret.toString();
	}

	protected String pointsStringForCircleOfRadius(int radius) {
		StringBuilder ret = new StringBuilder();
		ret.append("[");
		for (double i = 3 * Math.PI / 2; i < (3.5 * Math.PI + .3); i += Math.PI / 100) {
			String x = ""
					+ (radius + (double) Math.round(Math.cos(i) * radius
							* 100000) / 100000);
			String y = ""
					+ (radius + (double) Math.round(Math.sin(i) * radius
							* 100000) / 100000);

			ret.append(x + "," + y + ",");
		}
		ret.deleteCharAt(ret.length() - 1);// Get rid of that last comma
		ret.append("]");
		return ret.toString();
	}
}
