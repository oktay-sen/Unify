package com.gmail.senokt16.unify;

import android.app.Activity;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;

public class ChatViewHolder extends RecyclerView.ViewHolder {
    TextView sender, text;
    RoundedImageView image, profile;
    CardView container;
    public ChatViewHolder(View itemView) {
        super(itemView);
        sender = (TextView) itemView.findViewById(R.id.chat_username);
        text = (TextView) itemView.findViewById(R.id.chat_message);
        image = (RoundedImageView) itemView.findViewById(R.id.chat_image);
        container = (CardView) itemView.findViewById(R.id.chat_container);
        profile = (RoundedImageView) itemView.findViewById(R.id.chat_profile);
    }

    public void populateViewHolder(Chat chat, int position, Activity activity) {
        text.setText(chat.text);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getUid().equals(chat.uid)) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) container.getLayoutParams();
            layoutParams.gravity = Gravity.END;
            container.setLayoutParams(layoutParams);
            sender.setVisibility(View.GONE);
            profile.setVisibility(View.GONE);
        } else {
            sender.setText(chat.name);
            if (chat.photoUrl != null && chat.photoUrl.length() > 0) {
                Glide.with(activity)
                        .fromUri()
                        .asBitmap()
                        .load(Uri.parse(chat.photoUrl))
                        .into(profile);
            }
        }
        if (chat.imageUrl == null || chat.imageUrl.length() == 0) {
            image.setVisibility(View.GONE);
        } else {
            Log.d("StorageRef", chat.imageUrl);
            StorageReference ref;
            try {
                ref = FirebaseStorage.getInstance().getReferenceFromUrl(chat.imageUrl);
                Glide.with(activity)
                        .using(new FirebaseImageLoader())
                        .load(ref)
                        .asBitmap()
                        .into(image);
            } catch (Exception e) {
                Log.v("StorageRef", "StorageRef is invalid, skipping image loading.");
            }
            if (chat.text != null && chat.text.length() > 0) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) text.getLayoutParams();
                layoutParams.removeRule(RelativeLayout.BELOW);
                layoutParams.addRule(RelativeLayout.BELOW, R.id.chat_image);
                text.setLayoutParams(layoutParams);
            } else {
                text.setVisibility(View.GONE);
            }
        }
    }
}
