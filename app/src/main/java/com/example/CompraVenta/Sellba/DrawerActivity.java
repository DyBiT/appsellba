package com.example.CompraVenta.Sellba;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

/*Navigation Drawer Se trata de un panel lateral que contiene un menu de navegación de la APP permanece
oculto en nuestra aplicación y lo desplazaremos pinchando en la barra superior o desplazando a la drch.*/
public class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, new HomeFragment()).addToBackStack(null).commit();
    }

    //PARA NAVEGAR POR EL MENU HEMOS DECIDIDO CREAR UN SWITCH QUE INTERECTAU EN UN MENU(NAV_MENU.XML)
    //DENTRO DEL MENU CREAMOS LOS ITEMS LOS CUALES SE ASIGNARAN A SUS RESPECTIVAS ACTIVITYS/FRAGMENTS.XML
    //CUANDO EL USUARIO HAGA CLICK SOBRE UN ITEM NOS LLEVARÁ A LA ACTIVITY/FRAGMENT CORRESPONDIENTE.
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, new HomeFragment()).addToBackStack(null).commit();
                break;

            case R.id.nav_my_profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, new ProfileFragment()).addToBackStack(null).commit();
                break;

            case R.id.nav_sell:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, new SellFragment()).addToBackStack(null).commit();
                break;
            //EN EL CASO DE DESCONECTAR CERRAREMOS LA INSTANCIA DEL USUARIO MEDIANTE FIREBASE:
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                break;

            case R.id.nav_about:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, new AboutFragment()).addToBackStack(null).commit();
                break;

            case R.id.nav_feedback:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, new FeedbackFragment()).addToBackStack(null).commit();
                break;


        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //METODO PARA EL CUAL SI EL MENU ESTÁ DESPLEGADO AL PULSAR ATRÁS SE VUELVA A PLEGAR.
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}

