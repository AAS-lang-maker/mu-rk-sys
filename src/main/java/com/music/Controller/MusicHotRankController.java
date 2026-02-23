package com.music.Controller;

import com.music.Service.impl.HotRankService;
import com.music.dto.MyRankWithSong;
import com.music.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController//???
@RequestMapping("/api/hot")
public class MusicHotRankController {
    @Autowired
    private HotRankService hotRankService;

    @GetMapping("/hotrank")
    public Result<String> getHotRank(@RequestParam(defaultValue = "10") Integer topN){
        Set<Object> idset = hotRankService.getHotRankId(topN);
        if(idset.isEmpty()){
            return Result.success();
        }
        //通过流处理，将Set中的Object类型转为String类型，再从String转为Long类型rankId，最后把它整体转变为List集合
        //所以整体实现思路是redis的核心Service层，再到Mapper或者Controller层
        List<Long> rankId=idset.stream().map(Object::toString).map(Long::valueOf).collect(Collectors.toList());
        List<MyRankWithSong> list=hotRankService.listById(rankId);
        return Result.success();
    }
    //为什么要这样做，因为Service层的数据全部为Long类型，且Mybatis中通过foreach循环List集合
}
