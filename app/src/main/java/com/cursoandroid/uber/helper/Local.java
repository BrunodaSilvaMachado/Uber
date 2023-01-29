package com.cursoandroid.uber.helper;

import android.location.Location;
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class Local {
    public static double calcularDistancia(LatLng lInicial, LatLng lFinal){
        Location localInicial = new Location("Local inicial");
        localInicial.setLatitude(lInicial.latitude);
        localInicial.setLongitude(lInicial.longitude);

        Location localFinal = new Location("Local final");
        localFinal.setLatitude(lFinal.latitude);
        localFinal.setLongitude(lFinal.longitude);

        return localInicial.distanceTo(localFinal)/ 1000;
    }

    public static String formatarDistancia(double distancia){
        String distanciaFormatada;
        if (distancia < 1){
            distancia *= 1000;
            distanciaFormatada = Math.round(distancia) + "M";
        }else{
            DecimalFormat decimal = new DecimalFormat("0.0");
            distanciaFormatada = decimal.format(distancia) + "KM";
        }

        return distanciaFormatada;
    }

    public static long calculaCustoTempo(long tInicial, long tfinal){
        long diff = tfinal - tInicial;
        TimeUnit timeUnit = TimeUnit.MINUTES;

        return timeUnit.convert(diff, TimeUnit.MILLISECONDS);
    }
}
