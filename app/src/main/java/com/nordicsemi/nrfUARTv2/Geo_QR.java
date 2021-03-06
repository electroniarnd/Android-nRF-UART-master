package com.nordicsemi.nrfUARTv2;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.barcode.Barcode;
import com.nordicsemi.nrfUARTv2.barcode.barcodecaptureactivity;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import butterknife.ButterKnife;

public class Geo_QR extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener  {
    private static final String TAG ="markattendanceActivity" ;
    private static final int BARCODE_READER_REQUEST_CODE = 1;
    GoogleMap map;
    SupportMapFragment mapFragment;
    //  LatLng portLatLong,portLatLong1,portLatLong2,portLatLong3;
    Spinner spdLocations;
    HashMap<LatLng, String> locations = new HashMap<>();
    int  QrCodeId=0;
    int Geoid=0;
    String  building="",bestProvider;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    int shadeColor = 0x44ff0000, GeoID=0,count =0,counter=0;
    LocationManager locationManager;
    Circle circle, circle1;
    ProgressDialog dialog;
    private Location lastLocation;
    Location location;
    LatLng latLong;
    float dis=0;
    MediaPlayer in = null;
    MediaPlayer out = null;
    MediaPlayer qrcode = null;
    MediaPlayer error = null;
    ArrayList<String> locationArray = new ArrayList<>();
    Controllerdb db =new Controllerdb(this);
    SQLiteDatabase database;
    TextView txtBadgeNo,txtPunchDate,txtPunchTime,txtLastPunch,txtLogStatus,txtGeoname,QR_CodeName;
    //private static String EMPLOYEE_SERVICE_URI1="http://212.12.167.242:6002/Service1.svc";
    private static String EMPLOYEE_SERVICE_URI1="";
    private String BadgeNoReg="";
    Button check_in_button;
    private static int Tracking = 0;
    private boolean isGPS = false;
    static int   countGeofence=0;
    private  static String ServiceURL="";//+"/ElguardianService/Service1.svc/" +
    private static int Registration=0;

    private static int countQR=0;
    private static int countResult=0;
    String BadgeNo="",QRCODE="",QRCodeName="";

    private Context mContext;
    private Activity mActivity;

    private RelativeLayout mRelativeLayout;
    private Button mButton;

    private PopupWindow mPopupWindow;




