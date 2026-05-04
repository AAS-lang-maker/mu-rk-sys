package com.music.Controller;

import com.music.Service.SongService;
import com.music.pojo.Song;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/api/songGo")
@Tag(name = "歌曲播放相关接口", description = "提供歌曲播放的接口")
public class SongController {
    @Autowired
    private SongService songService;

    @GetMapping("/play/{songId}")
    @Operation(summary = "播放歌曲", description = "根据歌曲ID播放歌曲")
    public ResponseEntity<StreamingResponseBody> playSong(
            @RequestParam("token") String token,
            @PathVariable("songId") Integer songId) {

        // 1. 获取歌曲信息
        Song song = songService.getSongById(songId);
        // 2. 获取音频文件
        File mp3file = songService.getSongFile(song);

        // 3. 使用 StreamingResponseBody 输出音频流，避免 getWriter/getOutputStream 冲突
        StreamingResponseBody stream = out -> {
            try (FileInputStream in = new FileInputStream(mp3file)) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    out.flush();
                }
            } catch (IOException e) {
                // 客户端断开连接等IO异常，无需抛给上层，直接结束流
            }
        };

        String encodedFileName = URLEncoder.encode(mp3file.getName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("audio/mpeg"))
                .header("Content-Length", String.valueOf(mp3file.length()))
                .header("Content-Disposition", "inline; filename*=UTF-8''" + encodedFileName)
                .body(stream);
    }
}