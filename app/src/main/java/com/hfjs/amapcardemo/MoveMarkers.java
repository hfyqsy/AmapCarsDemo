//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hfjs.amapcardemo;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.Animation;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MoveMarkers {
    private AMap mAMap;
    private long duration = 10000L;
    private long mStepDuration = 20L;
    private LinkedList<LatLng> points = new LinkedList();
    private LinkedList<Double> eachDistance = new LinkedList();
    private double totalDistance = 0.0D;
    private double remainDistance = 0.0D;
    private ExecutorService mThreadPools;
    private Object mLock = new Object();
    private Marker marker = null;
    private BitmapDescriptor descriptor;
    private int index = 0;
    private boolean useDefaultDescriptor = false;
    AtomicBoolean exitFlag = new AtomicBoolean(false);
    private MoveMarkers.MoveListener moveListener;
    private MoveMarkers.a STATUS;
    private long pauseMillis;
    private long mAnimationBeginTime;

    public MoveMarkers(AMap var1) {
        this.STATUS = MoveMarkers.a.a;
        this.mAnimationBeginTime = System.currentTimeMillis();
        this.mAMap = var1;
        this.mThreadPools = new ThreadPoolExecutor(1, 2, 5L, TimeUnit.SECONDS, new SynchronousQueue(), new MoveMarkers.b((byte)0));
    }

    public void setPoints(List<LatLng> var1) {
        Object var2 = this.mLock;
        synchronized(this.mLock) {
            try {
                if (var1 == null || var1.size() < 2) {
                    return;
                }

                this.stopMove();
                this.points.clear();
                Iterator var7 = var1.iterator();

                while(var7.hasNext()) {
                    LatLng var3;
                    if ((var3 = (LatLng)var7.next()) != null) {
                        this.points.add(var3);
                    }
                }

                this.eachDistance.clear();
                this.totalDistance = 0.0D;

                for(int var8 = 0; var8 < this.points.size() - 1; ++var8) {
                    double var10 = (double)AMapUtils.calculateLineDistance((LatLng)this.points.get(var8), (LatLng)this.points.get(var8 + 1));
                    this.eachDistance.add(var10);
                    this.totalDistance += var10;
                }

                this.remainDistance = this.totalDistance;
                LatLng var9 = (LatLng)this.points.get(0);
                if (this.marker != null) {
                    this.marker.setPosition(var9);
                    this.checkMarkerIcon();
                } else {
                    if (this.descriptor == null) {
                        this.useDefaultDescriptor = true;
                    }

                    this.marker = this.mAMap.addMarker((new MarkerOptions()).belowMaskLayer(true).position(var9).icon(this.descriptor).title("").anchor(0.5F, 0.5F));
                }

                this.reset();
            } catch (Throwable var5) {
                var1 = null;
                var5.printStackTrace();
            }

        }
    }
    public void setDuration(int speed ){
        mStepDuration=speed;
    }

    public void reset() {
        try {
            if (this.STATUS == MoveMarkers.a.c || this.STATUS == MoveMarkers.a.d) {
                this.exitFlag.set(true);
                this.mThreadPools.awaitTermination(this.mStepDuration + 20L, TimeUnit.MILLISECONDS);
                if (this.marker != null) {
                    this.marker.setAnimation((Animation)null);
                }

                this.STATUS = MoveMarkers.a.a;
            }

        } catch (InterruptedException var1) {
            var1.printStackTrace();
        }
    }

    private void checkMarkerIcon() {
        if (this.useDefaultDescriptor) {
            if (this.descriptor == null) {
                this.useDefaultDescriptor = true;
                return;
            }

            this.marker.setIcon(this.descriptor);
            this.useDefaultDescriptor = false;
        }

    }

    public void setTotalDuration(int var1) {
        this.duration = (long)(var1 * 1000);
    }

    public void startSmoothMove() {
        if (this.STATUS == MoveMarkers.a.d) {
            this.STATUS = MoveMarkers.a.c;
            long var1 = System.currentTimeMillis() - this.pauseMillis;
            this.mAnimationBeginTime += var1;
        } else {
            if (this.STATUS == MoveMarkers.a.a || this.STATUS == MoveMarkers.a.e) {
                if (this.points.size() <= 0) {
                    return;
                }

                this.index = 0;

                try {
                    this.mThreadPools.execute(new MoveMarkers.c((byte)0));
                    return;
                } catch (Throwable var3) {
                    var3.printStackTrace();
                }
            }

        }
    }

    private IPoint getCurPosition(long var1) {
        if (var1 > this.duration) {
            this.exitFlag.set(true);
            IPoint var14 = new IPoint();
            this.index = this.points.size() - 1;
            LatLng var15 = (LatLng)this.points.get(this.index);
            --this.index;
            this.index = Math.max(this.index, 0);
            this.remainDistance = 0.0D;
            MapProjection.lonlat2Geo(var15.longitude, var15.latitude, var14);
            if (this.moveListener != null) {
                this.moveListener.move(this.remainDistance);
            }

            return var14;
        } else {
            double var3 = (double)var1 * this.totalDistance / (double)this.duration;
            this.remainDistance = this.totalDistance - var3;
            int var9 = 0;
            double var5 = 1.0D;

            for(int var2 = 0; var2 < this.eachDistance.size(); ++var2) {
                double var7 = (Double)this.eachDistance.get(var2);
                if (var3 <= var7) {
                    if (var7 > 0.0D) {
                        var5 = var3 / var7;
                    }

                    var9 = var2;
                    break;
                }

                var3 -= var7;
            }

            if (var9 != this.index && this.moveListener != null) {
                this.moveListener.move(this.remainDistance);
            }

            this.index = var9;
            LatLng var11 = (LatLng)this.points.get(var9);
            LatLng var16 = (LatLng)this.points.get(var9 + 1);
            IPoint var8 = new IPoint();
            MapProjection.lonlat2Geo(var11.longitude, var11.latitude, var8);
            IPoint var10 = new IPoint();
            MapProjection.lonlat2Geo(var16.longitude, var16.latitude, var10);
            int var13 = var10.x - var8.x;
            int var4 = var10.y - var8.y;
            if (AMapUtils.calculateLineDistance(var11, var16) > 5.0F) {
                float var12 = this.getRotate(var8, var10);
                this.marker.setRotateAngle(360.0F - var12 + this.mAMap.getCameraPosition().bearing);
            }

            return new IPoint((int)((double)var8.x + (double)var13 * var5), (int)((double)var8.y + (double)var4 * var5));
        }
    }

    private float getRotate(IPoint var1, IPoint var2) {
        if (var1 != null && var2 != null) {
            double var3 = (double)var2.y;
            double var5 = (double)var1.y;
            double var7 = (double)var1.x;
            return (float)(Math.atan2((double)var2.x - var7, var5 - var3) / 3.141592653589793D * 180.0D);
        } else {
            return 0.0F;
        }
    }

    public void stopMove() {
        if (this.STATUS == MoveMarkers.a.c) {
            this.STATUS = MoveMarkers.a.d;
            this.pauseMillis = System.currentTimeMillis();
        }

    }

    public Marker getMarker() {
        return this.marker;
    }

    public LatLng getPosition() {
        return this.marker == null ? null : this.marker.getPosition();
    }

    public int getIndex() {
        return this.index;
    }

    public void resetIndex() {
        this.index = 0;
    }

    public void destroy() {
        try {
            this.reset();
            this.mThreadPools.shutdownNow();
            if (this.descriptor != null) {
                this.descriptor.recycle();
            }

            if (this.marker != null) {
                this.marker.destroy();
                this.marker = null;
            }

            Object var1 = this.mLock;
            synchronized(this.mLock) {
                this.points.clear();
                this.eachDistance.clear();
            }
        } catch (Throwable var4) {
            var4.printStackTrace();
        }
    }

    public void removeMarker() {
        if (this.marker != null) {
            this.marker.remove();
            this.marker = null;
        }

        this.points.clear();
        this.eachDistance.clear();
    }

    public void setPosition(LatLng var1) {
        if (this.marker != null) {
            this.marker.setPosition(var1);
            this.checkMarkerIcon();
        } else {
            if (this.descriptor == null) {
                this.useDefaultDescriptor = true;
            }

            this.marker = this.mAMap.addMarker((new MarkerOptions()).belowMaskLayer(true).position(var1).icon(this.descriptor).title("").anchor(0.5F, 0.5F));
        }
    }

    public void setDescriptor(BitmapDescriptor var1) {
        if (this.descriptor != null) {
            this.descriptor.recycle();
        }

        this.descriptor = var1;
        if (this.marker != null) {
            this.marker.setIcon(var1);
        }

    }

    public void setRotate(float var1) {
        if (this.marker != null && this.mAMap != null) {
            this.marker.setRotateAngle(360.0F - var1 + this.mAMap.getCameraPosition().bearing);
        }

    }

    public void setVisible(boolean var1) {
        if (this.marker != null) {
            this.marker.setVisible(var1);
        }

    }

    public void setMoveListener(MoveMarkers.MoveListener var1) {
        this.moveListener = var1;
    }

    private class c implements Runnable {
        private c(byte b) {
        }

        public final void run() {
            try {
                MoveMarkers.this.mAnimationBeginTime = System.currentTimeMillis();
                MoveMarkers.this.STATUS = MoveMarkers.a.b;
                MoveMarkers.this.exitFlag.set(false);

                for(; !MoveMarkers.this.exitFlag.get() && MoveMarkers.this.index <= MoveMarkers.this.points.size() - 1; Thread.sleep(MoveMarkers.this.mStepDuration)) {
                    synchronized(MoveMarkers.this.mLock) {
                        if (MoveMarkers.this.exitFlag.get()) {
                            return;
                        }

                        if (MoveMarkers.this.STATUS != MoveMarkers.a.d) {
                            long var2 = System.currentTimeMillis() - MoveMarkers.this.mAnimationBeginTime;
                            IPoint var6 = MoveMarkers.this.getCurPosition(var2);
                            MoveMarkers.this.marker.setGeoPoint(var6);
                            MoveMarkers.this.STATUS = MoveMarkers.a.c;
                        }
                    }
                }

                MoveMarkers.this.STATUS = MoveMarkers.a.e;
            } catch (Throwable var5) {
                var5.printStackTrace();
            }
        }
    }

    private class b implements ThreadFactory {
        private b(byte b) {
        }

        public final Thread newThread(Runnable var1) {
            return new Thread(var1, "MoveSmoothThread");
        }
    }

    public interface MoveListener {
        void move(double var1);
    }

    private static enum a {
        a,
        b,
        c,
        d,
        e;

        private a() {
        }
    }
}
