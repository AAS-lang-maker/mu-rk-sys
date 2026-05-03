package com.music.Controller;

import com.music.Service.FriendService;
import com.music.dto.CommonLoveVO;
import com.music.dto.UserSimpleVO;
import com.music.utils.JwtUtils;
import com.music.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//查询 和 获取我关注的好友列表
@RestController
@RequestMapping("/api/friend")
@Tag(name = "FriendController", description = "好友相关接口")
public class FriendController {

    @Autowired
    private FriendService friendService;

    //互关好友共同收藏统计（收藏同一个榜单）。
      //前端在播放榜单歌曲时，传入 rankId 即可弹出“共同收藏”。

    @GetMapping("/common-love")
    @Operation(summary = "获取共同收藏的榜单", description = "获取共同收藏的榜单")
    public Result<CommonLoveVO> commonLove(@RequestParam("token") String token,
                                          @RequestParam("userId") Integer userId,
                                          @RequestParam("rankId") Integer rankId,
                                          @RequestParam(value = "limit", required = false) Integer limit) {
        if (token == null || token.isEmpty()) {
            return Result.badRequest("token不能为空");
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer tokenUserId;
        try {
            tokenUserId = JwtUtils.getUserIdFromToken(token);
        } catch (Exception e) {
            return Result.badRequest("token无效");
        }
        if (tokenUserId == null || userId == null || !tokenUserId.equals(userId)) {
            return Result.badRequest("无权访问他人数据");
        }
        if (rankId == null) {
            return Result.badRequest("rankId不能为空");
        }

        CommonLoveVO vo = friendService.getCommonLoveRank(userId, rankId, limit);
        return Result.success(vo);
    }

    // 2. 关注/取消关注好友
    @PostMapping("/follow")
    @Operation(summary = "关注/取消关注好友", description = "关注/取消关注好友")
    public Result<String> followUser(@RequestParam("token") String token,
                                     @RequestParam("UserId") Integer targetUserId) {
        // 1. 解析 Token
        if (token.startsWith("Bearer ")) token = token.substring(7);
        Integer UserId;
        try {
            UserId = JwtUtils.getUserIdFromToken(token);
        } catch (Exception e) {
            return Result.badRequest("Token无效");
        }

        // 2. 调用 Service 进行切换（如果已关注则取消，未关注则关注）
        // 你需要在 Service 里实现这个逻辑
        boolean isFollowed = friendService.isFollowed(UserId, targetUserId);
        //数据库有了
        if (isFollowed) {
            return Result.success("关注成功");
        } else {
            return Result.success("已取消关注");
        }
    }


    // 1. 获取我关注的好友列表
    @GetMapping("/listfans")
    @Operation(summary = "获取关注我的好友列表", description = "获取关注我的好友列表")
    public Result<UserSimpleVO> getMyFans(@RequestParam("token") String token) {
        // 1. 解析 Token 获取当前登录用户 ID
        if (token.startsWith("Bearer ")) token = token.substring(7);
        Integer currentUserId;
        try {
            currentUserId = JwtUtils.getUserIdFromToken(token);
        } catch (Exception e) {
            return Result.badRequest("Token无效");
        }

        // 2. 调用 Service 查询列表
        // 假设你的 Service 里有个方法叫 getFriendIds，返回的是 User 对象列表（包含头像、昵称）
        UserSimpleVO friends = friendService.getMyFans(currentUserId);

        return Result.success(friends);
    }

    // 1. 获取我关注的好友列表
    @GetMapping("/listmaster")
    @Operation(summary = "获取我关注的好友列表", description = "获取我关注的好友列表")
    public Result<UserSimpleVO> getMylover(@RequestParam("token") String token) {
        // 1. 解析 Token 获取当前登录用户 ID
        if (token.startsWith("Bearer ")) token = token.substring(7);
        Integer currentUserId;
        try {
            currentUserId = JwtUtils.getUserIdFromToken(token);
        } catch (Exception e) {
            return Result.badRequest("Token无效");
        }

        // 2. 调用 Service 查询列表
        // 假设你的 Service 里有个方法叫 getFriendIds，返回的是 User 对象列表（包含头像、昵称）
        UserSimpleVO master = friendService.getMymaster(currentUserId);

        return Result.success(master);
    }



}

