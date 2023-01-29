package com.cursoandroid.uber.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.cursoandroid.uber.R;
import com.cursoandroid.uber.config.FirebaseConfig;
import com.cursoandroid.uber.databinding.ActivityPassengerBinding;
import com.cursoandroid.uber.helper.Local;
import com.cursoandroid.uber.helper.MoneyTextWatcher;
import com.cursoandroid.uber.helper.PrecoCorrida;
import com.cursoandroid.uber.model.Destino;
import com.cursoandroid.uber.model.Requisicao;
import com.cursoandroid.uber.model.Usuario;
import com.cursoandroid.uber.service.RequisicaoService;
import com.cursoandroid.uber.service.UsuarioService;
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
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class PassengerActivity extends AppCompatActivity implements OnMapReadyCallback {

    /*
        Lat/lon Destino: -23.556407, -46.662365 (Av. Paulista, 2439)
        Lat/lon passageiro: -23.562791, -46.654668
        Lat/lon Motorista: (a caminho)
            inicial: -23.563196, -46.652196
            intermediario: -23.564801, -46.652129
            final: -23.562801, -46.654660
        Encerramento itemediario: -23.557499, -46.661084
        Enceramento corrida: -23.556439, -46.662313
     */
    private final RequisicaoService requisicaoService = new RequisicaoService();
    LocationCallback locationCallback;
    Location currentLocation = null;
    private GoogleMap mMap;
    private ActivityPassengerBinding binding;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Requisicao requisicao;
    private Usuario motorista;
    private Usuario passageiro;
    private String requestStatus;
    private Destino destino;
    private long inicioCorrida;
    private LatLng passageiroLocation = null;
    private LatLng motoristaLocation = null;
    private LatLng destinoLocation = null;
    private Marker markerMotorista;
    private Marker markerPassageiro;
    private Marker markerDestino;
    private final View.OnClickListener uberCall = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String endereco = binding.editDestino.getText().toString();

            if (!endereco.isEmpty()) {
                Address address = getAddress(endereco);
                if (address != null) {
                    Destino destino = new Destino(
                            address.getThoroughfare(), address.getFeatureName(), address.getSubAdminArea(),
                            address.getSubLocality(), address.getPostalCode(),
                            String.valueOf(address.getLatitude()), String.valueOf(address.getLongitude())
                    );
                    StringBuilder message = new StringBuilder();
                    message.append("Cidade: ").append(destino.getCidade())
                            .append("\nRua: ").append(destino.getRua())
                            .append("\nBairro: ").append(destino.getBairro())
                            .append("\nNúmero: ").append(destino.getNumero())
                            .append("\nCEP: ").append(destino.getCep());
                    AlertDialog alertDialog = new AlertDialog.Builder(PassengerActivity.this)
                            .setTitle(R.string.confirm_your_address)
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton(R.string.confirm, (d, w) -> saveRequest(destino))
                            .setNegativeButton(android.R.string.cancel, (d, w) -> {

                            }).create();
                    alertDialog.show();
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.teal_200));
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.teal_200));
                }
            } else {
                Toast.makeText(PassengerActivity.this, R.string.inform_the_address, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final View.OnClickListener uberCancel = v -> {
        requisicaoService.update(requisicao.getId(), "status", Requisicao.STATUS_ENCERRADO);
        showLayoutDestino();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPassengerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupActionBar();
        initMap();
        initComponents();
        checkRequestStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_passenger, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuSair) {
            FirebaseConfig.getFirebaseAuth().signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeLocationUpdate(locationCallback);
    }

    @Override
    public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
        mMap = googleMap;
        setupLocation();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(R.string.start_travel);
    }

    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    private void initComponents() {
        passageiro = UsuarioService.getCurrentUser();
        binding.btnChamarUber.setOnClickListener(uberCall);
    }

    private void hideLayoutDestino(String textBtnChamar) {
        binding.btnChamarUber.setText(textBtnChamar);
        binding.linearLayoutDestino.setVisibility(View.GONE);
    }

    private void hideLayoutDestinoCancelButton(String textBtnChamar) {
        hideLayoutDestino(textBtnChamar);
        binding.btnChamarUber.setOnClickListener(uberCancel);
    }

    private void hideLayoutDestinoDisableButton(String textBtnChamar) {
        hideLayoutDestino(textBtnChamar);
        binding.btnChamarUber.setEnabled(false);
    }

    private void showLayoutDestino() {
        binding.btnChamarUber.setOnClickListener(uberCall);
        binding.btnChamarUber.setText(R.string.call);
        binding.linearLayoutDestino.setVisibility(View.VISIBLE);
    }

    private void saveRequest(Destino destino) {
        Usuario usuarioPassageiro = UsuarioService.getCurrentUser();
        if (currentLocation != null) {
            usuarioPassageiro.setLatitude(String.valueOf(currentLocation.getLatitude()));
            usuarioPassageiro.setLongitude(String.valueOf(currentLocation.getLongitude()));
        }
        long timestamp = System.currentTimeMillis();
        Requisicao requisicao = new Requisicao(requisicaoService.UUID(), Requisicao.STATUS_AGUARDADNDO,
                usuarioPassageiro, null, destino, timestamp);

        requisicaoService.save(requisicao);
    }

    private void checkRequestStatus() {
        requisicaoService.findUserById(UsuarioService.getCurrentUserId(), "passageiro/id")
                .addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                List<Requisicao> requisicaoList = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    requisicaoList.add(ds.getValue(Requisicao.class));
                }

                if (requisicaoList.size() > 0) {
                    requisicao = requisicaoList.get(requisicaoList.size() - 1);

                    if (requisicao != null && !requisicao.getStatus().equals(Requisicao.STATUS_ENCERRADO)) {
                        passageiro = requisicao.getPassageiro();
                        passageiroLocation = new LatLng(Double.parseDouble(passageiro.getLatitude()), Double.parseDouble(passageiro.getLongitude()));
                        destino = requisicao.getDestino();
                        destinoLocation = new LatLng(Double.parseDouble(destino.getLatitude()), Double.parseDouble(destino.getLongitude()));
                        requestStatus = requisicao.getStatus();
                        inicioCorrida = requisicao.getInicioRequisicao();
                        if (requisicao.getMotorista() != null){
                            motorista = requisicao.getMotorista();
                            motoristaLocation = new LatLng(Double.parseDouble(motorista.getLatitude()), Double.parseDouble(motorista.getLongitude()));
                        }
                        upadateUiRequestStatus(requestStatus);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
    }

    @Nullable
    private Address getAddress(String adr) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            final List<Address> addressList = new ArrayList<>();
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                geocoder.getFromLocationName(adr, 1, addressList::addAll);
            } else {
                addressList.addAll(geocoder.getFromLocationName(adr, 1));
            }

            if (addressList.size() > 0) {
                return addressList.get(0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private void setupLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 6000)
                .setMinUpdateIntervalMillis(3000)
                .setMaxUpdateDelayMillis(12000)
                .build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
                if (currentLocation != null) {
                    //Atualiza geofire
                    passageiroLocation = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
                    UsuarioService.upadateLocation(currentLocation.getLatitude(),currentLocation.getLongitude());
                    if (requisicao != null){
                        requisicaoService.updateLocation(requisicao.getId(), "passageiro",
                                String.valueOf(currentLocation.getLatitude()), String.valueOf(currentLocation.getLongitude()));
                    }

                    if (requestStatus != null && !requestStatus.isEmpty()){
                        upadateUiRequestStatus(requestStatus);

                        if(requestStatus.equals(Requisicao.STATUS_VIAGEM) || requestStatus.equals(Requisicao.STATUS_NO_DESTINO)){
                            removeLocationUpdate(locationCallback);
                        }
                        else {
                            addLocationUpdate(locationRequest, locationCallback);
                        }
                    }else{
                        marcadorUsuario();
                    }

                }
            }
        };
        addLocationUpdate(locationRequest, locationCallback);
    }

    private void addLocationUpdate(LocationRequest locationRequest,  LocationCallback locationCallback){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    private void removeLocationUpdate(LocationCallback locationCallback){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private Marker setMapMarker(@androidx.annotation.Nullable Marker marker, @NonNull LatLng location, @NonNull String title,
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
        int espacoInterno = (int) Math.round(largura * 0.30);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, largura, altura, espacoInterno));
    }

    private void marcadorUsuario(){
        markerPassageiro = setMapMarker(markerPassageiro, passageiroLocation, passageiro.getNome(),
                R.drawable.usuario, true, true);
    }

    private void upadateUiRequestStatus(String status) {
        binding.btnChamarUber.setOnClickListener(uberCall);
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
            case Requisicao.STATUS_ENCERRADO:
                marcadorUsuario();
                break;
            case Requisicao.STATUS_CANCELADO_USUARIO:
                cancelRequest();
                break;
            case Requisicao.STATUS_CANCELADO_MOTORISTA:
                diverCancelRequest();
                break;
        }
    }

    private void waitingRequest(){
        hideLayoutDestinoCancelButton(getString(android.R.string.cancel));
        marcadorUsuario();
    }

    private void onMyWayRequest(){
        hideLayoutDestinoDisableButton(getString(R.string.on_my_way));
        markerMotorista = setMapMarker(markerMotorista, motoristaLocation, motorista.getNome(),
                R.drawable.carro, false, false);
        markerPassageiro = setMapMarker(markerPassageiro, passageiroLocation, passageiro.getNome(),
                R.drawable.usuario, false, false);
        centralizarDoisMarcadores(markerMotorista, markerPassageiro);
    }

    private void travelingRequest(){
        hideLayoutDestinoDisableButton(getString(R.string.on_the_way_to_destination));
        markerMotorista = setMapMarker(markerMotorista, motoristaLocation, motorista.getNome(),
                R.drawable.carro, false, false);

        if (markerPassageiro != null){
            markerPassageiro.remove();
        }
        markerDestino = setMapMarker(markerDestino, destinoLocation, destino.getNumero(),
                R.drawable.destino, false, false);
        centralizarDoisMarcadores(markerMotorista, markerDestino);
    }

    private void finishRequest(){
        markerDestino = setMapMarker(markerDestino, destinoLocation, destino.getNumero(),
                R.drawable.destino, true, true);
        double distancia = Local.calcularDistancia(passageiroLocation, destinoLocation);
        final long finalCorrida = System.currentTimeMillis();
        long custoTempo = Local.calculaCustoTempo(inicioCorrida, finalCorrida);
        double valor = PrecoCorrida.preco(distancia, custoTempo);
        final String valorFormated = MoneyTextWatcher.numberFormat.format(valor);
        hideLayoutDestinoDisableButton(String.format(Locale.getDefault(),"%s - %s",
                getString(R.string.finished_race), valorFormated));
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(R.string.travel_total)
                .setMessage(String.format(Locale.getDefault(),"%s - %s",getString(R.string.your_trip_cost),valorFormated))
                .setCancelable(false)
                .setNeutralButton(android.R.string.ok, (d, w)->{
                    Map<String, Object> mapRequisicao = new HashMap<>();
                    mapRequisicao.put("finalRequisicao", finalCorrida);
                    mapRequisicao.put("status", Requisicao.STATUS_ENCERRADO);
                    requisicaoService.update(requisicao.getId(), mapRequisicao);
                    finish();
                    startActivity(new Intent(getIntent()));
                }).create();
        alert.show();
    }

    private void cancelRequest(){
        showLayoutDestino();
    }

    private void diverCancelRequest(){
        Toast.makeText(this, R.string.cancel_driver, Toast.LENGTH_LONG).show();
        requisicaoService.update(requisicao.getId(),"status",Requisicao.STATUS_AGUARDADNDO);
    }
}