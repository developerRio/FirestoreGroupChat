package com.originalstocks.groupchatfirebase.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.originalstocks.groupchatfirebase.Models.Chatroom;
import com.originalstocks.groupchatfirebase.Models.User;
import com.originalstocks.groupchatfirebase.R;
import com.originalstocks.groupchatfirebase.UserClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button startChatButton;
    private DatabaseReference mRootReference;
    private ListenerRegistration mChatroomEventListener;
    private FirebaseFirestore mDb;
    private Set<String> mChatroomIds = new HashSet<>();
    private ArrayList<Chatroom> mChatrooms = new ArrayList<>();
    private String email;
    public static String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDb = FirebaseFirestore.getInstance();
        email = "Shaw@gmail.com"; // static user name so u can change different emails or put a dynamic value instead
        accessToken = email + "123";

        startChatButton = findViewById(R.id.start_group_chat_button);
        mRootReference = FirebaseDatabase.getInstance().getReference().child("GroupChat");

        settingUpNewUser();

        startChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // creating new chat room called..........
                buildNewChatroom("WE_ARE_LEGENDS");
            }
        });

    }// onCreate closes

    @Override
    protected void onResume() {
        super.onResume();
        getChatrooms();
    }

    private void settingUpNewUser(){
        final User user = new User();
        user.setEmail(email);
        user.setUsername(email.substring(0, email.indexOf("@")));
        user.setUser_id(accessToken);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mDb.setFirestoreSettings(settings);

        DocumentReference newUserRef = mDb
                .collection(getString(R.string.collection_users))
                .document(accessToken);

        newUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: successfully set the user client.");
                   // User user = task.getResult().toObject(User.class);

                    Log.i(TAG, "onComplete_user creds : " + user.getUsername() + " " + user.getUser_id());

                    ((UserClient)(getApplicationContext())).setUser(user);
                    Toast.makeText(MainActivity.this, "User Added : " + user.getUsername(), Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(MainActivity.this, "User already exist !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void buildNewChatroom(String chatroomName) {

        final Chatroom chatroom = new Chatroom();
        chatroom.setTitle(chatroomName);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mDb.setFirestoreSettings(settings);

        DocumentReference newChatroomRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document();

        chatroom.setChatroom_id("chat_database_firestore");

        newChatroomRef.set(chatroom).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    navChatroomActivity(chatroom);
                } else {
                    Toast.makeText(MainActivity.this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navChatroomActivity(Chatroom chatroom) {
        Intent intent = new Intent(MainActivity.this, GroupChatActivity.class);
        intent.putExtra(getString(R.string.intent_chatroom), chatroom);
        startActivity(intent);
    }

    private void getChatrooms() {

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mDb.setFirestoreSettings(settings);

        CollectionReference chatroomsCollection = mDb
                .collection(getString(R.string.collection_chatrooms));

        mChatroomEventListener = chatroomsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Log.d(TAG, "onEvent: called.");

                if (e != null) {
                    Log.e(TAG, "onEvent: Listen failed.", e);
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        Chatroom chatroom = doc.toObject(Chatroom.class);
                        if (!mChatroomIds.contains(chatroom.getChatroom_id())) {
                            mChatroomIds.add(chatroom.getChatroom_id());
                            mChatrooms.add(chatroom);
                        }
                    }
                    Log.d(TAG, "onEvent: number of chatrooms: " + mChatrooms.size());
                }

            }
        });
    }


}
