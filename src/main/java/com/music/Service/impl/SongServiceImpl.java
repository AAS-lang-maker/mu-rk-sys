package com.music.Service.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.music.Mapper.SongMapper;
import com.music.Service.SongService;
import com.music.dto.songQuery;
import com.music.pojo.Song;
import com.music.result.PagedTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;


@Service
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements SongService  {
    @Autowired
    private SongMapper songMapper;

    @Value("${music.file.path}")
    private String musicFilePath;

    @Override
    public Song getSongById(Integer songId) {
        Song song = songMapper.selectSongById(songId);
        if (song == null) {
            throw new RuntimeException("未找到ID为" + songId + "的歌曲");
        }
        return song;
    }

    @Override
    public PagedTO<Song> page1(songQuery songQu) {

        String name = songQu.getSongName();
        Integer singerId = songQu.getSingerId();
        Integer songId = songQu.getSongId();
        Page<Song> page=Page.of(songQu.getPageNum(), songQu.getPageSize());

        if (songQu.getOrderBy() != null){
            page.addOrder(OrderItem.asc(songQu.getOrderBy()));
        }else{
            page.addOrder(OrderItem.desc("song_id"));
        }
        //构建查询条件
        Page<Song>p= lambdaQuery()
                .like(name !=null,Song::getSongName, name)
                .eq(singerId != null, Song::getSingerId, singerId)
                .eq(songId != null, Song::getSongId, songId)
                .page(page);

        PagedTO<Song> pagedto = new PagedTO<>();
        //总条数
        pagedto.setTotal(p.getTotal());
        //总页数
        pagedto.setPages(p.getPages());
        //当前页数据
        List<Song> songList = p.getRecords();
        if(CollectionUtils.isEmpty(songList)){
            return pagedto;
        }
        pagedto.setList(songList);
        return pagedto;
    }
    @Override
    public File getSongFile(Song song) {
        String songUrl = song.getSongurl();
        String fullurl = musicFilePath + songUrl.trim();
        File mp3file = new File(fullurl);
        if (!mp3file.exists()) {
            throw new RuntimeException("音频文件不存在：" + fullurl);
        }
        if (!mp3file.isFile()) {
            throw new RuntimeException("路径不是有效文件：" + fullurl);
        }
        return mp3file;
    }
}