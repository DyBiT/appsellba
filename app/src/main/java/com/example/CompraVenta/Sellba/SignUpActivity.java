package com.example.CompraVenta.Sellba;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;


/*Clase para registrar el usuario a traves de FireBase 'FirebaseAuth'por el método
predefinido en Firebase(Authentication/Sign-in-method/Email-PW habilitado*
necesitaremos un email activo (el usuario tendrá que verificarlo previamente para acceder a SELLBA
y una contraseña con párametros específicos que la definiremos en la clase registerUser.*/

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{

    ProgressBar progressBar;
    EditText editTextEmail;
    TextInputEditText editTextPassword;
    private FirebaseAuth mAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword= (TextInputEditText) findViewById((R.id.editTextPassword));
        progressBar = findViewById(R.id.progressbar);

        mAuth = FirebaseAuth.getInstance();


        findViewById(R.id.buttonSignUp).setOnClickListener(this);
        findViewById(R.id.textViewLogin).setOnClickListener(this);
    }

    /*Registramos usuario con email y contraseña, cuando pinchemos en el editText con el metodo
    .trim() nos dejara el campo a escribir por el usuario vacio.*/
    private void registerUser(){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        /* Si el email está vacio, nos marcara el campo y nos indicará que es obligatorio */
        if(email.isEmpty()){
            editTextEmail.setError("El email es obligatorio");
            editTextEmail.requestFocus();
            return;
        }
        /* Si el email no está en el formato correcto a través del método Patterns marcará
        * que el email no es válido*/
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Por favor introduzca un email válido");
            editTextEmail.requestFocus();
            return;
        }

        //Lo mismo para la contraseña, no puede estar vacía.
        if(password.isEmpty()){
            editTextPassword.setError("La contraseña es obligatoria");
            editTextPassword.requestFocus();
            return;
        }

        //Metodo para validación de contraseña en caso erroneo la app se lo marcará al usuario

        //Para que sea correcta necesitamos que la contraseña tenga de 6-15 carácteres
        //Una mayuscula, una minuscula y un numero.
        if(password.length()<6 || password.length()>15){
            editTextPassword.setError("La contraseña debe contener de 6 a 15 caracteres");
            editTextPassword.requestFocus();
            return;
        }

        String upperCaseChars = "(.*[A-Z].*)";
        if (!password.matches(upperCaseChars ))
        {
            editTextPassword.setError("La contraseña tiene que tener al menos un numero,una letra minúscula y una mayúscula.");
            editTextPassword.requestFocus();
            return;
        }

        String lowerCaseChars = "(.*[a-z].*)";
        if (!password.matches(lowerCaseChars ))
        {
            editTextPassword.setError("La contraseña tiene que tener al menos un numero,una letra minúscula y una mayúscula.");
            editTextPassword.requestFocus();
            return;
        }

        String numbers = "(.*[0-9].*)";
        if (!password.matches(numbers))
        {
            editTextPassword.setError("La contraseña tiene que tener al menos un numero,una letra minúscula y una mayúscula.");
            editTextPassword.requestFocus();
            return;
        }

        //En caso de que sea correcto mostraremos la barra de progreso en caso de que la conexión sea inestable
        progressBar.setVisibility(View.VISIBLE);

        /* Una vez que los párametros sean correctos a través del metodo predeterminado de Firebase
        mAuth.CreateUserWhitEmailAndPassword al cual le pasamos los parámetros de mail y pw*/
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);

                /*En caso de que todo este correcto, FireBase mandadará el email para validar la cuenta */

                 if(task.isSuccessful()){
                    sendEmailVerification();
                 }

                 /*En caso de que el usuario esté ya registrado con ese email o ocurra algun problema
                 FireBase mostrará la excepción correspondiente y vaciará los editText */
                 else{
                     if(task.getException() instanceof FirebaseAuthUserCollisionException){
                         editTextPassword.setText("");
                         Toast.makeText(getApplicationContext(),"Ya estas registrado",Toast.LENGTH_SHORT).show();
                     }
                     else{
                         editTextPassword.setText("");
                         Toast.makeText(SignUpActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                     }
                 }
            }
        });
    }
    /*Este metodo a traves de FireBase nos enviará la verificación del email al usuario que acaba de
    registrarse. Si tod ha salido correctamente mostraremos un mensaje al usuario de que revise su bandeja
    si no mostrará la excepción correspondiente */
    private void sendEmailVerification() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this,"Revise su correo electrónico para la verificación (puede estar en SPAM)",Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                        editTextEmail.setText("");
                        editTextPassword.setText("");
                    }
                    else{
                        Toast.makeText(SignUpActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    //Asignamos la funcionalidad en los botones para registrar los párametros o para regresar
    //al fragment de login.
    @Override
    public void onClick(View view) {
        switch(view.getId()) {

            case R.id.buttonSignUp:
                registerUser();
                break;

            case R.id.textViewLogin:
                Intent intentLogin = new Intent(this, LoginActivity.class);
                intentLogin.addFlags(intentLogin.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentLogin);
                finish();

                break;
        }
    }
}
