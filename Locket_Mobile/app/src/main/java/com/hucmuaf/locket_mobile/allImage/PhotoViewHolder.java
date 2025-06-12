package com.hucmuaf.locket_mobile.allImage;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hucmuaf.locket_mobile.R;

public class PhotoViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;

    public PhotoViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageView);
    }
}
