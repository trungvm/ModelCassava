package com.example.demo.service;

import com.example.demo.entity.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class UserService {
    private static final String COLLECTION_NAME = "users";
    public String creatAcount(User user) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> clWriteResultApiFuture = dbFirestore.collection(COLLECTION_NAME).document(user.getEmail()).set(user);
        return clWriteResultApiFuture.get().getUpdateTime().toString();
    }
    public User getAcount(String name) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document(name);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot documentSnapshot = future.get();
        User user = null;
        if(documentSnapshot.exists()) {
            user = documentSnapshot.toObject(User.class);
            return user;
        } else {
            return null;
        }
    }
    public User getAccountByName(String name) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        CollectionReference collectionReference = dbFirestore.collection(COLLECTION_NAME);
        Query query = collectionReference.whereEqualTo("email", name).limit(1);

        try {
            QuerySnapshot querySnapshot = query.get().get();
            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                User user = documentSnapshot.toObject(User.class);
                return user;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(); // Xem xét log để kiểm tra lỗi cụ thể
        }

        return null;
    }

}
