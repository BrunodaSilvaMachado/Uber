package com.cursoandroid.uber.helper;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.cursoandroid.uber.activity.PassengerActivity;
import com.cursoandroid.uber.activity.RequisicoesActivity;
import com.cursoandroid.uber.config.FirebaseConfig;
import com.cursoandroid.uber.model.Usuario;
import com.cursoandroid.uber.service.UsuarioService;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;

public class RedirecionaUsuario {
    public static void updateUi(Activity activity){
        FirebaseUser user = FirebaseConfig.getFirebaseAuth().getCurrentUser();
        UsuarioService usuarioService = new UsuarioService();
        if (user != null) {
            usuarioService.currentUserSingleTask( new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Usuario u = snapshot.getValue(Usuario.class);
                    if(u!= null){
                        if(u.getTipo().equals("P")){
                            activity.startActivity(new Intent(activity, PassengerActivity.class));
                            activity.finish();
                        }else{
                            activity.startActivity(new Intent(activity, RequisicoesActivity.class));
                            activity.finish();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }
    }
}
