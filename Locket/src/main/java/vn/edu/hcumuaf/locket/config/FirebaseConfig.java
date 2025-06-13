package vn.edu.hcumuaf.locket.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.database.url:https://modis-8f5f6-default-rtdb.firebaseio.com}")
    private String databaseUrl;

    @Bean
    public FirebaseDatabase firebaseDatabase() throws IOException {
        try {
            // Load Firebase service account key
            InputStream serviceAccount = new ClassPathResource("modis-admin-keys.json").getInputStream();
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(databaseUrl)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            return FirebaseDatabase.getInstance();
        } catch (IOException e) {
            System.out.println("Warning: Could not load Firebase service account. Using default configuration.");
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setDatabaseUrl(databaseUrl)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            return FirebaseDatabase.getInstance();
        }
    }
} 