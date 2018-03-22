package com.example.admin.mmaptest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends Activity implements SensorEventListener {
//public class MainActivity extends Activity {

    //private Socket s;
    //OutputStream os = null;
    private SensorManager mSensorManager;
    TextView etState;
    Button sendfile;
    ArrayList<Double> ACC = new ArrayList<>();
    public double meanACC  = 0, sumACC = 0, rmsACC = 0, tempRMS = 0;
    int count_acc = 0;
    boolean dw = false, write = false, redWarm = false, shishi = false, bendi = false;
    String filePathRms = "/storage/emulated/0/Rms.txt";
    String filePathAdd = "/storage/emulated/0/Address.txt";
    String ipAddress = "222.204.248.26";
    int port = 1431;

    private MapView mapView;
    private AMap aMap;
    private LocationManager locationManager;
    SendThread sendThread;
    SendThreadIP sendThreadip;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        shishi = bundle.getBoolean("shishi");
        bendi = bundle.getBoolean("bendi");

        etState = (TextView) findViewById(R.id.etState);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        ToggleButton record = (ToggleButton) findViewById(R.id.record);
        sendfile = (Button) findViewById(R.id.sendfile);
        sendfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread sendThread = new Thread(new Runnable(){
                    @Override
                    public void run(){
                        SendFile(filePathRms, filePathAdd, ipAddress, port);
                    }
                });
                sendThread.start();
            }
        });

        init();
        ToggleButton startdw = (ToggleButton) findViewById(R.id.startdw);
        startdw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    dw = true;
                }
                else
                {
                    dw = false;
                }
            }
        });
        record.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    write = true;
                } else {
                    write = false;
                }
            }
        });
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location loc) {
                if (dw){
                    updatePosition(loc);
                }
                else {
                    updatePosition_clear(loc);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                if (dw) {
                    updatePosition(locationManager.getLastKnownLocation(provider));
                }
                else {
                    updatePosition_clear(locationManager.getLastKnownLocation(provider));
                }
            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });
        ToggleButton tb = (ToggleButton) findViewById(R.id.tb);
        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                }
                else
                {
                    aMap.setMapType(AMap.MAP_TYPE_NORMAL);
                }
            }
        });
        sendThread = new SendThread();
        sendThread.start();
        sendThreadip = new SendThreadIP();
        sendThreadip.start();
    }
    private void updatePosition_clear(Location location)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd,HH:mm:ss");
        Date curDate =  new Date(System.currentTimeMillis());
        String time = formatter.format(curDate);
        LatLng pos = new LatLng(location.getLatitude() - 0.002005, location.getLongitude() + 0.004575);
        CameraUpdate cu = CameraUpdateFactory.changeLatLng(pos);
        aMap.moveCamera(cu);
        aMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.blue1));
        markerOptions.draggable(true);
        aMap.addMarker(markerOptions);
        if (write) {
            writeFileADD(pos.toString(), time);
        }
    }
    private void updatePosition(Location location)
    {
        Message msgad = new Message();
        msgad.what = 1;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd,HH:mm:ss");
        Date curDate =  new Date(System.currentTimeMillis());
        String time = formatter.format(curDate);
        LatLng pos = new LatLng(location.getLatitude() - 0.002, location.getLongitude() + 0.005);
        CameraUpdate cu = CameraUpdateFactory.changeLatLng(pos);
        aMap.moveCamera(cu);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        if (redWarm == false) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.blue1));
        }
        else {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.red1));
        }
        markerOptions.draggable(true);
        aMap.addMarker(markerOptions);
        Bundle bundle = new Bundle();
        bundle.putString("dtADD", "$" + pos.toString() + time);
        msgad.setData(bundle);
        if (write&&bendi) {
            writeFileADD(pos.toString(), time);
            //sendThreadip.mHandlerad.sendMessage(msgad);
        }
        if (write&&shishi) {
            sendThreadip.mHandlerad.sendMessage(msgad);
        }
    }
    private  void init(){
        if (aMap == null) {
            aMap = mapView.getMap();
            CameraUpdate cu = CameraUpdateFactory.zoomTo(15);
            aMap.moveCamera(cu);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_GAME);
        mapView.onResume();
    }
    @Override
    protected void onStop(){
        mSensorManager.unregisterListener(this);
        super.onStop();
    }
    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
        mapView.onPause();
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy){}
    @Override
    public void onSensorChanged(SensorEvent event){
        float[] values = event.values;
        int sensorType = event.sensor.getType();
        switch (sensorType)
        {
            case Sensor.TYPE_ACCELEROMETER:
                double acc = Math.sqrt(values[0]*values[0]+values[1]*values[1]+values[2]*values[2]);
                if (count_acc<50){
                    ACC.add(acc);
                    count_acc = count_acc + 1;
                }
                else {
                    for (int a = 0; a<50; a++){
                        sumACC = sumACC + ACC.get(a);
                    }
                    meanACC = sumACC/50;
                    for (int a = 0; a<50; a++){
                        tempRMS = tempRMS + (ACC.get(a) - meanACC)*(ACC.get(a) - meanACC);
                    }
                    rmsACC = Math.sqrt(tempRMS/50);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd,HH:mm:ss");
                    Date curDate =  new Date(System.currentTimeMillis());
                    String time = formatter.format(curDate);
                    StateJudge(rmsACC, time);
                    count_acc = 0;
                    sumACC = 0;
                    meanACC = 0;
                    tempRMS = 0;
                    rmsACC = 0;
                    ACC.clear();
                }
                break;
        }
    }
    public void StateJudge(double rmsACC, String time){
        Message msg = new Message();
        msg.what = 0;
        DecimalFormat df = new DecimalFormat("######0.0000");
        if (rmsACC>5) {
            etState.setText(String.valueOf(df.format(rmsACC)) + "您所处的道路比较颠簸");
            redWarm = true;
        }
        else {
            etState.setText(String.valueOf(df.format(rmsACC)) + "正常");
            redWarm = false;
        }
        Bundle bundle = new Bundle();
        bundle.putString("dtDATA", "$RMS" + String.valueOf(df.format(rmsACC)) + time);
        msg.setData(bundle);
        if (write&&bendi) {
            writeFileRMS(String.valueOf(df.format(rmsACC)), time);
            //sendThread.mHandler.sendMessage(msg);
        }
        if (write&&shishi) {
            sendThread.mHandler.sendMessage(msg);
        }
    }
    class SendThread extends Thread{  //访问服务器要另外开一个线程，数据通过Handler的msg来传送
        public Handler mHandler;
        public void run() {
            Looper.prepare();
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if(msg.what == 0) {
                        try {
                            Socket s = new Socket(ipAddress, port);
                            DataOutputStream out = new DataOutputStream(s.getOutputStream());
                            out.write((msg.getData().getString("dtDATA")).getBytes());
                            out.flush();
                            out.close();
                            s.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };Looper.loop();
        }
    }
    class SendThreadIP extends Thread{  //访问服务器要另外开一个线程，数据通过Handler的msg来传送
        public Handler mHandlerad;
        public void run() {
            Looper.prepare();
            mHandlerad = new Handler() {
                @Override
                public void handleMessage(Message msgad) {
                    if(msgad.what == 1) {
                        try {
                            Socket s = new Socket(ipAddress, port);
                            DataOutputStream out = new DataOutputStream(s.getOutputStream());
                            out.write((msgad.getData().getString("dtADD")).getBytes());
                            out.flush();
                            out.close();
                            s.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };Looper.loop();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        SubMenu sendMenu = menu.addSubMenu("更改发送模式");
        sendMenu.setHeaderTitle("请选择发送模式");
        sendMenu.add(0,0x111,0,"实时传输");
        sendMenu.add(0,0x112,0,"文件传输");
        sendMenu.add(0,0x113,0,"实时&文件");
        /*SubMenu fileMenu = menu.addSubMenu("发送本地文件");
        fileMenu.setHeaderTitle("确定发送本地文件？");
        fileMenu.add(0,0x116,0,"发送");
        fileMenu.add(0,0x117,0,"取消");*/
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem mi){
        switch (mi.getItemId())
        {
            case 0x111:
                shishi = true;
                bendi = false;
                break;
            case 0x112:
                shishi = false;
                bendi = true;
                break;
            case 0x113:
                shishi = true;
                bendi = true;
                break;
            /*case 0x116:
                Thread sendThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SendFile(filePathRms, ipAddress, port);
                        //SendFile(filePathAdd, ipAddress, port);
                    }
                });
                sendThread.start();
                break;
            case 0x117:
                Toast toast = Toast.makeText(MainActivity.this, "已取消", Toast.LENGTH_SHORT);
                toast.show();*/
        }
        return true;
    }

    public void writeFileRMS(String data, String time){
        try {
            FileWriter fw = new FileWriter(filePathRms, true);
            fw.write("$rms" + data + "," + time);
            fw.write("\r\n");
            fw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void writeFileADD(String add, String time){
        try {
            FileWriter fw = new FileWriter(filePathAdd, true);
            fw.write("$" + add + "," + time);
            fw.write("\r\n");
            fw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SendFile(String pathrms, String pathadd, String ipAddress, int port) {
                try {
                    /*Socket name = new Socket(ipAddress, port);
                    OutputStream outputName = name.getOutputStream();
                    OutputStreamWriter outputWriter = new OutputStreamWriter(outputName);
                    BufferedWriter bwName = new BufferedWriter(outputWriter);
                    bwName.write(fileName);
                    bwName.close();
                    outputWriter.close();
                    outputName.close();
                    name.close();*/

                    Socket data = new Socket(ipAddress, port);
                    OutputStream outputData = data.getOutputStream();
                    FileInputStream fileInput = new FileInputStream(pathrms);
                    int size = -1;
                    byte[] buffer = new byte[1024];
                    while ((size = fileInput.read(buffer, 0, 1024)) != -1) {
                        outputData.write(buffer, 0, size);
                    }
                    outputData.close();
                    fileInput.close();
                    data.close();

                    Socket dataadd = new Socket(ipAddress, port);
                    OutputStream outputDataadd = dataadd.getOutputStream();
                    FileInputStream fileInputadd = new FileInputStream(pathadd);
                    int sizeadd = -1;
                    byte[] bufferadd = new byte[1024];
                    while ((sizeadd = fileInputadd.read(bufferadd, 0, 1024)) != -1) {
                        outputDataadd.write(bufferadd, 0, sizeadd);
                    }
                    outputDataadd.close();
                    fileInputadd.close();
                    dataadd.close();

                    //Toast toast = Toast.makeText(MainActivity.this, "发送完成", Toast.LENGTH_SHORT);
                    //toast.show();
                    //return fileName + " 发送完成";
                } catch (Exception e) {
                    //Toast toast = Toast.makeText(MainActivity.this, "发送错误：" + e.getMessage(), Toast.LENGTH_SHORT);
                    //toast.show();
                    //return "发送错误:\n" + e.getMessage();
                }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        mapView.onDestroy();
    }
}
