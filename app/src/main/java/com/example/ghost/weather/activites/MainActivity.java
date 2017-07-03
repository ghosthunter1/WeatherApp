package com.example.ghost.weather.activites;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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

import com.example.ghost.weather.objects.MainWeather;
import com.example.ghost.weather.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, NavigationView.OnNavigationItemSelectedListener {
    private TextView mTemp, mName, navHome, navFavorite, navTempUnit, mHumidity, mPreesure, mClouds, mLastUpdate, mWindSpeed, mDescription, line, mMinMax;
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
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;


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
        drawerLayout = (DrawerLayout) findViewById(R.id.mainactivity_drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        line = (TextView) findViewById(R.id.line);
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        coordinatLayout = (CoordinatorLayout) findViewById(R.id.mainactivity_coordinat_layout);
        View view = navigationView.getHeaderView(0);
        navHome = (TextView) view.findViewById(R.id.navigation_home);
        navFavorite = (TextView) view.findViewById(R.id.navigation_favorite);
        navTempUnit = (TextView) view.findViewById(R.id.navigation_temp_unit);
        navigationView.setNavigationItemSelectedListener(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        setSupportActionBar(toolbar);
        navigationView();
        if (preferences() != null) {
            unit = preferences();
        }
        buildGoogleClient();
        searchview();
        connectionProblemSnackbar();
        intents();

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

    private void navigationView() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }

    private String preferences() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("SAVE", MODE_PRIVATE);
        return sharedPreferences.getString("unit", null);
    }

    private void saveTemperatureUnit() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("SAVE", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("unit", unit);
        editor.commit();
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

    private void intents() {
        navFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
                startActivity(intent);
            }
        });
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
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
                    if (!(line.getVisibility() == TextView.VISIBLE)) {
                        line.setVisibility(TextView.VISIBLE);
                    }
                    if (unit.equals("metric")) {
                        mTemp.setText(String.valueOf(weather.getMain().getTemp()) + "\u2103");
                    } else {
                        mTemp.setText(String.valueOf(weather.getMain().getTemp()) + "\u2109");
                    }
                    mMinMax.setText(String.valueOf(weather.getMain().getTempMax()) + "\u00B0" + " / " + String.valueOf(weather.getMain().getTempMin()) + "\u00B0");
                    mClouds.setText(String.valueOf(weather.getClouds().getAll()) + "%" + "\n" + "Clouds");
                    mHumidity.setText(String.valueOf(weather.getMain().getHumidity()) + "%" + "\n" + "Humidity");
                    mPreesure.setText(String.valueOf(weather.getMain().getPressure()) + " hPa" + "\n" + "Pressure");
                    mLastUpdate.setText(String.valueOf(calendar.getTime()));
                    if (unit.equals("metric")) {
                        mWindSpeed.setText(String.valueOf(weather.getWind().getSpeed() + " Meter/Sec") + "\n" + "Wind Speed");
                    } else {
                        mWindSpeed.setText(String.valueOf(weather.getWind().getSpeed() + " Mile/Hour") + "\n" + "WindSpeed");
                    }
                    mName.setText(weather.getName());


                    for (int i = 0; i < weather.getWeather().size(); i++) {
                        Picasso.with(MainActivity.this).load("http://openweathermap.org/img/w/" + weather.getWeather().get(i).getIcon() + ".png").into(picture);
                        mDescription.setText(weather.getWeather().get(i).getDescription());
                    }


                } else {
                    if (line.getVisibility() == TextView.VISIBLE) {
                        line.setVisibility(TextView.GONE);
                    }
                    Snackbar snackbar = Snackbar.make(coordinatLayout, "Not Found", Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
                    snackbar.setActionTextColor(Color.WHITE);
                    snackbar.show();
                }

            } else {
                if (line.getVisibility() == TextView.VISIBLE) {
                    line.setVisibility(TextView.GONE);
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
                    if (!(line.getVisibility() == TextView.VISIBLE)) {
                        line.setVisibility(TextView.VISIBLE);
                    }
                    if (unit.equals("metric")) {
                        mTemp.setText(String.valueOf(weather.getMain().getTemp()) + "\u2103");
                    } else {
                        mTemp.setText(String.valueOf(weather.getMain().getTemp()) + "\u2109");
                    }
                    mMinMax.setText(String.valueOf(weather.getMain().getTempMax()) + "\u00B0" + " / " + String.valueOf(weather.getMain().getTempMin()) + "\u00B0");
                    mClouds.setText(String.valueOf(weather.getClouds().getAll()) + "%" + "\n" + "Clouds");
                    mHumidity.setText(String.valueOf(weather.getMain().getHumidity()) + "%" + "\n" + "Humidity");
                    mPreesure.setText(String.valueOf(weather.getMain().getPressure()) + " hPa" + "\n" + "Pressure");
                    mLastUpdate.setText(String.valueOf(calendar.getTime()));
                    if (unit.equals("metric")) {
                        mWindSpeed.setText(String.valueOf(weather.getWind().getSpeed() + " Meter/Sec") + "\n" + "Wind Speed");
                    } else {
                        mWindSpeed.setText(String.valueOf(weather.getWind().getSpeed() + " Mile/Hour") + "\n" + "WindSpeed");
                    }
                    mName.setText(weather.getName());


                    for (int i = 0; i < weather.getWeather().size(); i++) {
                        Picasso.with(MainActivity.this).load("http://openweathermap.org/img/w/" + weather.getWeather().get(i).getIcon() + ".png").into(picture);
                        mDescription.setText(weather.getWeather().get(i).getDescription());
                    }


                } else {
                    if (line.getVisibility() == TextView.VISIBLE) {
                        line.setVisibility(TextView.GONE);
                    }
                    Snackbar snackbar = Snackbar.make(coordinatLayout, "Not Found", Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
                    snackbar.setActionTextColor(Color.WHITE);
                    snackbar.show();
                }

            } else {
                if (line.getVisibility() == TextView.VISIBLE) {
                    line.setVisibility(TextView.GONE);
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
        saveTemperatureUnit();
    }

}




