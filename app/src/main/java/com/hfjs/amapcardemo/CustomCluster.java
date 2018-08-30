package com.hfjs.amapcardemo;

import com.amap.api.maps.model.LatLng;
import com.hfjs.amapcardemo.Cluster.ClusterItem;

public class CustomCluster implements ClusterItem {
    private LatLng mLatLng;
    private String mTitle;

    public CustomCluster(LatLng latLng, String title) {
        mLatLng = latLng;
        mTitle = title;
    }
    public CustomCluster(LatLng latLng) {
        mLatLng = latLng;
    }

    @Override
    public LatLng getPosition() {
        return mLatLng;
    }

    public String getTitle() {
        return mTitle;
    }
}
