package com.metroreal.thandibus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class RutasCActivity extends AppCompatActivity {

    ListView lsRutas;
    FirebaseFirestore fDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rutas_c);

        lsRutas = (ListView) findViewById(R.id.lstRutas);
        fDatabase = FirebaseFirestore.getInstance();
        llenarListaRutas();
    }

    private void llenarListaRutas()
    {
        fDatabase.collection("ruta").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful())
                {
                    ArrayList<QueryDocumentSnapshot> listaRutas = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult())
                    {

                        listaRutas.add(document);
                    }
                    if (listaRutas != null)
                    {
                        RutasCAdapter adapter = new RutasCAdapter(RutasCActivity.this,0,listaRutas);
                        lsRutas.setAdapter(adapter);
                    }
                    else 
                    {
                        Toast.makeText(RutasCActivity.this, "No hay rutas disponibles", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(RutasCActivity.this, "Error al obtener rutas", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}