package com.metroreal.thandibus;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;

import java.util.Map;
import java.util.Observable;

import javax.annotation.Nullable;

public class UserMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private FirebaseFirestore fDatabase;
    private FirebaseAuth fAuth;
    private GoogleMap mMap;
    private LatLng coordenadas;
    private double lat = 20.6529098;
    private  double lon = -100.4064888;
    private CameraUpdate cameraUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        coordenadas = new LatLng(20.6481054,-100.4132848);
        fDatabase = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.setMaxZoomPreference(20);
        mMap.setMinZoomPreference(12);
        mMap.setMyLocationEnabled(true);
        cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordenadas,12);
        mMap.animateCamera(cameraUpdate);
        final String[] a = {""};
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener()
        {
            @Override
            public void onCameraMove()
            {
                a[0] = String.valueOf(mMap.getCameraPosition().zoom);
               Log.w("zoom",a[0]);
            }
        });

        cambiarUbicacion();
    }
    private void cambiarUbicacion ()
    {
        MarkerOptions a = new MarkerOptions()
                .position(coordenadas);
        final Marker m = mMap.addMarker(a);
        DocumentReference docRef = fDatabase.collection("viaje").document("waRkjE2ZT4usjs2OO2tI").collection("TPWZaUXozVfVQ771a5CVf5ZPat42").document("304831595");
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null)
                {
                    GeoPoint geo = (GeoPoint) documentSnapshot.get("ubicacion");
                    coordenadas = new LatLng(geo.getLatitude(),geo.getLongitude());
                    cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordenadas,mMap.getCameraPosition().zoom);
                    m.setPosition(coordenadas);
                    mMap.animateCamera(cameraUpdate);
                }
                else
                {
                    Log.w("cambiarUbicacion","no hay datos");
                }
            }
        });
    }
    public void onClkSalir(View v)
    {
        fAuth.signOut();
        Intent intent = new Intent(UserMapsActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }
}