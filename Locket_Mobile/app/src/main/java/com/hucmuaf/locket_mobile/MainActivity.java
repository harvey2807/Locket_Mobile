package com.hucmuaf.locket_mobile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hucmuaf.locket_mobile.model.User;

// import java.util.Arrays;
// import java.util.List;

 public class MainActivity extends AppCompatActivity {

     @SuppressLint("ClickableViewAccessibility")
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         EdgeToEdge.enable(this);
 //        setContentView(R.layout.activity_loading);
         ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
             Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
             v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
             return insets;
         });

         // Firebase reference
         FirebaseDatabase database = FirebaseDatabase.getInstance();
         DatabaseReference myRef = database.getReference();
         // Read from the database
         myRef.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot) {
                 // This method is called once with the initial value and again
                 // whenever data at this location is updated.
                 String value = dataSnapshot.getValue(String.class);
                 // xử lý dữ liệu
                 Toast.makeText(getBaseContext(), "Value is", Toast.LENGTH_SHORT).show();
             }

             @Override
             public void onCancelled(DatabaseError error) {
                 // Failed to read value
                 Toast.makeText(getBaseContext(), "Failed to read value", Toast.LENGTH_SHORT).show();
             }
         });
         DatabaseReference usersRef = myRef.child("users");
         User user1 = new User("r63qZbcbomhxRmQwkLwsTWu3IHB2", "tanluc", "Đặng Trần Tấn Lực", "dttanluc2004@gmail.com", "0987654321", "https://cellphones.com.vn/sforum/wp-content/uploads/2024/01/avartar-anime-6.jpg", "123456");
//         User user2 = new User("u2", "linhphan", "Phan Linh", "linh@gmail.com", "0909123456", "https://example.com/avatars/user456.jpg", "123");
         usersRef.child(user1.getUserId()).setValue(user1);
//         usersRef.child(user2.getUserId()).setValue(user2);

//         DatabaseReference friendsRef = myRef.child("friends");
//         long timestamp = System.currentTimeMillis();
////         Friend f1 = new Friend("u1", "u2", timestamp);
////         Friend f2 = new Friend("u2", "u1", timestamp);
//         friendsRef.child("u1").child("u2").setValue(f1);
//         friendsRef.child("u2").child("u1").setValue(f2);
//
//         DatabaseReference requestsRef = myRef.child("friendRequests");
//         FriendRequest req = new FriendRequest("u1", "u2", "pending", System.currentTimeMillis());
//         requestsRef.child("requestId").setValue(req);
//
//         DatabaseReference imagesRef = myRef.child("images");
//         List<String> receivers = Arrays.asList("u1", "u2");  // danh sách id bạn nhận ảnh
//         Image image = new Image("imageId", "https://example.com/photo1.jpg", "Buổi chiều 🌇", System.currentTimeMillis(), "u1", receivers);
//         imagesRef.child("imageId").setValue(image);
//
//         DatabaseReference messagesRef = myRef.child("messages").child("u1_u2");
//         Message message1 = new Message("msgId1", "u1", "u2", "Hello 😄", "imageId", System.currentTimeMillis());
//         Message message2 = new Message("msgId2", "u2", "u1", "hiii", null, System.currentTimeMillis());
//         messagesRef.child("msgId1").setValue(message1);
//         messagesRef.child("msgId2").setValue(message2);
//
//         DatabaseReference reactionsRef = myRef.child("reactions").child("imageId");
//         Reaction reaction = new Reaction("u2", "imageId", "❤️", System.currentTimeMillis());
//         reactionsRef.child("u2").setValue(reaction);
     }
}

