
package com.hucmuaf.locket_mobile.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.hucmuaf.locket_mobile.R;

import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.holder.PhotoViewHolder;
import com.hucmuaf.locket_mobile.modedb.Image;
import com.hucmuaf.locket_mobile.modedb.User;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder> {
    private Context context;
    private List<Image> listImage; // Nếu dùng ảnh local (drawable)
    private List<User> listUsers;
    private Map<String, User> map;

    public PhotoAdapter(Context context, List<Image> listImage, List<User> users) {
        this.context = context;
        this.listImage = listImage;
        this.listUsers = users;
        map = new HashMap<>();
//        mapImageToUser(this.listUsers, this.listImage);
    }

    public void mapImageToUser(List<User> users, List<Image> images){
        for (int i = 0; i< images.size(); i++){
            String sendUserID = images.get(i).getSenderId();
            for(int j =0; j< users.size(); j++){
                if(sendUserID.equals(users.get(j).getUserId())){
                    map.put(sendUserID, users.get(j));
                }
            }
        }
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_react, parent, false);
        return new PhotoViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Log.e("Map", map.toString());
        Image item = listImage.get(position);
        String url = item.getUrlImage();
        String caption = item.getCaption();
        String avatar = "";
        String accName = item.getSenderId();
        String timestamp = getTimeDifferenceFromNow(item.getTimestamp());

        // Load ảnh từ URL bằng Glide
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.default_img) // ảnh tạm khi đang load
                .error(R.drawable.default_img) // ảnh khi load lỗi
                .into(holder.getImageView());
        Glide.with(context)
                .load(avatar)
                .placeholder(R.drawable.avt) // ảnh tạm khi đang load
                .error(R.drawable.avt) // ảnh khi load lỗi
                .into(holder.getAvatarImage());
        holder.getCaptionText().setText(caption);
        holder.getAvatarImage().setImageResource(R.drawable.default_img);
        holder.getAccNameText().setText(accName);
        holder.getTimestampText().setText(timestamp);
   }

    public static String getTimeDifferenceFromNow(long pastTime) {
        // Lấy thời gian hiện tại
        Timestamp now = Timestamp.now();

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        // Tính chênh lệch thời gian (milliseconds)
        long diffInMillis = now.toDate().getTime() - pastTime;

        if (diffInMillis < 0) {
            return "0m";
        }

        // Tính ra giờ và phút
        long hours = diffInMillis / (1000 * 60 * 60);
        long minutes = (diffInMillis / (1000 * 60)) % 60;
        if(hours == 0) return minutes+"m";
        else if (hours < 24)return hours +"h";
        else if(hours/24 < 7)return hours/24 + "d";
        else return sdf.format(new Date(pastTime));

    }


    @Override
    public int getItemCount() {
        return listImage.size();
    }
}
