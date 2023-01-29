package com.cursoandroid.uber.activity;

import android.util.Patterns;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.cursoandroid.uber.R;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<FormState> loginFormState = new MutableLiveData<>();

    LoginViewModel() {
    }

    LiveData<FormState> getLoginFormState() {
        return loginFormState;
    }

    public void loginDataChanged(String email, String password) {
        if (!isEmailValid(email)) {
            loginFormState.setValue(new FormState(null,R.string.invalid_email, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new FormState(null, null,  R.string.invalid_password));
        } else {
            loginFormState.setValue(new FormState(true));
        }
    }

    // A placeholder email validation check
    private boolean isEmailValid(String email) {
        if (email == null) {
            return false;
        }
        if (email.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        } else {
            return !email.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    public static class LoginViewModelFactory implements ViewModelProvider.Factory {

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(LoginViewModel.class)) {
                return (T) new LoginViewModel();
            } else {
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }
    }
}