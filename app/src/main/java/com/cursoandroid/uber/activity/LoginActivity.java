package com.cursoandroid.uber.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.cursoandroid.uber.R;
import com.cursoandroid.uber.config.FirebaseConfig;
import com.cursoandroid.uber.helper.RedirecionaUsuario;
import com.cursoandroid.uber.model.Usuario;
import com.cursoandroid.uber.service.UsuarioService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private final FirebaseAuth auth = FirebaseConfig.getFirebaseAuth();
    private ProgressBar loadingProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final EditText emailEditText = findViewById(R.id.etLoginEmail);
        final EditText passwordEditText = findViewById(R.id.etLoginPassword);
        final Button loginButton = findViewById(R.id.btnLogin);
        loadingProgressBar = findViewById(R.id.loginProgressBar);

        LoginViewModel loginViewModel = new ViewModelProvider(this, new LoginViewModel.LoginViewModelFactory())
                .get(LoginViewModel.class);
        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getEmailError() != null) {
                emailEditText.setError(getString(loginFormState.getEmailError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
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
                loginViewModel.loginDataChanged(emailEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        emailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login(emailEditText.getText().toString(), passwordEditText.getText().toString());
            }
            return false;
        });

        loginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            login(emailEditText.getText().toString(), passwordEditText.getText().toString());
        });
    }

    private void updateUiWithUser() {
        RedirecionaUsuario.updateUi(LoginActivity.this);
    }

    private void showLoginFailed(String errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void login(String email, String password) {
        final Usuario usuario = new Usuario(null, email, password,"");
        auth.signInWithEmailAndPassword(usuario.getEmail(), usuario.getSenha())
                .addOnCompleteListener(task->
                {
                    loadingProgressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()){
                        updateUiWithUser();
                    }else {
                        String excecao;
                        try {
                            throw Objects.requireNonNull(task.getException());
                        }catch (FirebaseAuthInvalidCredentialsException ignored){
                            excecao = getString(R.string.invalid_credentials);
                        }catch (FirebaseAuthInvalidUserException ignored){
                            excecao = getString(R.string.invalid_username);
                        }catch (Exception e){
                            excecao = getString(R.string.login_failed) + ": " + e.getMessage();
                            e.printStackTrace();
                        }
                        showLoginFailed(excecao);
                    }
                });
    }
}