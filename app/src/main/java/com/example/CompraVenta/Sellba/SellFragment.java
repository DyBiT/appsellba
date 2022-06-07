package com.example.CompraVenta.Sellba;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;


//CREAMOS EL FRAGMENT DE VENTA DE PRODUCTOS
public class SellFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_CAMERA_REQUEST = 0;

    private Button mButtonChooseImage;
    private Button mButtonUpload;
    private EditText mEditTextFileName;
    private EditText mEditTextFilePrice;
    private ImageView mImageView;
    private TextView mDescription;
    private ProgressBar mProgressBar;
    private Uri mImageUri;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;

    FirebaseAuth mAuth;
    Uri uri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sell, container, false);
        mButtonChooseImage = v.findViewById(R.id.button_choose_image);
        mButtonUpload = v.findViewById(R.id.button_upload);
        mEditTextFileName = v.findViewById(R.id.edit_text_file_name);
        mEditTextFilePrice = v.findViewById(R.id.edit_text_file_price);
        mImageView = v.findViewById(R.id.image_view);
        mProgressBar = v.findViewById(R.id.progress_bar);
        mDescription = v.findViewById(R.id.Description);
        mDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    mDescription.setHint("");
                else
                    mDescription.setHint("Por favor indica al usuario algunas especificaciones,para facilitar la venta, como:\n" +
                            "Marca, modelo, potencia, uso, etc.");
            }
        });
        //Establecemos en el hosting donde se van a guardar en firebase las imagenes.
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        //Establecemos en la base de datos REALTIME donde se van a guardar los datos de la subida.
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        //Cuando pulsemos el botón de seleccionar imagen, ejecutará el metodo para selccionar la imagen
        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
        //METODO PARA SUBIR EL PRODUCTO
        mButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(getActivity(), "Carga en progreso", Toast.LENGTH_SHORT).show();
                } else {
                    uploadFile();
                }
            }
        });
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
    //METODO PARA ABRIR LA GALERIA DEL USUARIO Y PUEDA SELECCIONAR LA IMAGEN DESEADA.
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    //METODO PARA OBTENER EL RESULTADO DE LA ACTIVIDADA, SI LOS PARAMETROS SON CORRECTOS
    //EL METODO OBTIENES LOS DATOS DE LA IMAGEN (mImageUri) y la carga con la biblioteca
    //Picasso en mImageView.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            //cropImage();
            Picasso.with(getActivity()).load(mImageUri).into(mImageView);
        }
    }

    /*Testing para subir una imagen desde la camara
    private void cropImage() {
        try {
            Intent cropIntent;
            cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(mImageUri, "image/*");
            cropIntent.putExtra("crop", " true");
            cropIntent.putExtra("outputx", 180);
            cropIntent.putExtra("outputY", 180);
            cropIntent.putExtra("aspectx", 3);
            cropIntent.putExtra("aspecty", 4);
            cropIntent.putExtra("scaleUpIfNeeded", true);
            cropIntent.putExtra("return-data ", true);
            startActivityForResult(cropIntent, 1);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

     */

    //METODO PARA OBTENER LA EXTENSION DE LA IMAGEN Y POSTERIOR SELECCIONAR SOLO IMG'S
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    //METODO para que el usuario establezca su nickName en caso de que este vacio cuando
    // subamos un artículo a sellba, nos dirigará a ProfileFragment.
    private void uploadFile() {
        if (mAuth.getInstance().getCurrentUser().getDisplayName() == null) {
            ProfileFragment profileFragment = new ProfileFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("seller", 1);
            profileFragment.setArguments(bundle);
            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction().replace(R.id.frag_container, profileFragment)
                    .commit();
            return;
        }
        //VERIFICACIÓN Y MARCAJE DEL NOMBRE DEL PRODUCTO
        if (mEditTextFileName.getText().toString().trim().isEmpty()) {
            mEditTextFileName.setError("Nombre obligatorio");
            mEditTextFileName.requestFocus();
            return;
        }
        //VERIFICACIÓN Y MARCAJE DEL PRECIO DEL PRODUCTO
        if (mEditTextFilePrice.getText().toString().trim().isEmpty()) {
            mEditTextFilePrice.setError("Precio obligatorio");
            mEditTextFilePrice.requestFocus();
            return;
        }
        //VERIFICACIÓN Y MARCAJE DE LA DESCRIPCION DEL PRODUCTO
        if (mDescription.getText().toString().trim().isEmpty()) {
            mDescription.setError("Descripción obligatoria");
            mDescription.requestFocus();
            return;
        }
        //VERIFICACIÓN Y MARCAJE DE LA IMAGEN DEL PRODUCTO
        //URI donde hemos guardado la imagen de la galería y la subiremos a firebase.
        if (mImageUri != null) {
            //Para subir un archivo local a Cloud Storage, creamos una referencia a mImageUri y con el metodo
            // .putFile() lo subimos al Cloud Storage.
            final StorageReference fileReference
                    = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));
            mUploadTask = fileReference.putFile(mImageUri)
                    //Cuando subimos la imagen, mediante UploadTask administramos y supervisamos el estado de la carga.
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        //UploadTask onSucces:
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //MIENTRAS SE CARGA LA IMAGEN A TRAVÉS DE HANDLER mostramos una barra de carga
                            //mPorgressBar para evitar errores y fallos visuales.
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setProgress(0);
                                }
                            }, 500);
                            //una vez seleccionamos la imagen y limpiamos el buffer de las variables para evitar problemas
                            mImageUri = null;
                            mImageView.setImageBitmap(null);
                            Toast.makeText(getActivity(), "Subida exitosa", Toast.LENGTH_LONG).show();
                            //Cuando hemos realizado la subida a traves de los metodos de FIREBASE procedemos
                            // a guardarlos en la base de datos
                            taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Upload upload = new Upload(mEditTextFileName.getText().toString().trim(),
                                            uri.toString(), mEditTextFilePrice.getText().toString().trim(), mDescription.getText().toString().trim());
                                    String uploadId = mDatabaseRef.push().getKey();
                                    mDatabaseRef.child(uploadId).setValue(upload);
                                    mEditTextFileName.setText("");
                                    mEditTextFilePrice.setText("");
                                    mDescription.setText("");
                                }
                            })
                                    //Mostramos la expceción en caso de que la subida falle.
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    //Cuando pulsamos el botón subir obtenemos los bytes que ocupa la imagen
                    //y los vamos cargando en la barra de progreso(mProgressBar) por si ocurriese algún problema
                    // el usuario pueda ver el progreso de la subida.
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mProgressBar.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(getActivity(), "\n" +
                    "Ningún archivo seleccionado", Toast.LENGTH_SHORT).show();
        }
    }
}
