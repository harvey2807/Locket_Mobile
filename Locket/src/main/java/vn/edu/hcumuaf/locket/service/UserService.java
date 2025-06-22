package vn.edu.hcumuaf.locket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.edu.hcumuaf.locket.dto.UserProfileRequest;
import vn.edu.hcumuaf.locket.responsitory.UserDao;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    public void updateInit(String uId, UserProfileRequest profile) {
        userDao.updateInit(uId, profile);
    }


}
