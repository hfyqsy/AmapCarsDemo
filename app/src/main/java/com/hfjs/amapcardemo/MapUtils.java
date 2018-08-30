package com.hfjs.amapcardemo;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

public class MapUtils {
    /***
     * 设置marker
     * @param latLng
     * @param bitmapId
     */
    public static void setPointMarker(AMap aMap,LatLng latLng, int bitmapId) {
        aMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(bitmapId)));
    }

    /**
     * 设置marker  并获取marker对象
     * @param aMap
     * @param latLng
     * @param bitmapId
     * @return
     */
    public static Marker getPointMarker(AMap aMap, LatLng latLng,int bitmapId) {
       return aMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(bitmapId)).belowMaskLayer(true));
    }
    public static void  setMap(AMap aMap,LatLng latLng){
        aMap.animateCamera(CameraUpdateFactory.changeLatLng(latLng));
    }
}
