package com.cursoandroid.uber.activity;

import android.util.Patterns;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.cursoandroid.uber.R;


public class CadastroViewModel extends ViewModel {
    private final MutableLiveData<FormState> cadastroFormState = new MutableLiveData<>();

    CadastroViewModel() {
    }

    LiveData<FormState> getCadastroFormState() {
        return cadastroFormState;
    }

    public void cadastroDataChanged(String username, String email, String password) {
        if (!isUserNameValid(username)) {
            cadastroFormState.setValue(new FormState(R.string.invalid_username,null, null));
        } else if (!isEmailValid(email)){
            cadastroFormState.setValue(new FormState(null, R.string.invalid_email, null));
        } else if (!isPasswordValid(password)) {
            cadastroFormState.setValue(new FormState(null, null,  R.string.invalid_password));
        } else {
            cadastroFormState.setValue(new FormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        return username != null && username.trim().length() > 0;
    }

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

    final static class CadastroViewModelFactory implements ViewModelProvider.Factory {

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CadastroViewModel.class)) {
                return (T) new CadastroViewModel();
            } else {
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }
    }
}
