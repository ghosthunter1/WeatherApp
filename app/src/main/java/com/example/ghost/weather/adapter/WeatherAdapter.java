package com.example.ghost.weather.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ghost.weather.R;
import com.example.ghost.weather.objects.current.MainWeather;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ghost on 7/3/17.
 */

public class WeatherAdapter extends ArrayAdapter<MainWeather> {
    private Context context;

    public WeatherAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<MainWeather> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MainWeather weather = getItem(position);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.view_adapter, parent, false);
        TextView cityName = view.findViewById(R.id.adapter_name);
        TextView temp = view.findViewById(R.id.adapter_temp);
        ImageView picture = view.findViewById(R.id.adapter_icon);
        cityName.setText(weather.getName());

        if (preferences().equals("metric")) {
            temp.setText(String.valueOf(weather.getMain().getTemp()) + "\u2103");
        } else {
            temp.setText(String.valueOf(weather.getMain().getTemp()) + "\u2109");
        }

        for (int i = 0; i < weather.getWeather().size(); i++) {
            Picasso.with(context).load("http://openweathermap.org/img/w/" + weather.getWeather().get(i).getIcon() + ".png").into(picture);
        }


        return view;
    }

    private String preferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("SAVE", Context.MODE_PRIVATE);
        if (sharedPreferences.getString("unit", null) != null) {
            return sharedPreferences.getString("unit", null);
        }
        return "metric";

    }
}
