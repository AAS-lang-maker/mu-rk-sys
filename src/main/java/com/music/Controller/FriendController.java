package com.music.Controller;

import com.music.Service.FriendService;
import com.music.dto.CommonLoveVO;
import com.music.utils.JwtUtils;
import com.music.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    //互关好友共同收藏统计（收藏同一个榜单）。
      //前端在播放榜单歌曲时，传入 rankId 即可弹出“共同收藏”。

    @GetMapping("/common-love")
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
}

