package com.example.chattingdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ChatRoomMessage extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ChatRoomMessage";
    private ProgressBar progressbar;
    private RecyclerView recyclerView;
    //private RecyclerView.LayoutManager layoutManager;
    private LinearLayoutManager layoutManager;
    private MessageThreadAdapter adapter;
    private ArrayList<Message> messages;
    private String chat_id;
    private String message_state;
    private ImageButton buttonSend;
    private ImageButton buttonUpload;
    private EditText editTextMessage;
    private User user;
    private boolean isReceiverRegistered;
    private boolean isScrolling;
    private int LoadStart = 0;
    private int LoadEnd = 20;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    private int TotalItems;
    private int currentVisible;
    private int scrolledOutItems = 0;

    protected BroadcastReceiver RegistrationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String action = intent.getAction();
                    if (action != null && action.equals(Utils.NOTIFICATION_CHAT)) {
                        String message = intent.getStringExtra("message");
                        String meta = intent.getStringExtra("meta");
                        String isImage = intent.getStringExtra("image");
                        String messageType = (isImage == null ? "text" : "image");
                        Log.e("NOTIFICATION_CHAT", message);
                        Log.e("NOTIFICATION_TYPE", messageType);

                        if (meta != null) {
                            try {
                                JSONObject info = new JSONObject(meta);
                                String bucket_id = info.getString("bucket_id");
                                String sender_id = info.getString("sender_id");
                                String sender_name = info.getString("sender_name");
                                messageType = info.getString("chat_type");
                                String row = info.getString("row");
                                if (bucket_id.equals(chat_id)) {
                                    processMessage(sender_name, message, sender_id, messageType, row);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.e(TAG, "RegistrationBroadcastReceiver meta " + meta);
                        } else {
                            processMessage(user.getName(), message, "SLApO64gAktgmuLl", messageType, "NAN");
                        }
                    } else {
                        Log.e(TAG, "RegistrationBroadcastReceiver Confirmed action = " + action);
                    }
                }
            });
        }
    };

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_message);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        progressbar = findViewById(R.id.progressBar);
        progressbar.getIndeterminateDrawable().setColorFilter(this.getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
        user = new User(getApplicationContext());

//        Intent intent = getIntent();
//        chat_id = intent.getStringExtra("message_id");
//        message_state = intent.getStringExtra("message_state");
//        MainActivity.currentChatBucketId = chat_id;

        //Initializing recyclerview
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        layoutManager.setStackFromEnd(true);

        adapter = new MessageThreadAdapter(this, new ArrayList<Message>(), "");
        recyclerView.setAdapter(adapter);

        messages = new ArrayList<>();

        loadMessages(LoadStart, LoadEnd, "bottom");

        //Calling function to fetch the existing messages on the thread
//        if(message_state.equals("STARTER")){
//            setTitle(intent.getStringExtra("business_name"));
//            processMessage(
//                    user.getName(),
//                    intent.getStringExtra("message"),
//                    user.getKey(),
//                    intent.getStringExtra("type"),
//                    null
//            );
//            progressbar.setVisibility(View.GONE);
//        }else {
//            loadMessages(LoadStart, LoadEnd, "bottom");
//        }

        //initializing button and edittext
        buttonSend = findViewById(R.id.buttonSend);
        buttonUpload = findViewById(R.id.buttonUpload);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0 || start > 0) {
                    buttonUpload.setVisibility(View.GONE);
                    buttonSend.setVisibility(View.VISIBLE);
                } else {
                    buttonUpload.setVisibility(View.VISIBLE);
                    buttonSend.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        recyclerView.addOnScrollListener(new PaginationListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
//                currentPage++;
                loadMessages(LoadStart, LoadEnd, "top");
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
//                    Log.d(TAG, "Hello I am scrolling screen ");
//                    isScrolling = true;
//                }
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                currentVisible = layoutManager.getChildCount();
//                TotalItems = layoutManager.getItemCount();
//                //scrolledOutItems = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
//                scrolledOutItems = layoutManager.findFirstVisibleItemPosition();
//                int check = TotalItems - currentVisible + scrolledOutItems;
//                Log.d(TAG, "Current Visible = " + currentVisible + " Total = " + TotalItems + " Scrolled Out Items = " + scrolledOutItems + " Check = " + check);
//                Log.d(TAG, "currentVisible + scrolledOutItems = " + currentVisible + scrolledOutItems);
//                Log.d(TAG, "LoadStart = " + LoadStart + " LoadEnd = " + LoadEnd);
//
//                //if (isScrolling && TotalItems == currentVisible + scrolledOutItems ){
//                if (isScrolling && TotalItems >= 20 && TotalItems >= LoadStart && scrolledOutItems == 2) {
//                    Log.d(TAG, "Fetch Data Now ");
//                    loadMessages(LoadStart, LoadEnd, "top");
//                    isScrolling = false;
//                }
//            }
//        });

//        LocalBroadcastManager.getInstance(this).registerReceiver(RegistrationBroadcastReceiver, new IntentFilter(Utils.NOTIFICATION_CHAT));
        isReceiverRegistered = true;
        //if (Utils.checkPlayServices(this, this)){ }
    }

    //This method will fetch all the messages of the thread
    private void loadMessages(int start, int end, final String position) {

        final ArrayList<Message> list = new ArrayList<>();

        Log.d("beant", "start=" + start + " end=" + end);

        RequestCache stringLoad = new RequestCache(Request.Method.GET,
                Utils.API_FETCH_CHAT + "?chat_message_id=" +
                        "SLApO64gAktgmuLl_sDDHxu8xIPFV9Nh19a4h&" +
                        "chat_message_user_id=SLApO64gAktgmuLl" +
                        "&chat_from=buyer&start=" + start + "&end=" + end,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        isLoading = false;
                        progressbar.setVisibility(View.GONE);
                        try {
                            Log.e("response", response);
                            JSONObject res = new JSONObject(response);
                            if (res.getInt("status") == 200) {
                                setTitle(res.getString("shop"));
                                if (res.getInt("messageStatus") == 200) {
                                    int LoadTotal = res.getInt("count");
                                    LoadStart = (LoadStart + LoadTotal);
                                    // LoadStart = ((LoadTotal >= LoadEnd) ? LoadEnd : (LoadStart + LoadTotal));
                                    LoadEnd = (LoadEnd + LoadTotal);
                                    Log.e(TAG, "New start" + LoadStart + " New End " + LoadEnd);
                                    JSONArray thread = res.getJSONArray("messages");
                                    for (int i = 0; i < thread.length(); i++) {
                                        JSONObject obj = thread.getJSONObject(i);
                                        Message chatObject = new Message(
                                                obj.getString("sender"),
                                                obj.getString("message"),
                                                obj.getString("timestamp"),
                                                obj.getString("name"),
                                                obj.getString("type"),
                                                obj.getString("id")
                                        );
                                        list.add(chatObject);
                                    }
                                    if (position.equals("bottom")) {
                                        adapter.addItemsBottom(list);
                                        scrollToBottom();
                                    } else {
//                                        Collections.reverse(list);
                                        adapter.addItemsTop(list);
                                        layoutManager.scrollToPositionWithOffset(LoadTotal - 1, 10);
                                        //scrollToTop();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressbar.setVisibility(View.GONE);
                    }
                });
        Log.e(TAG, stringLoad.getUrl());
        AppController.getInstance(this).addToRequestQueue(stringLoad);
    }


    //This method will send the new message to the thread
    private void sendMessage() {
        final String message = editTextMessage.getText().toString().trim();
        if (message.equalsIgnoreCase("")) {
            return;
        }

        processMessage(user.getName(), message, "SLApO64gAktgmuLl", "text", null);
        editTextMessage.setText("");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Utils.API_PUT_CHAT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "message sent " + response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "message sent error " + error.getLocalizedMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", "SLApO64gAktgmuLl");
                params.put("chat_message_id", "SLApO64gAktgmuLl_sDDHxu8xIPFV9Nh19a4h");
                params.put("message", message);
                params.put("name", "abc");
                params.put("SendMessageChat", "true");
                params.put("type", "text");
                params.put("from", "buyer");
                return params;
            }
        };

        //Disabling retry to prevent duplicate messages
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        stringRequest.setRetryPolicy(policy);
        AppController.getInstance(this).addToRequestQueue(stringRequest);
    }

    //Processing message to add on the thread
    private void processMessage(String name, String message, String id, String type, String row) {
        Message m = new Message(id, message, TimeAgo.getTimeStamp(), name, type, row);
        List<Message> list = new ArrayList<>();
        list.add(m);
        adapter.addItemsBottom(list);
        //messages.add(m);
        scrollToBottom();
    }

    private void scrollToBottom() {
//        if (adapter == null) {
//            adapter = new MessageThreadAdapter(ChatRoomMessage.this, messages, "SLApO64gAktgmuLl");
//            recyclerView.setAdapter(adapter);
//        }
        if (adapter.getItemCount() > 1) {
            Objects.requireNonNull(recyclerView.getLayoutManager()).smoothScrollToPosition(recyclerView, null, adapter.getItemCount() - 1);
        }
        adapter.notifyDataSetChanged();
    }

    private void scrollToTop() {
//        if (adapter == null) {
//            adapter = new MessageThreadAdapter(ChatRoomMessage.this, messages, "SLApO64gAktgmuLl");
//        }
//        recyclerView.setAdapter(adapter);
        adapter.notifyItemRangeChanged(0, messages.size() - 1);
        //adapter.notifyItemRangeInserted(0, messages.size() - 1);
    }

    //Registering broadcast receivers
    @Override
    protected void onResume() {
        super.onResume();
//        MainActivity.currentChatBucketId = chat_id;
//        if (!isReceiverRegistered) {
//            LocalBroadcastManager.getInstance(this).registerReceiver(RegistrationBroadcastReceiver, new IntentFilter(Utils.NOTIFICATION_CHAT));
//        }
    }

    //Unregistering receivers
    @Override
    protected void onPause() {
        super.onPause();
//        MainActivity.currentChatBucketId = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        MainActivity.currentChatBucketId = null;
//        if (isReceiverRegistered) {
//            LocalBroadcastManager.getInstance(this).unregisterReceiver(RegistrationBroadcastReceiver);
//            isReceiverRegistered = false;
//        }
    }

    @Override
    public void onClick(View view) {
        if (view == buttonSend) {
            sendMessage();
        } else if (view == buttonUpload) {
//            Intent fileChooser = new Intent(ChatRoomMessage.this, imageEditClass.class);
//            fileChooser.putExtra("UploadUri", Utils.API_PUT_CHAT);
//            fileChooser.putExtra("UploadType", Utils.NOTIFY_SELF_CHAT_UPLOAD);
//            fileChooser.putExtra("messageBucket", chat_id);
//            startActivityForResult(fileChooser, Utils.ACTIVITY_REQUEST_CODE);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.e(TAG, String.valueOf(item.getItemId()));
        if (item.getItemId() == android.R.id.home) {
            if (isReceiverRegistered) {
//                LocalBroadcastManager.getInstance(this).unregisterReceiver(RegistrationBroadcastReceiver);
                isReceiverRegistered = false;
            }
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
