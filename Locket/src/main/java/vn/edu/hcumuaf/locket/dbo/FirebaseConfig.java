package vn.edu.hcumuaf.locket.dbo;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class FirebaseConfig {
    @Bean
    public FirebaseDatabase firebaseDatabase() throws IOException {
        System.out.println("Firebase database intializing");
        InputStream serviceAccount = new ClassPathResource("firebase/modis-admin-keys.json").getInputStream();
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://modis-8f5f6-default-rtdb.firebaseio.com")
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
            System.out.println("Firebase app initialized successfully");
        }else{
            System.out.println("Firebase app initialization failed");
        }

        return FirebaseDatabase.getInstance();
    }

    // thực hiện các tác vụ bất đồng bộ (asynchronous)
    //Không chặn luồng hoạt động chính của app
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        return Executors.newCachedThreadPool();
    }

    public static void main(String[] args) throws IOException {
        FirebaseConfig  firebaseConfig = new FirebaseConfig();
        System.out.println(firebaseConfig.firebaseDatabase());
    }
}
