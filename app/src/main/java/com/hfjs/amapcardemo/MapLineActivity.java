package com.hfjs.amapcardemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.utils.SpatialRelationUtil;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.jaredrummler.android.colorpicker.ColorShape;

import java.util.ArrayList;
import java.util.List;

public class MapLineActivity extends AppCompatActivity {
    private MapView mMapView;
    private TextView mapType, lineColor, lineWidth, startMove, stopMove, pauseMove, speedMove;
    private AMap aMap;
    private int colorValue = Color.RED;
    private int type = AMap.MAP_TYPE_NORMAL;
    private MoveMarkers smoothMarker;
    private Polyline mPolyline;
    private LinearLayout infoWindowLayout;
    private TextView title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_line);
        mMapView = findViewById(R.id.map_view);
        mapType = findViewById(R.id.tv_state);
        lineColor = findViewById(R.id.tv_color);
        lineWidth = findViewById(R.id.tv_width);
        startMove = findViewById(R.id.tv_start);
        pauseMove = findViewById(R.id.tv_pause);
        stopMove = findViewById(R.id.tv_stop);
        speedMove = findViewById(R.id.tv_speed);
        mMapView.onCreate(savedInstanceState);
        if (mMapView != null) aMap = mMapView.getMap();
        setLinePoints();
        initLine();
        setListener();
    }

    private void setListener() {
        mapType.setOnClickListener(v -> {
            if (type == AMap.MAP_TYPE_NORMAL) {
                type = AMap.MAP_TYPE_SATELLITE;
                mapType.setText("卫星");
            } else {
                type = AMap.MAP_TYPE_NORMAL;
                mapType.setText("标准");
            }
            aMap.setMapType(type);
        });
        lineColor.setOnClickListener(v -> {
            changeColor();

        });
        lineWidth.setOnClickListener(v -> {
            changeWith();
        });
        startMove.setOnClickListener(v -> {
            startMove();
        });
        pauseMove.setOnClickListener(v -> {
            pauseMove();
        });
        stopMove.setOnClickListener(v -> {
            stopMove();
        });
        speedMove.setOnClickListener(v -> {
            if (duration == 300) {
                duration = 20;
            } else {
                duration = 300;
            }
            smoothMarker.setDuration(duration);
        });
    }

    private int duration = 20;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        smoothMarker.stopMove();
        smoothMarker.destroy();
        aMap.clear();
        mMapView.onDestroy();
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

    private List<LatLng> subList;

    private void initLine() {
//        // 获取轨迹坐标点
        List<LatLng> points = readLatLngs();
        setPointMarker(points.get(0), R.mipmap.start);
        setPointMarker(points.get(points.size() - 1), R.mipmap.end);
        LatLngBounds.Builder bounds = LatLngBounds.builder();
        for (LatLng latLng : points) {
            bounds.include(latLng);
        }
        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 150));//150表示缩放后的显示所有点的边距
        smoothMarker = new MoveMarkers(aMap);
        // 设置滑动的图标
        smoothMarker.setDescriptor(BitmapDescriptorFactory.fromResource(R.mipmap.icon_car));


        LatLng drivePoint = points.get(0);
        Pair<Integer, LatLng> pair = SpatialRelationUtil.calShortestDistancePoint(points, drivePoint);
        points.set(pair.first, drivePoint);
        subList = points.subList(pair.first, points.size());
        // 设置轨迹点
        smoothMarker.setPoints(subList);
        // 设置平滑移动的总时间  单位  秒
        smoothMarker.setTotalDuration(40);

        // 设置  自定义的InfoWindow 适配器
        aMap.setInfoWindowAdapter(infoWindowAdapter);
        // 显示 infowindow
        smoothMarker.getMarker().showInfoWindow();
        smoothMarker.setMoveListener(v -> {
            runOnUiThread(() -> {
                if (infoWindowLayout != null && title != null) {
                    //设置移动到中心点
                    aMap.animateCamera(CameraUpdateFactory.changeLatLng(subList.get(smoothMarker.getIndex())));
                    title.setText("这是第几个点" + smoothMarker.getIndex() + "");
                }
            });
        });


    }


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

    /***
     * 设置marker
     * @param latLng
     * @param bitmapId
     */
    private void setPointMarker(LatLng latLng, int bitmapId) {
        aMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(bitmapId)));
    }

    //开始移动
    private void startMove() {
        if (!smoothMarker.getMarker().isInfoWindowShown())
            smoothMarker.getMarker().showInfoWindow();
        smoothMarker.startSmoothMove();

    }

    //暂停
    private void pauseMove() {
        smoothMarker.stopMove();
    }

    //停止移动
    private void stopMove() {
//        smoothMarker.reset();
//        smoothMarker.getMarker().setPosition(subList.get(0));
//        smoothMarker.getMarker().hideInfoWindow();
        smoothMarker.setPosition(new LatLng(35.909736, 80.947266));
//        smoothMarker.resetIndex();
//        smoothMarker.stopMove();
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

    private void setLinePoints() {
        List<LatLng> list = readLatLngs();
        mPolyline = aMap.addPolyline((new PolylineOptions())
                .addAll(list)
                .width(10)
                .setDottedLine(true)//设置虚线
                .color(R.color.colorPrimary));
        LatLngBounds bounds = new LatLngBounds(list.get(0), list.get(list.size() - 2));
        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }


    //颜色选择器
    private void changeColor() {
        ColorPickerDialog dialog = ColorPickerDialog.newBuilder()
                .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                .setDialogTitle(R.string.color_choose)
                .setColorShape(ColorShape.CIRCLE)
                .setPresets(ColorPickerDialog.MATERIAL_COLORS)
                .setAllowPresets(true)
                .setAllowCustom(true)
                .setShowAlphaSlider(false)
                .setShowColorShades(true)
                .setColor(colorValue)
                .create();
        dialog.show(getFragmentManager(), "color-picker-dialog");
        dialog.setColorPickerDialogListener(pickerDialogListener);
    }

    ColorPickerDialogListener pickerDialogListener = new ColorPickerDialogListener() {
        @Override
        public void onColorSelected(int dialogId, int color) {
            colorValue = color;
            mPolyline.setColor(color);
        }

        @Override
        public void onDialogDismissed(int dialogId) {

        }
    };

    private void changeWith() {
        mPolyline.setWidth(16);
    }

    /**
     * 读取坐标点
     *
     * @return
     */
    private List<LatLng> readLatLngs() {
        List<LatLng> points = new ArrayList<LatLng>();
        for (int i = 0; i < coords.length; i += 2) {
            points.add(new LatLng(coords[i + 1], coords[i]));
        }
        return points;
    }

    /**
     * 坐标点数组数据
     */
    private double[] coords = {116.3499049793749, 39.97617053371078,
            116.34978804908442, 39.97619854213431, 116.349674596623,
            39.97623045687959, 116.34955525200917, 39.97626931100656,
            116.34943728748914, 39.976285626595036, 116.34930864705592,
            39.97628129172198, 116.34918981582413, 39.976260803938594,
            116.34906721558868, 39.97623535890678, 116.34895185151584,
            39.976214717128855, 116.34886935936889, 39.976280148755315,
            116.34873954611332, 39.97628182112874, 116.34860763527448,
            39.97626038855863, 116.3484658907622, 39.976306080391836,
            116.34834585430347, 39.976358252119745, 116.34831166130878,
            39.97645709321835, 116.34827643560175, 39.97655231226543,
            116.34824186261169, 39.976658372925556, 116.34825080406188,
            39.9767570732376, 116.34825631960626, 39.976869087779995,
            116.34822111635201, 39.97698451764595, 116.34822901510276,
            39.977079745909876, 116.34822234337618, 39.97718701787645,
            116.34821627457707, 39.97730766147824, 116.34820593515043,
            39.977417746816776, 116.34821013897107, 39.97753930933358
            , 116.34821304891533, 39.977652209132174, 116.34820923399242,
            39.977764016531076, 116.3482045955917, 39.97786190186833,
            116.34822159449203, 39.977958856930286, 116.3482256370537,
            39.97807288885813, 116.3482098441266, 39.978170063673524,
            116.34819564465377, 39.978266951404066, 116.34820541974412,
            39.978380693859116, 116.34819672351216, 39.97848741209275,
            116.34816588867105, 39.978593409607825, 116.34818489339459,
            39.97870216883567, 116.34818473446943, 39.978797222300166,
            116.34817728972234, 39.978893492422685, 116.34816491505472,
            39.978997133775266, 116.34815408537773, 39.97911413849568,
            116.34812908154862, 39.97920553614499, 116.34809495907906,
            39.979308267469264, 116.34805113358091, 39.97939658036473,
            116.3480310509613, 39.979491697188685, 116.3480082124968,
            39.979588529006875, 116.34799530586834, 39.979685789111635,
            116.34798818413954, 39.979801430587926, 116.3479996420353,
            39.97990758587515, 116.34798697544538, 39.980000796262615,
            116.3479912988137, 39.980116318796085, 116.34799204219203,
            39.98021407403913, 116.34798535084123, 39.980325006125696,
            116.34797702460183, 39.98042511477518, 116.34796288754136,
            39.98054129336908, 116.34797509821901, 39.980656820423505,
            116.34793922017285, 39.98074576792626, 116.34792586413015,
            39.98085620772756, 116.3478962642899, 39.98098214824056,
            116.34782449883967, 39.98108306010269, 116.34774758827285,
            39.98115277119176, 116.34761476652932, 39.98115430642997,
            116.34749135408349, 39.98114590845294, 116.34734772765582,
            39.98114337322547, 116.34722082902628, 39.98115066909245,
            116.34708205250223, 39.98114532232906, 116.346963237696,
            39.98112245161927, 116.34681500222743, 39.981136637759604,
            116.34669622104072, 39.981146248090866, 116.34658043260109,
            39.98112495260716, 116.34643721418927, 39.9811107163792,
            116.34631638374302, 39.981085081075676, 116.34614782996252,
            39.98108046779486, 116.3460256053666, 39.981049089345206,
            116.34588814050122, 39.98104839362087, 116.34575119741586,
            39.9810544889668, 116.34562885420186, 39.981040940565734,
            116.34549232235582, 39.98105271658809, 116.34537348820508,
            39.981052294975264, 116.3453513775533, 39.980956549928244
    };
}
