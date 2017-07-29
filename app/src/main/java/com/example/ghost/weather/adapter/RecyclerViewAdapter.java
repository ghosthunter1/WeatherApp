package com.example.ghost.weather.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ghost.weather.Days;
import com.example.ghost.weather.R;
import com.example.ghost.weather.objects.threeday.DayList;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;

/**
 * Created by ghost on 7/28/17.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyHolder> {

    private List<DayList> lists;
    private Context context;

    public RecyclerViewAdapter(List<DayList> lists, Context context) {
        this.lists = lists;
        this.context = context;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.single_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        holder.temp.setText(String.valueOf(lists.get(position).getTemp().getDay()));
        days(holder.day, position);
        for (int i = 0; i < lists.get(position).getWeather().size(); i++) {
            Picasso.with(context).load("http://openweathermap.org/img/w/" + lists.get(position).getWeather().get(i).getIcon() + ".png").into(holder.picture);
            holder.desc.setText(String.valueOf(lists.get(position).getWeather().get(i).getDescription()));
        }
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }


    class MyHolder extends RecyclerView.ViewHolder {

        public TextView day;
        public ImageView picture;
        public TextView temp;
        public TextView desc;

        public MyHolder(View itemView) {
            super(itemView);
            day = itemView.findViewById(R.id.recyclerview_day);
            desc = itemView.findViewById(R.id.recyclerview_description);
            temp = itemView.findViewById(R.id.recyclerview_temp);
            picture = itemView.findViewById(R.id.recyclerview_image);
        }
    }


    public void days(TextView textView, int position) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (calendar.getTime().toString().trim().startsWith("Sun")) {
            textView.setText(Days.sun[position]);
        } else if (calendar.getTime().toString().trim().startsWith("Wed")) {
            textView.setText(Days.wed[position]);
        } else if (calendar.getTime().toString().trim().startsWith("Mon")) {
            textView.setText(Days.mon[position]);
        } else if (calendar.getTime().toString().trim().startsWith("Tues")) {
            textView.setText(Days.tue[position]);
        } else if (calendar.getTime().toString().trim().startsWith("Thurs")) {
            textView.setText(Days.thu[position]);
        } else if (calendar.getTime().toString().trim().startsWith("Sat")) {
            textView.setText(Days.sat[position]);
        } else if (calendar.getTime().toString().trim().startsWith("Fri")) {
            textView.setText(Days.fri[position]);
        }else {
            textView.setText(calendar.getTime().toString());
        }
    }
}