    public static  int BLE = 0;
    public static  int geofence =0; //Integer.valueOf(cursor.getString(cursor.getColumnIndex("Geofence")));
    public static  int QRCode=0;
    private static int startvalue = 0;
    private static int AutoPunch = 0;
    private static int Biometric = 0;
    private static final int  REQUEST_ENABLE_FT=3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo__qr);

        check_in_button = (Button) findViewById(R.id.check_in_button);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //  Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //  setSupportActionBar(toolbar);
        //  toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        check_in_button.setEnabled(true);

        if(GetServiceURL()==1) {
            Toast.makeText(this, "Error in reading local Database", Toast.LENGTH_LONG).show();
            return;
        }
        else
        {
            if(Registration==0)
            {
                Toast.makeText(this, "Registration not found", Toast.LENGTH_LONG).show();
                Intent Registration = new Intent(Geo_QR.this, RegistrationPage.class);
                startActivity(Registration);
                return;
            }
        }


        if(Geofence()==1)
        {
            if(countGeofence==0) {
                Toast.makeText(this, "Goefence not found for this employee", Toast.LENGTH_LONG);
                check_in_button.setEnabled(false);
            }
        }
        else
        {
            Toast.makeText(this,"Error in reading local database",Toast.LENGTH_LONG);
        }
        if(ReadSysSetting()==0) {//check System Setting
            Toast.makeText(this, "Error in Reading system_setting", Toast.LENGTH_LONG).show();//check_Setting();
            return;
        }



        in = MediaPlayer.create(this, R.raw.in);
        out = MediaPlayer.create(this, R.raw.out);
        qrcode=MediaPlayer.create(this, R.raw.qrcode);
        // error = MediaPlayer.create(this, R.raw.error);
        Geo_QR.AsyncTaskRunner runner = new Geo_QR.AsyncTaskRunner();
        runner.execute();


        ButterKnife.bind(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_frame);
        // setupToolbar();
        dialog = new ProgressDialog(this);

        Criteria criteria = new Criteria();
        bestProvider = locationManager.getBestProvider(criteria, true);
        if( RaadGeoFence() !=1)
            Toast.makeText(this,"Problem in Reading Grofence",Toast.LENGTH_LONG).show();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        ////////// GetGeofenceFromServer();
        mapFragment.getMapAsync(this);
        mContext = getApplicationContext();
        // Get the activity
        mActivity = Geo_QR.this;

        // Get the widgets reference from XML layout
        mRelativeLayout = (RelativeLayout) findViewById(R.id.rl);

        check_in_button.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                validateLocation();
                 if(count>0) {
                     Intent intent = new Intent(getApplicationContext(), barcodecaptureactivity.class);
                     startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
                 }
                 else
                 {
                     Toast.makeText(Geo_QR.this,"Geofence not found",Toast.LENGTH_LONG).show();
                     return;
                 }
            }
        });





        if(!checkServiceRunningGeoLog()) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    Intent startServiceIntent = new Intent(Geo_QR.this, GeofenceLogService.class);
                    startService(startServiceIntent);
                }
            };
            thread.start();
        }

        if(!checkServiceRunning()) {




            Thread thread = new Thread() {
                @Override
                public void run() {
                    Intent startServiceIntent = new Intent(Geo_QR.this, LocationUpdateservice.class);
                    startService(startServiceIntent);
                }
            };
            thread.start();






        }



    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Map_InitValue(googleMap);
        if(count>0)
        {
            Toast.makeText(this, "You are inside:"+building, Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "You are outside", Toast.LENGTH_SHORT).show();
        }
        buildApiClient();

        if ((startvalue == 0) && AutoPunch == 1 && Biometric==1) {

            Intent newIntent = new Intent(Geo_QR.this, biometric.class);
            startActivityForResult(newIntent, REQUEST_ENABLE_FT);
        }
      //  if ((startvalue == 0) && AutoPunch == 1 && Biometric==0) {

          //  validateLocation();
       // }
        startvalue=1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu1, menu);
        return true;
    }


    private MarkerOptions generateMarker(LatLng latLng, String title) {
        TextView text = new TextView(getApplicationContext());
        text.setText(title);
        text.setTextColor(Color.parseColor("#ffffff"));
        text.setPadding(5, 5, 5, 5);
        return new MarkerOptions().position(latLng).title("Building");
    }





    private void buildApiClient() {

    }


    void validateLocation() {
        if (startvalue == 0)
            Map_InitValue(map);
            buildApiClient();
       // }
       // catch(Exception ex)
       // {
            //Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show();
       // }
    }


