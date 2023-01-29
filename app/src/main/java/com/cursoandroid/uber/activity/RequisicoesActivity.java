package com.cursoandroid.uber.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cursoandroid.uber.R;
import com.cursoandroid.uber.adapter.GroupAdapter;
import com.cursoandroid.uber.config.FirebaseConfig;
import com.cursoandroid.uber.databinding.ActivityRequisicoesBinding;
import com.cursoandroid.uber.helper.Local;
import com.cursoandroid.uber.model.Requisicao;
import com.cursoandroid.uber.model.Usuario;
import com.cursoandroid.uber.service.RequisicaoService;
import com.cursoandroid.uber.service.UsuarioService;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RequisicoesActivity extends AppCompatActivity {

    private final RequisicaoService requisicaoService = new RequisicaoService();
    private final List<Requisicao> requisicaoList = new ArrayList<>();
    private final Usuario motorista = UsuarioService.getCurrentUser();
    private ActivityRequisicoesBinding binding;
    private GroupAdapter<Requisicao> groupAdapter;
    private LocationCallback locationCallback;
    private Location currentLocation = null;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequisicoesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupActionBar();
        setupLocation();
        fetchRequests();
        initComponents();
    }

    /*@Override
    protected void onStart() {
        super.onStart();
        checkRequestStatus();
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_requisicoes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menuSair){
            FirebaseConfig.getFirebaseAuth().signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(R.string.requests);
    }

    private void initComponents(){
        binding.progressBarRequisicoes.setVisibility(View.VISIBLE);
        groupAdapter = new GroupAdapter<>(requisicaoList, l->R.layout.adapter_requisicoes,
                (requisicao, itemView) -> {
                    TextView name = itemView.findViewById(R.id.tvRequestsName);
                    TextView distance = itemView.findViewById(R.id.tvRequestDistance);

                    Usuario passageiro = requisicao.getPassageiro();
                    name.setText(passageiro.getNome());
                    if(currentLocation != null){
                        LatLng lm = new LatLng(Double.parseDouble(motorista.getLatitude()),
                                Double.parseDouble(motorista.getLongitude()));
                        LatLng lp = new LatLng(Double.parseDouble(passageiro.getLatitude()),
                                Double.parseDouble(passageiro.getLongitude()));
                        double distancia = Local.calcularDistancia(lp, lm);
                        distance.setText(Local.formatarDistancia(distancia));
                    }
                    else {
                        distance.setText("--");
                    }
        });
        groupAdapter.setOnItemClickListener(requisicao -> updateUiRequisicaoToCorrida(requisicao, motorista, false));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        binding.recyclerRequest.setLayoutManager(layoutManager);
        binding.recyclerRequest.setHasFixedSize(true);
        binding.recyclerRequest.setAdapter(groupAdapter);
    }

    private void updateUiRequisicaoToCorrida(Requisicao requisicao, Usuario motorista, boolean requisicaoAtiva){
        if(currentLocation != null){
            Intent intent = new Intent(RequisicoesActivity.this, CorridaActivity.class);
            intent.putExtra("idRequisicao", requisicao.getId());
            intent.putExtra("motorista", motorista);
            intent.putExtra("requisicaoAtiva", requisicaoAtiva);
            startActivity(intent);
        }
    }

    private void hideRequests(){
        binding.recyclerRequest.setVisibility(View.GONE);
        binding.tvResultados.setVisibility(View.VISIBLE);
    }

    private void showRequests(){
        binding.recyclerRequest.setVisibility(View.VISIBLE);
        binding.tvResultados.setVisibility(View.GONE);
    }

    private void checkRequestStatus(){
        requisicaoService.findUserById(UsuarioService.getCurrentUserId(), "motorista/id")
                .addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Requisicao requisicao = ds.getValue(Requisicao.class);
                    if (requisicao != null){
                        if(requisicao.getStatus().equals(Requisicao.STATUS_A_CAMINHO)
                                || requisicao.getStatus().equals(Requisicao.STATUS_VIAGEM)
                                || requisicao.getStatus().equals(Requisicao.STATUS_NO_DESTINO)){
                            Usuario motorista = requisicao.getMotorista();
                            updateUiRequisicaoToCorrida(requisicao, motorista, true);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
    }

    private void fetchRequests(){
        requisicaoService.findStatus(Requisicao.STATUS_AGUARDADNDO, new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() > 0){
                    showRequests();
                }
                else {
                    hideRequests();
                }
                requisicaoList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    requisicaoList.add(ds.getValue(Requisicao.class));
                }
                binding.progressBarRequisicoes.setVisibility(View.GONE);
                groupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
    }

    private void setupLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 6000)
                .setMinUpdateIntervalMillis(1000)
                .setMaxUpdateDelayMillis(10000)
                .build();
        locationCallback = new LocationCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
                if (currentLocation != null) {
                    motorista.setLatitude(String.valueOf(currentLocation.getLatitude()));
                    motorista.setLongitude(String.valueOf(currentLocation.getLongitude()));
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    groupAdapter.notifyDataSetChanged();
                    UsuarioService.upadateLocation(currentLocation.getLatitude(),currentLocation.getLongitude());
                    checkRequestStatus();//sempre por último
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }
}