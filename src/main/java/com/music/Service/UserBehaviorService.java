package com.music.Service;

import com.music.pojo.UserBehavior;

import java.util.List;

public interface UserBehaviorService {
    List<UserBehavior> getUserBehavior(int userId, int UTId, int songId, int STId);
}
