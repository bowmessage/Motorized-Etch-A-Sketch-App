package edu.tamu.csce462.etchasketcher;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class PhotoFragment extends Fragment {

	public class PhotoCanvasView extends View {

		private Paint paint;
		public String pathString;

		public PhotoCanvasView(Context context) {
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
		}
	}

	public String pathStringFromBitmap(Bitmap edges) {
		StringBuilder ret = new StringBuilder();
		ret.append("[");

		int width = edges.getWidth();
		int height = edges.getHeight();

		int[] pixels = new int[width * height];
		edges.getPixels(pixels, 0, width, 0, 0, width, height);

		int pts = 0;

		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] == 0xFFFFFFFF) { //Pixel is still white, let's check neighbors.
				int x = i % width;
				int y = i / width;
				ret.append(x + "," + y + ",");
				i += 10;
				pts++;
			}
		}

		Log.d("", "Points in the image's path:" + pts);

		ret.deleteCharAt(ret.length() - 1);// Get rid of that last comma
		ret.append("]");
		return ret.toString();
	}

	static final int REQUEST_IMAGE_CAPTURE = 1;
	ImageView mImageView;
	ImageView resultImageView;
	PhotoCanvasView canvasView;

	String mCurrentPhotoFilePath;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.photo_fragment, container, false);

		LinearLayout parentLayout = (LinearLayout) view
				.findViewById(R.id.photo_parent_layout);
		canvasView = new PhotoCanvasView(getActivity());
		parentLayout.addView(canvasView);

		mImageView = (ImageView) view.findViewById(R.id.imageView1);
		resultImageView = (ImageView) view.findViewById(R.id.imageView2);

		Button takePictureButton = (Button) view
				.findViewById(R.id.takePictureButton);
		takePictureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dispatchTakePictureIntent();
			}
		});

		Button drawPictureButton = (Button) view
				.findViewById(R.id.drawPictureButton);
		drawPictureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (MainActivity.isConnected) {
					PrintWriter writer = new PrintWriter(
							MainActivity.outputStream);

					String toWrite = canvasView.pathString;

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

		return view;
	}

	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent
				.resolveActivity(getActivity().getPackageManager()) != null) {
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

	private File createImageFile() throws IOException {
		// Create an image file name
		// String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
		// Locale.US).format(new Date());
		// String imageFileName = "JPEG_" + timeStamp + "_";
		String imageFileName = "imageBeforeEdgeDection";
		File storageDir = getActivity().getExternalFilesDir(null);
		File image = File.createTempFile(imageFileName, /* prefix */
				".jpg", /* suffix */
				storageDir /* directory */
		);

		// Save a file: path for use with ACTION_VIEW intents
		mCurrentPhotoFilePath = image.getAbsolutePath();
		return image;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE
				&& resultCode == Activity.RESULT_OK) {
			setPic();
		}

	}

	private void setPic() { // Get the dimensions of the View
		int targetW = mImageView.getWidth();
		int targetH = mImageView.getHeight();

		// Get the dimensions of the bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoFilePath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		// Determine how much to scale down the image
		int scaleFactor = Math.min(photoW / (targetW / 1), photoH
				/ (targetH / 1));

		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		// bmOptions.inPurgeable = true;

		Bitmap scaledBitmap = BitmapFactory.decodeFile(mCurrentPhotoFilePath,
				bmOptions);

		try {
			ExifInterface exif = new ExifInterface(mCurrentPhotoFilePath);
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
			Bitmap result = edgeDetector.getEdgesImage();
			resultImageView.setImageBitmap(result);
			canvasView.pathString = pathStringFromBitmap(result);
			canvasView.invalidate();

		} catch (IOException e) { // print the stack trace and abort the
									// rotation, just scale.
									// e.printStackTrace();
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

}
