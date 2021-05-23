package com.dam.camarasens;

        import android.app.Service;
        import android.content.Context;
        import android.content.Intent;
        import android.location.Location;
        import android.os.Bundle;
        import android.os.IBinder;
        import android.util.Log;

        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
        import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
        import com.google.android.gms.location.LocationRequest;
        import com.google.android.gms.location.LocationServices;
        import com.google.android.gms.location.LocationListener;

/**
 * Created by vicente on 16/5/15.
 */

public class LocationService extends Service implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener {

    private static final String TAG = "LocationService";

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 2000; // Deseado
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2; // Mínimo

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private Boolean mRequestingLocationUpdates;
    private Boolean mMustRequestLocationUpdates;
    private Context mContext;

    public LocationService(){

    }

    public LocationService(Context context){
        Log.d(TAG, "Creando servicio");

        mContext = context;

        mRequestingLocationUpdates = false;
        mMustRequestLocationUpdates = false;

        // Construimos el cliente de la api de google
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Construimos el tipo de petición que vamos a hacerle
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Conectamos el cliente
        mGoogleApiClient.connect();
    }


    public void startTrack() {
        mMustRequestLocationUpdates = true;
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }else {
            if (!mRequestingLocationUpdates) {
                mRequestingLocationUpdates = true;
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                Log.d(TAG, "Inicio de rastreo GPS");
            }
        }
    }

    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    public void stopTrack() {

        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d(TAG, "Fin de rastreo GPS");
        }

        if (mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }

        mMustRequestLocationUpdates = false;

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "Conectado a GoogleApiClient");

        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if (mMustRequestLocationUpdates) {
            startTrack();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Conexión suspendida");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "Conexión fallida: Error = " + result.getErrorCode());
        try{
            Thread.sleep(5000);
            mGoogleApiClient.connect();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Parando servicio");
        super.onDestroy();
        stopTrack();
    }

}