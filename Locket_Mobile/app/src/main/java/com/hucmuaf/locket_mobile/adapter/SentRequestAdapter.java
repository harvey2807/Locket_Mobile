package com.hucmuaf.locket_mobile.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.holder.SentRequestViewHolder;
import com.hucmuaf.locket_mobile.inteface.OnSentRequestActionListener;
import com.hucmuaf.locket_mobile.model.FriendRequest;

import java.util.List;

public class SentRequestAdapter extends RecyclerView.Adapter<SentRequestViewHolder> {

    private List<FriendRequest> sentRequests;
    private final Context context;
    private final OnSentRequestActionListener listener;
    public SentRequestAdapter(Context context, List<FriendRequest> sentRequests, OnSentRequestActionListener listener) {
        this.context = context;
        this.sentRequests = sentRequests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SentRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sent_request, parent, false);
        return new SentRequestViewHolder(view, context, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull SentRequestViewHolder holder, int position) {
        if (canSafelyBind(position)) {
            FriendRequest request = sentRequests.get(position);
            if (request != null) {
                holder.bind(request);
            }
        }
    }

    @Override
    public int getItemCount() {
        return sentRequests != null ? sentRequests.size() : 0;
    }

    private boolean canSafelyBind(int position) {
        return sentRequests != null && 
               position >= 0 && 
               position < sentRequests.size() && 
               context != null;
    }
}