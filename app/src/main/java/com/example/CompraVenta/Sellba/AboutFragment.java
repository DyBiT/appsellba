package com.example.CompraVenta.Sellba;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/* FRAGMENT MUY SIMPLE PARA MOSTRAR UN POCO DE INFORMACIÓN SOBRE LA APP Y LOS CREADORES*/
public class AboutFragment extends Fragment{

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }



}
