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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_connect:
			try {
				bluetooth();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		case R.id.action_clear:
			if (MainActivity.isConnected == true) {
				PrintWriter writer = new PrintWriter(MainActivity.outputStream);
				writer.write("[0,0]");
				writer.flush();
			} else {
				Context context = this;
				CharSequence text = "Must connect to device first!";
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	CustomPagerAdapter mCustomPagerAdapter;
	SwipeToggleableViewPager mViewPager;
	
	class CustomPagerAdapter extends FragmentPagerAdapter {

		Context mContext;

		public CustomPagerAdapter(FragmentManager fragmentManager,
				Context context) {
			super(fragmentManager);
			mContext = context;
		}

		@Override
		public Fragment getItem(int position) {

			switch (position) {
			case 0:
				return new PresetFragment();
			case 1:
				return new LiveDrawFragment();
			case 2:
				return new PhotoFragment();
			}

			return null;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return "Presets";
			case 1:
				return "Free Draw";
			case 2:
				return "Take Photo";
			}
			return "";
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mCustomPagerAdapter = new CustomPagerAdapter(
				getSupportFragmentManager(), this);

		mViewPager = (SwipeToggleableViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mCustomPagerAdapter);

		/*
		 * Button connectButton = (Button) findViewById(R.id.connectButton);
		 * connectButton.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { try { bluetooth(); } catch
		 * (IOException ioe) { ioe.printStackTrace(); } catch
		 * (IllegalAccessException ioe) { ioe.printStackTrace(); } catch
		 * (IllegalArgumentException ioe) { ioe.printStackTrace(); } catch
		 * (InvocationTargetException ioe) { ioe.printStackTrace(); } catch
		 * (NoSuchMethodException ioe) { ioe.printStackTrace(); } } });
		 */
		/*
		 * Button livedrawButton = (Button) findViewById(R.id.livedrawButton);
		 * livedrawButton.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { if (isConnected == true) {
		 * Intent intent = new Intent(MainActivity.this,
		 * LiveDrawFragment.class); startActivity(intent); } else { Context
		 * context = getApplicationContext(); CharSequence text =
		 * "Must connect to device first!"; int duration = Toast.LENGTH_SHORT;
		 * 
		 * Toast toast = Toast.makeText(context, text, duration); toast.show();
		 * } } });
		 */
	}

	// create bluetooth connection (maybe...will need testing)
	public static OutputStream outputStream;
	public static InputStream inStream;
	public static boolean isConnected = false;
	public static boolean swipeable = true;

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
