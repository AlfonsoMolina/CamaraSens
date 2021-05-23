package com.dam.camarasens;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    LocationService locationService;
    SensorService sensorService;
    private static final int TAKE_PHOTO_CODE = 0;
    static String dir;
    String timeStamp;

    DataBase mDB;
    ArrayAdapter<String> adapter;
    ArrayList<String> lista_fotos;
    ArrayList<String> lista_id;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    Map mapFragment;

    static Photo photo;

    static TextView nombre;
    static TextView fecha;
    static TextView ubicacion;
    static TextView orientacion;

    static ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_view);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        //De MainActivity
        locationService = new LocationService(this);
        sensorService = new SensorService((SensorManager) getSystemService(Context.SENSOR_SERVICE));
        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/CamaraSens/";
        File newdir = new File(dir);
        newdir.mkdirs();

        //Del mio
        mDB = new DataBase(this);

        //LISTA
        lista_fotos = new ArrayList<String>();
        lista_id = new ArrayList<String>();

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lista_fotos);

        mapFragment = new Map();

        updateTabs();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mapFragment.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapFragment.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorService.onDestroy();
        locationService.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detailed_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.vaciar) {
            ArrayList<Photo> fDB = mDB.recuperarFotos();
            int j = 0;
            while(j < fDB.size()){
                mDB.borrarFoto(fDB.get(j++).getId());
            }
            return true;
        } else if (id == R.id.mostrar_lista) {
            ArrayList<Photo> fDB = mDB.recuperarFotos();
            lista_fotos.clear();
            lista_id.clear();
            int j = 0;
            while (j < fDB.size()) {
                lista_fotos.add(fDB.get(j).getNombre());
                lista_id.add(fDB.get(j).getId());
                j++;
            }
            new Aviso().show(getFragmentManager(), "listar");
            return true;

        } else if (id == R.id.camara) {

            takePhoto();


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
        switch (tab.getPosition()){
            case 1:
                updateTab1();
                break;
            case 2:
                updateTab2();
                break;
            case 3:
                updateTab3();
                break;
        }
        updateTabs();
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TAKE_PHOTO_CODE) {

            locationService.stopTrack();
            sensorService.stopListening();

            if (resultCode != 0) {
                Location locationFound = locationService.getCurrentLocation();
                Float orientationValue = sensorService.getLastOrientation();

                try {

                    try{
                        ExifInterface ei = new ExifInterface(dir + timeStamp + ".jpg");

                        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                        switch(orientation) {
                            case ExifInterface.ORIENTATION_NORMAL:
                                orientationValue -= 45;
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_90:

                                break;
                            case ExifInterface.ORIENTATION_ROTATE_180:
                                orientationValue -= 225;
                                break;
                        }

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    photo = new Photo(timeStamp, timeStamp + ".jpg", String.valueOf(locationFound.getLatitude()) + "," + String.valueOf(locationFound.getLongitude()), orientationValue.toString(), new Date().toString());
                    mDB.insertarFoto(photo);

                    Log.d("MainActivity", "Foto realizada bajo nombre: " + timeStamp + ".jpg");
                    Log.d("MainActivity", "Localización: " + photo.getUbicacion());
                    Log.d("MainActivity", "Orientación: " + photo.getOrientacion());

                } catch (Exception e) {
                    Log.e("MainActivity", "No se ha podido tomar la foto");

                    e.getMessage();
                }

                updateTabs();
            }
        }
    }

    public void updateTab1(){
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            Bitmap b = BitmapFactory.decodeFile(dir + photo.getNombre(), options);
            ExifInterface ei = new ExifInterface(dir + photo.getNombre());
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    b = RotateBitmap(b, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    b = RotateBitmap(b, 180);
                    break;
            }
            imageView.setImageBitmap(b);
        } catch (Exception e) {
            //
        }
    }

    public void updateTab2(){
        try {
            mapFragment.updateMap(Double.valueOf(photo.getUbicacion().split(",")[0]), Double.valueOf(photo.getUbicacion().split(",")[1]), Double.valueOf(photo.getOrientacion()));
        }catch (Exception e){
            //
        }
    }

    public void updateTab3(){
        try {
            nombre.setText(photo.getNombre());
            fecha.setText("Fecha: " + photo.getFecha());
            ubicacion.setText("Ubicación: " + photo.getUbicacion());
            orientacion.setText("Orientación: " + photo.getOrientacion());

        }catch (Exception e){
            //
        }
    }

    public void updateTabs(){
        updateTab1();
        updateTab2();
        updateTab3();
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void takePhoto(){
        // here,counter will be incremented each time,and the picture taken by camera will be stored as 1.jpg,2.jpg and likewise.
        locationService.startTrack();
        sensorService.startListening();

        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String file = dir + timeStamp + ".jpg";
        File newfile = new File(file);
        try {
            newfile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri outputFileUri = Uri.fromFile(newfile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    public class Aviso extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("Elige foto para mostrar").setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    photo = mDB.recuperarFoto(lista_id.get(which));
                    updateTabs();
                }
            });
            return builder.create();
        }
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public static PlaceholderFragment newInstance(int sectionNumber) {

            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);

            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView;

            switch (this.getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_view_photo, container, false);
                    try {
                        preparaPestana1(rootView);
                    }catch (NullPointerException e){

                    }
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_view_map, container, false);
                    break;
                case 3:
                    rootView = inflater.inflate(R.layout.fragment_view_details, container, false);
                    try {
                        preparaPestana3(rootView);
                    }catch (NullPointerException e){

                    }
                    break;
                default:
                    rootView = inflater.inflate(R.layout.activity_detailed_view, container, false);
                    break;
            }

            return rootView;
        }

        private void preparaPestana1(View rootView){
            imageView = (ImageView) rootView.findViewById(R.id.photo);
        }



        private void preparaPestana3(View rootView){
            nombre = (TextView) rootView.findViewById(R.id.nombre);
            fecha = (TextView) rootView.findViewById(R.id.fecha);
            ubicacion = (TextView) rootView.findViewById(R.id.ubicacion);
            orientacion = (TextView) rootView.findViewById(R.id.orientacion);
        }

    }

    public void cambiarNombre (View view){
        photo.setNombre(nombre.getText().toString());
        mDB.borrarFoto(photo.getId());
        mDB.insertarFoto(photo);
    }

    public void eliminarFoto (View view) {
        mDB.borrarFoto(photo.getId());
        photo = mDB.recuperarFotos().get(0);

        updateTabs();
    }

}
