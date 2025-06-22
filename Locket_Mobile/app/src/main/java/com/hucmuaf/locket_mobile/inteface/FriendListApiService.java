package com.hucmuaf.locket_mobile.inteface;

import com.hucmuaf.locket_mobile.model.FriendListResponse;
import com.hucmuaf.locket_mobile.model.FriendRequest;
import com.hucmuaf.locket_mobile.model.User;
import com.hucmuaf.locket_mobile.dto.SearchUserRequest;
//import com.hucmuaf.locket_mobile.dto.FriendRequestDto;
import com.hucmuaf.locket_mobile.dto.ShareRequest;
import com.hucmuaf.locket_mobile.dto.ImportRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface FriendListApiService {
    
    // Lấy danh sách bạn bè
    @GET("api/friend-list/friends/{userId}")
    Call<FriendListResponse> getFriendList(@Path("userId") String userId);
    
    // Tìm kiếm bạn bè
    @POST("api/friend-list/search")
    Call<List<User>> searchUsers(@Body SearchUserRequest request);
    
    // Gửi lời mời kết bạn
    @POST("api/friend-list/send-request")
    Call<Void> sendFriendRequest(@Body FriendRequest request);

    // Chấp nhận lời mời kết bạn
    @PUT("api/friend-list/accept-request/{requestId}")
    Call<Void> acceptFriendRequest(@Path("requestId") String requestId);

    // Từ chối lời mời kết bạn
    @PUT("api/friend-list/reject-request/{requestId}")
    Call<Void> rejectFriendRequest(@Path("requestId") String requestId);
    
    // Xóa bạn bè
    @DELETE("api/friend-list/remove-friend/{userId}/{friendId}")
    Call<String> removeFriend(@Path("userId") String userId, @Path("friendId") String friendId);
    
    // Lấy danh sách gợi ý bạn bè
    @GET("api/friend-list/suggestions/{userId}")
    Call<List<User>> getFriendSuggestions(@Path("userId") String userId);
    
    // Lấy danh sách lời mời kết bạn đang chờ
    @GET("api/friend-list/pending-requests/{userId}")
    Call<List<FriendRequest>> getPendingRequests(@Path("userId") String userId);
    
    // Lấy danh sách lời mời đã gửi
    @GET("api/friend-list/sent-requests/{userId}")
    Call<List<FriendRequest>> getSentRequests(@Path("userId") String userId);
    
    // Hủy lời mời kết bạn
    @PUT("api/friend-list/cancel-request/{requestId}")
    Call<String> cancelFriendRequest(@Path("requestId") String requestId);
    
    // Chia sẻ qua social media
    @POST("api/friend-list/share/{platform}")
    Call<String> shareToSocialMedia(@Path("platform") String platform, @Body ShareRequest request);
    
    // Import bạn bè từ ứng dụng khác
    @POST("api/friend-list/import/{platform}")
    Call<List<User>> importFriendsFromPlatform(@Path("platform") String platform, @Body ImportRequest request);
    
    // Lấy user ID từ email
    @GET("api/friend-list/user-id/email/{email}")
    Call<String> getUserIdByEmail(@Path("email") String email);
    
    // Test API để kiểm tra kết nối
    @GET("api/test-data/test")
    Call<String> testConnection();
} 