package ar.edu.ips.aus.seminario2.buscaminas.util;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FirebaseUtil {
    public static FirebaseDatabase mFirebaseDatabase;

    private static FirebaseUtil firebaseUtil;

    private FirebaseUtil() {
    }

    public static DatabaseReference openFbReference(String ref) {
        if (firebaseUtil == null){
            firebaseUtil = new FirebaseUtil();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
        }
        return mFirebaseDatabase.getReference().child(ref);
    }
}