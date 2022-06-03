package com.example.CompraVenta.Sellba;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

/*FRAGMENT SOBRE COMENTARIOS(FEEDBACK)*/

public class FeedbackFragment extends Fragment implements View.OnClickListener {
    private EditText editTextMessage;
    private Button buttonSend;
    private String Name;
    private String Email;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feedback, container, false);
        editTextMessage = (EditText) v.findViewById(R.id.editTextMessage);
        buttonSend = (Button) v.findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(this);
        Name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        Email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        return v;
    }
    //MÉTODO PARA ENVIAR EL EMAIL, CREAMOS UN OBJETO DE LA CLASE SENDMAIL YA DEFINIDA Y LE PASAMOS
    //LOS PARÁMETROS "email, subject, message".NOS MOSTRARÁ DESDE EL CORREO PREDEFINIDO DE SELLBACOMPANY@GMAIL.COM
    //NUESTRO CORREO RECIBIRÁ EL COMENTARIO DEL USUARIO Y SU EMAIL CON UN DESPLIEGUE MUY RÁPIDO.
    private void sendEmail() {
        String email = "sellbacompany@gmail.com";
        String subject = "[FEEDBACK] usuario " + Name;
        String message = editTextMessage.getText().toString().trim() + "\n\nEnviado por "+Name+"." +"\nEmail del usuario: "+ Email;
        SendMail sm = new SendMail(getActivity(), email, subject, message);
        sm.execute();
    }
    //COMPROVAMOS QUE EL MENSAJE NO ESTÉ VACIO.
    @Override
    public void onClick(View v) {
        if(editTextMessage.getText().toString().length() < 1){
            editTextMessage.setError("El mensaje no puede estar vacío.");
            editTextMessage.requestFocus();
            return;
        }
        //CUANDO ENVIAMOS EL MENSAJE DESPLEGAMOS UNA ALERTA CON UNA CABECERA, UN MENSAJE Y LA
        //OPCION DE ENVIAR O CANCELAR
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Comentario");
        builder.setMessage("Gracias por tu comentario. ¡Es muy valioso para nosotros!");
        builder.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendEmail();
                editTextMessage.setText("");
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editTextMessage.setText("");
                Toast.makeText(getActivity(),"Mensaje descartado", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog ad = builder.create();
        ad.show();
    }
}
