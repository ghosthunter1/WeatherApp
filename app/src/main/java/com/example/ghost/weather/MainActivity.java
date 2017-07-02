package com.example.ghost.weather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.security.Permission;
import java.util.Calendar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private TextView mTemp, mName, mHumidity, mPreesure, mClouds, mLastUpdate, mWindSpeed, mDescription, mMinMax;
    private MaterialSearchView searchView;
    private ImageView picture;
    private MainWeather weather;
    private Gson gson = new Gson();
    private CoordinatorLayout coordinatLayout;
    private Toolbar toolbar;
    private Calendar calendar;
    private LocationRequest locationRequest;
    private String unit = "metric";
    private GoogleApiClient client;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        iniUI();
    }

    private void iniUI() {
        mTemp = (TextView) findViewById(R.id.city_temperature);
        mName = (TextView) findViewById(R.id.city_name);
        mHumidity = (TextView) findViewById(R.id.city_humidity);
        mPreesure = (TextView) findViewById(R.id.city_preesure);
        mClouds = (TextView) findViewById(R.id.city_clouds);
        mLastUpdate = (TextView) findViewById(R.id.last_update);
        mDescription = (TextView) findViewById(R.id.city_description);
        mMinMax = (TextView) findViewById(R.id.city_min_max);
        picture = (ImageView) findViewById(R.id.picture);
        mWindSpeed = (TextView) findViewById(R.id.city_wind_speed);
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        coordinatLayout = (CoordinatorLayout) findViewById(R.id.mainactivity_coordinat_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        setSupportActionBar(toolbar);
        buildGoogleClient();
        searchview();
        connectionProblemSnackbar();

    }


    private void searchview() {
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (isNetworkAvailable()) {
                    new FindCityByName().execute(query, unit);
                } else {
                    connectionProblemSnackbar();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_search, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }

    private synchronized void buildGoogleClient() {
        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


    }


    @Override
    public void onConnected(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            locationRequest();
        }
        location = LocationServices.FusedLocationApi.getLastLocation(client);
        if (location != null) {
            double latitude = location.getLatitude();
            double longtitude = location.getLongitude();
            new FindCityByCoordinates().execute(String.valueOf(latitude), String.valueOf(longtitude), unit);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void locationRequest() {
        requestPermissions(new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
        }, 1);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void connectionProblemSnackbar() {
        if (!isNetworkAvailable()) {
            Snackbar snackbar = Snackbar.make(coordinatLayout, "Connection Problem", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Retry", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recreate();
                }
            });
            snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
            snackbar.show();
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    class FindCityByName extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("http://api.openweathermap.org/data/2.5/weather?q=" + strings[0] + "&units=" + strings[1] + "&appid=0559b29e30ef329bb28d598ec6bab17d")
                    .build();


            Response response = null;
            try {
                response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null && s.contains("cod")) {
                weather = gson.fromJson(s, MainWeather.class);
                if (weather.getCod() == 200) {
                    mTemp.setText(String.valueOf(weather.getMain().getTemp()));
                    mMinMax.setText(String.valueOf(weather.getMain().getTempMax()) + " / " + String.valueOf(weather.getMain().getTempMin()));
                    mClouds.setText(String.valueOf(weather.getClouds().getAll()));
                    mHumidity.setText(String.valueOf(weather.getMain().getHumidity()));
                    mPreesure.setText(String.valueOf(weather.getMain().getPressure()));
                    mLastUpdate.setText(String.valueOf(calendar.getTime()));
                    mWindSpeed.setText(String.valueOf(weather.getWind().getSpeed()));
                    mName.setText(weather.getName());


                    for (int i = 0; i < weather.getWeather().size(); i++) {
                        Picasso.with(MainActivity.this).load("http://openweathermap.org/img/w/" + weather.getWeather().get(i).getIcon() + ".png").into(picture);
                        mDescription.setText(weather.getWeather().get(i).getDescription());
                    }


                } else {
                    Snackbar.make(coordinatLayout, "Not Found", Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    class FindCityByCoordinates extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("http://api.openweathermap.org/data/2.5/weather?lat=" + strings[0] + "&lon=" + strings[1] + "&units=" + strings[2] + "&appid=0559b29e30ef329bb28d598ec6bab17d")
                    .build();


            Response response = null;
            try {
                response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null && s.contains("cod")) {
                weather = gson.fromJson(s, MainWeather.class);
                if (weather.getCod() == 200) {
                    mTemp.setText(String.valueOf(weather.getMain().getTemp()));
                    mMinMax.setText(String.valueOf(weather.getMain().getTempMax()) + " / " + String.valueOf(weather.getMain().getTempMin()));
                    mClouds.setText(String.valueOf(weather.getClouds().getAll()));
                    mHumidity.setText(String.valueOf(weather.getMain().getHumidity()));
                    mPreesure.setText(String.valueOf(weather.getMain().getPressure()));
                    mLastUpdate.setText(String.valueOf(calendar.getTime()));
                    mWindSpeed.setText(String.valueOf(weather.getWind().getSpeed()));
                    mName.setText(weather.getName());
                    Toast.makeText(MainActivity.this , "WORKING" , Toast.LENGTH_LONG).show();


                    for (int i = 0; i < weather.getWeather().size(); i++) {
                        Picasso.with(MainActivity.this).load("http://openweathermap.org/img/w/" + weather.getWeather().get(i).getIcon() + ".png").into(picture);
                        mDescription.setText(weather.getWeather().get(i).getDescription());
                    }


                } else {
                    Snackbar.make(coordinatLayout, "Not Found", Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        client.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        client.disconnect();
    }
}




