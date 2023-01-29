package com.cursoandroid.uber.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.cursoandroid.uber.config.FirebaseConfig;
import com.cursoandroid.uber.model.Requisicao;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RequisicaoService extends DatabaseService<Requisicao> {

    private static DatabaseReference requisicaoRef = null;

    public RequisicaoService(){

    }
    @NonNull
    @NotNull
    @Override
    protected DatabaseReference newReference() {
        if (requisicaoRef == null) {
            requisicaoRef = FirebaseConfig.getDatabaseReference().child("requisicoes");
        }
        return requisicaoRef;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    protected DatabaseReference newUidReference() {
        return null;
    }

    @Override
    public void finish() {
        requisicaoRef = null;
    }

    public void save(Requisicao requisicao){
        super.save(requisicaoRef, requisicao);
    }

   public void update(@NonNull String id, Map<String, Object> mapUser){
        super.update(requisicaoRef, id, mapUser);
   }

   public void update(@NonNull String id, @NonNull String fieldName, Object value){
       Map<String, Object> mapRequisicao = new HashMap<>();
       mapRequisicao.put(fieldName, value);
       this.update(id, mapRequisicao);
   }

   public void updateLocation(@NonNull String id, @NonNull String fieldName, String lat, String lng){
        DatabaseReference ref = requisicaoRef.child(id);
       Map<String, Object> mapRequisicao = new HashMap<>();
       mapRequisicao.put("latitude", lat);
       mapRequisicao.put("longitude", lng);
       super.update(ref, fieldName, mapRequisicao);
   }

    public Query findUserById(String uid, @NonNull String path){
        return requisicaoRef.orderByChild(path)
                .equalTo(uid);
    }

    public void findStatus(String status, ValueEventListener eventListener){
        Query query = requisicaoRef.orderByChild("status")
                .equalTo(status);
        query.addValueEventListener(eventListener);
    }
}
