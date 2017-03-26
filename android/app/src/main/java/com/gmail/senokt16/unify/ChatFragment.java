package com.gmail.senokt16.unify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;


@SuppressWarnings("VisibleForTests")
public class ChatFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String CHAT_ID = "chat_id";
    private static final int PICK_IMAGE = 12345;

    private String chatId;

    private RecyclerView chatList;
    private Activity activity;
    private Toolbar toolbar, toolbar2;
    private ImageView chatArrow;
    private FrameLayout bottomSheet;
    private BottomSheetBehavior behavior;

    private FrameLayout addImage;
    private TextView messageText;
    private FloatingActionButton messageSend;

    InputStream uploadStream;

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
        toolbar2 = (Toolbar) view.findViewById(R.id.toolbar2);
        chatArrow = (ImageView) view.findViewById(R.id.chat_arrow);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        bottomSheet = (FrameLayout) view.findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setPeekHeight(dp(56));
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING
                        || newState == BottomSheetBehavior.STATE_SETTLING) {
                    View v = getActivity().getCurrentFocus();
                    if (v != null) {
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                toolbar2.setAlpha(new FastOutSlowInInterpolator().getInterpolation(
                        (1-(slideOffset)) < 0 ? 0 : (1-(slideOffset))
                ));
                chatArrow.setRotation((slideOffset * -720) < -180 ? -180 : (slideOffset * -720));
            }
        });

        chatList.setLayoutManager(new LinearLayoutManager(getContext()));
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("messages");
        chatList.setAdapter(new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(Chat.class, R.layout.fragment_chat_message, ChatViewHolder.class, ref) {
            @Override
            protected void populateViewHolder(ChatViewHolder viewHolder, Chat model, int position) {
                Log.d("ChatFragment", "Populating viewHolder for \"" + model.text + "\"");
                viewHolder.populateViewHolder(model, position, getActivity());
            }
        });

        addImage = (FrameLayout) view.findViewById(R.id.message_add_image);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
/*                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image*//*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image*//*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});*/

                Intent pickIntent = new Intent();
                pickIntent.setType("image/*");
                pickIntent.setAction(Intent.ACTION_GET_CONTENT);

                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                String pickTitle = "Select or take a new Picture"; // Or get from strings.xml
                Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                                new Intent[] { takePhotoIntent });

                startActivityForResult(chooserIntent, PICK_IMAGE);
            }
        });
        messageText = (TextView) view.findViewById(R.id.message_text);
        messageSend = (FloatingActionButton) view.findViewById(R.id.message_send);
        messageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (messageText.getText().length() > 0) {
                    messageSend.setEnabled(false);
                    final Chat c = new Chat();
                    c.photoUrl = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
                    c.text = messageText.getText().toString();
                    c.name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                    c.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("messages").push();

                    final DatabaseReference.CompletionListener listener = new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            messageSend.setEnabled(true);
                            if (databaseError != null) {
                                Log.e("ChatFragment", "Message send failed.", databaseError.toException());
                            } else {
                                messageText.setText("");
                                chatList.smoothScrollToPosition(chatList.getAdapter().getItemCount());
                            }
                        }
                    };

                    if (uploadStream == null) {
                        ref.setValue(c, listener);
                    } else {
                        FirebaseStorage.getInstance().getReference().child(c.uid).child(ref.getKey())
                                .putStream(uploadStream).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                c.imageUrl = taskSnapshot.getDownloadUrl().toString();
                                ref.setValue(c, listener);
                            }
                        });
                    }
                }
            }
        });

        return view;
    }

    private int dp(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.chat_rooms, menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                Toast.makeText(getContext(), "Image not received.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                uploadStream = getContext().getContentResolver().openInputStream(data.getData());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
