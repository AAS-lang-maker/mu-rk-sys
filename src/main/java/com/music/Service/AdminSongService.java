package com.music.Service;

import com.music.dto.ApplySongVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminSongService {
    List<ApplySongVo> selectapplySong();

    void uploadadmin(MultipartFile file);
}
