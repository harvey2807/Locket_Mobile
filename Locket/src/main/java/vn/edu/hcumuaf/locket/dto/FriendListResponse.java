package vn.edu.hcumuaf.locket.dto;

import vn.edu.hcumuaf.locket.model.entity.User;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FriendListResponse {
    @SerializedName("friends")
    private List<User> friends;

    @SerializedName("totalFriends")
    private int totalFriends;

    @SerializedName("maxFriends")
    private int maxFriends;

    @SerializedName("suggestions")
    private List<User> suggestions;

    public FriendListResponse() {}

    public FriendListResponse(List<User> friends, int totalFriends, int maxFriends, List<User> suggestions) {
        this.friends = friends;
        this.totalFriends = totalFriends;
        this.maxFriends = maxFriends;
        this.suggestions = suggestions;
    }

    public List<User> getFriends() {
        return friends;
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }

    public int getTotalFriends() {
        return totalFriends;
    }

    public void setTotalFriends(int totalFriends) {
        this.totalFriends = totalFriends;
    }

    public int getMaxFriends() {
        return maxFriends;
    }

    public void setMaxFriends(int maxFriends) {
        this.maxFriends = maxFriends;
    }

    public List<User> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<User> suggestions) {
        this.suggestions = suggestions;
    }
} 