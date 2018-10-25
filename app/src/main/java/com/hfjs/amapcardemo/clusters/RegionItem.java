package com.hfjs.amapcardemo.clusters;

import com.amap.api.maps.model.LatLng;

public class RegionItem implements ClusterItem {
    private LatLng mLatLng;
    private LocationBean mLocationBean;

    public RegionItem(LatLng latLng, LocationBean locationBean) {
        mLatLng = latLng;
        mLocationBean = locationBean;
    }

    @Override
    public LatLng getPosition() {
        return mLatLng;
    }

    @Override
    public LocationBean getLocationBean() {
        return mLocationBean;
    }


}
