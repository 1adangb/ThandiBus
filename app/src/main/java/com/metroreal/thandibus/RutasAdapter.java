package com.metroreal.thandibus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RutasAdapter extends ArrayAdapter<QueryDocumentSnapshot>
{
    private Context mContext;
    private List<QueryDocumentSnapshot> listaRutas = new ArrayList<>();


    public RutasAdapter(@NonNull Context context, int resource, @NonNull List<QueryDocumentSnapshot> objects) {
        super(context, resource, objects);
        mContext = context;
        listaRutas = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);

        QueryDocumentSnapshot estaRuta = listaRutas.get(position);

        TextView name = (TextView) listItem.findViewById(R.id.txtNombreRuta);
        name.setText(estaRuta.getString("nombreRuta"));

        return listItem;
    }
}
