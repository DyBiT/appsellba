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



/*FRAGMENT QUE PERMITE LA COMUNICACIÓN ENTRE COMPRADOR Y VENDEDOR CON DOS OPCIONES *COMPRAR Y MENSAJE*
* CON *MENSAJE AUTOMATIZADO POR EL SISTEMA SELLBA Y ENVIADOS DESDE EL EMAIL sellbacompany@gmail.com
* -EL COMPRADOR RECIBIRÁ UN EMAIL DE CONFIRMACIÓN DE CONEXIÓN INDICANDO QUE LA COMUNICACIÓN CON
* EL VENDEDOR SE HA REALIZADO CORRECTAMENTE ADJUNTANDO EL MENSAJE PREDETERMINADO CON EL NICKNAME
* DEL VENDEDOR Y EL NOMBRE DEL PRODUCTO.
* -EL VENDEDOR RECIBIRÁ UNA NOTIFICACION PREDETERMINADA DEL SISTEMA INDICANDO: SU NICKNAME,
* EL NICKNAME DEL COMPRADOR Y EL NOMBRE DEL PRODUCTO EN EL QUE ESTÁ INTERESADO, TAMBIEN RECIBIRÁ
* EL EMAIL POR SI QUIERE PONERSE EN CONTACTO CON EL DIRECTAMENTE
*
* CON *COMENTARIO EL COMPRADOR PUEDE ESCRIBIR EL MENSAJE PERSONALIZADO QUE DESEE AL VENDEDOR DEL PRODUCTO
* EL VENDEDOR RECIBIRÁ EL EMAIL DEL MENSAJE PERSONALIZADO Y AUTOMATICO GENERADO POR SELLBA INDICANDO
* EL NICKNAME DEL COMPRADOR Y SU CORREO ELECTRÓNICO.*/

public class MsgFragment extends Fragment implements View.OnClickListener {

    private EditText editTextMessage;
    private Button buttonSend;
    private String sName;
    private String sEmail;
    private String pName;
    private String bName;
    private String bEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_msg, container, false);
        editTextMessage = (EditText) v.findViewById(R.id.editTextMessage);
        buttonSend = (Button) v.findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(this);

        Bundle bundle = getArguments();
        if(bundle != null){
            sName = bundle.getString("sName");
            sEmail = bundle.getString("sEmail");
            bEmail = bundle.getString("bEmail");
            bName = bundle.getString("bName");
            pName = bundle.getString("pName");

        }

        return v;
    }
    //PASAMOS LOS PARÁMETROS REQUERIDOS PARA LA CLASE CREADA SENDMAIL.
    private void sendEmailToSeller() {
        String email = sEmail;
        String subject = "[SELLBA] Petición de producto " + pName;
        String msg = "nombre-desconocido";
        if (bName != "")
            msg = bName;
        String thankMsg = "\n\nGracias por usar SELLBA :)";
        String autoMsg = "\n\nEsto es un email autogenerado por SELLBA. Por favor no responda a este correo.";
        String messageTxt ="Hola " + sName + ", " + msg + " está interesado en su producto \"" + pName + "\"."+"\n"+
         "Mensaje de " +bName   +".\n"+editTextMessage.getText().toString().trim() + "\n\nSi quieres puedes escribir " + bName + " en el email " + bEmail + " ." + thankMsg + autoMsg;
        SendMail sm = new SendMail(getActivity(), email, subject, messageTxt);
        sm.execute();
    }


    //VERIFICACIÓN PARA QUE EL COMENTARIO NO PUEDA ESTAR VACIO.
    @Override
    public void onClick(View v) {
        if(editTextMessage.getText().toString().length() < 1){
            editTextMessage.setError("El mensaje no puede estar vacío.");
            editTextMessage.requestFocus();
            return;
        }
        //ALERTA DE QUE EL MENSAJE SE HA ENVIADO CORRECTAMENTE
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Mensaje enviado");
        builder.setMessage("El mensaje se va a enviar por email al propietario de este producto");

        //CUANDO PULSEMOS A ENVIAR EL USUARIO PODRÁ CONFIRMAR O DESCARTAR
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendEmailToSeller();
                sendEmailToBuyer();
                editTextMessage.setText("");
            }
        });

        builder.setNegativeButton("Descartar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editTextMessage.setText("");
                Toast.makeText(getActivity(),"Mensaje descartado", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog ad = builder.create();
        ad.show();
    }

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
