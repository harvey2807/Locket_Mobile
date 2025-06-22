package com.hucmuaf.locket_mobile.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseService {
    // This class can be used to handle Firebase-related operations such as authentication, database access, etc.
    // Currently, it is empty and can be expanded as needed.

    private static FirebaseService instance;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseFirestore firestore;

    public FirebaseService() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public static FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseDatabase getDatabase() {
        return database;
    }
}
