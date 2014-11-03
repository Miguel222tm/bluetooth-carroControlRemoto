package com.retrorat.v1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;








import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


public class MonitorActivity extends MyActivity implements SensorEventListener {

	private static final int REQUEST_DISCOVERY = 0x1;

	private final String TAG = "MonitorActivity";
	private Handler _handler = new Handler();
	private final int maxlength = 2048;
	private BluetoothDevice device = null;
	private BluetoothSocket socket = null;
	
	private OutputStream outputStream;
	private InputStream inputStream;
	
	private Object obj1 = new Object();
	private Object obj2 = new Object();
	private OnTouchListener OnTouchListener;
	public static boolean canRead = true;

	public static StringBuffer hexString = new StringBuffer();
	//movimiento
	private List<String> values = new ArrayList<String>();
	private boolean wait = true;
	private SensorManager smng;				//objeto de admon de sensor
	private Sensor magnetometro;			//objeto que vincula al sensor magnético
	private Sensor acelerometro;			//objeto que vincula al acelerómetro
	protected Vibrator vibr;
	private float[] matR = new float[9];	//matriz de rotación;
	private float[] matI = new float[9];	//matriz de inclinación;
	private float[] matO = new float[3];	//matriz de orientación;
	private float [] vAnterior= {0,0,0};
	private float lux;
	private float[] valoresMagn = new float[3];
	private float[] valoresAcel = new float[3];	
	int h, w;
	private int xcuad = 3;
	private int ycuad = 3;
	String txt3;
	String txt2;
	int Port;
	Bolita lienzo;
	Box lienzo2;
	int cont=0;
	int mensaje;
	ProgressDialog progressDoalog;
	
