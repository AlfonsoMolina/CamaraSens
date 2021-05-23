package com.dam.camarasens;

import android.graphics.Bitmap;

/**
 * Created by Alfonso on 16/05/2015.
 */
public class Photo {

    private String id;

    private String nombre;
    private String ubicacion;
    private String orientacion;
    private String fecha;
    private Bitmap bmp;

    public Photo(String id, String nombre, String ubicacion, String orientacion, String fecha) {
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.orientacion = orientacion;
        this.fecha = fecha;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getOrientacion() {
        return orientacion;
    }

    public void setOrientacion(String orientacion) {
        this.orientacion = orientacion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}