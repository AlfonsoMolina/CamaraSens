package com.dam.camarasens;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by vicente on 16/5/15.
 */
public class Map extends SupportMapFragment implements OnMapReadyCallback {

    private static GoogleMap googleMap;
    private LatLng latLng;
    private float compass;

    public Map(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        getMapAsync(this);

        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Fix for black background on devices < 4.1
        if (android.os.Build.VERSION.SDK_INT <
                android.os.Build.VERSION_CODES.JELLY_BEAN) {
            setMapTransparent((ViewGroup) view);
        }
        return view;
    }

    private void setMapTransparent(ViewGroup group) {
        int childCount = group.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = group.getChildAt(i);
            if (child instanceof ViewGroup) {
                setMapTransparent((ViewGroup) child);
            } else if (child instanceof SurfaceView) {
                child.setBackgroundColor(0x00000000);
            }
        }
    }

    public void updateMap(double lat, double lon, double deg){

        this.latLng = new LatLng(lat, lon);
        this.compass = (float) (deg - 90.0);

        if (this.compass > 360) {
            this.compass -= 180;
        }else if (this.compass <= -360) {
            this.compass += 180;
        }
        googleMap.clear();

        MarkerOptions markerOptions = new MarkerOptions().position(latLng).rotation(compass + 180).icon(BitmapDescriptorFactory.fromResource(R.drawable.orienta2));
        googleMap.addMarker(markerOptions);
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(latLng);
        LatLngBounds bounds = boundsBuilder.build();

        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 1));

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

}
