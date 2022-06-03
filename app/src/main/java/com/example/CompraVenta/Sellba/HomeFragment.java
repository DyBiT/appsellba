package com.example.CompraVenta.Sellba;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;


/* CLASE "PRINCIPAL"(provisional) LA CUAL NOS MUESTRA LA LISTA DE LOS ARTICULOS EN VENTA HACIENDO
* REFERENCIA A LA BASE DE DATOS REALTIME DE FIREBASE EN EL APARTADO UPLOADS
* PROGRAMAMOS UN MÉTODO CON mProgressCircle EL CUAL HEREDA DEL WIDGET.PROGRESSBAR QUE SE EJECUTARÁ
* HASTA RECIBIR LOS DATOS, CUANDO EXISTA ALGÚN PROBLEMA Y NO SE PUEDAN OBTENER LOS DATOS
* SE MANTENDRÁ FIJA LA PROGRESSBAR (BARRA DE CARGA) , EN EL LAYOUT fragment_home INSERTAREMOS UN
* RECICLEDVIEW EL CUAL SE ENCARGARÁ DE ALOJAR LA LISTA DE LOS DATOS Y EL PROGRESS BAR MIENTRAS SE CARGAR
* DICHOS DATOS*/
public class HomeFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;
    private ProgressBar mProgressCircle;
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;
    private List<Upload> mUploads;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        mRecyclerView = v.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mProgressCircle = v.findViewById(R.id.progress_circle);
        mUploads = new ArrayList<>();
        mAdapter = new ImageAdapter(getActivity(), mUploads);
        mRecyclerView.setAdapter(mAdapter);

        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUploads.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    upload.setKey(postSnapshot.getKey());
                    mUploads.add(upload);
                }
                mAdapter.notifyDataSetChanged();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });
        return v;
    }
}
