package com.music;

import com.music.Service.HotRankService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HotRankInitTest {
    @Autowired
    private HotRankService hotRankService;
  @Test
    public void hotRankInitTest(){
        hotRankService.updateHotRank(12L);
        hotRankService.updateHotRank(13L);
        hotRankService.updateHotRank(14L);
    }
}
