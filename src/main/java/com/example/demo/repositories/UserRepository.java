package com.example.demo.repositories;

import com.example.demo.entity.User;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

import java.util.concurrent.ExecutionException;

public class UserRepository {
    private static final String COLLECTION_NAME = "users";
    Firestore dbFirestore = FirestoreClient.getFirestore();
    CollectionReference collectionReference = dbFirestore.collection(COLLECTION_NAME);
    public User getAccountByName(String name) {
        Query query = collectionReference.whereEqualTo("email", name).limit(1);
        try {
            QuerySnapshot querySnapshot = query.get().get();
            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                User user = documentSnapshot.toObject(User.class);
                return user;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
