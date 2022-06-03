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

    @Override
    public void onStart() {
        super.onStart();
        String testEmail = mAuth.getInstance().getCurrentUser().getEmail();
        if (testEmail.equals(sEmail)) {
            button_make_offer.setVisibility(View.GONE);
            button_message.setVisibility(View.GONE);
            button_delete.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(), "Eres el vendedor de este producto", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_buy, container, false);
        name = (TextView) v.findViewById(R.id.product_name);
        price = (TextView) v.findViewById(R.id.product_price);
        seller = (TextView) v.findViewById(R.id.product_seller);
        sellDate = (TextView) v.findViewById(R.id.product_date);
        button_make_offer = (Button) v.findViewById(R.id.offer_button);
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

        button_make_offer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Estas seguro de esto?");
                builder.setMessage("Se enviará una notificación por email con tu email a el vendedor.");

                builder.setPositiveButton("confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendEmailToSeller();
                        sendEmailToBuyer();
                    }
                });

                builder.setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });



                AlertDialog ad = builder.create();
                ad.show();



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

    private void sendEmailToBuyer() {
        String email = bEmail;
        String subject = "[SELLBA] Solicitud exitosa para " + pName;
        String thankMsg = "\n\n Gracias por usar SELLBA :)";
        String autoMsg = "\n\n Esto es un Email autogenerado por SELLBA. Por favor no responda este mensaje.";

        String message = "Hola " + bName + ". tienes una petición " + sName  +" de \"" + pName + "\". Puedes enviar un mensaje a " + sName + " en la app clickeando en la opción mensaje." + thankMsg + autoMsg ;
        SendMail sm2b = new SendMail(getActivity(), email, subject, message);
        sm2b.execute();
    }


}
