package com.corentin.bigredbutton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private Location coordonnees;
    private Button BoutonLireSenseurGps;
    private TextView ChampTexteEmplacementGeographique;
    private boolean permissionsOk;
    private LocationManager responsableMiseAJourReguliereGoordonneésTelephone;
    private Button BoutonPartagerContenuChampTexteEmplacementGeographique;

    private void demanderPermissionsGPS() {
        String[] listeDePermissionsNecessaires = {Manifest.permission.ACCESS_FINE_LOCATION};
        int request_code = 100; // TODO la doc n est pas claire à ce propos
        ActivityCompat.requestPermissions(MainActivity.this,
                listeDePermissionsNecessaires,
                request_code);
    }

    private boolean estPermissionGPSOk() {
        boolean pretATravailler = true;
        int valeur_permission_tout_est_ok = PackageManager.PERMISSION_GRANTED;
        int valeur_permission_reelle = ContextCompat
                .checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
        if (valeur_permission_reelle != valeur_permission_tout_est_ok) {
            pretATravailler = false;
        }
        return pretATravailler;

    }
    private String getHorodatageMessage(){
        // l utilisation de localDateTime necesssite bricolage dans le fichier graddle
        // pour que l api 21 accepte d utiliser des trucs de l api 30
        // (alors que c est du java 8+, faut pas déconner!)
        LocalDateTime hododatageMessage = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String horodatageMessageFormatte = (String) hododatageMessage.format(formatter);
        return horodatageMessageFormatte;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // perms
        this.permissionsOk = this.estPermissionGPSOk();
        if (!this.permissionsOk) {
            this.demanderPermissionsGPS();
        }
        BoutonPartagerContenuChampTexteEmplacementGeographique = findViewById(R.id.BoutonPartagerCoordonneesGeographiquesDuChampTexte);
        ChampTexteEmplacementGeographique = findViewById(R.id.ChampTexteCoordonneesGeoGraphiques);
        BoutonLireSenseurGps = findViewById(R.id.BoutonLireCoordonneesGeographiquesDuRecepteurGPSTelephone);
        BoutonPartagerContenuChampTexteEmplacementGeographique.setEnabled(false);

        BoutonLireSenseurGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lireRecepteurGPS();
            }
        });



        BoutonPartagerContenuChampTexteEmplacementGeographique.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BoutonPartagerContenuChampTexteEmplacementGeographique.setEnabled(false);
                Intent partager = new Intent(Intent.ACTION_SEND);
                partager.setType("text/plain");
                String TexteAPartager = getHorodatageMessage() + "," + ChampTexteEmplacementGeographique.getText();
                Log.i("BigRedButton -",  "texte vaut : " + TexteAPartager);
                partager.putExtra(getIntent().EXTRA_TEXT,TexteAPartager);

                startActivity(partager.createChooser(partager,"Partager"));
            }
        });


    }


    private LocationManager fabriquerResponsableMiseAJourCoordonneesTelephone() {
        LocationManager responsable;
        responsable = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        return responsable;
    }

    @SuppressLint("MissingPermission")
    private void demanderMiseAJourToutesLes5Secondes(LocationManager resp) {
        int auMoins5secondesEntreLesTests = 5000;
        String nomFournisseurGPS = LocationManager.GPS_PROVIDER;
        int auMoins5MetresEntreLesTests = 5;
        android.location.LocationListener prevenirQui = MainActivity.this;

        try{
            resp.requestLocationUpdates(nomFournisseurGPS,
                    auMoins5secondesEntreLesTests,
                    auMoins5MetresEntreLesTests,
                    prevenirQui);
        }
        catch (Exception e){
            e.printStackTrace();
        }



    }

    private void lireRecepteurGPS() {
        responsableMiseAJourReguliereGoordonneésTelephone = this.fabriquerResponsableMiseAJourCoordonneesTelephone();
        this.demanderMiseAJourToutesLes5Secondes(responsableMiseAJourReguliereGoordonneésTelephone);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        String latitude = Double.toString(location.getLatitude());
        String longitude = Double.toString(location.getLongitude());
        String resultat = "https://www.google.com/maps/search/?api=1&query=" + latitude+","+ longitude;
        this.ChampTexteEmplacementGeographique.setText(resultat);
        this.BoutonPartagerContenuChampTexteEmplacementGeographique.setEnabled(true);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

}