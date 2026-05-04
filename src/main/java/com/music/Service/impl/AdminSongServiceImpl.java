package com.music.Service.impl;

import com.music.Mapper.AdminSongMapper;
import com.music.Service.AdminSongService;
import com.music.dto.ApplySongVo;
import com.music.pojo.Singer;
import com.music.pojo.Song;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.music.utils.NcmDecoder;
import org.jaudiotagger.audio.AudioFileIO;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class AdminSongServiceImpl implements AdminSongService {
    private static final Logger log = LoggerFactory.getLogger(AdminSongServiceImpl.class);
    @Autowired
    private AdminSongMapper adminSongMapper;
    @Value("${music.file.path:D:/music/music_files/}")
    private String storagePath;

    @Override
    public List<ApplySongVo> selectapplySong() {
        return adminSongMapper.selectAllApply();
    }

    @Override
    @Async("rankTask")
    public void uploadadmin(MultipartFile file) {
      try{
        File tempNcm=saveTempFile(file);
        File mp3File=NcmDecoder.decode(tempNcm,storagePath);
        AudioFile audioFile=AudioFileIO.read(mp3File);
          Tag tag= (Tag) audioFile.getTag();
          String title = "未知歌曲";
          String artist = "未知歌手";
          if (tag != null) {
              title = tag.getFirst(FieldKey.TITLE);
              artist = tag.getFirst(FieldKey.ARTIST);
          }


          // 4. 保存歌曲信息到数据库
          Song song = new Song();
          Singer singer = new Singer();
          song.setSongName(title);
          singer.setSingerName(artist);
          song.setSongurl(mp3File.getAbsolutePath());
          adminSongMapper.insertSong(song);
          adminSongMapper.insertSinger(singer);

          // 5. 清理临时文件
          tempNcm.delete();

      } catch (Exception e) {
          log.error("NCM 转码失败：{}", e.getMessage());
          // 可以在这里抛出自定义异常，让 Controller 捕获
          throw new RuntimeException("文件处理失败：" + e.getMessage());
      }
      }
    // 保存临时文件到系统临时目录
    private File saveTempFile(MultipartFile file) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        File tempFile = new File(tempDir, file.getOriginalFilename());
        file.transferTo(tempFile);
        return tempFile;
    }
}
