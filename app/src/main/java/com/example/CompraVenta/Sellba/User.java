package com.example.CompraVenta.Sellba;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Exclude;

//Creamos la clase usuario la cual con la ayuda de firebase y sus metodos predefinidos
//podremos registarnos y logearnos.
public class User {
    private String uName;
    private String mKey;
    private String uEmail;
    private String uToken;
    FirebaseAuth mAuth;
   //Necesitamos un constructor vacio.
    public User() {

    }

    public User(String token) {
        uName = mAuth.getInstance().getCurrentUser().getDisplayName();
        uEmail = mAuth.getInstance().getCurrentUser().getEmail();
        uToken = token;
    }

    public String getuToken() {
        return uToken;
    }

    public void setuToken(String token) {
        uToken = token;
    }

    public String getName() {
        return uName;
    }

    public void setName(String name) {
        uName = name;
    }

    public String getEmail() {
        return uEmail;
    }

    public void setEmail(String email) {
        uEmail = email;
    }


    @Exclude
    public String getKey() {
        return mKey;
    }

    @Exclude
    public void setKey(String key) {
        mKey = key;
    }


}
