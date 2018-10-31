package com.hfjs.amapcardemo.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.hfjs.amapcardemo.R;
import com.hfjs.amapcardemo.clusters.Cluster;
import com.hfjs.amapcardemo.clusters.ClusterClickListener;
import com.hfjs.amapcardemo.clusters.ClusterItem;
import com.hfjs.amapcardemo.clusters.ClusterItemClickListener;
import com.hfjs.amapcardemo.clusters.ClusterOverlay;
import com.hfjs.amapcardemo.clusters.ClusterRender;
import com.hfjs.amapcardemo.clusters.LocationBean;
import com.hfjs.amapcardemo.clusters.RegionItem;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ClusterActivity extends RxAppCompatActivity {
    private Button add, point, clear, cluster, list, open, close, cart;
    private MapView mMapView;
    private AMap mAMap;
    private ClusterOverlay mClusterOverlay;
    private List<ClusterItem> mClusterItems;
    private String imgUrl = "http://106.14.186.44:9999/gps-web/rs/img/map/car/default/12.png";


    private boolean mIsCluster = true;//是否聚合

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster);
        initView();
        initMap(savedInstanceState);
    }

    private void initMap(Bundle savedInstanceState) {
        mMapView = findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);
        mAMap = mMapView.getMap();
        mAMap.setInfoWindowAdapter(infoWindowAdapter);//设置弹窗
        mAMap.setOnInfoWindowClickListener(infoWindowClickListener);
        mAMap.animateCamera(CameraUpdateFactory.changeLatLng(new LatLng(31.206078, 121.602948)));

    }

    private void initView() {
        add = findViewById(R.id.btn_1);
        point = findViewById(R.id.btn_2);
        clear = findViewById(R.id.btn_3);
        cluster = findViewById(R.id.btn_4);
        list = findViewById(R.id.btn_5);
        open = findViewById(R.id.btn_6);
        close = findViewById(R.id.btn_7);
        cart = findViewById(R.id.btn_8);
        setListener();
    }

    private void setListener() {
        add.setOnClickListener(v -> {
            new Thread(() -> addMapClusters()).start();
        });

        point.setOnClickListener(v -> {
            addPointCluster();
        });

        clear.setOnClickListener(v -> {
            if (mClusterOverlay != null) {
                mClusterItems.clear();
                mClusterOverlay.onDestroy();
                mClusterOverlay = null;
            }
            mAMap.clear();
        });

        cluster.setOnClickListener(v -> {
            mIsCluster = !mIsCluster;
            mClusterOverlay.updateClusters(mIsCluster);
        });

        list.setOnClickListener(v -> {

        });
        open.setOnClickListener(v -> {
            getMarkers(new LatLng(31.206078, 121.602948));
        });
        close.setOnClickListener(v -> {
            mClusterOverlay.hiedPopup();
        });
        cart.setOnClickListener(v -> {
            getMarkers(new LatLng(31.211078, 121.611948));
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mClusterOverlay != null) {
            mClusterItems.clear();
            mClusterOverlay.onDestroy();
            mClusterOverlay = null;
        }
        mAMap.clear();
        mMapView.onDestroy();
    }


    private List<LatLng> addLatLng(int num) {
        List<LatLng> latLngs = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            double lat = 31.206078 + Math.random();
            double lon = 121.602948 + Math.random();
            LatLng latlng = new LatLng(lat, lon);
            latLngs.add(latlng);
        }
        latLngs.add(new LatLng(31.206078, 121.602948));
        latLngs.add(new LatLng(31.206079, 121.602948));
        latLngs.add(new LatLng(31.211078, 121.611948));
        latLngs.add(new LatLng(31.211078, 121.611948));

        return latLngs;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

//*************************************添加聚合点*********************************************************************

    /**
     * 添加大量聚合点
     */
    private void addMapClusters() {
        if (mClusterItems == null) mClusterItems = new ArrayList<>();
        for (LatLng latLng : addLatLng(60)) {
            LocationBean bean = new LocationBean(imgUrl, String.valueOf(latLng.latitude).substring(0, 5));
            RegionItem cluster = new RegionItem(latLng, bean);
            mClusterItems.add(cluster);
        }

        if (mClusterOverlay == null) {
            mClusterOverlay = new ClusterOverlay(mAMap, mClusterItems, dp2px(this, 100), this);
//            mClusterOverlay.setClusterRenderer(render);
            mClusterOverlay.setOnClusterClickListener(clickListener);
            mClusterOverlay.setOnClusterItemClickListener(itemClickListener);
        } else {
            mClusterOverlay.setMorePoint(mClusterItems);
        }
        initMapBounds();
    }

    /**
     * 将聚合点显示到一屏上
     */
    private void initMapBounds() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (ClusterItem clusterItem : mClusterItems) {
            builder.include(clusterItem.getPosition());
        }
        LatLngBounds latLngBounds = builder.build();
        mAMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 0));
    }

    /**
     * 添加一个新点
     */
    private void addPointCluster() {
        double lat = Math.random() + 31.206078;
        double lon = Math.random() + 121.602948;

        LatLng latLng = new LatLng(lat, lon, false);
        LocationBean bean = new LocationBean(imgUrl, String.valueOf(latLng.latitude).substring(0, 5));
        RegionItem cluster = new RegionItem(latLng, bean);
        mClusterOverlay.addClusterItem(cluster);
    }
