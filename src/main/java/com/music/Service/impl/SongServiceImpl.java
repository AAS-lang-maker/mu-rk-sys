package com.music.Service.impl;

import com.music.Mapper.SongMapper;
import com.music.Service.SongService;
import com.music.pojo.Song;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URLEncoder;

@Service
public class SongServiceImpl implements SongService {
    @Autowired
    private SongMapper songMapper;
    //音乐文件的存储路径，这部分还要在yml文件中配置
    @Value("${music.file.path:D:/music/music_files/}")
    private String musicFilePath;

    @Override
    public void playSong(Integer songId, HttpServletResponse response) throws UnsupportedEncodingException {
        /*思路：独特的音乐文件上传播放
               1.检查songId对应的歌曲在数据库中是否存在
               2.拼接完整的URL路径
               3.设置前端响应头
               4.把URL给响应头让前端能够收到音频文件
               */
        Song song =songMapper.selectSongById(songId);
        if(song==null){
            throw new RuntimeException("未找到ID为"+songId+"的歌曲");
        }
        String songUrl=song.getSongurl();
        String fullurl=musicFilePath+ songUrl.trim();
        System.out.println("===== 拼接后的完整文件路径 =====");
        System.out.println(fullurl);
        System.out.println("==================================");
        File mp3file=new File(fullurl);
        System.out.println("文件是否存在：" + mp3file.exists());
        System.out.println("是否是文件：" + mp3file.isFile());
        System.out.println("文件绝对路径：" + mp3file.getAbsolutePath());
        if(!mp3file.exists()){
            throw new RuntimeException();
        }
        if (!mp3file.exists()) {
            throw new RuntimeException("音频文件不存在：" + fullurl);
        }
        if (!mp3file.isFile()) {
            throw new RuntimeException("路径不是有效文件：" + fullurl);
        }

        //第三步是啥雷霆代码，我直接AI完成，猎奇666
        // 设置响应头，返回音频流
        response.setContentType("audio/mpeg");
        response.setHeader("Content-Length", String.valueOf(mp3file.length()));
        response.setHeader("Content-Disposition", "inline; filename=\"" + mp3file.getName() + "\"");
//利用文件流让前端网络能够读取音频文件
        try (
            FileInputStream in =new FileInputStream(mp3file);//从内存中读出文件
            OutputStream out=response.getOutputStream())//将读出的文件放入请求头
        {
            byte[] buffer=new byte[4096];//建立一个缓冲流，此处用byte类型，从-128~127，它占用的字节只有8个，可以大大节省空间
            //创建了一个4kB的搬运车，用来搬运5MB的文件，效率不低的同时防止把所有文件塞进内存时导致服务器崩溃
            int len;
            while((len=in.read(buffer))!=-1){//用in读取搬运车中的文件，用len表示每次从“小车”中读取的长度，当最后一次读取长度小于0时，完毕
                out.write(buffer,0,len);//用out把读出的文件读入前端，把小车里的前len个字节一次读入
            }
        }catch (IOException e) {
            throw new RuntimeException(e+"播放歌曲失败");
        }
    }
}