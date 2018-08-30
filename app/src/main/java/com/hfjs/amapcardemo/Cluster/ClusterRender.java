package com.hfjs.amapcardemo.Cluster;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.amap.api.maps.model.BitmapDescriptor;

/**
 * Created by yiyi.qi on 16/10/10.
 */

public interface ClusterRender {
    /**
     * 根据聚合点的元素数目返回渲染背景样式
     *
     * @param clusterNum
     * @return
     */
    Drawable getDrawAble(int clusterNum);

    BitmapDescriptor getView(String title);
}