package com.hucmuaf.locket_mobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.model.User;

import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder> {
    
    private List<User> suggestionsList;
    private OnSuggestionActionListener listener;

    public interface OnSuggestionActionListener {
        void onAddFriend(User user);
    }

    public SuggestionAdapter(List<User> suggestionsList, OnSuggestionActionListener listener) {
        this.suggestionsList = suggestionsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        User suggestion = suggestionsList.get(position);
        holder.bind(suggestion);
    }

    @Override
    public int getItemCount() {
        return suggestionsList.size();
    }

    public class SuggestionViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatar;
        private TextView name;
        private Button addButton;

        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.suggestionAvatar);
            name = itemView.findViewById(R.id.suggestionName);
            addButton = itemView.findViewById(R.id.btnAdd);
        }

        public void bind(User suggestion) {
            name.setText(suggestion.getFullName() != null ? suggestion.getFullName() : suggestion.getUserName());
            
            // Load avatar image using Glide
            if (suggestion.getUrlAvatar() != null && !suggestion.getUrlAvatar().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(suggestion.getUrlAvatar())
                        .transform(new CircleCrop())
                        .placeholder(R.drawable.avt)
                        .error(R.drawable.avt)
                        .into(avatar);
            } else {
                // Set default avatar
                avatar.setImageResource(R.drawable.avt);
            }
            
            addButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddFriend(suggestion);
                }
            });
        }
    }
} 