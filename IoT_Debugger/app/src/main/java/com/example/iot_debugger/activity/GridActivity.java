package com.example.iot_debugger.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Toast;

import com.test.service.R;

import java.util.ArrayList;

public class GridActivity extends Activity {

    private Context mContext;
    private GridView grid_photo;
    private BaseAdapter mAdapter = null;
    private ArrayList<Icon> mData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        mContext = GridActivity.this;
        grid_photo = findViewById(R.id.grid_photo);

        mData = new ArrayList<Icon>();
        mData.add(new Icon(R.drawable.blue, "蓝牙调试"));
        mData.add(new Icon(R.drawable.wifi, "wifi调试"));

        mAdapter = new MyAdapter<Icon>(mData, R.layout.item_grid_icon) {
            @Override
            public void bindView(ViewHolder holder, Icon obj) {
                holder.setImageResource(R.id.img_icon, obj.getiId());
                holder.setText(R.id.txt_icon, obj.getiName());
            }
        };

        grid_photo.setAdapter(mAdapter);


        grid_photo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    Toast.makeText(mContext, "进入蓝牙串口调试器", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(GridActivity.this, BlueActivity.class);
                    startActivity(intent);
                    return;
                }
                if(position==1){
                    Toast.makeText(mContext, "进入无线网络调试器", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(GridActivity.this, WifiActivity.class);
                    startActivity(intent);
                    return;
                }
            }
        });
    }
}
