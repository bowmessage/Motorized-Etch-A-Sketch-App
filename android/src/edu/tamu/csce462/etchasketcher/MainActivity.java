package edu.tamu.csce462.etchasketcher;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View.OnClickListener;
import android.bluetooth.*;
import android.os.*;

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
	    int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

	    // Decode the image file into a Bitmap sized to fill the View
	    bmOptions.inJustDecodeBounds = false;
	    bmOptions.inSampleSize = scaleFactor;
	    bmOptions.inPurgeable = true;

	    Bitmap scaledBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
	    
	    
	    try {
			ExifInterface exif = new ExifInterface(mCurrentPhotoPath);
			int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);  
			int rotationInDegrees = exifToDegrees(rotation);
			
			Matrix matrix = new Matrix();
			if (rotation != 0f) {matrix.preRotate(rotationInDegrees);}
			
			Bitmap rotated = Bitmap.createBitmap(scaledBitmap, 0,0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
			mImageView.setImageBitmap(rotated);
			
		   CannyEdgeDetector edgeDetector = new CannyEdgeDetector();
		   edgeDetector.setSourceImage(rotated);
		   edgeDetector.process();
		   resultImageView.setImageBitmap(edgeDetector.getEdgesImage());


		} catch (IOException e) {
			//print the stack trace and abort the rotation, just scale.
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//generate various buttons...
		Button takePictureButton = (Button) findViewById(R.id.takePictureButton);
		//mImageView = (ImageView) findViewById(R.id.imageView1);
		//resultImageView = (ImageView) findViewById(R.id.imageView2);
		takePictureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dispatchTakePictureIntent();
			}
		});
		
		Button drawCircleButton = (Button) findViewById(R.id.drawCircleButton);
		drawCircleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
		
		Button drawSquareButton = (Button) findViewById(R.id.drawSquareButton);
		drawSquareButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//sudo code
				//create array of strings
				//insert points 50,10; 50,50; 100,50; 100,10;
				//send array to bluetooth
			}
		});
		
		Button drawTriangleButton = (Button) findViewById(R.id.drawTriangleButton);
		drawTriangleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
		
		Button drawTAMUButton = (Button) findViewById(R.id.drawTAMUButton);
		drawTAMUButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
		
		Button drawPuppyButton = (Button) findViewById(R.id.drawPuppyButton);
		drawPuppyButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
		
		Button drawInfoButton = (Button) findViewById(R.id.drawInfoButton);
		drawInfoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
		
		Button clearButton = (Button) findViewById(R.id.clearButton);
		clearButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
	}
	
	//create bluetooth connection (maybe...will need testing)
	private OutputStream outputStream;
	private InputStream inStream;
	
	private void bluetooth() throws IOException {
		BluetoothAdapter blue1 = BluetoothAdapter.getDefaultAdapter();
		if(blue1 != null) {
			if(blue1.isEnabled()) {
				Set<BluetoothDevice> boundedDevices = blue1.getBondedDevices();
				
				if(boundedDevices.size() > 0) {
					BluetoothDevice[] devices = (BluetoothDevice[]) boundedDevices.toArray();
					BluetoothDevice device = devices[0];
					ParcelUuid[] uuids = device.getUuids();
					BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
					socket.connect();
					outputStream = socket.getOutputStream();
					inStream = socket.getInputStream();
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
