package com.hucmuaf.locket_mobile.holder;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hucmuaf.locket_mobile.R;

public class ItemFriendReactHolder extends RecyclerView.ViewHolder {
    ImageView ivAvt;

    @SuppressLint("WrongViewCast")
    public ItemFriendReactHolder(@NonNull View itemView) {
        super(itemView);
        ivAvt = itemView.findViewById(R.id.accAvatar);
    }

    public ImageView getIvAvt() {
        return ivAvt;
    }

    public void setIvAvt(ImageView ivAvt) {
        this.ivAvt = ivAvt;
    }

}
