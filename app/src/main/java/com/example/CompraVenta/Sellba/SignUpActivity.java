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

    private void registerUser(){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        if(email.isEmpty()){
            editTextEmail.setError("El email es obligatorio");
            editTextEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Por favor introduzca un email válido");
            editTextEmail.requestFocus();
            return;
        }

        if(password.isEmpty()){
            editTextPassword.setError("La contraseña es obligatoria");
            editTextPassword.requestFocus();
            return;
        }

        if(password.length()<6 || password.length()>15){
            editTextPassword.setError("La contraseña debe contener de 6 a 15 caracteres");
            editTextPassword.requestFocus();
            return;
        }

        String upperCaseChars = "(.*[A-Z].*)";
        if (!password.matches(upperCaseChars ))
        {
            //editTextPassword.setError("Password should contain atleast one upper case alphabet");
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

        /*String specialChars = "(.*[,~,!,@,#,$,%,^,&,*,(,),-,_,=,+,[,{,],},|,;,:,<,>,/,?].*$)";
        if (!password.matches(specialChars ))
        {
            editTextPassword.setError("Password should contain at least one number, one lowercase letter, one uppercase letter, and one special character.");
            editTextPassword.requestFocus();
            return;
        }*/

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                 if(task.isSuccessful()){
                     /*Toast.makeText(getApplicationContext(),"User Registered Successfully",Toast.LENGTH_SHORT).show();
                     Intent intent=new Intent(SignUpActivity.this,DrawerActivity.class);
                     intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                     startActivity(intent);
                     finish();*/
                    sendEmailVerification();
                 }
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

    private void sendEmailVerification() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this,"Revise su correo electrónico para la verificación",Toast.LENGTH_LONG).show();
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
