package com.cursoandroid.uber.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.cursoandroid.uber.R;
import com.cursoandroid.uber.helper.Permissoes;
import com.cursoandroid.uber.helper.RedirecionaUsuario;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        Permissoes.validarPermissoes(MainActivity.this).launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    @Override
    protected void onStart() {
        super.onStart();
        RedirecionaUsuario.updateUi(MainActivity.this);
    }

    public void abrirTelaLogin(View v){
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void abrirTelaCadastro(View v){
        startActivity(new Intent(this, CadastroActivity.class));
    }
}