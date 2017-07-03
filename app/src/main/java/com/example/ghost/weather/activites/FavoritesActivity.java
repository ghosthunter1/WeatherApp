package com.example.ghost.weather.activites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ghost.weather.R;
import com.example.ghost.weather.adapter.WeatherAdapter;
import com.example.ghost.weather.objects.MainWeather;
import com.google.gson.Gson;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ghost on 7/3/17.
 */

public class FavoritesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ListView listView;
    private WeatherAdapter adapter;
    private MainWeather weather;
    private CoordinatorLayout coordinatLayout;
    private Gson gson;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawer;
    private MaterialSearchView searchView;
    private String unit = "metric";
    private Set<String> set = new HashSet<>();
    private TextView navHome, navFavorite, navTemp;
    private View view;
    private PopupMenu popupMenu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_favorites);
        iniUI();
    }

    private void iniUI() {
        listView = (ListView) findViewById(R.id.favorite_listview);
        coordinatLayout = (CoordinatorLayout) findViewById(R.id.favorites_activity_coordinat_layout);
        toolbar = (Toolbar) findViewById(R.id.favorite_activity_toolbar);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        drawer = (DrawerLayout) findViewById(R.id.favorite_activity_drawer);
        searchView = (MaterialSearchView) findViewById(R.id.favorite_search_view);
        view = navigationView.getHeaderView(0);
        navHome = view.findViewById(R.id.navigation_home);
        navFavorite = view.findViewById(R.id.navigation_favorite);
        navTemp = view.findViewById(R.id.navigation_temp_unit);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        gson = new Gson();
        adapter = new WeatherAdapter(this, 0, new ArrayList<MainWeather>());
        listView.setAdapter(adapter);
        navigationView();
        navigationView.setNavigationItemSelectedListener(this);
        savedCityNames();
        searchView();
        intents();
        onListviewItemClick();


    }

    private void savedCityNames() {
        SharedPreferences sharedPreferences = FavoritesActivity.this.getSharedPreferences("CITYNAMES", MODE_APPEND);
        Set<String> saves = sharedPreferences.getStringSet("NAMES", null);
        if (saves != null) {
            Iterator<String> iterator = saves.iterator();
            while (iterator.hasNext()) {
                new FindCityByName().execute(iterator.next(), preferences());
            }
        }
    }

    private void saveTemperatureUnit() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("SAVE", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("unit", unit);
        editor.commit();
    }


    private void intents() {
        view = navigationView.getHeaderView(0);
        navFavorite = view.findViewById(R.id.navigation_favorite);
        navHome = view.findViewById(R.id.navigation_home);
        navTemp = view.findViewById(R.id.navigation_temp_unit);
        navFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FavoritesActivity.this, FavoritesActivity.class);
                startActivity(intent);
            }
        });

        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        navTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMenu = new PopupMenu(FavoritesActivity.this, navTemp);
                popupMenu.getMenuInflater()
                        .inflate(R.menu.view_temp, popupMenu.getMenu());
                final MenuItem celsius = popupMenu.getMenu().findItem(R.id.menu_celsius);
                final MenuItem fahrenheit = popupMenu.getMenu().findItem(R.id.menu_fahrenheit);
                String units = preferences();
                if (units.endsWith("metric")) {
                    fahrenheit.setChecked(false);
                    celsius.setChecked(true);
                    saveTemperatureUnit();


                } else if (units.endsWith("imperial")) {

                    celsius.setChecked(false);
                    fahrenheit.setChecked(true);
                    savedCityNames();

                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_celsius:
                                unit = "metric";
                                saveTemperatureUnit();
                                if (!item.isChecked()) {
                                    item.setChecked(true);
                                    if (fahrenheit.isChecked()) {
                                        fahrenheit.setChecked(false);
                                        ;
                                    }
                                }
                                break;
                            case R.id.menu_fahrenheit:
                                unit = "imperial";
                                saveTemperatureUnit();
                                if (!item.isChecked()) {
                                    item.setChecked(true);
                                    if (celsius.isChecked()) {
                                        celsius.setChecked(false);
                                    }
                                }
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();

            }
        });


    }


    private void onListviewItemClick() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                ImageView remove = view.findViewById(R.id.adapter_remove_item);
                if (remove.getVisibility() == ImageView.VISIBLE) {
                    remove.setVisibility(ImageView.GONE);
                } else {
                    remove.setVisibility(ImageView.VISIBLE);
                }
                removeItem(remove, adapter.getItem(i), adapter.getItem(i).getName());
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
                intent.putExtra("CITYNAME", adapter.getItem(i).getName());
                startActivity(intent);
            }
        });
    }

    private void removeItem(ImageView remove, final MainWeather weather, final String cityName) {
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.remove(weather);
                set.remove(cityName);
            }
        });
    }

    private String preferences() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("SAVE", MODE_PRIVATE);
        return sharedPreferences.getString("unit", null);
    }

    private void navigationView() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(FavoritesActivity.this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

    public void searchView() {
        if (isNetworkAvailable()) {
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
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_search, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
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
                    if (!set.contains(weather.getName())) {
                        adapter.add(weather);
                        set.add(weather.getName());
                    }
                }


            } else {

                Snackbar snackbar = Snackbar.make(coordinatLayout, "Not Found", Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
                snackbar.setActionTextColor(Color.WHITE);
                snackbar.show();
            }

        }
    }


    private void sharedPreferences() {
        SharedPreferences sharedPreferences = FavoritesActivity.this.getSharedPreferences("CITYNAMES", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("NAMES", set);
        editor.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sharedPreferences();
    }
}