//

    @Override
    public void onLocationChanged(Location locations) {
        lastLocation=locations;
        location=locations;
        map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("It's You!").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_person)));
        String time = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(location.getTime());

        if( location.getProvider().equals(android.location.LocationManager.GPS_PROVIDER))
            android.util.Log.d("Location", "Time GPS: " + time); // This is what we want!
        else
            android.util.Log.d("Location", "Time Device (" + location.getProvider() + "): " + time);
    }





    public Integer RaadGeoFence()//////CHANGE INTO COMMON FUNCTION LATTER
    {
        int res =0;
        try {
            database = db.getReadableDatabase();
            Cursor cursor = database.rawQuery("SELECT * FROM  Geofence", null);
            if (cursor.moveToFirst()) {
                do {
                    locations.put(new LatLng(Float.valueOf(cursor.getString(cursor.getColumnIndex("Lat"))), Float.valueOf(cursor.getString(cursor.getColumnIndex("Long")))), String.valueOf(cursor.getString(cursor.getColumnIndex("KeyName")))+"~"+String.valueOf(cursor.getString(cursor.getColumnIndex("Radius")))+"~"+String.valueOf(cursor.getString(cursor.getColumnIndex("Badgeno")))+"~"+String.valueOf(cursor.getString(cursor.getColumnIndex("GeoID"))));
                } while (cursor.moveToNext());
            }
            cursor.close();
            res=1;
        }
        catch (Exception ex) {
            res=0;
            Log.d(TAG, ex.getMessage());
        }
        return res;
    }



    public boolean SaveLog(String BadgeNo,int punchtype,int geoid,int QRId) {

        String msg = "", s="", lang="",number="",urlar="";
        Date dt =new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd");
        SimpleDateFormat df1 = new SimpleDateFormat("HH.mm.ss");

        String formattedDate = df.format(dt.getTime());
        String formattedDate1 = df1.format(dt.getTime());
        SimpleDateFormat time = new SimpleDateFormat("hh.mm.ss aa");
        String formattedDate2 = time.format(dt.getTime());

        SimpleDateFormat dfDate = new SimpleDateFormat(" dd MMM yyyy");
        String formatDate = dfDate.format(dt.getTime());

        try {

            if (!(checkConnection())) {
                WriteLogData( BadgeNo, formatDate,formattedDate2, punchtype, geoid, QRId,0);

                // fillData(BadgeNo, formattedDate, formattedDate2,  punchtype, building,"OFFLINE");
                popup( BadgeNo, formattedDate, formattedDate2,  punchtype, building,"OFFLINE",QRCodeName);
                //if(punchtype==1)
                //Toast.makeText(this,"Info: No Internet Connection. IN Punch Saved Offline. Please Insure Internet next time otherwise data would be loose",Toast.LENGTH_LONG).show();
                //else
                //   Toast.makeText(this,"Info: No Internet Connection. OUT Punch Saved Offline. Please Insure Internet next time otherwise data would be loose",Toast.LENGTH_LONG).show();
                return false;
            }
            urlar="";
            urlar= ServiceURL+"/ElguardianService/Service1.svc/";
            s ="";
            s = urlar + "/LogSaveQR" + "/'" + BadgeNo+"'" +"/"+formattedDate+"/"+ formattedDate1 +"/"+punchtype+"/"+"/"+geoid+"/"+QRId;
            String  EMPLOYEE_SERVICE_URI1 = s.replace(' ','-');
            URL url = new URL(EMPLOYEE_SERVICE_URI1);
            URLConnection conexion = url.openConnection();
            conexion.connect();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            InputStream input = new BufferedInputStream(url.openStream());


            int b=-1;
            while ((b = input.read()) != -1)
                buffer.write(b);
            input.close();

            String json = new String(buffer.toString());
            Log.d( getResources().getString(R.string.download), getResources().getString(R.string.Lenght_of_file) + json.length());

            if (json.contains("`")) {
                String getstring = json;
                int iend = getstring.indexOf("`");

                if (iend != -1)
                    getstring = json.substring(iend, json.length()); //this will give abc
                PAlertDialog( getResources().getString(R.string.Error), getstring);
                WriteLogData( BadgeNo, formatDate,formattedDate2, punchtype, geoid,QRId,0);
                return false;
            }
            else
            {
                int ret= WriteLogData( BadgeNo, formatDate,formattedDate2, punchtype, geoid,QRId,1);
                if(ret==1) {
                    // fillData(BadgeNo, formattedDate, formattedDate2,  punchtype, building,"ONLINE");
                    popup(BadgeNo, formattedDate, formattedDate2,  punchtype, building,"ONLINE",QRCodeName);
                    //////  if (punchtype == 1)
                    ////////  PAlertDialog(getResources().getString(R.string.Information), "IN Punch Saved Successfully");
                    ///////  else
                    /////////// PAlertDialog(getResources().getString(R.string.Information), "OUT Punch Saved Successfully");
                }
                else
                    Toast.makeText(getBaseContext(), "Error:Log did not save Offline", Toast.LENGTH_SHORT).show();

            }
            //  int lnth = json.length();
            //  String json1 = json.substring(1, lnth - 1);
        } catch (Exception e) {
            WriteLogData( BadgeNo, formatDate,formattedDate2, punchtype, geoid,QRId,0);
            //fillData(BadgeNo, formattedDate, formattedDate2,  punchtype, building,"OFFLINE");
            popup(BadgeNo, formattedDate, formattedDate2,  punchtype, building,"OFFLINE",QRCodeName);
            e.printStackTrace();
//            Toast.makeText(getBaseContext(), e.getMessage(),
//                    Toast.LENGTH_SHORT).show();
            //  PAlertDialog("ERROR", e.getMessage());
            return false;
        }
        return true;

    }


    public boolean checkConnection() {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
            if (activeNetworkInfo != null) { // connected to the internet
                if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    return true;
                } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return true;
                }
            }
        }
        catch(Exception e){
            Toast.makeText(Geo_QR.this, " Network Connection Error",
                    Toast.LENGTH_SHORT).show();
            Log.e("",e.getMessage());
        }
        return false;
    }

    private void PAlertDialog(String title, String msg)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(Geo_QR.this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton( getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }



    public Integer WriteLogData(String Badgeno,String dt, String time,int punchtype,int geofenceid,int QrCodeID, int sent)//////CHANGE INTO COMMON FUNCTION LATTER
    {
        String Type="QR+Geo";
        int res =0;
        try {
            database=db.getWritableDatabase();
            database.execSQL("INSERT INTO tblLogs(BadgeNo,date,time,direction,GeoID,sent,QRId,PunchType,Ter)VALUES('"+BadgeNo+"','"+dt+"','"+time+"',"+punchtype+","+geofenceid+","+sent+","+QrCodeID+",'"+Type+"','"+QRCodeName+"')" );
            res=1;
        }
        catch (Exception ex) {
            res=0;
            Log.d(TAG, ex.getMessage());
        }
        return res;
    }









    public void Map_InitValue(GoogleMap googleMap )
    {
        googleMap.clear();
        count =0;
        GeoID=0;
        BadgeNo="";
        map = googleMap;
        // Get an iterator
        Set set = locations.entrySet();
        Iterator i = set.iterator();
        String str="";
        String[]  getredius;
        boolean gps_enabled = false;
        boolean network_enabled = false;


        try {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED

                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED

                    ) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
                return;
            }

            location = locationManager.getLastKnownLocation(bestProvider);
            //  if (location != null) {
            //     onLocationChanged(location);
            // }

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            assert locationManager != null;

            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);



            if (!gps_enabled && !network_enabled) {  Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, "nothing is enabled", duration);
                toast.show();

            }
            if (gps_enabled)
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
            else  if (network_enabled)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4, 0, this);
            googleMap.setMyLocationEnabled(true);
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
            //googleMap.addMarker(new MarkerOptions().position(portLatLong).title("RKGIT"));

            //onLocationChanged(null);
            // Get a set of the entries
            if (locations.size() == 0) {
                Toast.makeText(this, "You are not assigned any Geofence", Toast.LENGTH_LONG).show();
                return;
            }

            while (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                System.out.print(me.getKey() + ": ");
                System.out.println(me.getValue());

                LatLng latLng = (LatLng) me.getKey();
                str = (String) me.getValue();
                getredius = str.split("~");
                //    map.addMarker(generateMarker(latLng, locations.get(latLng)));

                CircleOptions circleOptions = new CircleOptions().center(latLng).radius(Integer.valueOf(getredius[1])).fillColor(shadeColor).strokeColor(Color.BLUE);
                map.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).title(getredius[0]));
                circle = googleMap.addCircle(circleOptions);


                float[] distance = new float[2];
                if (location == null) {
                    Toast.makeText(this, "Your Location is not found Please check your Internet", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (location.getLatitude() > 0 && location.getLongitude() > 0) {
                    Location.distanceBetween(location.getLatitude(),
                            location.getLongitude(), circle.getCenter().latitude,
                            circle.getCenter().longitude, distance);

                    if (distance[0] > circle.getRadius()) {
                        //    map.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
                        //    map.moveCamera(CameraUpdateFactory.newLatLngZoom(portLatLong, 17.0f));
                        //    Toast.makeText(this, "You are outsid", Toast.LENGTH_SHORT).show();
                        // dis=distance[0];
                        if (dis > distance[0]) {
                            dis = distance[0];
                        }

                    } else if (distance[0] < circle.getRadius()) {
                        // dialog.setMessage("Fetching data");
                        //  dialog.setCanceledOnTouchOutside(false);
                        // dialog.show();
                        //     markAttendence();
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
                        latLong = latLng;
                        circle1 = circle;
                        building = getredius[0];
                        map.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
                        BadgeNo = getredius[2];
                        GeoID = Integer.valueOf(getredius[3]);
                        count++;
                    }
                }
            }
        }
        catch(Exception ex)
        {
            Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }


    public Integer ReadLastPunch()//////CHANGE INTO COMMON FUNCTION LATTER
    {
        int res =0,punchtype=2;
        String BadgeNo="";
        Date dt;//=new Date();
        try {
            database = db.getReadableDatabase();
            Cursor cursor = database.rawQuery("SELECT * FROM  tblLogs  order by ID Desc LiMIT 1", null);
            if (cursor.moveToFirst()) {
                do {
                    punchtype =  Integer.valueOf( cursor.getString(cursor.getColumnIndex("direction")));

                } while (cursor.moveToNext());
            }
            cursor.close();

        }
        catch (Exception ex) {
            punchtype=3;
            Log.d(TAG, ex.getMessage());
        }
        return punchtype;
    }


    private void SoundIt(int index) {
        switch (index) {
            case 1:
                out.start();
                break;
            case 0:
                in.start();
                break;
            case 2:
                qrcode.start();
                break;

        }

    }
    public void popup(String BadgeNo,String formattedDate,String formattedDate2, int punchtype,String building,String LogType,String QrCodeName)
    {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

        // Inflate the custom layout/view
        View customView = inflater.inflate(R.layout.custom_qr_geo,null);
        txtBadgeNo =(TextView) customView.findViewById(R.id.txtBadgeNo);
        txtPunchDate = (TextView) customView.findViewById(R.id.txtPunchDate);
        txtPunchTime = (TextView)customView.findViewById(R.id.txtPunchTime);
        txtLastPunch = (TextView)customView.findViewById(R.id.txtLastPunch);
        txtLogStatus =(TextView)customView.findViewById(R.id.txtLogStatus);
        txtGeoname= (TextView)customView.findViewById(R.id.txtGeoname);
        QR_CodeName=(TextView)customView.findViewById(R.id.txtQRCodeName);
        ImageButton closeButton = (ImageButton) customView.findViewById(R.id.ib_close);
        // Initialize a new instance of popup window
        mPopupWindow = new PopupWindow(
                customView,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                android.support.v7.widget.Toolbar.LayoutParams.WRAP_CONTENT
        );

        // Set an elevation value for popup window
        // Call requires API level 21
        if(Build.VERSION.SDK_INT>=21){
            mPopupWindow.setElevation(5.0f);
        }

        // Get a reference for the custom view close button

        txtBadgeNo.setText(BadgeNo);
        txtPunchDate.setText(formattedDate);
        txtPunchTime.setText(formattedDate2);
        if(punchtype==0) {
            txtLastPunch.setText("IN");
            if(!isMyServiceRunning(LocationMonitoringService.class)) {
                Intent intent = new Intent(Geo_QR.this, LocationMonitoringService.class);
                startService(intent);
            }
        }
        else {
            txtLastPunch.setText("OUT");
            stopService(new Intent(this, LocationMonitoringService.class));
        }
        txtLogStatus.setText(LogType);
        txtGeoname.setText(building);
        QR_CodeName.setText(QrCodeName);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setFocusable(true);
        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                txtBadgeNo.setText("");
                txtPunchDate.setText("");
                txtPunchTime.setText("");
                txtLastPunch.setText("");
                txtLogStatus.setText("");
                txtGeoname.setText("");
                mPopupWindow.dismiss();
            }
        });
        //mPopupWindow.showAtLocation(mRelativeLayout, Gravity.CENTER,0,0);
        mRelativeLayout.post(new Runnable() {
            public void run() {
                mPopupWindow.showAtLocation(mRelativeLayout, Gravity.CENTER, 0, 0);
            }
        });

        SoundIt(punchtype);


    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void  ClearData()
    {
        txtBadgeNo.setText("");
        txtPunchDate.setText("");
        txtPunchTime.setText("");

        txtLastPunch.setText("");

        txtLogStatus.setText("");
        txtGeoname.setText("");
    }




    public boolean GetGeofenceFromServer() {

        String msg = "", s="", lang="",number="",urlar="";
        try {

            if (!checkConnection()) {

                PAlertDialog( getResources().getString(R.string.Error), "Internet not found");
                return false;
            }
            Badgeno();
            if (BadgeNoReg == null && BadgeNoReg.isEmpty() && BadgeNoReg.equals("null"))
            {
                PAlertDialog( getResources().getString(R.string.Error), "Registration not found");
                return false;
            }
            urlar =ServiceURL+"/ElguardianService/Service1.svc/";


            lang= Locale.getDefault().getDisplayLanguage();
            s = urlar + "/GetGeoData/" + "/'" + BadgeNoReg+"'"+ "/'" +lang;

            EMPLOYEE_SERVICE_URI1="";
            EMPLOYEE_SERVICE_URI1 = s.replace(' ','-');

            URL url = new URL(EMPLOYEE_SERVICE_URI1);

            URLConnection conexion = url.openConnection();
            conexion.connect();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            InputStream input = new BufferedInputStream(url.openStream());
            int b=-1;
            while ((b = input.read()) != -1)
                buffer.write(b);
            input.close();

            String json = new String(buffer.toString());
            Log.d( getResources().getString(R.string.download), getResources().getString(R.string.Lenght_of_file) + json.length());

            if (json.contains("`")) {
                String getstring = json;
                int iend = getstring.indexOf("`");

                if (iend != -1)
                    getstring = json.substring(iend, json.length()); //this will give abc


                PAlertDialog( getResources().getString(R.string.Error), getstring);

                return false;
            }


            int lnth = json.length();
            String json1 = json.substring(1, lnth - 1);
            if(WriteGeofanceData(json1)!=1) {

                return false;
            }


        } catch (Exception e) {
            e.printStackTrace();
//            Toast.makeText(getBaseContext(), e.getMessage(),
//                    Toast.LENGTH_SHORT).show();
            PAlertDialog("ERROR", e.getMessage());
            return false;
        }
        return true;

    }
    public Integer WriteGeofanceData(String Value)//////CHANGE INTO COMMON FUNCTION LATTER
    {
        int res =0;
        try {
            String[] Val=Value.split(";");
            database=db.getWritableDatabase();
            database.execSQL("delete from  Geofence");
            for(int i=0;i<Val.length;i++)
            {
                String[] Val1=Val[i].split("~");
                database=db.getWritableDatabase();
                database.execSQL("INSERT INTO Geofence(GeoID,Lat,Long,Radius,GeoName,KeyName,Badgeno,Shape_Name,Group_Name,Zoom_value)VALUES("+Val1[0]+","+Val1[1]+","+Val1[2]+","+Val1[3]+",'"+Val1[4]+"','"+Val1[4]+"','"+Val1[5]+"','"+Val1[6]+"','"+Val1[7]+"','"+Val1[8]+"')" );
            }

            res=1;
        }
        catch (Exception ex) {
            res=0;
            Log.d(TAG, ex.getMessage());
            Toast.makeText(this, ex.getMessage(),Toast.LENGTH_LONG ).show();
        }
        return res;
    }


    public Integer Badgeno()//////CHANGE INTO COMMON FUNCTION LATTER
    {
        int res=0;
        try {
            database = db.getReadableDatabase();
            Cursor cursor = database.rawQuery("SELECT BadgeNo FROM  Registration", null);
            if (cursor.moveToFirst()) {
                do {
                    BadgeNoReg = cursor.getString(cursor.getColumnIndex("BadgeNo"));

                } while (cursor.moveToNext());
            }
            cursor.close();
            res=1;
        }
        catch (Exception ex) {
            res=0;
            Log.d(TAG, ex.getMessage());
        }
        return res;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.Registration_id:
                startActivity(new Intent(this, RegistrationActivity.class));
                break;
            case R.id.Setting_id:
                startActivity(new Intent(this, settingGeo.class));
                break;
            case R.id.About_id:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.Record_id:
                startActivity(new Intent(this,recordLogs.class));
                break;
            case R.id.Tracking_id:
                if(ReadTrackingValue()==0) {
                    if(Tracking==1 )
                        startActivity(new Intent(this, Tracking.class));
                    else
                        Toast.makeText(this,"not authorized",Toast.LENGTH_LONG).show();
                }
                else
                    Toast.makeText(this,"Local Database Error",Toast.LENGTH_LONG).show();
                break;

            default:
                return super.onOptionsItemSelected(item);

        }

        return super.onOptionsItemSelected(item);
    }

    private void setSupportActionBar(android.widget.Toolbar toolbar) {
    }

    public Integer ReadTrackingValue()//////CHANGE INTO COMMON FUNCTION LATTER
    {
        String MacValue="";
        int res =0;
        try {
            database = db.getReadableDatabase();
            Cursor cursor = database.rawQuery("SELECT * FROM  Registration", null);
            if (cursor.moveToFirst()) {
                do {
                    Tracking =  Integer.valueOf(cursor.getString(cursor.getColumnIndex("Tracking")));

                } while (cursor.moveToNext());
            }
            cursor.close();
            res =0;
        }
        catch (Exception ex) {
            res=-1;
            Log.d(TAG, ex.getMessage());
        }
        return res;
    }


    public Integer Geofence()//////CHANGE INTO COMMON FUNCTION LATTER
    {
        String MacValue="";
        int res =0;
        try {
            database = db.getReadableDatabase();
            Cursor cursor = database.rawQuery("SELECT * FROM  Geofence", null);
            if (cursor.moveToFirst()) {
                do {
                    countGeofence++;

                } while (cursor.moveToNext());
            }
            cursor.close();
            res =1;
        }
        catch (Exception ex) {
            res=-0;
            Log.d(TAG, ex.getMessage());
        }
        return res;
    }


    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;


        @Override
        protected String doInBackground(String... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()
            try {
                new GpsUtils(Geo_QR.this).turnGPSOn(new GpsUtils.onGpsListener() {
                    @Override
                    public void gpsStatus(boolean isGPSEnable) {
                        // turn on GPS
                        isGPS = isGPSEnable;
                    }
                });

                //startStep1();

            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }
            return resp;
        }


        @Override
        protected void onPostExecute(String result) {
            String  res="";



        }


        @Override
        protected void onPreExecute() {



        }


        @Override
        protected void onProgressUpdate(String... text) {

        }
    }




    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        locationManager.removeUpdates(this);

    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        locationManager.removeUpdates(this);

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.d(TAG, "onRestart");
    }




    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    public Integer GetServiceURL()//////CHANGE INTO COMMON FUNCTION LATTER
    {
        String MacValue="";
        int res =1;
        try {
            database = db.getReadableDatabase();
            Cursor cursor = database.rawQuery("SELECT * FROM  Registration", null);
            if (cursor.moveToFirst()) {
                do {

                    ServiceURL=cursor.getString(cursor.getColumnIndex("url"));
                    Registration++;
                } while (cursor.moveToNext());
            }
            cursor.close();
            res =0;
        }
        catch (Exception ex) {
            res=-1;
            Log.d(TAG, ex.getMessage());
        }
        return res;
    }
    public Integer ReadSystemValue()//////CHANGE INTO COMMON FUNCTION LATTER
    {
        String MacValue = "";
        int res = 0;
        try {
            database = db.getReadableDatabase();
            Cursor cursor = database.rawQuery("SELECT BLE,Geofence,QRCode  From   Registration", null);
            if (cursor.moveToFirst()) {
                do {

                    BLE = Integer.valueOf(cursor.getString(cursor.getColumnIndex("BLE")));
                    geofence = Integer.valueOf(cursor.getString(cursor.getColumnIndex("Geofence")));
                    QRCode = Integer.valueOf(cursor.getString(cursor.getColumnIndex("QRCode")));
                } while (cursor.moveToNext());
            }
            cursor.close();
            res = 1;
        } catch (Exception ex) {
            res = 0;
            Log.d(TAG, ex.getMessage());
        }
        return res;
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.popup_title)
                .setMessage(R.string.popup_message)
                .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        if (ReadSystemValue() == 1) {
                            if (BLE == 0 && QRCode == 0 && geofence == 1) {

                                Geo_QR.this.moveTaskToBack(true);
                            }
                            else
                                finish();
                        }
                        //  finish();
                    }
                })
                .setNegativeButton(R.string.popup_no, null)
                .show();

    }





    public Integer ReadSysSetting()//////CHANGE INTO COMMON FUNCTION LATTER
    {
        int res =0;
        try {
            database = db.getReadableDatabase();
            Cursor cursor = database.rawQuery("SELECT * FROM  system_setting", null);
            if (cursor.moveToFirst()) {
                do {

                    AutoPunch= Integer.valueOf( cursor.getString(cursor.getColumnIndex("AutoPunchGeo")));
                    Biometric= Integer.valueOf( cursor.getString(cursor.getColumnIndex("Biometric")));
                } while (cursor.moveToNext());
            }
            cursor.close();
            res=1;
        }
        catch (Exception ex) {
            res=0;
            Log.d(TAG, ex.getMessage());
        }
        return res;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case BARCODE_READER_REQUEST_CODE:


                if (resultCode == CommonStatusCodes.SUCCESS) {
                    if (data != null) {
                        Barcode barcode = data.getParcelableExtra(barcodecaptureactivity.BarcodeObject);
                        Point[] p = barcode.cornerPoints;
                        // scanResult.setText(barcode.displayValue);
                        ReadQRCode(barcode.displayValue);
                        if (countResult == 1) {
                            int punchtype = ReadLastPunch();
                            if (punchtype == 0)
                                punchtype = 1;
                            else
                                punchtype = 0;

                            Toast.makeText(this,"Punched Successfully",Toast.LENGTH_LONG).show();
                           // scanResult.setText("Punched Successfully");
                           // scanResult.setTextColor(Color.GREEN);
                            countResult = 0;

                            SaveLog(BadgeNo, punchtype, Geoid, QrCodeId);
                        } else

                        {
                            Toast.makeText(this,"QR Code not Matching",Toast.LENGTH_LONG).show();
                            SoundIt(2);
                           // scanResult.setText("QR Code not Matching");
                          //  scanResult.setTextColor(Color.RED);//  Toast.makeText(this,"QR Code not Matching",Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this,"No Result Found",Toast.LENGTH_LONG).show();
                        //scanResult.setText("No Result Found");
                       // scanResult.setTextColor(Color.RED);
                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }

                break;

            case REQUEST_ENABLE_FT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                     validateLocation();
                    if(count>0) {
                        Intent intent = new Intent(getApplicationContext(), barcodecaptureactivity.class);
                        startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);



                    }
                }
                break;

            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    public Integer ReadQRCode(String QRCodeValue)//////CHANGE INTO COMMON FUNCTION LATTER
    {
        int res =0;
        Date dt;
        try {
            database = db.getReadableDatabase();
            Cursor cursor = database.rawQuery("SELECT * FROM  QRCode_Permission  order by ID Desc", null);
            if (cursor.moveToFirst()) {
                do {
                    QRCODE= ( cursor.getString(cursor.getColumnIndex("Qrcode")));
                    if(QRCodeValue.equals(QRCODE)) {
                        BadgeNo = (cursor.getString(cursor.getColumnIndex("BadgeNo")));
                        QrCodeId = Integer.valueOf(cursor.getString(cursor.getColumnIndex("QRId")));
                        Geoid = Integer.valueOf(cursor.getString(cursor.getColumnIndex("geoID")));
                        QRCodeName = cursor.getString(cursor.getColumnIndex("QRCode_Name"));
                        countResult=1;
                        break;

                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
            res=1;
        }
        catch (Exception ex) {
            res=0;
            Log.d(TAG, ex.getMessage());
        }
        return res;
    }



    public boolean checkServiceRunning(){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if ("com.nordicsemi.nrfUARTv2.LocationUpdateservice"
                    .equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }


    public boolean checkLocationMonitoringServiceRunning(){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if ("com.nordicsemi.nrfUARTv2.LocationMonitoringService"
                    .equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }


    public boolean checkServiceRunningGeoLog(){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if ("com.nordicsemi.nrfUARTv2.GeofenceLogService"
                    .equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }
}