//***********************************点击事件******************************************************
    /**
     * 聚合点 点击事件
     */
    private ClusterClickListener clickListener = new ClusterClickListener() {
        @Override
        public void onClick(Marker marker, List<ClusterItem> clusterItems) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (ClusterItem clusterItem : clusterItems) {
                builder.include(clusterItem.getPosition());
            }
            LatLngBounds latLngBounds = builder.build();
            mAMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100));
        }
    };
    /**
     * 单个点击事件
     */
    private ClusterItemClickListener itemClickListener = new ClusterItemClickListener() {
        @Override
        public void onClick(Marker marker, ClusterItem clusterItem) {
            Log.e("onClick: ", clusterItem.getLocationBean().getTitle());
            marker.showInfoWindow();
            mAMap.animateCamera(CameraUpdateFactory.newLatLng(clusterItem.getPosition()));
        }
    };

//***********************************弹窗**************************************************

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

    /**
     * 自定义View并且绑定数据方法
     *
     * @param marker 点击的Marker对象
     * @return 返回自定义窗口的视图
     */
    private View getInfoWindowView(Marker marker) {
        Cluster cluster = (Cluster) marker.getObject();

        return infoView(cluster.getClusterItems().get(0).getLocationBean());
    }

    private AMap.OnInfoWindowClickListener infoWindowClickListener = marker -> {
        if (marker.isInfoWindowShown()) {
            marker.hideInfoWindow();//这个是隐藏infowindow窗口的方法
        }
    };


    private TextView mTitle;
    private View view;

    private View infoView(LocationBean bean) {
        if (view == null) {
            view = View.inflate(this, R.layout.view_marker_info, null);
            mTitle = view.findViewById(R.id.tv_marker_title);
        }
        mTitle.setText(bean.getTitle());
        return view;
    }

    //***********************************************************************************************
    private void getMarkers(LatLng latLng) {

//        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//        builder.include(latLng);
//        LatLngBounds latLngBounds = builder.build();
//        mAMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100));
//        mAMap.animateCamera(CameraUpdateFactory.changeLatLng(latLng));
//        mAMap.animateCamera(CameraUpdateFactory.zoomTo(18));

//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        List<Marker> markers = mAMap.getMapScreenMarkers();
//
//        Log.e("Marker", markers.size() + "");
//        for (Marker marker : markers) {
////            if (latLng.equals(marker.getPosition()))
////            Log.e("Marker", marker.getPosition() + "");
//            if (latLng.toString().equals(marker.getTitle())) {
//                marker.showInfoWindow();
//                Log.e("getMarkers:--44---> ", marker.getTitle() + "");
//                break;
//            }
//            Log.e("getMarkers:-----> ", marker.getTitle() + "");
//        }
//        popupMarker.showInfoWindow();
        mClusterOverlay.showPopup(latLng);
    }
}
