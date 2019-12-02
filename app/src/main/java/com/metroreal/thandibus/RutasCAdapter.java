package com.metroreal.thandibus;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RutasCAdapter extends ArrayAdapter<QueryDocumentSnapshot>
{
    private Context mContext;
    private List<QueryDocumentSnapshot> listaRutas = new ArrayList<>();


    public RutasCAdapter(@NonNull Context context, int resource, @NonNull List<QueryDocumentSnapshot> objects)
    {
        super(context, resource, objects);
        mContext = context;
        listaRutas = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);

        final QueryDocumentSnapshot estaRuta = listaRutas.get(position);

        TextView txRuta = (TextView) listItem.findViewById(R.id.txtNombreRuta);
        txRuta.setText(estaRuta.getString("nombreRuta"));

        txRuta.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(mContext,ConductorActivity.class);
                intent.putExtra("idRuta",estaRuta.getId());
                mContext.startActivity(intent);
            }
        });
        return listItem;
    }
}