	public TextView imagenMostrar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		this.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.monitor);
		
		progressDoalog = new ProgressDialog(MonitorActivity.this);
		progressDoalog.setMax(100);
		progressDoalog.setMessage("Getting all the packets....");
		progressDoalog.setTitle("Conecting devices..");
		progressDoalog
				.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDoalog.show();
		new Thread(new Runnable() {
			public void run() {
				try {
					while (progressDoalog.getProgress() <= progressDoalog
							.getMax()) {
						Thread.sleep(200);
						handle.sendMessage(handle.obtainMessage());
						if (progressDoalog.getProgress() == progressDoalog
								.getMax()) {
							progressDoalog.dismiss();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		
		
		
		
		lienzo = (Bolita) findViewById(R.id.view3);
		lienzo2 = (Box) findViewById(R.id.view2);
		
		//movimiento
		
		h = lienzo.getHeight();
		w = lienzo.getWidth();
		
		smng = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		vibr= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		magnetometro = smng.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		acelerometro = smng.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
	
		
//termina movimiento
		BluetoothDevice finalDevice = this.getIntent().getParcelableExtra(
				BluetoothDevice.EXTRA_DEVICE);
		SocketApplication app = (SocketApplication) getApplicationContext();
		device = app.getDevice();
		Log.d(TAG, "test1");
		if (finalDevice == null) {
			if (device == null) {
				Log.d(TAG, "test2");
				Intent intent = new Intent(this, SearchDeviceActivity.class);
				startActivity(intent);
				finish();
				return;
			}
			Log.d(TAG, "test4");
		} else if (finalDevice != null) {
			Log.d(TAG, "test3");
			app.setDevice(finalDevice);
			device = app.getDevice();
		}
		new Thread() {
			public void run() {
				connect(device);
			};
		}.start();
		
		
	}
	
	/* after select, connect to device */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != REQUEST_DISCOVERY) {
			finish();
			return;
		}
		if (resultCode != RESULT_OK) {
			finish();
			return;
		}
		final BluetoothDevice device = data
				.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		new Thread() {
			public void run() {
				connect(device);
			};
		}.start();
	}

	protected void onDestroy() {
		super.onDestroy();
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			Log.e(TAG, ">>", e);
		}
	}
	Handler handle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			progressDoalog.incrementProgressBy(3);
			
		}
	};
	protected void connect(BluetoothDevice device) {
		try {
			Log.d(TAG, "³¢ÊÔÁ¬½Ó");
			// Create a Socket connection: need the server's UUID number of
			// registered
			Method m = device.getClass().getMethod("createRfcommSocket",
					new Class[] { int.class });
			socket = (BluetoothSocket) m.invoke(device, 1);
			socket.connect();
			Log.d(TAG, ">>Client connectted");
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			int read = -1;
			final byte[] bytes = new byte[2048];
			while (true) {
				synchronized (obj1) {
					read = inputStream.read(bytes);
					Log.d(TAG, "read:" + read);
					if (read > 0) {
						final int count = read;
						String str = SamplesUtils.byteToHex(bytes, count);
//						Log.d(TAG, "test1:" + str);
						String hex = hexString.toString();
						if (hex == "") {
							hexString.append("<--");
						} else {
							if (hex.lastIndexOf("<--") < hex.lastIndexOf("-->")) {
								hexString.append("\n<--");
							}
						}
						hexString.append(str);
						hex = hexString.toString();
//						Log.d(TAG, "test2:" + hex);
						if (hex.length() > maxlength) {
							try {
								hex = hex.substring(hex.length() - maxlength,
										hex.length());
								hex = hex.substring(hex.indexOf(" "));
								hex = "<--" + hex;
								hexString = new StringBuffer();
								hexString.append(hex);
							} catch (Exception e) {
								e.printStackTrace();
								Log.e(TAG, "e", e);
							}
						}
						_handler.post(new Runnable() {
							public void run() {
							}
						});
					}
				}
			}

		} catch (Exception e) {
			Log.e(TAG, ">>", e);
			Toast.makeText(getBaseContext(),
					getResources().getString(R.string.ioexception),
					Toast.LENGTH_SHORT).show();
			return;
		} finally {
			if (socket != null) {
				try {
					Log.d(TAG, ">>Client Socket Close");
					socket.close();
					socket = null;
					// this.finish();
					return;
				} catch (IOException e) {
					Log.e(TAG, ">>", e);
				}
			}
		}
	}
	

	@Override
	public void onResume() {
		super.onResume();
		smng.registerListener(this, magnetometro, SensorManager.SENSOR_DELAY_GAME);
		smng.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	public void onPause() {
		super.onPause();
		smng.unregisterListener(this, magnetometro);
		smng.unregisterListener(this, acelerometro);
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	
	public void onSensorChanged(SensorEvent evento) {
		// TODO Auto-generated method stub
	switch(evento.sensor.getType()){
	case Sensor.TYPE_LIGHT:
			
		//	text4.setText("Luz:"+String.valueOf(evento.values[0]));
			lux= evento.values[0];
			break;
			
			case Sensor.TYPE_ACCELEROMETER:
				for (int x=0; x < 3; x++) valoresAcel[x] = evento.values[x];
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:	
				for (int x=0; x < 3; x++) valoresMagn[x] = evento.values[x];
				break;	 
		}
		boolean exito = SensorManager.getRotationMatrix(matR,matI,valoresAcel,valoresMagn);
		//dolor de cabeza justo aqui empieza...
		if (exito && progressDoalog.getProgress()==100) {
			SensorManager.getOrientation(matR, matO);
			matO[0]= (float) Math.toDegrees(matO[0]);
				matO[1]=(float) -Math.toDegrees(matO[1]);
				matO[2]=(float) Math.toDegrees(matO[2]);
				vAnterior[0]= Math.round(filtro(matO[0], vAnterior[0]));
					
			lienzo.actualizar(matO[2],matO[1], vAnterior[0]);		
			boxMovement (matO[2], matO[1]);

		}
	}
	public float filtro(float vActual, float vAnterior){	
		float a= 0.8f; // valor entre 0 y 1
		return vAnterior* (1-a)+vActual*a;		
	}
	private void boxMovement (float x, float y) {
		x = x + 90;
		y = y + 90;
		w = lienzo.getWidth();
		h = lienzo.getHeight();
		boolean moved = false;
		x = x * w/180;
		y = y * h/270;
		int r = w/20;
		if ((xcuad < 5) && x > (xcuad*w/5 + r)) { xcuad++; moved = true;}
		else if ((xcuad > 1) && x < ((xcuad-1)*w/5 - r)) { xcuad--; moved = true; }	
		if ((ycuad < 5) && y > (ycuad*2*h/15 + r)) { ycuad++; moved = true; }
		else if ((ycuad > 1) && y < ((ycuad-1)*2*h/15 - r)) { ycuad--; moved = true; }
		if (moved){
			
		lienzo2.updbox(xcuad,ycuad);
			if(xcuad==2 ){
				
				if(ycuad==1){
					lienzo.setImagen(21);
					txt3="a";
					new Thread( new enviarUDP()).start();
					vibr.vibrate(100);
				}
				if(ycuad==2){
					lienzo.setImagen(22);
					txt3="b";
					new Thread( new enviarUDP()).start();
					vibr.vibrate(100);
				}if(ycuad==3){
				lienzo.setImagen(23);
					txt3="c";
					new Thread( new enviarUDP()).start();
					vibr.vibrate(100);	
				}
				if(ycuad==4){
				lienzo.setImagen(24);
					txt3="d";
					new Thread( new enviarUDP()).start();
					vibr.vibrate(100);	
				}
				if(ycuad==5){
					lienzo.setImagen(25);
					txt3="e";
					new Thread( new enviarUDP()).start();
					vibr.vibrate(100);
				}		
			}
			if(xcuad==3 ){
				
			
				if(ycuad==1){
					lienzo.setImagen(31);
					txt3="f";
					new Thread( new enviarUDP()).start();
					vibr.vibrate(100);
					
				}
				if(ycuad==2){
					lienzo.setImagen(32);
					txt3="g";
					new Thread( new enviarUDP()).start();
					vibr.vibrate(100);
					
				}if(ycuad==3){
					lienzo.setImagen(33);
					txt3="h";
					new Thread( new enviarUDP()).start();
					vibr.vibrate(100);
					
				}
				if(ycuad==4){
					lienzo.setImagen(34);
					txt3="i";
					new Thread( new enviarUDP()).start();
					vibr.vibrate(100);
					
				}
				if(ycuad==5){
					lienzo.setImagen(35);
					txt3="j";
					new Thread( new enviarUDP()).start();
					vibr.vibrate(100);
					
				}
					
			}
			if(xcuad==4 ){
				//imagenMostrar.setText(lienzo.setImagen(4));
				if(ycuad==1){
					lienzo.setImagen(41);
					txt3="k";
					new Thread( new enviarUDP()).start();
					vibr.vibrate(100);	
				}
				if(ycuad==2){
					lienzo.setImagen(42);
					txt3="l";
					new Thread( new enviarUDP()).start();
					vibr.vibrate(100);
					
				}if(ycuad==3){
					lienzo.setImagen(43);
					txt3="n";
					new Thread( new enviarUDP()).start();
					vibr.vibrate(100);
					
				}
				if(ycuad==4){
					lienzo.setImagen(44);
					txt3="m";
					new Thread( new enviarUDP()).start();
					vibr.vibrate(100);
					
				}
				if(ycuad==5){
					lienzo.setImagen(45);
					txt3="o";
					new Thread( new enviarUDP()).start();
					vibr.vibrate(100);		
				}			
			}	
		}
	}
	protected class enviarUDP implements Runnable{
		public void run() {
			
			//imagenMostrar.setText(lienzo2.x);
			try {
				if (outputStream != null || inputStream!=null) {
					synchronized (obj2) {
						outputStream.write(txt3.getBytes());
					}
				} else {
					Toast.makeText(getBaseContext(),
							"failed to send ... 7",
							Toast.LENGTH_SHORT).show();
				}
			} 		catch (IOException e) {
						Log.e(TAG, ">>", e);
							e.printStackTrace();
				}	
		}
		
		
	}	
}