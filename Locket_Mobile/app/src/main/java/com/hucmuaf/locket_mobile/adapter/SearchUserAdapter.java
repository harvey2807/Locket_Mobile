package com.hucmuaf.locket_mobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.holder.SearchUserViewHolder;
import com.hucmuaf.locket_mobile.model.User;
import com.hucmuaf.locket_mobile.inteface.OnAddFriendClickListener;

import java.util.List;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserViewHolder> {
    
    private List<User> userList;
    private Context context;
    private OnAddFriendClickListener listener;

    public SearchUserAdapter(Context context, List<User> userList, OnAddFriendClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_user, parent, false);
        return new SearchUserViewHolder(view, context, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchUserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

}