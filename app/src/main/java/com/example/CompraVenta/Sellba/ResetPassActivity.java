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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    EditText editTextEmail;
    TextInputEditText editTextPassword;
    ProgressBar progressBar;
    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pass);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        progressBar=findViewById(R.id.progressbar);

        mAuth=FirebaseAuth.getInstance();

        findViewById(R.id.buttonReset).setOnClickListener(this);
        findViewById(R.id.textViewBack).setOnClickListener(this);

    }

    private void passReset(){
        mEmail = editTextEmail.getText().toString().trim();
        if(mEmail.isEmpty()){
            editTextEmail.setError("El email es obligatorio");
            editTextEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()){
            editTextEmail.setError("Introduzca unn email válido");
            editTextEmail.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.getInstance().sendPasswordResetEmail(mEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    editTextEmail.setText("");
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ResetPassActivity.this, "Enlace de restablecimiento de contraseña enviado a su correo electrónico. Por favor revise la bandeja de su email", Toast.LENGTH_SHORT).show();
                }
                else{
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ResetPassActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ResetPassActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){

            case R.id.buttonReset:
                passReset();
                break;

            case R.id.textViewBack:
                Intent intentLogin = new Intent(this,LoginActivity.class);
                intentLogin.addFlags(intentLogin.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentLogin);
                finish();
                break;

        }

    }
}
