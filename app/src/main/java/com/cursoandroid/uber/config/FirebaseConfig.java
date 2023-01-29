package com.cursoandroid.uber.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseConfig {
    private static FirebaseAuth firebaseAuth = null;
    private static DatabaseReference databaseReference = null;
    private static StorageReference storageReference = null;

    public static FirebaseAuth getFirebaseAuth() {
        if (firebaseAuth == null){
            firebaseAuth = FirebaseAuth.getInstance();
        }
        return firebaseAuth;
    }

    @androidx.annotation.Nullable
    public static String getFirebaseAuthUid(){
        FirebaseAuth auth = getFirebaseAuth();
        return (auth!= null)? auth.getUid(): null;
    }

    public static DatabaseReference getDatabaseReference(){
        if (databaseReference == null){
            databaseReference = FirebaseDatabase.getInstance().getReference();
        }
        return databaseReference;
    }

    public static StorageReference getStorageReference() {
        if (storageReference == null) {
            storageReference = FirebaseStorage.getInstance().getReference();
        }
        return storageReference;
    }
}
