package com.example.CompraVenta.Sellba;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/* Creamos la clase para enviar mensaje entre usuarios, extiende de la clase AsyncsTaks
la cual nos permite ejercutar el proceso en segundo plano onPre,doInBackground, onPost
*  */
public class SendMail extends AsyncTask<Void, Void, Void> {

    private Context context;
    private Session session;
    private String email;
    private String subject;
    private String message;
    private ProgressDialog progressDialog;

    public SendMail(Context context, String email, String subject, String message) {

        this.context = context;
        this.email = email;
        this.subject = subject;
        this.message = message;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Mostramos enviando mensaje mientras se completa.
        progressDialog = ProgressDialog.show(context, "Enviando mensaje", "Por favor espera...", false, false);
    }
    //Cuando se ejecuta mostramos al usuario que se ha enviado correctamente.
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        progressDialog.dismiss();
        Toast.makeText(context, "Mensaje enviado correctamente", Toast.LENGTH_LONG).show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        Properties props = new Properties();
        //Configuración de propiedades para email autómatico, solo funciona en GMAIL
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        //Creamos la nueva sesion para conectar el correo con el que se enviarán los emails
        //Creamos el Config.class para pasarle los parámetros email y pw.
        session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            //Authenticating the password
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Config.EMAIL, Config.PASSWORD);
            }
        });
        try {
            //instaciamos el objeto de la clase MimeMessage.
            MimeMessage mm = new MimeMessage(session);
            //Set from fija la dirección origen del mensaje
            mm.setFrom(new InternetAddress(Config.EMAIL));
            //El método addRecipients() determina los receptores del mensaje (to, cc, bcc):
            mm.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            //EL método setSubject() establece el tema del mensaje.
            mm.setSubject(subject);
            //setText y Transport establece el mensaje y se encarga de enviarlo.
            mm.setText(message);
            Transport.send(mm);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
