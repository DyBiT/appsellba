package com.example.CompraVenta.Sellba;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;


/*
--ESTA CLASE NOS PERMITE ADAPTAR LAS IMAGENES (EN EL ESPACIO DEFINIDO)
 Y TEXTVIEWS DEFINIDAS POR LOS USUARIOS Y MOSTRARLAS/ORDENARLAS DONDE NOSOTROS INDIQUEMOS
 PUDIENDO ASI DARLES FORMA Y FORMATO.*/



/*/* https://developer.android.com/guide/topics/ui/layout/recyclerview?hl=es-419
RecyclerView facilita que se muestren de manera eficiente grandes conjuntos de datos.
la biblioteca RecyclerView creará los elementos de forma dinámica cuando se los necesite
utilizaremos datos de tipo lista para mostrar las "Upload" de los productos*/

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private Context mContext;
    private List<Upload> mUploads;

    //CONSTRUCTOR
    public ImageAdapter(Context context, List<Upload> uploads) {
        mContext = context;
        mUploads = uploads;
    }

    //
    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(v);
    }

    //onBindViewHolder en vez de tener una lista, creamos un objeto uploadCurrent el cual poserá
    //La posición en al que se encuentra, el nombre del producto actual y el precio.
    //Con la librería picaso piccaso https://square.github.io/picasso/ transformamos las imagenes
    //obtenidas desde el cloud de firebase(getImageUrl) y las ubica en "imageView",picaso nos
    //permite dar párametros para ajustar al imagen y reducir su tamaño en memoria.
    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Upload uploadCurrent = mUploads.get(position);
        holder.textViewName.setText(uploadCurrent.getName());
        holder.textViewPrice.setText("€ " + uploadCurrent.getPrice());
        Picasso.with(mContext)
                .load(uploadCurrent.getImageUrl())
                .placeholder(R.mipmap.ic_loading)
                .fit()
                .centerInside()
                .into(holder.imageView);
    }

    //Método para obtener el tamaño del conjunto de datos y determinar cuando no hay más "Uploads"
    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewName;
        public TextView textViewPrice;
        public ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            //DEFINIMOS LOS VIEWS DONDE SE MOSTRARAN LOS DATOS
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewPrice = itemView.findViewById(R.id.text_view_price);
            imageView = itemView.findViewById(R.id.image_view_upload);
            imageView.setOnClickListener(new View.OnClickListener() {
                /*Cuando hagamos click sobre una imagen de la lista, nos creará un objeto de la clase
                * BuyFragment la cual pasará los párametros de los datos correspondientes del artículo
                * seleccionado de la lista*/
                @Override
                public void onClick(View v) {
                    //Utilizaremos Bundle para almacenar los datos correspondientes y enviarlos a el
                    //fragment correspondiente "BuyFragment", le pasamos la posicion, nombre, precio
                    //la imagen a traves de cloud de firebase (getImageUrl).
                    BuyFragment buyFragment = new BuyFragment();
                    Bundle bundle = new Bundle();
                    int position = getAdapterPosition();
                    Upload current = mUploads.get(position);
                    String name = current.getName();
                    bundle.putInt("position", position);
                    bundle.putString("name", name);
                    bundle.putString("price", current.getPrice());

                    if (imageView != null)
                        bundle.putString("imageUrl", current.getImageUrl());
                    else
                        bundle.putString("imageUrl", null);
                    bundle.putString("userName", current.getUserName());
                    bundle.putString("date", current.getDate());
                    bundle.putString("desc", current.getDesc());
                    bundle.putString("email", current.getEmail());
                    bundle.putString("key", current.getKey());
                    buyFragment.setArguments(bundle);

                    //ENVIAMOS LOS DATOS Y LOS REMPLAZAMOS EN LA POSICION QUE OCUPA FRAG_CONTAINER
                    //EN EL OBJETO buyFragment que hace referencia a la clase BuyFragment.
                    ((FragmentActivity) mContext)
                            .getSupportFragmentManager()
                            .beginTransaction().replace(R.id.frag_container, buyFragment)
                            .addToBackStack(null).commit();


                }
            });
        }


    }
}
