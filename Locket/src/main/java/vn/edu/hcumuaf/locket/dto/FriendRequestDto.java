package vn.edu.hcumuaf.locket.dto;

public class FriendRequestDto {
    private String senderId;
    private String receiverId;

    public FriendRequestDto() {}

    public FriendRequestDto(String senderId, String receiverId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
} 