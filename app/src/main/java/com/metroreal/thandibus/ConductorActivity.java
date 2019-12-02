package com.metroreal.thandibus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class ConductorActivity extends AppCompatActivity
{

    private Button btLogout;
    private Button btStatusViaje;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fDatabase;
    private TextView txInfo;
    private TextView txRuta;
    LocationManager locationManager;
    double latitud;
    double longitud;
    boolean viajando = false;
    String idUsuario;
    String idRuta;
    String idViaje = "304831595";
    TextView txLatitud;
    TextView txLongitud;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conductor);

        fAuth = FirebaseAuth.getInstance();
        fDatabase = FirebaseFirestore.getInstance();
        btStatusViaje = (Button) findViewById(R.id.btnStatus);
        btLogout = (Button) findViewById(R.id.btnLogout);
        txInfo = (TextView) findViewById(R.id.txtInfo);
        txRuta = (TextView) findViewById(R.id.txtRuta);
        Intent esteIntent = getIntent();
        idRuta = esteIntent.getStringExtra("idRuta");
        txRuta.setText(idRuta);
        txRuta.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btLogout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Map<String, Object> map = new HashMap<>();
                map.put("latitud", "0");
                map.put("longitud","0");
                fDatabase.collection("usuarios")
                        .document(fAuth.getCurrentUser().getUid())
                        .update(map)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task2) {
                        if (task2.isSuccessful())
                        {
                            fAuth.signOut();
                            Intent intent = new Intent(ConductorActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(ConductorActivity.this, "Error al actualizar coordenadas a 0 y no hizo logout", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        txLatitud = (TextView) findViewById(R.id.txtLatitud);
        txLongitud = (TextView) findViewById(R.id.txtLongitud);
        idUsuario = fAuth.getCurrentUser().getUid();
        mostrarInfo();

    }

    private void mostrarAlert()
    {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Activar Ubicacion")
                .setMessage("Su ubicación esta desactivada.\npor favor active su ubicación " +
                        "usa esta app")
                .setPositiveButton("Configuración de ubicación", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }
    private boolean checkUbicacion()
    {
        if (!isUbicacionActivada())
            mostrarAlert();
        return isUbicacionActivada();
    }
    private boolean isUbicacionActivada()
    {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private final LocationListener location = new LocationListener()
    {
        public void onLocationChanged(Location location) {
            latitud = location.getLatitude();
            longitud = location.getLongitude();
            runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    txLatitud.setText(latitud + "");
                    txLongitud.setText(longitud + "");
                    Toast.makeText(ConductorActivity.this, "GPS Actualizado", Toast.LENGTH_SHORT).show();
                    Log.w("GPS conductor", "GPS actualizado" );
                    enviarGPS();
                }
            });
        }
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }
        @Override
        public void onProviderDisabled(String s) {
        }
    };
    private void mostrarInfo()
    {
        String idUsuario = fAuth.getCurrentUser().getUid();
        fDatabase.collection("usuarios").document(idUsuario).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists())
                    {
                        txInfo.setText(document.getString("nombre") + ", " + document.getString("tipo"));
                    }
                    else
                    {
                        Toast.makeText(ConductorActivity.this, "No such document", Toast.LENGTH_SHORT).show();

                    }
                }
                else
                {
                    Toast.makeText(ConductorActivity.this, "failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onClkViaje(View v)
    {
        if (!viajando)
        {
            if (!checkUbicacion())
            {
                return;
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "No hay permisos", Toast.LENGTH_SHORT).show();
            }
            else
            {
                iniciarViaje();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1 * (1 * 1000), 1, location);
                Toast.makeText(this, "Ubicacion GPS Iniciado", Toast.LENGTH_LONG).show();
                btStatusViaje.setText("Terminar");
            }
        }
        else
        {
            preTerminarViaje();
            locationManager.removeUpdates(location);
            btStatusViaje.setText("Iniciar");
        }
    }

    private void preTerminarViaje()
    {

        CollectionReference colRef = fDatabase.collection("viaje").document(idRuta).collection(idUsuario);
        colRef.whereEqualTo("viajando",true).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful())
                {
                    Log.w("preTerminarViaje","taskSuccesfull");
                    for (QueryDocumentSnapshot document : task.getResult())
                    {
                        idViaje = document.getId();
                        Log.w("DocRef",idViaje);
                    }
                    viajando = false;
                    terminarViaje(idViaje);

                }
                else
                {
                    Log.w("Buscando ruta activa", "no encontro");
                }
            }
        });
    }
     public void obtenerViajeActivo()
     {
         CollectionReference colRef = fDatabase.collection("viaje").document(idRuta).collection(idUsuario);
         colRef.whereEqualTo("viajando",true).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
             @Override
             public void onComplete(@NonNull Task<QuerySnapshot> task) {
                 if (task.isSuccessful())
                 {
                     if (!task.getResult().isEmpty())
                     {
                         for (QueryDocumentSnapshot document : task.getResult())
                         {
                             idViaje = document.getId();
                         }
                     }
                     else
                     {
                         Log.i("obtenerViajeActivo","No hay viaje Activo");
                     }
                 }
                 else
                 {
                     Toast.makeText(ConductorActivity.this, "Error al conectar al servidor", Toast.LENGTH_SHORT).show();
                 }
             }
         });
     }
    public void terminarViaje(String idViaje)
    {
        DocumentReference docRef = fDatabase.collection("viaje").document(idRuta).collection(idUsuario).document(idViaje);
        Map<String, Object> map = new HashMap<>();
        map.put("viajando", viajando);
        map.put("finViaje", FieldValue.serverTimestamp());
        docRef.update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(ConductorActivity.this, "Viaje Terminado", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(ConductorActivity.this, "Error al terminar viaje", Toast.LENGTH_SHORT).show();
                    viajando = true;
                }
            }
        });
    }

    public void iniciarViaje()
    {
        viajando = true;
        Map<String, Object> map = new HashMap<>();
        map.put("usuario",idUsuario);
        map.put("ubicacion",new GeoPoint(0,0));
        map.put("viajando",viajando);
        map.put("inicioViaje", FieldValue.serverTimestamp());
        fDatabase.collection("viaje").document(idRuta).collection(idUsuario).document("304831595").set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(ConductorActivity.this, "viaje iniciado", Toast.LENGTH_SHORT).show();
                    //obtenerViajeActivo();
                }
                else    
                {
                    Toast.makeText(ConductorActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void enviarGPS()
    {

        Map<String, Object> map = new HashMap<>();
        map.put("ubicacion", new GeoPoint(latitud,longitud));
        Log.w("DocRef",idRuta + " " + idUsuario + " " + idViaje);
        DocumentReference docRef = fDatabase.collection("viaje").document(idRuta).collection(idUsuario).document(idViaje);
        docRef.update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task2) {
                if (task2.isSuccessful())
                {
                    Toast.makeText(ConductorActivity.this, "Datos enviados a Firestore", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(ConductorActivity.this, "No se actualizaron los datos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
