package com.hfjs.amapcardemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MultiPointItem;
import com.amap.api.maps.model.MultiPointOverlay;
import com.amap.api.maps.model.MultiPointOverlayOptions;
import com.hfjs.amapcardemo.Cluster.ClusterClickListener;
import com.hfjs.amapcardemo.Cluster.ClusterItem;
import com.hfjs.amapcardemo.Cluster.ClusterOverlay;
import com.hfjs.amapcardemo.Cluster.ClusterRender;
import com.koushikdutta.ion.Ion;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ClusterMarkersActivity extends RxAppCompatActivity {

    private MapView mMapView;
    private AMap aMap;
    private TextView addMarkers, openCluster, closeCluster, addAgain;

    private ClusterOverlay overlay;
    private LruCache<Integer, Drawable> mBackDrawAbles = new LruCache<Integer, Drawable>(3);
    private LruCache<String, BitmapDescriptor> mLruCache;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster_markers);
        mMapView = findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();
        initView();
    }

    private void initView() {
        addMarkers = findViewById(R.id.tv_add_markers);
        openCluster = findViewById(R.id.tv_cluster_open);
        closeCluster = findViewById(R.id.tv_cluster_close);
        addAgain = findViewById(R.id.tv_cluster_again);
        mLruCache = new LruCache<String, BitmapDescriptor>(80) {
            protected void entryRemoved(boolean evicted, String key, BitmapDescriptor oldValue, BitmapDescriptor newValue) {
                oldValue.getBitmap().recycle();
            }
        };
        setListener();
    }

    private void setListener() {
        addMarkers.setOnClickListener(v -> {
            setMultiPoint();
        });
        openCluster.setOnClickListener(v -> {
            new Thread(() -> addClustersToMap(addLatLng(80))).start();

        });
        closeCluster.setOnClickListener(v -> {
            overlay.updateClusters(0);
        });
        addAgain.setOnClickListener(v -> {
            clusterItems.clear();
            int i = 100;
            for (LatLng latLng : addLatLng(20)) {
                CustomCluster cluster = new CustomCluster(latLng, i++ + "-again");
                clusterItems.add(cluster);
            }
            overlay.setMorePoint(clusterItems);
        });
        aMap.setOnMarkerClickListener(marker -> {
            overlay.responseClusterClickEvent(marker);
            return true;
        });
    }

    private List<LatLng> addLatLng(int num) {
        List<LatLng> latLngs = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            double lat = 31.206078 + Math.random();
            double lon = 121.602948 + Math.random();
            LatLng latlng = new LatLng(lat, lon);
            latLngs.add(latlng);
        }
        return latLngs;
    }

    private List<ClusterItem> clusterItems = new ArrayList<>();

    private void addClustersToMap(List<LatLng> latLngs) {
        int i = 0;
        for (LatLng latLng : latLngs) {
            CustomCluster cluster = new CustomCluster(latLng, i++ + "-add");
            clusterItems.add(cluster);
        }

        if (overlay == null) {
            int clusterRadius = 48;
            overlay = new ClusterOverlay(aMap, clusterItems, dp2px(getApplicationContext(), clusterRadius), getApplicationContext());
            overlay.setClusterRenderer(render);
            overlay.setOnClusterClickListener(clickListener);
        } else {
            overlay.setMorePoint(clusterItems);
        }

    }

    private ClusterRender render = new ClusterRender() {
        @Override
        public Drawable getDrawAble(int clusterNum) {
            Drawable bitmapDrawable = mBackDrawAbles.get(2);
            if (bitmapDrawable == null) {
                bitmapDrawable = getApplication().getResources().getDrawable(R.mipmap.marker_bg);
                mBackDrawAbles.put(2, bitmapDrawable);
            }
            return bitmapDrawable;
        }

        @Override
        public BitmapDescriptor getView(String title) {
            BitmapDescriptor descriptor = mLruCache.get(title);
            if (descriptor == null) {
                View markerView = LayoutInflater.from(ClusterMarkersActivity.this).inflate(R.layout.item_marker, null);
                ImageView imageMark = markerView.findViewById(R.id.img_marker);
                TextView markerTitle = markerView.findViewById(R.id.tv_marker_title);
                try {
                    imageMark.setImageBitmap(Ion.with(ClusterMarkersActivity.this).load(url).asBitmap().get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                markerTitle.setText(title);
//                descriptor = BitmapDescriptorFactory.fromBitmap(viewToBitmap(markerView));
                descriptor = BitmapDescriptorFactory.fromView(markerView);
                mLruCache.put(title, descriptor);
            }
            return descriptor;
        }
    };
    String url = "http://ucardstorevideo.b0.upaiyun.com/test/e8c8472c-d16d-4f0a-8a7b-46416a79f4c6.png";

    private ClusterClickListener clickListener = new ClusterClickListener() {
        @Override
        public void onClick(Marker marker, List<ClusterItem> clusterItems) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (ClusterItem clusterItem : clusterItems) {
                builder.include(clusterItem.getPosition());
            }
            LatLngBounds latLngBounds = builder.build();
            aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 10));
        }
    };

    private void setMultiPoint() {
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.icon_car);
        MultiPointOverlayOptions overlayOptions = new MultiPointOverlayOptions();
        overlayOptions.icon(bitmapDescriptor);
        overlayOptions.anchor(0.1f, 0.5f);
        MultiPointOverlay multiPointOverlay = aMap.addMultiPointOverlay(overlayOptions);
        new Thread(() -> {
            if (isDestroy) return;
            List<MultiPointItem> list = new ArrayList<MultiPointItem>();
            for (LatLng latLng : addLatLng(50)) {
                MultiPointItem multiPointItem = new MultiPointItem(latLng);
                list.add(multiPointItem);
            }
            if (multiPointOverlay != null) {
                multiPointOverlay.setItems(list);
                multiPointOverlay.setEnable(true);
            }
        }).start();

    }

    boolean isDestroy = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overlay.onDestroy();
        overlay = null;
        isDestroy = true;
        mLruCache.evictAll();
        mBackDrawAbles.evictAll();
        mMapView.onDestroy();
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

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

//    public Bitmap viewToBitmap(View view) {
//
//        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//
//        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
//
//        view.buildDrawingCache();
//
//        Bitmap bitmap = view.getDrawingCache();
//
//        return bitmap;
//
//    }
}
