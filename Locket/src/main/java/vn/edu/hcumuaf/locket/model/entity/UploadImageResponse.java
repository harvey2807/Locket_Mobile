package vn.edu.hcumuaf.locket.model.entity;

public class UploadImageResponse {
    private String url;
    public UploadImageResponse(String url) {
        this.url = url;
    }
    public String getUrl(){
        return url;
    }
}
