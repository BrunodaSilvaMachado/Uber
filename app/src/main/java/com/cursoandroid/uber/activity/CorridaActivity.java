package com.cursoandroid.uber.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.cursoandroid.uber.R;
import com.cursoandroid.uber.databinding.ActivityCorridaBinding;
import com.cursoandroid.uber.helper.Local;
import com.cursoandroid.uber.helper.MoneyTextWatcher;
import com.cursoandroid.uber.helper.Packager;
import com.cursoandroid.uber.helper.PrecoCorrida;
import com.cursoandroid.uber.model.Destino;
import com.cursoandroid.uber.model.Requisicao;
import com.cursoandroid.uber.model.Usuario;
import com.cursoandroid.uber.service.RequisicaoService;
import com.cursoandroid.uber.service.UsuarioService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class CorridaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final RequisicaoService requisicaoService = new RequisicaoService();
    private ActivityCorridaBinding binding;
    private GoogleMap mMap;
    private LocationCallback locationCallback;
    private Location currentLocation = null;
    private LatLng passageiroLocation = null;
    private LatLng motoristaLocation = null;
    private LatLng destinoLocation = null;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Usuario motorista;
    private Usuario passageiro;
    private String idRequisicao;
    private Marker markerMotorista;
    private Marker markerPassageiro;
    private Marker markerDestino;
    private String requestStatus;
    private Boolean requisicaoAtiva;
    private Destino destino;
    private long inicioCorrida;

    //-22.518284, -44.050412
    private final View.OnClickListener aceitarCorrida = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Map<String, Object> mapRequisicao = new HashMap<>();
            mapRequisicao.put("motorista", motorista);
            mapRequisicao.put("status", Requisicao.STATUS_A_CAMINHO);

            requisicaoService.update(idRequisicao, mapRequisicao);
        }
    };

    private final View.OnClickListener cancelarCorrida = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog alert = new AlertDialog.Builder(CorridaActivity.this)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setMessage(R.string.want_to_cancel)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, (d,w)->{
                        Map<String, Object> mapRequisicao = new HashMap<>();
                        mapRequisicao.put("motorista", null);
                        mapRequisicao.put("status", Requisicao.STATUS_CANCELADO_MOTORISTA);

                        requisicaoService.update(idRequisicao, mapRequisicao);
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            alert.show();

        }
    };

    private final View.OnClickListener finalizarCorrida = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Map<String, Object> mapRequisicao = new HashMap<>();
            mapRequisicao.put("finalRequisicao", System.currentTimeMillis());
            mapRequisicao.put("status", Requisicao.STATUS_ENCERRADO);
            requisicaoService.update(idRequisicao, mapRequisicao);
            finish();
        }
    };

    private final View.OnClickListener rotasCorrida = l->{
        String status = requestStatus;
        if (status != null && !status.isEmpty()){
            String lat = "";
            String lng = "";
            switch (status) {
                case Requisicao.STATUS_A_CAMINHO:
                    lat = String.valueOf(passageiroLocation.latitude);
                    lng = String.valueOf(passageiroLocation.longitude);
                    break;
                case Requisicao.STATUS_VIAGEM:
                    if(destino != null){
                        lat = destino.getLatitude();
                        lng = destino.getLongitude();
                    }
                    break;
            }
            //Abrir rota
            String latLng = lat +"," + lng;
            if (Packager.isExited(this, "com.google.android.apps.maps")){
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latLng +"&mode=d");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            } else {
                Toast.makeText(this, "Google maps is required", Toast.LENGTH_SHORT).show();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCorridaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupActionBar();
        initMap();

        if (getIntent().getExtras().containsKey("idRequisicao") &&
                getIntent().getExtras().containsKey("motorista")) {
            Bundle extras = getIntent().getExtras();
            if (Build.VERSION.SDK_INT >= 33) {
                motorista = extras.getParcelable("motorista", Usuario.class);
            } else {
                motorista = extras.getParcelable("motorista");
            }
            motoristaLocation = new LatLng(Double.parseDouble(motorista.getLatitude()),
                    Double.parseDouble(motorista.getLongitude()));
            idRequisicao = extras.getString("idRequisicao");
            requisicaoAtiva = extras.getBoolean("requisicaoAtiva");
            checkRequestStatus();
        }
        initComponents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        requisicaoService.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        requisicaoService.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        requisicaoService.finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (requisicaoAtiva){
            Toast.makeText(this, R.string.need_to_close_current_request, Toast.LENGTH_SHORT).show();
        }else{
            startActivity(new Intent(CorridaActivity.this, RequisicoesActivity.class));
        }
        if (requestStatus != null && requestStatus.equals(Requisicao.STATUS_NO_DESTINO)){
            requisicaoService.update(idRequisicao, "status", Requisicao.STATUS_ENCERRADO);
        }
        return false;
    }

    @Override
    public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
        mMap = googleMap;
        setupLocation();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(R.string.start_run);
    }

    private void initComponents() {
        binding.btnAceitar.setOnClickListener(aceitarCorrida);
        binding.fabRota.setOnClickListener(rotasCorrida);
    }

    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    private void confBtnAceitarDisable(String text){
        binding.btnAceitar.setText(text);
        binding.btnAceitar.setEnabled(false);
    }

    private void confBtnAceitarEnable(String text, View.OnClickListener listener){
        binding.btnAceitar.setText(text);
        binding.btnAceitar.setEnabled(true);
        binding.btnAceitar.setOnClickListener(listener);
    }

    private void setupLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 6000)
                .setMinUpdateIntervalMillis(3000)
                .setMaxUpdateDelayMillis(12000)
                .build();
        locationCallback = new LocationCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
                if (currentLocation != null) {
                    motoristaLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    UsuarioService.upadateLocation(currentLocation.getLatitude(),currentLocation.getLongitude());
                    requisicaoService.updateLocation(idRequisicao, "motorista",
                            String.valueOf(currentLocation.getLatitude()), String.valueOf(currentLocation.getLongitude()));
                    upadateUiRequestStatus(requestStatus);
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    private Marker setMapMarker(@Nullable Marker marker, @NonNull LatLng location, @NonNull String title,
                                @DrawableRes int idDrawable, boolean hasClear, boolean hasMoveCamera) {
        if (mMap != null) {
            if (marker != null)
                marker.remove();
            if (hasClear)
                mMap.clear();
            marker = mMap.addMarker(new MarkerOptions().position(location)
                    .title(title)
                    .icon(BitmapDescriptorFactory.fromResource(idDrawable)));
            if (hasMoveCamera)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }
        return marker;
    }

    private void centralizarDoisMarcadores(@NotNull Marker m1, @NotNull Marker m2) {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        builder.include(m1.getPosition());
        builder.include(m2.getPosition());

        LatLngBounds bounds = builder.build();
        int largura = getResources().getDisplayMetrics().widthPixels;
        int altura = getResources().getDisplayMetrics().heightPixels;
        int espacoInterno = (int) Math.round(largura * 0.25);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, largura, altura, espacoInterno));
    }

    private void checkRequestStatus() {
        requisicaoService.setEventsById(idRequisicao, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Requisicao requisicao = snapshot.getValue(Requisicao.class);

                if (requisicao != null) {
                    passageiro = requisicao.getPassageiro();
                    passageiroLocation = new LatLng(Double.parseDouble(passageiro.getLatitude()), Double.parseDouble(passageiro.getLongitude()));
                    destino = requisicao.getDestino();
                    destinoLocation = new LatLng(Double.parseDouble(destino.getLatitude()), Double.parseDouble(destino.getLongitude()));
                    requestStatus = requisicao.getStatus();
                    inicioCorrida = requisicao.getInicioRequisicao();
                    upadateUiRequestStatus(requestStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }

    private void upadateUiRequestStatus(String status) {

        switch (status) {
            case Requisicao.STATUS_AGUARDADNDO:
                waitingRequest();
                break;
            case Requisicao.STATUS_A_CAMINHO:
                onMyWayRequest();
                break;
            case Requisicao.STATUS_VIAGEM:
                travelingRequest();
                break;
            case Requisicao.STATUS_NO_DESTINO:
                finishRequest();
                break;
            case Requisicao.STATUS_CANCELADO_USUARIO:
                passengerCancelRequest();
                break;
            case Requisicao.STATUS_CANCELADO_MOTORISTA:
                diverCancelRequest();
                break;
        }
    }

    private void waitingRequest() {
        confBtnAceitarEnable(getString(R.string.accept), aceitarCorrida);
        binding.fabRota.setVisibility(View.GONE);
        markerMotorista = setMapMarker(markerMotorista, motoristaLocation, motorista.getNome(),
                R.drawable.carro, true, true);
    }

    private void onMyWayRequest() {
        confBtnAceitarEnable(getString(R.string.on_my_way), cancelarCorrida);
        binding.fabRota.setVisibility(View.VISIBLE);
        markerMotorista = setMapMarker(markerMotorista, motoristaLocation, motorista.getNome(),
                R.drawable.carro, false, false);
        markerPassageiro = setMapMarker(markerPassageiro, passageiroLocation, passageiro.getNome(),
                R.drawable.usuario, false, false);
        centralizarDoisMarcadores(markerMotorista, markerPassageiro);

        //Monitoramento da corrida
        monitoramento(motorista, passageiroLocation, Requisicao.STATUS_VIAGEM);
    }

    private void travelingRequest(){
        binding.fabRota.setVisibility(View.VISIBLE);
        confBtnAceitarDisable(getString(R.string.on_the_way_to_destination));
        markerMotorista = setMapMarker(markerMotorista, motoristaLocation, motorista.getNome(),
                R.drawable.carro, false, false);

        if (markerPassageiro != null){
            markerPassageiro.remove();
        }
        markerDestino = setMapMarker(markerDestino, destinoLocation, destino.getNumero(),
                R.drawable.destino, false, false);
        centralizarDoisMarcadores(markerMotorista, markerDestino);
        monitoramento(motorista, destinoLocation, Requisicao.STATUS_NO_DESTINO);
    }

    private void finishRequest(){
        requisicaoAtiva = false;
        binding.fabRota.setVisibility(View.GONE);
        if (markerMotorista != null){
            markerMotorista.remove();
        }

        if (markerDestino != null){
            markerDestino.remove();
        }
        markerDestino = setMapMarker(markerDestino, destinoLocation, destino.getNumero(),
                R.drawable.destino, true, true);

        double distancia = Local.calcularDistancia(passageiroLocation, destinoLocation);
        long custoTempo = Local.calculaCustoTempo(inicioCorrida, new Date().getTime());
        double valor = PrecoCorrida.preco(distancia, custoTempo);

        confBtnAceitarEnable(String.format(Locale.getDefault(),"%s - %s",
                getString(R.string.finished_race), MoneyTextWatcher.numberFormat.format(valor)),
                finalizarCorrida);
    }

    private void passengerCancelRequest(){
        Toast.makeText(this, R.string.cancel_passenger, Toast.LENGTH_LONG).show();
        startActivity(new Intent(CorridaActivity.this, RequisicoesActivity.class));
    }

    private void diverCancelRequest(){
        requisicaoAtiva = false;
        binding.fabRota.setVisibility(View.GONE);
        confBtnAceitarEnable(getString(R.string.accept), aceitarCorrida);
    }

    private void monitoramento(Usuario uOrigem, LatLng localDestino, String status){
        GeoFire geoFire = UsuarioService.geoFireLocalUsuario();
        //adicionando circulo no passageiro
        Circle circle = mMap.addCircle(
                new CircleOptions().center(localDestino)
                        .radius(50)
                        .fillColor(Color.argb(90, 255, 153, 0))
                        .strokeColor(Color.argb(190, 255, 152, 0))
        );
        GeoQuery geoQuery = geoFire.queryAtLocation(
                new GeoLocation(localDestino.latitude, localDestino.longitude), 0.05
        );
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (key.equals(uOrigem.getId())) {
                    //Alterar
                    requisicaoService.update(idRequisicao, "status", status);
                    geoQuery.removeAllListeners();
                    circle.remove();
                }
            }

            @Override
            public void onKeyExited(String key) {}

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                /*if(passageiro != null && key.equals(passageiro.getId())){
                    passageiroLocation = new LatLng(location.latitude, location.longitude);
                }*/
            }

            @Override
            public void onGeoQueryReady() {}

            @Override
            public void onGeoQueryError(DatabaseError error) {}
        });
    }
}