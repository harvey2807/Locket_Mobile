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
import com.hucmuaf.locket_mobile.model.FriendRequest;

import java.util.List;

/**
 * Adapter cho danh sách lời mời kết bạn đang chờ
 * Hiển thị danh sách các lời mời kết bạn với nút chấp nhận (✓) và từ chối (✕)
 * Mỗi item hiển thị: avatar người gửi, tên người gửi, và 2 nút action
 */
public class PendingRequestAdapter extends RecyclerView.Adapter<PendingRequestAdapter.PendingRequestViewHolder> {
    
    // ===== DATA =====
    private List<FriendRequest> pendingRequestsList;  // Danh sách lời mời kết bạn đang chờ
    private OnPendingRequestActionListener listener;  // Listener để xử lý sự kiện chấp nhận/từ chối

    /**
     * Interface để xử lý sự kiện khi user tương tác với lời mời kết bạn
     */
    public interface OnPendingRequestActionListener {
        void onAcceptRequest(FriendRequest request);  // Khi user bấm nút chấp nhận
        void onRejectRequest(FriendRequest request);  // Khi user bấm nút từ chối
    }

    /**
     * Constructor - khởi tạo adapter với danh sách lời mời và listener
     */
    public PendingRequestAdapter(List<FriendRequest> pendingRequestsList, OnPendingRequestActionListener listener) {
        this.pendingRequestsList = pendingRequestsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PendingRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tạo view cho mỗi item lời mời kết bạn
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_request, parent, false);
        return new PendingRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingRequestViewHolder holder, int position) {
        // Gán dữ liệu cho view holder tại vị trí position
        if (pendingRequestsList != null && position < pendingRequestsList.size()) {
            FriendRequest request = pendingRequestsList.get(position);
            if (request != null) {
                holder.bind(request);
            }
        }
    }

    @Override
    public int getItemCount() {
        // Trả về số lượng lời mời kết bạn trong danh sách
        return pendingRequestsList != null ? pendingRequestsList.size() : 0;
    }

    /**
     * Cập nhật danh sách lời mời kết bạn mới
     * Được gọi khi có thay đổi từ backend (thêm/xóa lời mời)
     */
    public void updateData(List<FriendRequest> newRequests) {
        this.pendingRequestsList.clear();
        this.pendingRequestsList.addAll(newRequests);
        notifyDataSetChanged();  // Thông báo cho RecyclerView cập nhật UI
    }

    /**
     * ViewHolder cho mỗi item lời mời kết bạn
     * Chứa các view: avatar, tên người gửi, nút chấp nhận, nút từ chối
     */
    public class PendingRequestViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatar;        // Avatar của người gửi lời mời
        private TextView name;           // Tên của người gửi lời mời
        private Button acceptButton;     // Nút chấp nhận (✓) - màu xanh lá
        private Button rejectButton;     // Nút từ chối (✕) - màu đỏ

        public PendingRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các view từ layout item_pending_request.xml
            avatar = itemView.findViewById(R.id.pendingRequestAvatar);
            name = itemView.findViewById(R.id.pendingRequestName);
            acceptButton = itemView.findViewById(R.id.btnAccept);
            rejectButton = itemView.findViewById(R.id.btnReject);
        }

        /**
         * Gán dữ liệu lời mời kết bạn vào các view
         * Hiển thị tên người gửi, avatar, và setup click listener cho các nút
         */
        public void bind(FriendRequest request) {
            if (request == null) return;
            
            // ===== HIỂN THỊ TÊN NGƯỜI GỬI =====
            // Logic ưu tiên hiển thị tên: senderName > sender.fullName > sender.userName > senderId > "Unknown User"
            String senderName;
            if (request.getSenderName() != null && !request.getSenderName().isEmpty()) {
                // Ưu tiên 1: senderName từ backend (tên thật đã được xử lý)
                senderName = request.getSenderName();
            } else if (request.getSender() != null && request.getSender().getFullName() != null && !request.getSender().getFullName().isEmpty()) {
                // Ưu tiên 2: fullName từ sender object
                senderName = request.getSender().getFullName();
            } else if (request.getSender() != null && request.getSender().getUserName() != null && !request.getSender().getUserName().isEmpty()) {
                // Ưu tiên 3: userName từ sender object
                senderName = request.getSender().getUserName();
            } else if (request.getSenderId() != null && !request.getSenderId().isEmpty()) {
                // Ưu tiên 4: senderId (fallback cuối cùng)
                senderName = request.getSenderId();
            } else {
                // Fallback cuối cùng nếu không có thông tin gì
                senderName = "Unknown User";
            }
            
            if (name != null) {
                name.setText(senderName);
            }
            
            // ===== LOAD AVATAR NGƯỜI GỬI =====
            // Sử dụng Glide để load avatar từ URL, với fallback về avatar mặc định
            if (avatar != null) {
                if (request.getSender() != null && request.getSender().getUrlAvatar() != null && !request.getSender().getUrlAvatar().isEmpty()) {
                    // Load avatar từ URL với hiệu ứng tròn và placeholder
                    Glide.with(itemView.getContext())
                            .load(request.getSender().getUrlAvatar())
                            .transform(new CircleCrop())  // Làm tròn avatar
                            .placeholder(R.drawable.avt)  // Avatar mặc định khi đang load
                            .error(R.drawable.avt)        // Avatar mặc định khi lỗi
                            .into(avatar);
                } else {
                    // Set avatar mặc định nếu không có URL avatar
                    avatar.setImageResource(R.drawable.avt);
                }
            }
            
            // ===== SETUP CLICK LISTENER CHO NÚT CHẤP NHẬN =====
            // Khi user bấm nút ✓ (màu xanh lá)
            if (acceptButton != null) {
                acceptButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAcceptRequest(request);
                    }
                });
            }
            
            // ===== SETUP CLICK LISTENER CHO NÚT TỪ CHỐI =====
            // Khi user bấm nút ✕ (màu đỏ)
            if (rejectButton != null) {
                rejectButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRejectRequest(request);
                    }
                });
            }
        }
    }
} 