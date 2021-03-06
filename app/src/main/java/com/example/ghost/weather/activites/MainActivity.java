package com.example.ghost.weather.activites;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
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
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ghost.weather.adapter.RecyclerViewAdapter;
import com.example.ghost.weather.objects.current.MainWeather;
import com.example.ghost.weather.R;
import com.example.ghost.weather.objects.threeday.DayList;
import com.example.ghost.weather.objects.threeday.MainWeatherWeek;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, NavigationView.OnNavigationItemSelectedListener {
    private TextView mTemp, mName, navHome, navFavorite, navTempUnit, mHumidity, mPreesure, mClouds, mLastUpdate, mWindSpeed, mDescription, mMinMax;
    private MaterialSearchView searchView;
    private ImageView picture;
    private MainWeather weather;
    private Gson gson = new Gson();
    private CoordinatorLayout coordinatLayout;
    private Toolbar toolbar;
    private Calendar calendar;
    private String unit = "metric";
    private GoogleApiClient client;
    private Location location;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private View view;
    private PopupMenu popupMenu;
    private boolean intentHasExtras = false;
    private double longtitude, latitude;
    private boolean afterRecreate = false;
    private String name;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private List<DayList> dayLists = new ArrayList<>();
    private static boolean unitChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        mLastUpdate = (TextView) findViewById(R.id.last_update);
        mDescription = (TextView) findViewById(R.id.city_description);
        mMinMax = (TextView) findViewById(R.id.city_min_max);
        picture = (ImageView) findViewById(R.id.picture);
        view = navigationView.getHeaderView(0);
        navFavorite = view.findViewById(R.id.navigation_favorite);
        navHome = view.findViewById(R.id.navigation_home);
        navTempUnit = view.findViewById(R.id.navigation_temp_unit);
        mWindSpeed = (TextView) findViewById(R.id.city_wind_speed);
        drawerLayout = (DrawerLayout) findViewById(R.id.mainactivity_drawer_layout);
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        coordinatLayout = (CoordinatorLayout) findViewById(R.id.mainactivity_coordinat_layout);
        navigationView.setNavigationItemSelectedListener(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        buildGoogleClient();
        navigationView();
        if (preferences() != null) {
            unit = preferences();
        }
        if (unit.equals("metric")) {
            navTempUnit.setText("\u2103");
        } else {
            navTempUnit.setText("\u2109");
        }
        afterRecreateSavedCityName();
        if (name != null) {
            afterRecreate = true;
            new FindCityByName().execute(name, unit);
            new WeekForecast().execute(name, unit);
        }
        intentHasExtras();
        searchview();
        connectionProblemSnackbar();
        intents();
        recyclerView();
        defaultCity();


    }

    private void defaultCity() {
        if (name == null && !intentHasExtras && isNetworkAvailable() && getSavedCityName() == null) {
            new FindCityByName().execute("new york", unit);
            new WeekForecast().execute("new york", unit);
        }
    }

    private void onRetryButtonClick(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recreate();
            }
        });
    }

    private void recyclerView() {
        recyclerViewAdapter = new RecyclerViewAdapter(dayLists, this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void background(int i) {
        if (!unitChanged) {
            switch (weather.getWeather().get(i).getIcon()) {
                case "09d":
                    coordinatLayout.setBackgroundResource(R.drawable.rain);
                    break;
                case "09n":
                    coordinatLayout.setBackgroundResource(R.drawable.rain);
                    break;
                case "10d":
                    coordinatLayout.setBackgroundResource(R.drawable.rain);
                    break;
                case "10n":
                    coordinatLayout.setBackgroundResource(R.drawable.rain);
                    break;
                case "01d":
                    coordinatLayout.setBackgroundResource(R.drawable.clearsky);
                    break;
                case "01n":
                    coordinatLayout.setBackgroundResource(R.drawable.nightclearsky);
                    break;
                case "02d":
                    coordinatLayout.setBackgroundResource(R.drawable.fewcloud);
                    break;
                case "02n":
                    coordinatLayout.setBackgroundResource(R.drawable.nightcloud);
                    break;
                case "03d":
                    coordinatLayout.setBackgroundResource(R.drawable.scattered);
                    break;
                case "03n":
                    coordinatLayout.setBackgroundResource(R.drawable.nightcloud);
                    break;
                case "04d":
                    coordinatLayout.setBackgroundResource(R.drawable.brokencloud);
                    break;
                case "04n":
                    coordinatLayout.setBackgroundResource(R.drawable.brokencloud);
                    break;
                case "11d":
                    coordinatLayout.setBackgroundResource(R.drawable.storm);
                    break;
                case "11n":
                    coordinatLayout.setBackgroundResource(R.drawable.storm);
                    break;
                case "13d":
                    coordinatLayout.setBackgroundResource(R.drawable.snow);
                    break;
                case "13n":
                    coordinatLayout.setBackgroundResource(R.drawable.nightsnow);
                    break;
                case "50d":
                    coordinatLayout.setBackgroundResource(R.drawable.mist);
                    break;
                case "50n":
                    coordinatLayout.setBackgroundResource(R.drawable.nightmist);
                    break;
                default:
                    coordinatLayout.setBackgroundResource(R.drawable.clearsky);


            }
            palette();
        }
    }


    private void searchview() {
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (isNetworkAvailable()) {
                    new FindCityByName().execute(query, unit);
                    new WeekForecast().execute(query, unit);
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

    private void saveCityNametBeforeRecreate(String cityName) {
        SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences("RECREATE", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (cityName != null) {
            editor.putString("CITYNAME", cityName);
            editor.commit();
        }
    }

    private void afterRecreateSavedCityName() {
        SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences("RECREATE", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.getString("CITYNAME", null) != null) {
            name = sharedPreferences.getString("CITYNAME", null);

        }
        editor.clear();
        editor.commit();

    }


    private void navigationView() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }

    private void intentHasExtras() {
        if (getIntent().hasExtra("CITYNAME")) {
            saveCityNametBeforeRecreate(getIntent().getStringExtra("CITYNAME"));
            new FindCityByName().execute(getIntent().getStringExtra("CITYNAME"), unit);
            new WeekForecast().execute(getIntent().getStringExtra("CITYNAME"), unit);
            intentHasExtras = true;
        } else {
            intentHasExtras = false;

        }
    }

    private void saveCityName(String cityName) {
        SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences("CITY_SAVED_NAME", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("NAME", cityName);
        editor.commit();


    }

    private String getSavedCityName() {
        SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences("CITY_SAVED_NAME", MODE_PRIVATE);
        if (sharedPreferences.getString("NAME", null) != null) {
            return sharedPreferences.getString("NAME", null);
        }
        return null;
    }

    private void palette() {
        coordinatLayout.setDrawingCacheEnabled(true);
        coordinatLayout.buildDrawingCache();
        Drawable drawable = coordinatLayout.getBackground();
        Bitmap bitmap = drawableToBitmap(drawable);
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                navigationView.setBackgroundColor(palette.getLightMutedColor(Color.BLUE));
            }
        });
    }


    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    private String preferences() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("SAVE", MODE_PRIVATE);
        if (sharedPreferences.getString("unit", null) != null) {
            return sharedPreferences.getString("unit", null);
        }
        return "metric";
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

        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });

        navTempUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMenu = new PopupMenu(MainActivity.this, navTempUnit);
                popupMenu.getMenuInflater()
                        .inflate(R.menu.view_temp, popupMenu.getMenu());
                final MenuItem celsius = popupMenu.getMenu().findItem(R.id.menu_celsius);
                final MenuItem fahrenheit = popupMenu.getMenu().findItem(R.id.menu_fahrenheit);
                String units = preferences();
                if (preferences() != null) {
                    if (units.endsWith("metric")) {
                        fahrenheit.setChecked(false);
                        celsius.setChecked(true);


                    } else if (units.endsWith("imperial")) {

                        celsius.setChecked(false);
                        fahrenheit.setChecked(true);

                    }
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_celsius:
                                unitChanged = true;
                                if (!item.isChecked()) {
                                    item.setChecked(true);
                                    unit = "metric";
                                    saveTemperatureUnit();
                                    if (fahrenheit.isChecked()) {
                                        fahrenheit.setChecked(false);
                                    }
                                }
                                if (weather != null) {
                                    saveCityNametBeforeRecreate(weather.getName());
                                }
                                drawerLayout.closeDrawers();
                                recreate();
                                break;
                            case R.id.menu_fahrenheit:
                                unitChanged = true;
                                if (!item.isChecked()) {
                                    item.setChecked(true);
                                    unit = "imperial";
                                    saveTemperatureUnit();
                                    if (celsius.isChecked()) {
                                        celsius.setChecked(false);
                                    }
                                }

                                if (weather != null) {
                                    saveCityNametBeforeRecreate(weather.getName());
                                }
                                recreate();
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();

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
            if (!intentHasExtras && !afterRecreate) {
                latitude = location.getLatitude();
                longtitude = location.getLongitude();
                new FindCityByCoordinates().execute(String.valueOf(latitude), String.valueOf(longtitude), unit);
                new WeekForecastByCoordinates().execute(String.valueOf(latitude), String.valueOf(longtitude), unit);
            }

        } else {
            if (!intentHasExtras && !afterRecreate) {
                if (getSavedCityName() != null) {
                    new FindCityByName().execute(getSavedCityName(), unit);
                    new WeekForecast().execute(getSavedCityName(), unit);
                }
            }
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
                        mWindSpeed.setText(String.valueOf(weather.getWind().getSpeed() + "\n" + "Met/Sec") + "\n" + "Wind Speed");
                    } else {
                        mWindSpeed.setText(String.valueOf(weather.getWind().getSpeed() + "\n" + " Mil/Hou") + "\n" + "Wind Speed");
                    }
                    mName.setText(weather.getName());


                    for (int i = 0; i < weather.getWeather().size(); i++) {
                        Picasso.with(MainActivity.this).load("http://openweathermap.org/img/w/" + weather.getWeather().get(i).getIcon() + ".png").into(picture);
                        mDescription.setText(weather.getWeather().get(i).getDescription());
                        background(i);
                    }

                    saveCityName(weather.getName());

                    palette();

                } else {
                    Snackbar snackbar = Snackbar.make(coordinatLayout, "Not Found", Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
                    snackbar.setActionTextColor(Color.WHITE);
                    snackbar.show();
                }

            } else {

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
                        mWindSpeed.setText(String.valueOf(weather.getWind().getSpeed() + "\n" + "Met/Sec") + "\n" + "Wind Speed");
                    } else {
                        mWindSpeed.setText(String.valueOf(weather.getWind().getSpeed() + "\n" + " Mil/Hou") + "\n" + "WindSpeed");
                    }
                    mName.setText(weather.getName());


                    for (int i = 0; i < weather.getWeather().size(); i++) {
                        Picasso.with(MainActivity.this).load("http://openweathermap.org/img/w/" + weather.getWeather().get(i).getIcon() + ".png").into(picture);
                        mDescription.setText(weather.getWeather().get(i).getDescription());
                        background(i);
                    }
                    if (unitChanged){
                        unitChanged = false;
                    }

                } else {

                    Snackbar snackbar = Snackbar.make(coordinatLayout, "Not Found", Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
                    snackbar.setActionTextColor(Color.WHITE);
                    snackbar.show();
                }

            } else {
            }
        }
    }

    class WeekForecast extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("http://api.openweathermap.org/data/2.5/forecast/daily?q=" + strings[0] + "&units=" + strings[1] + "&cnt=7&appid=0559b29e30ef329bb28d598ec6bab17d")
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
            if (s.contains("lon") && s.contains("lat")) {
                MainWeatherWeek weatherWeek = gson.fromJson(s, MainWeatherWeek.class);
                if (Integer.parseInt(weatherWeek.getCod()) == 200) {
                    for (int i = 0; i < weatherWeek.getList().size(); i++) {
                        if (dayLists.size() == 7) {
                            dayLists.clear();
                        }
                        dayLists.add(weatherWeek.getList().get(i));
                    }
                    recyclerViewAdapter.notifyDataSetChanged();

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

    class WeekForecastByCoordinates extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("http://api.openweathermap.org/data/2.5/forecast/daily?lat=" + strings[0] + "&lon=" + strings[1] + "&units=" + strings[2] + "&cnt=7&appid=0559b29e30ef329bb28d598ec6bab17d")
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
            if (s != null && s.contains("lon") && s.contains("lat")) {
                MainWeatherWeek weatherWeek = gson.fromJson(s, MainWeatherWeek.class);
                if (Integer.parseInt(weatherWeek.getCod()) == 200) {
                    for (int i = 0; i < weatherWeek.getList().size(); i++) {
                        if (dayLists.size() == 7) {
                            dayLists.clear();
                        }
                        dayLists.add(weatherWeek.getList().get(i));
                    }
                    recyclerViewAdapter.notifyDataSetChanged();

                }
            }
        }
    }


}






