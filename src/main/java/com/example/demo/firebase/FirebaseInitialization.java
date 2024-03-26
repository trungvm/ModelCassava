package com.example.demo.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.servlet.ServletContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

@Configuration
public class FirebaseInitialization {
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        FileInputStream serviceAccount = null;
        String os = System.getProperty("os.name").toLowerCase();

        String filePath;
        if (os.contains("win")) {
            // Đường dẫn cho Windows
            filePath = "./serviceAccountKey.json";
        } else {
            // Đường dẫn cho Ubuntu hoặc các hệ điều hành Unix-like khác
            filePath = "/home/agribot/Downloads/serviceAccountKey.json";
        }
        System.out.println(filePath + "file hiện tại");
        serviceAccount = new FileInputStream(filePath);
            // serviceAccount = new FileInputStream("./serviceAccountKey.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://directionproject-1e798-default-rtdb.firebaseio.com")
                    .build();

           return FirebaseApp.initializeApp(options);
    }
}
