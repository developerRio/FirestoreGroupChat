package com.originalstocks.groupchatfirebase.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.originalstocks.groupchatfirebase.Adapters.ChatMessageRecyclerAdapter;
import com.originalstocks.groupchatfirebase.Models.ChatMessage;
import com.originalstocks.groupchatfirebase.Models.Chatroom;
import com.originalstocks.groupchatfirebase.Models.User;
import com.originalstocks.groupchatfirebase.R;
import com.originalstocks.groupchatfirebase.UserClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.originalstocks.groupchatfirebase.Activities.MainActivity.accessToken;

public class GroupChatActivity extends AppCompatActivity {
    private static final String TAG = "GroupChatActivity";

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ListenerRegistration mChatMessageEventListener, mUserListEventListener;
    private ChatMessageRecyclerAdapter mChatMessageRecyclerAdapter;
    private FirebaseFirestore mDb;
    private ArrayList<ChatMessage> mMessages = new ArrayList<>();
    private Set<String> mMessageIds = new HashSet<>();
    private ArrayList<User> mUserList = new ArrayList<>();
    private Chatroom mChatroom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        chatRecyclerView = findViewById(R.id.group_chat_recycler_view);
        messageEditText = findViewById(R.id.chat_message_view);
        sendButton = findViewById(R.id.chat_send_button);

        mDb = FirebaseFirestore.getInstance();

        User user = ((UserClient)(getApplicationContext())).getUser();
        if (user != null) {
            Log.d(TAG, "insertNewMessage: retrieved user client: " + user.toString());
        }else {
            Log.e(TAG, "onCreate_user is null, value = " + user);
        }

        getIncomingIntent();
        initChatroomRecyclerView();
        getChatroomUsers();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertNewMessage();
            }
        });

    }// onCreate closes

    private void getChatMessages(){

        CollectionReference messagesRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_chat_messages));

        mChatMessageEventListener = messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: Listen failed.", e);
                            return;
                        }

                        if(queryDocumentSnapshots != null){
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                                ChatMessage message = doc.toObject(ChatMessage.class);
                                if(!mMessageIds.contains(message.getMessage_id())){
                                    mMessageIds.add(message.getMessage_id());
                                    mMessages.add(message);
                                    chatRecyclerView.smoothScrollToPosition(mMessages.size() - 1);
                                }

                            }
                            mChatMessageRecyclerAdapter.notifyDataSetChanged();

                        }
                    }
                });
    }

    private void getChatroomUsers(){

        Log.i(TAG, "getChatroomUsers_mChatroom id = " + mChatroom.getChatroom_id());

        CollectionReference usersRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_chatroom_user_list));

        mUserListEventListener = usersRef
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: Listen failed.", e);
                            return;
                        }

                        if(queryDocumentSnapshots != null){

                            // Clear the list and add all the users again
                            mUserList.clear();
                            mUserList = new ArrayList<>();

                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                User user = doc.toObject(User.class);
                                mUserList.add(user);
                            }

                            Log.d(TAG, "onEvent: user list size: " + mUserList.size());
                        }
                    }
                });
    }

    private void initChatroomRecyclerView(){
        mChatMessageRecyclerAdapter = new ChatMessageRecyclerAdapter(mMessages, new ArrayList<User>(), this);
        chatRecyclerView.setAdapter(mChatMessageRecyclerAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    chatRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(mMessages.size() > 0){
                                chatRecyclerView.smoothScrollToPosition(
                                        chatRecyclerView.getAdapter().getItemCount() - 1);
                            }

                        }
                    }, 100);
                }
            }
        });

    }

    private void insertNewMessage(){
        String message = messageEditText.getText().toString();

        if(!message.equals("")){
            message = message.replaceAll(System.getProperty("line.separator"), "");

            DocumentReference newMessageDoc = mDb
                    .collection(getString(R.string.collection_chatrooms))
                    .document(mChatroom.getChatroom_id())
                    .collection(getString(R.string.collection_chat_messages))
                    .document();

            ChatMessage newChatMessage = new ChatMessage();
            newChatMessage.setMessage(message);
            newChatMessage.setMessage_id(newMessageDoc.getId());

            User user = ((UserClient)(getApplicationContext())).getUser();
            Log.d(TAG, "insertNewMessage: retrieved user client: " + user.toString());
            newChatMessage.setUser(user);

            newMessageDoc.set(newChatMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        clearMessage();
                    }else{
                        View parentLayout = findViewById(android.R.id.content);
                        Toast.makeText(GroupChatActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void clearMessage(){
        messageEditText.setText("");
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void getIncomingIntent(){
        if(getIntent().hasExtra(getString(R.string.intent_chatroom))){
            mChatroom = getIntent().getParcelableExtra(getString(R.string.intent_chatroom));
            joinChatroom();
        }
    }

    private void leaveChatroom(){

        DocumentReference joinChatroomRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_chatroom_user_list))
                .document(accessToken);

        joinChatroomRef.delete();
    }

    private void joinChatroom(){

        DocumentReference joinChatroomRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mChatroom.getChatroom_id())
                .collection(getString(R.string.collection_chatroom_user_list))
                .document(accessToken);

        User user = ((UserClient)(getApplicationContext())).getUser();

        if (user != null){
            Log.i(TAG, "joinChatroom: User_id " + user.getUser_id());
            Log.d(TAG, "joinChatroom: user: " + user.getEmail());
            joinChatroomRef.set(user); // Don't care about listening for completion.
        }else {
            Log.e(TAG, "joinChatroom: user is null" + user);
        }

    }

    private void setChatroomName(){
        getSupportActionBar().setTitle(mChatroom.getTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getChatMessages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mChatMessageEventListener != null){
            mChatMessageEventListener.remove();
        }
        if(mUserListEventListener != null){
            mUserListEventListener.remove();
        }
    }



}
