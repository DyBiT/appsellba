package com.example.CompraVenta.Sellba;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.IOException;

import static android.support.v7.app.AppCompatActivity.RESULT_OK;

/*FRAGMENT PERFIL DE USUARIO ,POSIBLES IMPLEMENTACIONES:
ELIMINAR LA FOTO DE PERFIL, DEFINIR SOLO 1 VEZ EL NOMBRE DE USU, AÑADIR EL RESTABLECIMIENTO DE CONTRASEÑA*/
public class ProfileFragment extends Fragment {

    private static final int CHOOSE_IMAGE = 101;
    TextView textView, textViewEmail;
    ImageView imageView;
    EditText editText;
    Uri uriProfileImage;
    String profileimageUrl;
    FirebaseAuth mAuth;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_profile, container, false);

        //OBTENEMOS LA INSTANCIA ACTUAL DEL USUARIO ACTIVO.
        mAuth = FirebaseAuth.getInstance();
        editText = (EditText) v.findViewById(R.id.editTextDisplayName);
        imageView = (ImageView) v.findViewById(R.id.imageView);

        textView = v.findViewById(R.id.textViewVerified);
        textViewEmail = v.findViewById(R.id.text_view_email);
        textViewEmail.setText(mAuth.getCurrentUser().getEmail());

        //CUANDO PINCHAMOS EN EL ICONO DE CAMARA ABRIMOS LA GALERIA PARA SELECCIONAR FOTO DE PERFIL
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageChooser();
            }
        });
        loadUserInformation();
        //CUANDO PULSAMOS EL BOTON DE GUARDAR LLAMAMOS AL MÉTODO QUE ACTUALIZA LA INFORMACION DE USUARIO
        v.findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });
        return v;
    }


    //CUANDO EL VENDEDOR INTENTA SUBIR UN PRODUCTO POR PRIMERA VEZ PRIMERO TENDRÁ QUE ACTUALIZAR SU PERFIL
    //PARA DEFINIR SU NICKNAME Y COMPROBAR QUE SU CORREO ESTÉ VALIDADO
    @Override
    public void onStart() {
        super.onStart();
        Bundle bundle = getArguments();
        if (bundle != null && bundle.getInt("Vendedor") == 1) {
            Toast.makeText(getActivity(), "Completa tu perfil primero", Toast.LENGTH_SHORT).show();
            return;
        }

    }
    //* variable User obtenemos todos los datos del usuario, si el nickName,email,foto perfil están definidos
    // lo muestra cada uno en su posición correspondiente.
    private void loadUserInformation() {
        final FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {

            if (user.getPhotoUrl() != null) {
                String photoUrl = user.getPhotoUrl().toString();
                Glide.with(this)
                        .load(photoUrl)
                        .into(imageView);
            }

            if (user.getDisplayName() != null) {
                String displayName = user.getDisplayName();
                editText.setText(displayName);
            }

            if (user.isEmailVerified()) {
                textView.setText("Verificado");


                //POR SI HUBIESE ALGUN PROBLEMA CON EL EMAIL Y NO ESTUVIESE VERIFICADO SI EL USUARIO
                //CLICKEA EN LA OPCIÓN SE ENVIARÁ LA VERIFICACIÓN A SU EMAIL.
            } else {

                textView.setText("Email no verificado(pulsa aquí para verificar)");

                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        textView.setTextColor(1);
                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getActivity(), "Verificación email enviado", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
        }
    }

    //OBLIGAMOS AL USUARIO A QUE INSERTE UN NOMBRE DE USUARIO
    private void saveUserInformation() {
        String displayName = editText.getText().toString();
        if (displayName.isEmpty()) {
            editText.setError("Nombre obligatorio");
            editText.requestFocus();
            return;
        }
        //OBLIGAMOS AL USUARIO A QUE INSERTE UNA IMAGEN DE PERFIL
        FirebaseUser user = mAuth.getCurrentUser();
        if (profileimageUrl == null && imageView.getDrawable() == null) {
            Toast.makeText(getActivity(), "Imagen no seleccionada. Click en la cámara para seleccionar la imagen de perfil", Toast.LENGTH_SHORT).show();
            return;
        }
        //CUANDO ESTAN LOS PARAMETROS DEFINIDOS MOSTRAMOS LA INFORMACIÓN AL USUARIO.
        if (user != null && profileimageUrl != null) {
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setPhotoUri(Uri.parse(profileimageUrl))
                    .build();
            //METODO PARA ACTUALIZAR SU PERFIL
            user.updateProfile(profile)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Perfil actualizado con éxito", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    });

        } else {
            Toast.makeText(getActivity(), "Ocurrió algún error", Toast.LENGTH_LONG).show();
            return;
        }

    }

    //METODO PARA LA SUBIDA, COMPRESION Y REGISTRAR EN EL CLOUD Y LA URL EN LA BASE DE DATOS.
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriProfileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uriProfileImage);
                imageView.setImageBitmap(bitmap);
                uploadImageToFirebaseStorage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //METODO PARA SUBIR LA IMAGEN EL CLOUD DE FIREBASE
    private void uploadImageToFirebaseStorage() {
        //REFERENCIAMOS DESDE LOS METODOS DE FIREBASE
        final StorageReference ProfileImageRef;
        ProfileImageRef = FirebaseStorage.getInstance().getReference(mAuth.getCurrentUser().getEmail() + ".jpg");
        //Para subir un archivo local a Cloud Storage, creamos una referencia a uriProfileImage
        // y con el metodo  .putFile() lo subimos al Cloud Storage.
        if (uriProfileImage != null) {
            ProfileImageRef.putFile(uriProfileImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //OPTENEMOS LA URL REFERENTE EN EL CLOUD PARA INCLUIRLA EN LA BASE DE DATOS
                            ProfileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    profileimageUrl = uri.toString();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    //MOSTRAMOS LA GALERIA LOCAL DEL USUARIO PARA QUE ELIGA UNA FOTO CON LA EXTENSION
    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen de perfil"), CHOOSE_IMAGE);
    }
}