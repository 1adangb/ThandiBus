package com.metroreal.thandibus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class LoginActivity extends AppCompatActivity
{
    private EditText edCorreo;
    private EditText edContraseña;
    private Button btAcceder;
    private Button btRegistrarse;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fDatabase;

    private String correo = "";
    private String contraseña = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        fDatabase = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        edCorreo = (EditText) findViewById(R.id.txtCorreo);
        edContraseña = (EditText) findViewById(R.id.txtContraseña);
        btAcceder = (Button) findViewById(R.id.btnAcceder);
        btRegistrarse = (Button) findViewById(R.id.btnRegistrarse);

        btAcceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                correo = edCorreo.getText().toString();
                contraseña = edContraseña.getText().toString();

                if (!correo.isEmpty() && !contraseña.isEmpty())
                {
                    loginUser();
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Llene todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        edCorreo.setText("adan.no.adan@gmail.com");
        edContraseña.setText("123456");
    }



    private void loginUser()
    {
        fAuth.signInWithEmailAndPassword(correo,contraseña).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    aActivityPersonalizado();
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "No se pudo inicar sesion, compruebe sus datos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void aActivityPersonalizado()
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
                        String tipo = "";
                        tipo = document.getString("tipo");
                        if (tipo.equals("pasajero"))
                        {
                            Intent intent = new Intent(LoginActivity.this, UserMapsActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else if (tipo.equals("conductor"))
                        {
                            Intent intent = new Intent(LoginActivity.this, RutasCActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, "Error enviando a intent personalizado", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this, "No such document", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void aRegistrarse(View v)
    {
        startActivity(new Intent(LoginActivity.this,RegistrarseActivity.class));
    }
    public void aGPS(View v)
    {
        startActivity(new Intent(LoginActivity.this,MapaTestActivity.class));
    }
}
