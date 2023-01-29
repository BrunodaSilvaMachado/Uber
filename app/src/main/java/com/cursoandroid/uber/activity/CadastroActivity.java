package com.cursoandroid.uber.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.cursoandroid.uber.R;
import com.cursoandroid.uber.config.FirebaseConfig;
import com.cursoandroid.uber.model.Usuario;
import com.cursoandroid.uber.service.UsuarioService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.util.Objects;

public class CadastroActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        EditText campoNome = findViewById(R.id.etCadastroNome);
        EditText campoEmail = findViewById(R.id.etCadastroEmail);
        EditText campoSenha = findViewById(R.id.etCadastroPassword);
        Button btnCadastrar = findViewById(R.id.btnCadastro);
        final Switch tipoAcesso = findViewById(R.id.swAccess);
        progressBar = findViewById(R.id.cadastroProgressBar);
        progressBar.setVisibility(View.GONE);
        CadastroViewModel cadastroViewModel = new ViewModelProvider(this, new CadastroViewModel.CadastroViewModelFactory())
                .get(CadastroViewModel.class);

        cadastroViewModel.getCadastroFormState().observe(this, formState -> {
            if (formState == null) {
                return;
            }
            btnCadastrar.setEnabled(formState.isDataValid());
            if (formState.getUsernameError() != null) {
                campoNome.setError(getString(formState.getUsernameError()));
            }
            if (formState.getEmailError() != null) {
                campoEmail.setError(getString(formState.getEmailError()));
            }
            if (formState.getPasswordError() != null) {
                campoSenha.setError(getString(formState.getPasswordError()));
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                cadastroViewModel.cadastroDataChanged(campoNome.getText().toString(),
                        campoEmail.getText().toString(), campoSenha.getText().toString());
            }
        };
        campoNome.addTextChangedListener(afterTextChangedListener);
        campoEmail.addTextChangedListener(afterTextChangedListener);
        campoSenha.addTextChangedListener(afterTextChangedListener);
        campoSenha.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                cadastrarUsuario(campoNome.getText().toString(), campoEmail.getText().toString(),
                        campoSenha.getText().toString(),tipoAcesso.isChecked());
            }
            return false;
        });

        btnCadastrar.setOnClickListener(view -> cadastrarUsuario(campoNome.getText().toString(),
                campoEmail.getText().toString(), campoSenha.getText().toString(),tipoAcesso.isChecked()));
    }

    private void cadastrarUsuario(String username, String email, String password, boolean isDriver) {
        final UsuarioService usuarioService = new UsuarioService();
        final Usuario usuario = new Usuario(username, email, password, verificaTipoUsuario(isDriver));
        progressBar.setVisibility(View.VISIBLE);

        FirebaseAuth autenticacao = FirebaseConfig.getFirebaseAuth();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        try {
                            usuario.setId(Objects.requireNonNull(task.getResult().getUser()).getUid());
                            usuarioService.save(usuario);
                        }catch (Exception e){
                            e.printStackTrace();
                        }finally {
                            if(Objects.equals(usuario.getTipo(), "P")){
                                startActivity(new Intent(getApplicationContext(),PassengerActivity.class));
                                finish();
                            }else{
                                startActivity(new Intent(getApplicationContext(),RequisicoesActivity.class));
                                finish();
                            }
                        }
                    } else {
                        String excecao;
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (FirebaseAuthWeakPasswordException ignored) {
                            excecao = getString(R.string.invalid_password);
                        } catch (FirebaseAuthInvalidCredentialsException ignored) {
                            excecao = getString(R.string.invalid_credentials);
                        } catch (FirebaseAuthUserCollisionException ignored) {
                            excecao = getString(R.string.invalid_username);
                        } catch (Exception e) {
                            excecao = e.getMessage();
                            e.printStackTrace();
                        }
                        Toast.makeText(this, excecao, Toast.LENGTH_LONG).show();
                    }
                }
        );

    }

    public String verificaTipoUsuario(boolean isDriver){
        return isDriver? "M": "P";
    }
}