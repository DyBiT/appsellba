package com.example.CompraVenta.Sellba;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;

import static com.example.CompraVenta.Sellba.R.mipmap.ic_loading;

public class BuyFragment extends Fragment {

    ImageView pImage;
    private TextView name;
    private TextView price;
    private TextView seller;
    private TextView sellDate;
    private TextView Desc_tag;
    private TextView Desc_text;
    private Button button_make_offer;
    private Button button_message;
    private Button button_delete;
    private String sName;
    private String sEmail;
    private String pName;
    private String bName;
    private String bEmail;
    private int position;
    private String key;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRef;
    private FirebaseStorage mStorage;
    private ValueEventListener mDBListener;
    private List<Upload> mUploads;


    //Método para saber si el anuncio pertenece al usuario comparando el email, en tal caso
    //ocultaremos el botón de conectar y mensaje y mostrareros el de eliminar para eliminar el producto.
    @Override
    public void onStart() {
        super.onStart();
        String testEmail = mAuth.getInstance().getCurrentUser().getEmail();
        if (testEmail.equals(sEmail)) {
            button_message.setVisibility(View.GONE);
            button_delete.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(), "Eres el vendedor de este producto", Toast.LENGTH_SHORT).show();
        }
    }

    //Definimos los botones, texto y variables.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_buy, container, false);
        name = (TextView) v.findViewById(R.id.product_name);
        price = (TextView) v.findViewById(R.id.product_price);
        seller = (TextView) v.findViewById(R.id.product_seller);
        sellDate = (TextView) v.findViewById(R.id.product_date);
        button_message = (Button) v.findViewById(R.id.msg_button);
        button_delete = (Button) v.findViewById(R.id.delete_button);

        pImage = (ImageView) v.findViewById(R.id.product_image);
        Desc_tag = (TextView) v.findViewById(R.id.Description_tag);
        Desc_text = (TextView) v.findViewById(R.id.Description);
        bName = mAuth.getInstance().getCurrentUser().getDisplayName();
        bEmail = mAuth.getInstance().getCurrentUser().getEmail();


        mUploads = new ArrayList<>();

        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        Bundle bundle = getArguments();
        if (bundle != null) {
            position = bundle.getInt("position");
            pName = bundle.getString("name");
            String pImageUrl = bundle.getString("imageUrl");
            String pPrice = bundle.getString("price");
            sName = bundle.getString("userName");
            key = bundle.getString("key");
            String date = bundle.getString("date");
            String desc = bundle.getString("desc");
            sEmail = bundle.getString("email");
            name.setText(pName);
            price.setText("€ " + pPrice);
            seller.setText(sName);
            sellDate.setText(date);
            if (desc != null) {
                Desc_tag.setVisibility(View.VISIBLE);
                Desc_text.setVisibility(View.VISIBLE);
                Desc_text.setText(desc);
            }
            if (pImageUrl != null) {
                String photoUrl = pImageUrl;
                Glide.with(this)
                        .load(photoUrl)
                        .into(pImage);
            }

        }

        //Cuando el usuario clickea en el botón mensaje, creamos un objeto de la clase MsgFragment();
        //al cual le pasamos los párametros con el método bundle.
        button_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MsgFragment msgFragment = new MsgFragment();
                Bundle bundle = new Bundle();
                bundle.putString("sEmail", sEmail);
                bundle.putString("pName", pName);
                bundle.putString("sName", sName);
                bundle.putString("bName", mAuth.getInstance().getCurrentUser().getDisplayName());
                bundle.putString("bEmail", mAuth.getInstance().getCurrentUser().getEmail());
                msgFragment.setArguments(bundle);
                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction().replace(R.id.frag_container, msgFragment)
                        .addToBackStack(null).commit();
            }
        });
        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mUploads.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    upload.setKey(postSnapshot.getKey());
                    mUploads.add(upload);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //Método borrar con mensaje de confirmación para la eliminación del producto el cual nos llama
        // a el método (deleteProduct).
        button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("¡Alerta!");
                builder.setMessage("Borrar es permanente. Estas seguro que quieres borrar?");

                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteProduct();
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog ad = builder.create();
                ad.show();
            }
        });

        return v;
    }

    //Eliminamos el producto de la lista y su referencia en la base de datos.
    //Obtenemos la posición del elemento asignamos una variable para obtener su KEY(predefinido de firebase)
    //Referenciamos la URL de la imagen y con el metodo mDatabaseRef.child(selectedKey).removeValue();
    //Eliminamos el producto con todos sus parámetros(Usuario,fecha,nombre,etc) de la BD.
    private void deleteProduct(){
        Upload selectedItem = mUploads.get(position);
        final String selectedKey = selectedItem.getKey();

        StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                startActivity(new Intent(getActivity(), DrawerActivity.class));
                mDatabaseRef.child(selectedKey).removeValue();
                Toast.makeText(getActivity(), "Elemento eliminado", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        });
    }
    //Método para enviar mensaje al vendedor , le pasamos los párametros requeridos por la clase SendMail.
    private void sendEmailToSeller() {
        String email = sEmail;
        String subject = "[SELLBA] Petición de producto " + pName;

        String msg = "nombre-desconocido";
        if (bName != "")
            msg = bName;
        String thankMsg = "\n\nGracias por usar SELLBA :)";
        String autoMsg = "\n\nEsto es un Email autogenerado por SELLBA. Por favor no responda este mensaje.";

        String message = "Hola " + sName + ". " + msg + " está interesado en su producto \"" + pName + "\". Espera más respuestas de " + msg + ". Si quieres puedes escribir " + msg + " en el email " + bEmail + " ." + thankMsg + autoMsg;
        SendMail sm2s = new SendMail(getActivity(), email, subject, message);
        sm2s.execute();
    }
    //Método para enviar mensaje al comprador , le pasamos los párametros requeridos por la clase SendMail.
    private void sendEmailToBuyer() {
        String email = bEmail;
        String subject = "[SELLBA] Confirmación" + pName;
        String thankMsg = "\n\n Gracias por usar SELLBA :)";
        String autoMsg = "\n\n Esto es un Email autogenerado por SELLBA. Por favor no responda este mensaje.";

        //String message = "Hola " + bName + ".\nSu petición de contacto con " + sName  +" para el producto \"" + pName + "\". Puedes enviar un mensaje a " + sName + " en la app clickeando en la opción mensaje." + thankMsg + autoMsg ;
        String message = "Hola " + bName + ".\nSu petición de contacto con " + sName  +" para el producto \"" + pName + "\". Se ha registrado correctamente, espera futuros mensajes de "+sName + thankMsg + autoMsg ;
        SendMail sm2b = new SendMail(getActivity(), email, subject, message);
        sm2b.execute();
    }


}
