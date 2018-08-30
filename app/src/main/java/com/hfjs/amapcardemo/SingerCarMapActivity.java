package com.hfjs.amapcardemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class SingerCarMapActivity extends RxAppCompatActivity {
    private MapView mMapView;
    private AMap aMap;
    private PolylineOptions options;
    private LatLng latLng;
    private double lat = 35.909736, lng = 80.947266;
    private int timerTime = 5;
    private Disposable mDisposable;
    private TextView start, stop;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singer_map);
        mMapView = findViewById(R.id.map_view);
        start = findViewById(R.id.tv_start);
        stop = findViewById(R.id.tv_stop);

        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();
        aMap.setInfoWindowAdapter(infoWindowAdapter);
        latLng = new LatLng(lat, lng);
        setLine(latLng);
        setListener();
    }

    private void setLine(LatLng latLng) {
        if (options == null)
            options = new PolylineOptions();
        options.add(latLng).color(Color.GREEN).width(15);
        aMap.addPolyline(options);
        MapUtils.setMap(aMap, latLng);
    }

    private void setListener() {
        start.setOnClickListener(v -> {
            timeLocation();
        });

        stop.setOnClickListener(v -> {
            stopDisposable();
        });
    }


//    private void timeLocation() {
//        aMap.clear();
//        setLine(latLng);
//        MapUtils.setPointMarker(aMap, latLng, R.mipmap.start);
//
//        mDisposable = Observable.interval(timerTime, TimeUnit.SECONDS).compose(bindToLifecycle()).observeOn(AndroidSchedulers.mainThread())
//                .subscribe(aLong -> {
//                    latLng = new LatLng(latLng.latitude + 0.001, latLng.longitude + 0.001);
//                    setLine(latLng);
//                    Marker marker = MapUtils.getPointMarker(aMap, latLng, R.mipmap.end);
//                    marker.showInfoWindow();
//                    if (title!=null)title.setText(latLng.toString());
//                });
//    }

    private Marker marker;
    private BitmapDescriptor descriptor;

    private void timeLocation() {
        aMap.clear();
        setLine(latLng);
        MapUtils.setPointMarker(aMap, latLng, R.mipmap.start);

        marker = MapUtils.getPointMarker(aMap, latLng, R.mipmap.start);
        descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.icon_car);
        marker.setIcon(descriptor);

        mDisposable = Observable.interval(timerTime, TimeUnit.SECONDS).compose(bindToLifecycle()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    latLng = new LatLng(latLng.latitude + 0.001, latLng.longitude + 0.001);
                    setLine(latLng);
                    if (marker == null) {
                        marker = MapUtils.getPointMarker(aMap, latLng, R.mipmap.start);
                        descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.icon_car);
                        marker.setIcon(descriptor);
                    } else {
                        marker.setPosition(latLng);
                        marker.setIcon(descriptor);
                    }
                    marker.showInfoWindow();
                    if (title != null) title.setText(latLng.toString());
                });
    }

    private void stopDisposable() {
        if (mDisposable != null && !mDisposable.isDisposed()) mDisposable.dispose();
        options.getPoints().clear();
        marker.remove();
        MapUtils.setPointMarker(aMap, latLng, R.mipmap.end);
    }
    AMap.InfoWindowAdapter infoWindowAdapter = new AMap.InfoWindowAdapter() {
        @Override
        public View getInfoWindow(Marker marker) {
            Log.e("", marker.getId());
            return getInfoWindowView(marker);
        }

        @Override
        public View getInfoContents(Marker marker) {
            return getInfoWindowView(marker);
        }
    };
    private LinearLayout infoWindowLayout;
    private TextView title;
    /**
     * 自定义View并且绑定数据方法
     *
     * @param marker 点击的Marker对象
     * @return 返回自定义窗口的视图
     */
    private View getInfoWindowView(Marker marker) {
        if (infoWindowLayout == null) {
            infoWindowLayout = new LinearLayout(this);
            infoWindowLayout.setOrientation(LinearLayout.VERTICAL);

            title = new TextView(this);
            title.setTextColor(Color.BLACK);

            infoWindowLayout.setBackgroundResource(R.drawable.infowindow_bg);
            infoWindowLayout.addView(title);
        }
        return infoWindowLayout;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        aMap.clear();
        aMap = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
}
