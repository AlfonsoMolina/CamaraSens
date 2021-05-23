package com.dam.camarasens;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//BBDD

public class DataBase extends SQLiteOpenHelper {

    private static final int VERSION_BASEDATOS = 1;
    private static final String NOMBRE_BASEDATOS = "mibasedatos.db";
    private static final String TABLA_FOTOS ="CREATE TABLE IF NOT EXISTS fotos " +
            " (_id TEXT PRIMARY KEY, nombre TEXT, ubicacion TEXT, orientacion TEXT, fecha TEXT)";
    private Context ctx;

    public DataBase(Context context) {
        super(context, NOMBRE_BASEDATOS, null, VERSION_BASEDATOS);
        this.ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("bbdd", "CREANDO BASE DE DATOS");
        db.execSQL(TABLA_FOTOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLA_FOTOS);
        onCreate(db);
    }

    public boolean insertarFoto(Photo p){
        return insertarFoto(p.getId(),p.getNombre(),p.getUbicacion(),p.getOrientacion(),p.getFecha());
    }

    public boolean insertarFoto(String id, String nombre, String ubicacion, String orientacion, String fecha) {
        long salida=0;
        Log.d("bbdd", "INSERTANDO FOTO");

        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            ContentValues valores = new ContentValues();
            //  if(id!=0)
            valores.put("_id", id);
            valores.put("nombre", nombre);
            valores.put("ubicacion", ubicacion);
            valores.put("orientacion", orientacion);
            valores.put("fecha", fecha);
            salida=db.insert("fotos", null, valores);

            db.close();
        } else
            Log.d("bbdd", "BBDD NO ENCONTRADA");



        return(salida>0);
    }

    public boolean  borrarFoto(String id) {
        SQLiteDatabase db = getWritableDatabase();
        long salida=0;
        if (db != null) {
            salida=db.delete("fotos", "_id='" + id+"'", null);
            db.close();
        }

        return(salida>0);
    }

    public Photo recuperarFoto(String id) {
        SQLiteDatabase db = getReadableDatabase();
        String[] valores_recuperar = {"_id", "nombre", "ubicacion", "orientacion", "fecha"};
        Cursor c = db.query("fotos", valores_recuperar, "_id='" + id + "'", null, null, null, null, null);
        Photo photo;
        if(c != null) {
            c.moveToFirst();
            photo = new Photo(id, c.getString(1), c.getString(2), c.getString(3), c.getString(4));
            db.close();
            c.close();
            return photo;
        }
        db.close();

        return null;
    }


    public ArrayList<Photo> recuperarFotos() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Photo> lista_fotos = new ArrayList<Photo>();
        String[] valores_recuperar = {"_id", "nombre", "ubicacion", "orientacion", "fecha"};
        Cursor c = db.query("fotos", valores_recuperar, null, null, null, null, null, null);


        c.moveToFirst();
        do {
            if(!c.isNull(0)) {
                Photo foto = new Photo(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4));
                lista_fotos.add(foto);
            }
        } while (c.moveToNext());
        db.close();
        c.close();
        return lista_fotos;
    }

    public void eliminarDB(){
        ctx.deleteDatabase(NOMBRE_BASEDATOS);

    }
}


