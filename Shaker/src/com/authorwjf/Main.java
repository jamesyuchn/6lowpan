package com.authorwjf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Path;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AnalogClock;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Environment;
import android.provider.MediaStore.Files;

public class Main extends Activity implements SensorEventListener {
	
	private float mLastX, mLastY, mLastZ, mGyroX, mGyroY, mGyroZ, mGrav, mRVX, mRVY, mRVZ, mRVcos, mStepD;
	private boolean mInitialized; //variable to seenif sensor is is initialized
	private SensorManager mSensorManager;
    private Sensor mAccelerometer, mGyroscope, mGravity, mRot_Vec, mSigM, mSigStepC, mSigStepD;
    private TriggerEventListener mTriggerEventListener;
    private final float NOISE = (float) 2.0;
    private int mStepC = 0;
    //private Path file = new String("/data/data/com.authorwjf/data.txt");
    byte dt[];
    //RandomAccessFile file;

    //private DebugServer server;
    
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mInitialized = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mRot_Vec = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if((mSigM = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION))==null){
        	TextView sig= (TextView)findViewById(R.id.mSig);
        	sig.setText("No sensor");
        }
        else{
        	TextView sig= (TextView)findViewById(R.id.mSig);
        	sig.setText("Sensor Detected");
        	mTriggerEventListener = new TriggerEventListener() {
                @Override
                public void onTrigger(TriggerEvent event) {
                	TextView sigO= (TextView)findViewById(R.id.mSigO);
                	sigO.setText("Sensor Activated");
                	DebugServer.MotionChange(event.timestamp, "Detected");
                	//sigO.setText(String.valueOf(++triggerCount));
                }
            };
            mSensorManager.requestTriggerSensor(mTriggerEventListener, mSigM);
        }
        if((mSigStepC = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER))==null){
        	TextView sigstepL= (TextView)findViewById(R.id.SigStepLabel);
        	sigstepL.setText("No StepCount");
        }
        else{
        	TextView sigstepL= (TextView)findViewById(R.id.SigStepLabel);
        	sigstepL.setText("StepCount Detected");
        	mSensorManager.registerListener(this, mSigStepC , SensorManager.SENSOR_DELAY_NORMAL);
        	mSigStepD = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        	mSensorManager.registerListener(this, mSigStepD , SensorManager.SENSOR_DELAY_NORMAL);
        }
        	
        mSensorManager.registerListener(this, mAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope , SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGravity , SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mRot_Vec , SensorManager.SENSOR_DELAY_NORMAL);
        
        final TextView D = (TextView)findViewById(R.id.dbg);
        final EditText server_a = (EditText)findViewById(R.id.server_address);
        Button B = (Button)findViewById(R.id.button1);
        final EditText input_id = (EditText)findViewById(R.id.id);
        final EditText input_un = (EditText)findViewById(R.id.username);
        final EditText input_pt = (EditText)findViewById(R.id.protocol);
        final String default_svr = "Input server address";
        final String default_id = "id";
        final String default_un = "username";
        final String default_pt = "protocol";
        
        server_a.setHint(default_svr);
        input_id.setHint(default_id);
        input_un.setHint(default_un);
        input_pt.setHint(default_pt);
        /*
        server_a.setOnFocusChangeListener(new EditText.OnFocusChangeListener(){

            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                if(hasFocus){
                    server_a.setHint(null);
                }else{
                    server_a.setHint(default_svr);
                }
            }
            
        });
        input_id.setOnFocusChangeListener(new EditText.OnFocusChangeListener(){

            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                if(hasFocus){
                	input_id.setHint(null);
                }else{
                	input_id.setHint(default_id);
                }
            }
            
        });
        input_un.setOnFocusChangeListener(new EditText.OnFocusChangeListener(){

            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                if(hasFocus){
                	input_un.setHint(null);
                }else{
                	input_un.setHint(default_un);
                }
            }
            
        });
        input_pt.setOnFocusChangeListener(new EditText.OnFocusChangeListener(){

            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                if(hasFocus){
                	input_pt.setHint(null);
                }else{
                	input_pt.setHint(default_pt);
                }
            }
            
        });
        */
        B.setOnClickListener(new Button.OnClickListener(){ 
            public void onClick(View v)
            {
                String svr_addr = new String(server_a.getText().toString());
                String in_id = new String(input_id.getText().toString());
                String in_un = new String(input_un.getText().toString());
                String in_pt = new String(input_pt.getText().toString());
            
        
        /*try {
			file = new RandomAccessFile("/data/data/com.authorwjf/data.txt", "rw");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
        
        ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
        pairs.add(new BasicNameValuePair( "id", in_id ));
        pairs.add(new BasicNameValuePair( "username", in_un ));
        pairs.add(new BasicNameValuePair( "protocol", in_pt ));
        
     // 2.编码键值对来符合 URL 规范
        UrlEncodedFormEntity encodedEntity;
		try {
			encodedEntity = new UrlEncodedFormEntity( pairs );
		
        
        // 3.创建一个 HTTP Post 的请求对象
        HttpPost post = new HttpPost( "http://"+svr_addr+":8080/my_app/dbcheck.jsp" );
        
        // 4.放入编码过的键值对，最终得到类似如下 Get 请求的形式：
        // http://dict.youdao.com/search?q=top
        post.setEntity( encodedEntity );
        
        // 5.得到一个默认的 HTTP 客户端来执行上面的 Post 请求
        DefaultHttpClient client = new DefaultHttpClient();
        //client.execute( post );
        HttpResponse response = client.execute( post );
        
        // 6.得到 HTTP 应答
        HttpEntity entity = response.getEntity();
        InputStream is = entity.getContent();
                                
        // 最好设定为最多可能收到的字节数，以减少动态变化时的性能损耗
        ByteArrayBuffer bArray = new ByteArrayBuffer( 1024 );
        byte[] ba = new byte[1024];
        //StringBuffer op = new StringBuffer();
        //byte[] buf = new byte[128];
        int i;
        
        // 7.从 HTTP 应答中读取收到的字节数据
        while ( (i = is.read( ba )) != -1 )
        {                
             bArray.append(ba, 0, i);// i 为读出的实际字节个数
        }
        String strHtmlPage = EncodingUtils.getString( bArray.toByteArray(), "utf-8");
        D.setMovementMethod(new ScrollingMovementMethod());
        D.setText( strHtmlPage );
        //D.setText(op);
        
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
            }
        });
        /*final Timer timer = new Timer(); 
        TimerTask task; 
        final Handler handler = new Handler() { 
            @Override 
            public void handleMessage(Message msg) { */
            	/*try {
        			RandomAccessFile file = new RandomAccessFile("/data/data/com.authorwjf/data.txt", "rw");
        			//dt = (Float.toString(mLastX)+' '+Float.toString(mLastY)+' '+Float.toString(mLastZ)+'\n').getBytes();
        			file.seek(file.length());
        			//file.write(dt,0,dt.length);
        			
        			file.close();
            		
            	} catch (FileNotFoundException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
            	//File f = Environment.getExternalStorageDirectory();//获取SD卡目录
        		/*File fileDir = new File("/data/data/com.authorwjf","test.txt");
        		FileOutputStream os = null;
				try {
					os = new FileOutputStream(fileDir);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		try {
        			byte[] dt = (Float.toString(mLastX)+' '+Float.toString(mLastY)+' '+Float.toString(mLastZ)+'\n').getBytes();
        			os.write(dt);
        		    os.close();
        		} catch (IOException e) {
        		    // TODO Auto-generated catch block
        		    e.printStackTrace();
        		}*/
            	/*try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("config.txt", Context.MODE_PRIVATE));
                    outputStreamWriter.write((Float.toString(mLastX)+' '+Float.toString(mLastY)+' '+Float.toString(mLastZ)+'\n').toString());
                    outputStreamWriter.close();
                    //filepath = getFileStreamPath("config.txt");

                }
                catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                } 
                super.handleMessage(msg); 
            }
        };
        task = new TimerTask() { 
            @Override 
            public void run() { 
                // TODO Auto-generated method stub 
                Message message = new Message(); 
                message.what = 1; 
                handler.sendMessage(message); 
            } 
        };
        timer.schedule(task, 1000, 1000); */
        //TODO: New stuff
        
      //as http client
      //  HttpClient client = new DefaultHttpClient();
      //  HttpPost post = new HttpPost("www.google.com"); //just for now
        
      //as http server using nanohttpd
        /*server = new DebugServer();
        try {
            server.start();
        } catch(IOException ioe) {
            Log.w("Httpd", "The server could not start.");
        }
        Log.w("Httpd", "Web server initialized.");*/
     
    }
    
 // DON'T FORGET to stop the server
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //if (server != null)
        //    server.stop();
    }

    protected void onResume() {
        super.onResume();
      //  mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
       // mSensorManager.unregisterListener(this);
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// can be safely ignored for this demo
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		TextView tvX= (TextView)findViewById(R.id.x_axis);
		TextView tvY= (TextView)findViewById(R.id.y_axis);
		TextView tvZ= (TextView)findViewById(R.id.z_axis);
		ImageView iv = (ImageView)findViewById(R.id.image);
		AnalogClock clk = (AnalogClock)findViewById(R.id.analogClock1);
		
		TextView GyroX= (TextView)findViewById(R.id.gyro_x);
		TextView GyroY= (TextView)findViewById(R.id.gyro_y);
		TextView GyroZ= (TextView)findViewById(R.id.gyro_z);
		
		TextView Grav= (TextView)findViewById(R.id.mgrav);
		
		TextView Rot_VecX= (TextView)findViewById(R.id.rot_vecX);
		TextView Rot_VecY= (TextView)findViewById(R.id.rot_vecY);
		TextView Rot_VecZ= (TextView)findViewById(R.id.rot_vecZ);
		//TextView Rot_VecCos= (TextView)findViewById(R.id.rot_veccos);
		
		Sensor sensor = event.sensor;
		if(sensor.getType()==Sensor.TYPE_ACCELEROMETER){
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			if (!mInitialized) {
				mLastX = x;
				mLastY = y;
				mLastZ = z;
				tvX.setText("0.0");
				tvY.setText("0.0");
				tvZ.setText("0.0");
				mInitialized = true;
			//	clk.setVisibility(View.VISIBLE);
			//	clk.bringToFront();
			} else {
				float deltaX = Math.abs(mLastX - x);
				float deltaY = Math.abs(mLastY - y);
				float deltaZ = Math.abs(mLastZ - z);
				if (deltaX < NOISE) deltaX = (float)0.0;
				if (deltaY < NOISE) deltaY = (float)0.0;
				if (deltaZ < NOISE) deltaZ = (float)0.0;
				mLastX = x;
				mLastY = y;
				mLastZ = z;
				tvX.setText(Float.toString(deltaX));
				tvY.setText(Float.toString(deltaY));
				tvZ.setText(Float.toString(deltaZ));
				clk.setVisibility(View.VISIBLE);
				clk.bringToFront();
				
				if(deltaZ > NOISE)
				{				
					//DebugServer.changeValues(deltaX, deltaY, deltaZ, "Lateral");
				}
				
				
				if (deltaX > deltaY) {
					clk.setScaleY(1);
					clk.setScaleX(deltaX/2);
					
					if(deltaZ>deltaX)
					{
					    //DebugServer.changeValues(deltaX, deltaY, deltaZ, "Lateral");
					}else
					{
						//DebugServer.changeValues(deltaX, deltaY, deltaZ, "Horizontal");
					}
					
				} else if (deltaY > deltaX) {
					clk.setScaleY(deltaY/2);
					clk.setScaleX(1);
					
					if(deltaZ>deltaY)
					{
					    //DebugServer.changeValues(deltaX, deltaY, deltaZ, "Lateral");
					}else
					{
						//DebugServer.changeValues(deltaX, deltaY, deltaZ, "Vertical");
					}
				} else {
					clk.bringToFront();			
					clk.setScaleY(1);
					clk.setScaleX(1);
				}
			}
		}else if(sensor.getType()==Sensor.TYPE_GYROSCOPE){
			float gyro_x = event.values[0];
			float gyro_y = event.values[1];
			float gyro_z = event.values[2];
			if (!mInitialized) {
				mGyroX = gyro_x;
				mGyroY = gyro_y;
				mGyroZ = gyro_z;
				GyroX.setText("0.0");
				GyroY.setText("0.0");
				GyroZ.setText("0.0");
				mInitialized = true;
			//	clk.setVisibility(View.VISIBLE);
			//	clk.bringToFront();
			} else {
				mGyroX = gyro_x;
				mGyroY = gyro_y;
				mGyroZ = gyro_z;
				GyroX.setText(Float.toString(mGyroX));
				GyroY.setText(Float.toString(mGyroY));
				GyroZ.setText(Float.toString(mGyroZ));
				//DebugServer.GyroChangeValues(mGyroX, mGyroY, mGyroZ);
			}
		}else if(sensor.getType()==Sensor.TYPE_GRAVITY){
			float grav = event.values[0];
			if (!mInitialized) {
				mGrav = grav;
				Grav.setText("0.0");
				mInitialized = true;
			//	clk.setVisibility(View.VISIBLE);
			//	clk.bringToFront();
			} else {
				mGrav = grav;
				Grav.setText(Float.toString(mGrav));
				//DebugServer.GravChangeValues(mGrav);
			}
		}else if(sensor.getType()==Sensor.TYPE_ROTATION_VECTOR){
			float rot_vec_x = event.values[0];
			float rot_vec_y = event.values[1];
			float rot_vec_z = event.values[2];
			//float rot_vec_cos = event.values[3];
			if (!mInitialized) {
				mRVX = rot_vec_x;
				mRVY = rot_vec_y;
				mRVZ = rot_vec_z;
				//mRVcos = rot_vec_cos;
				Rot_VecX.setText("0.0");
				Rot_VecY.setText("0.0");
				Rot_VecZ.setText("0.0");
				//Rot_VecCos.setText("0.0");
				mInitialized = true;
			//	clk.setVisibility(View.VISIBLE);
			//	clk.bringToFront();
			} else {
				mRVX = rot_vec_x;
				mRVY = rot_vec_y;
				mRVZ = rot_vec_z;
				//mRVcos = rot_vec_cos;
				Rot_VecX.setText(Float.toString(mRVX));
				Rot_VecY.setText(Float.toString(mRVY));
				Rot_VecZ.setText(Float.toString(mRVZ));
				//Rot_VecZ.setText(Float.toString(mRVcos));
				//DebugServer.RotVecChangeValues(mRVX, mRVY, mRVZ, mRVcos);
				//DebugServer.RotVecChangeValues(mRVX, mRVY, mRVZ);
			}
		}else if(sensor.getType()==Sensor.TYPE_STEP_COUNTER){
			int StepCounter = (int)event.values[0];
			TextView sigstepC= (TextView)findViewById(R.id.mStepC);
			if (!mInitialized) {
				mStepC = StepCounter;
				sigstepC.setText("0");
				mInitialized = true;
			//	clk.setVisibility(View.VISIBLE);
			//	clk.bringToFront();
			} else {
				mStepC = StepCounter;
				sigstepC.setText(Integer.toString(mStepC));
				//DebugServer.StepCChangeValues(mStepC);
			}
		}else if(sensor.getType()==Sensor.TYPE_STEP_DETECTOR){
			float StepD = event.values[0];
			TextView sigstepD= (TextView)findViewById(R.id.mStepD);
			//TextView sigstepTS= (TextView)findViewById(R.id.Timestamp);
			if (!mInitialized) {
				mStepD = StepD;
				sigstepD.setText("0.0");
				mInitialized = true;
			//	clk.setVisibility(View.VISIBLE);
			//	clk.bringToFront();
			} else {
				mStepD = StepD;
				sigstepD.setText(Float.toString(mStepD));
				//sigstepTS.setText(Long.toString(event.timestamp));
				//DebugServer.StepDChangeValues(event.timestamp);
			}
		}
	}
}