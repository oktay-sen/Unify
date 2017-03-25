package com.gmail.senokt16.unify;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class ChatFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String CHAT_ID = "chat_id";

    private String chatId;

    private RecyclerView chatList;
    private Activity activity;
    private Toolbar toolbar;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(String chatId) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(CHAT_ID, chatId);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.app_bar_chat_messages, container, false);

        if (getArguments() != null)
            chatId = getArguments().getString(CHAT_ID);
        else
            chatId = "chatId"; //TODO: Find the last chat user was looking at.
        chatList = (RecyclerView) view.findViewById(R.id.chat_list);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        chatList.setLayoutManager(new LinearLayoutManager(getContext()));
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("messages");
        chatList.setAdapter(new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(Chat.class, R.layout.fragment_chat_message, ChatViewHolder.class, ref) {
            @Override
            protected void populateViewHolder(ChatViewHolder viewHolder, Chat model, int position) {
                Log.d("ChatFragment", "Populating viewHolder for \"" + model.text + "\"");
                viewHolder.populateViewHolder(model, position, getActivity());
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.chat_rooms, menu);
    }
}
