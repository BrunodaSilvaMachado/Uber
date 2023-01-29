package com.cursoandroid.uber.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.cursoandroid.uber.config.FirebaseConfig;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class DatabaseService<T extends DatabaseService.ModelId> {
    protected final HashMap<String, ValueEventListener> baseEventsById;
    private final DatabaseReference baseReference;
    @Nullable
    private final DatabaseReference uidReference;
    protected ValueEventListener uidEventListener;
    public DatabaseService() {
        baseReference = newReference();
        uidReference = newUidReference();
        uidEventListener = null;
        baseEventsById = new HashMap<>();
    }

    protected static String getCurrenteAuthId() {
        return FirebaseConfig.getFirebaseAuthUid();
    }

    @NonNull
    protected abstract DatabaseReference newReference();

    @Nullable
    protected abstract DatabaseReference newUidReference();

    public void start() {
        if (uidReference != null && uidEventListener != null) {
            uidReference.addValueEventListener(uidEventListener);
        }

        if (baseEventsById.size() > 0) {
            for (String id : baseEventsById.keySet()) {
                baseReference.child(id).addValueEventListener(Objects.requireNonNull(baseEventsById.get(id)));
            }
        }
    }

    public void stop() {
        if (uidReference != null && uidEventListener != null) {
            uidReference.removeEventListener(uidEventListener);
        }

        if (baseReference != null && baseEventsById.size() > 0) {
            for (String id : baseEventsById.keySet()) {
                baseReference.child(id).removeEventListener(Objects.requireNonNull(baseEventsById.get(id)));
            }
        }

    }

    public abstract void finish();

    public String UUID(){
        return baseReference.push().getKey();
    }
    public void save(@NonNull DatabaseReference reference, @NonNull T t) {
        reference.child(t.getId()).setValue(t);
    }

    public void update(@NonNull DatabaseReference reference, @NonNull String id, Map<String, Object> mapUser) {
        reference.child(id).updateChildren(mapUser);
    }

    public void delete(@NonNull DatabaseReference reference, @NonNull T t) {
        reference.child(t.getId()).removeValue();
    }

    public void setUidEventListener(ValueEventListener uidEventListener) {
        this.uidEventListener = uidEventListener;
    }

    public void setEventsById(@NonNull String id, ValueEventListener eventListener) {
        baseEventsById.put(id, eventListener);
    }

    public void singleTask(@NonNull String id, ValueEventListener eventListener) {
        baseReference.child(id).addListenerForSingleValueEvent(eventListener);
    }

    public interface ModelId {
        String getId();
    }
}
