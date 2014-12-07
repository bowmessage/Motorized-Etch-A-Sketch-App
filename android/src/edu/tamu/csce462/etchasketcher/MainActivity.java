package edu.tamu.csce462.etchasketcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Scanner;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity {

	ImageView mImageView;
	ImageView resultImageView;

	String mCurrentPhotoPath;

	private File createImageFile() throws IOException {
		// Create an image file name
		// String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
		// Locale.US).format(new Date());
		// String imageFileName = "JPEG_" + timeStamp + "_";
		String imageFileName = "imageBeforeEdgeDection";
		File storageDir = getExternalFilesDir(null);
		File image = File.createTempFile(imageFileName, /* prefix */
				".jpg", /* suffix */
				storageDir /* directory */
		);

		// Save a file: path for use with ACTION_VIEW intents
		mCurrentPhotoPath = image.getAbsolutePath();
		return image;
	}

	private void setPic() {
		// Get the dimensions of the View
		int targetW = mImageView.getWidth();
		int targetH = mImageView.getHeight();

		// Get the dimensions of the bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		// Determine how much to scale down the image
		int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		Bitmap scaledBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath,
				bmOptions);

		try {
			ExifInterface exif = new ExifInterface(mCurrentPhotoPath);
			int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			int rotationInDegrees = exifToDegrees(rotation);

			Matrix matrix = new Matrix();
			if (rotation != 0f) {
				matrix.preRotate(rotationInDegrees);
			}

			Bitmap rotated = Bitmap.createBitmap(scaledBitmap, 0, 0,
					scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
					true);
			mImageView.setImageBitmap(rotated);

			CannyEdgeDetector edgeDetector = new CannyEdgeDetector();
			edgeDetector.setSourceImage(rotated);
			edgeDetector.process();
			resultImageView.setImageBitmap(edgeDetector.getEdgesImage());

		} catch (IOException e) {
			// print the stack trace and abort the rotation, just scale.
			e.printStackTrace();
			mImageView.setImageBitmap(scaledBitmap);
		}

	}

	private static int exifToDegrees(int exifOrientation) {
		if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
			return 90;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
			return 180;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
			return 270;
		}
		return 0;
	}

	/*
	 * private BufferedImage bufferedImageFromBitmap(Bitmap in){
	 * 
	 * 
	 * BufferedImage ret = new BufferedImage(in.getWidth(), in.getHeight(),
	 * BufferedImage.TYPE_INT_ARGB);
	 * 
	 * int[] buffer = new int[in.getWidth() * in.getHeight()];
	 * in.getPixels(buffer, 0, 0, 0, 0, in.getWidth(), in.getHeight());
	 * ret.setRGB(0, 0, in.getWidth(), in.getHeight(), buffer, 0,
	 * in.getWidth());
	 * 
	 * return ret; }
	 * 
	 * private Bitmap bitampFromBufferedImage(BufferedImage in){ int[] buffer =
	 * new int[in.getWidth() * in.getHeight()]; in.getRGB(0, 0, in.getWidth(),
	 * in.getHeight(), buffer, 0, in.getWidth()); return
	 * Bitmap.createBitmap(buffer,in.getWidth(),in.getHeight(),
	 * Bitmap.Config.ARGB_4444); }
	 */

	static final int REQUEST_IMAGE_CAPTURE = 1;

	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			// Create the File where the photo should go
			File photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException ex) {
				// Error occurred while creating the File
				ex.printStackTrace();
			}
			// Continue only if the File was successfully created
			if (photoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));
				startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			setPic();
		}

	}
	
	protected String pointsStringForCircleOfRadius(int radius){
		StringBuilder ret = new StringBuilder();
		ret.append("[");
		for(float i = 0; i < 2*Math.PI; i += Math.PI/100){
			String x = "" + (double) Math.round(Math.cos(i) * radius * 100000) / 100000;
			String y = "" + (double) Math.round(Math.sin(i) * radius * 100000) / 100000;
			
			ret.append(x + "," + y + ",");
		}
		ret.deleteCharAt(ret.length()-1);
		ret.append("]");
		return ret.toString();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.drawable_array,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);

		// generate various buttons...
		Button takePictureButton = (Button) findViewById(R.id.takePictureButton);
		// mImageView = (ImageView) findViewById(R.id.imageView1);
		// resultImageView = (ImageView) findViewById(R.id.imageView2);
		takePictureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dispatchTakePictureIntent();
			}
		});

		Button drawButton = (Button) findViewById(R.id.drawButton);
		drawButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (true) {
					//PrintWriter writer = new PrintWriter(outputStream);
					Spinner spinner = (Spinner) findViewById(R.id.spinner1);
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
					//writer.write(toWrite);
					//writer.flush();
				} else {
					Context context = getApplicationContext();
					CharSequence text = "Must connect to device first!";
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}
			}
		});

		Button clearButton = (Button) findViewById(R.id.clearButton);
		clearButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isConnected == true) {
					PrintWriter writer = new PrintWriter(outputStream);
					writer.write("[0,0]");
					writer.flush();
				} else {
					Context context = getApplicationContext();
					CharSequence text = "Must connect to device first!";
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}
			}
		});

		Button connectButton = (Button) findViewById(R.id.connectButton);
		connectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					bluetooth();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} catch (IllegalAccessException ioe) {
					ioe.printStackTrace();
				} catch (IllegalArgumentException ioe) {
					ioe.printStackTrace();
				} catch (InvocationTargetException ioe) {
					ioe.printStackTrace();
				} catch (NoSuchMethodException ioe) {
					ioe.printStackTrace();
				}
			}
		});

		Button livedrawButton = (Button) findViewById(R.id.livedrawButton);
		livedrawButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isConnected == true) {
					Intent intent = new Intent(MainActivity.this,
							LiveDrawActivity.class);
					startActivity(intent);
				} else {
					Context context = getApplicationContext();
					CharSequence text = "Must connect to device first!";
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}
			}
		});
	}

	// create bluetooth connection (maybe...will need testing)
	public static OutputStream outputStream;
	public static InputStream inStream;
	private boolean isConnected;

	private void bluetooth() throws IOException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException {
		BluetoothAdapter blue1 = BluetoothAdapter.getDefaultAdapter();
		if (blue1 != null) {
			if (blue1.isEnabled()) {

				Set<BluetoothDevice> boundedDevices = blue1.getBondedDevices();

				if (boundedDevices.size() > 0) {
					BluetoothDevice[] devices = boundedDevices
							.toArray(new BluetoothDevice[0]);
					BluetoothDevice rasp = null;
					for (int i = 0; i < devices.length; i++) {
						if (devices[i].getName().startsWith("rasp")) {
							rasp = devices[i];
							break;
						}
					}
					if (rasp != null) {
						Log.d("BLUETOOTH", rasp.getName());
						ParcelUuid[] uuids = rasp.getUuids();
						// UUID testUUID =
						// UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
						// BluetoothSocket socket =
						// rasp.createRfcommSocketToServiceRecord(uuids[0].getUuid());
						Method method = rasp.getClass()
								.getMethod("createRfcommSocket",
										new Class[] { int.class });
						BluetoothSocket socket = (BluetoothSocket) method
								.invoke(rasp, 1);
						// BluetoothSocket socket = rasp.createRfcommSocket(1);
						socket.connect();
						Log.d("BLUETOOTH", "Connection success, thus far: "
								+ socket.isConnected());
						outputStream = socket.getOutputStream();
						inStream = socket.getInputStream();
						Scanner scan = new Scanner(inStream);
						// if(scan.hasNext()){
						// Log.d("BLUETOOTH",scan.next());
						// }
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						PrintWriter writer = new PrintWriter(outputStream);
						writer.write("Connected to Pi.");
						isConnected = true;
						writer.flush();

					}

				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);


		return true;
	}

}
