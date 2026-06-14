package com.music.Service.impl;

import com.github.pagehelper.PageInfo;
import com.music.Mapper.WorkPublishMapper;
import com.music.Service.WorkPunlishService;
import com.music.dto.CommentVo;
import com.music.dto.MyRankWithSong;
import com.music.dto.RankAddRequest;
import com.music.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkPublishServiceImpl implements WorkPunlishService {
    @Autowired
    private WorkPublishMapper workPublishMapper;

    @Override
    public PageInfo<MyRankWithSong> selectAllRank(Integer offset, Integer category, Integer pageNum, Integer pageSize) {
        List<MyRankWithSong> workRanks=workPublishMapper.selectAllRank(category,pageSize,offset);
        PageInfo<MyRankWithSong> pageInfo=new PageInfo<>(workRanks);
        Integer total=workPublishMapper.selectTotal(category);
        Integer pages=(total+pageSize-1)/pageSize;
        pageInfo.setList(workRanks);
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(total);
        pageInfo.setPages(pages);
        return pageInfo;
    }
    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)//事务注解:主表和子表的Service层操作必须同时成功，否则就是失败
    public boolean insertRank(Integer categoryId, Integer userId, RankAddRequest rankAddRequestDto) {
        //？依旧难想，将Dto类转化为对应数据库的实体类
        //先创建一个pojo对应的对象，再将前端dto的对应值传进去，最后调用Mapper层
        PersonalRank personalRank = new PersonalRank();
        personalRank.setCategoryId(categoryId);
        personalRank.setUserId(userId);
        //细心豆包，数据库里面默认target——id为空，所以当它真的为空，就设置其值为0
        personalRank.setTargetId(rankAddRequestDto.getTargetId() == null ? 0 : rankAddRequestDto.getTargetId());
        personalRank.setRankName(rankAddRequestDto.getRankName());
        workPublishMapper.insertRank(personalRank);
        //主表和子表的外键关联要拿出来（666）
        Integer rankId = personalRank.getRankId();
        System.out.println("获取的rankId：" + rankId);
        if (rankId == null) {
            System.out.println("rankId为null，直接返回失败"); // 新增：打印跳过逻辑
            return false;
        }

        List<RankSong> ranksongList=new ArrayList<>();//用集合接受前端榜单数据，因为歌曲和排名有很多，一个用户还可能有多个榜单
        //豆包大人教我写最难写的lamda？？表达式
        rankAddRequestDto.getSongItems().forEach(item->{
            RankSong rankSong=new RankSong();
            rankSong.setRankId(rankId);
            rankSong.setRanking(item.getRanking());
            rankSong.setSongId(item.getSongId());
            ranksongList.add(rankSong);
        });
        int insertCount = workPublishMapper.sixInsert(ranksongList);
        if (insertCount != ranksongList.size()) {
            System.out.println("子表插入行数与预期不符，返回失败");
            return false; // 触发事务回滚
        }
        return  true;
    }

    @Override
    public List<Work> selectWork(Integer categoryId) {
        List<Work> works=workPublishMapper.selectWork(categoryId);
        return works;
    }

    @Override
    public List<Song> selectSong(Integer workId) {
        return workPublishMapper.selectSong(workId);
    }

    @Override
    public boolean insertVote(Integer userId, Integer rankId, String ip) {
        int record=workPublishMapper.checkip(ip,rankId);//防刷票，匿名投票，利用ip检查
        if(record>0)
        { return false;}
        int rows=workPublishMapper.insertVote(rankId,ip);
     return rows>0;
    }

    @Override
    public boolean insertLove(Integer userId, String ip, Integer rankId) {
        int record1=workPublishMapper.checkip(ip,rankId);
        if(record1>0)
        { return false;}
        int row1=workPublishMapper.insertLove(userId,ip,rankId);
        return row1>0;
    }

    @Override
    public PageInfo<MyRankWithSong> selectSearch(Integer category, Integer pageNum,
                                                 Integer offset, Integer pageSize,String keyword) {
        if(keyword!=null){
            keyword=keyword.trim();
        }else{
            keyword=null;
        }
        List<MyRankWithSong> list=workPublishMapper.selectSearch(category,pageNum,offset,pageSize,keyword);
        PageInfo<MyRankWithSong> searchRank=new PageInfo<>();
        Integer total=workPublishMapper.selectSearchTotal(category,keyword);
        Integer pages=(total+pageSize-1)/pageSize;
        searchRank.setList(list);
        searchRank.setPageNum(pageNum);
        searchRank.setPageSize(pageSize);
        searchRank.setTotal(total);
        searchRank.setPages(pages);
        return searchRank;
    }


    @Override
    public Comment insertComment(Integer rankId, Integer userId, String content, Integer parentId) {
        Comment comment=new Comment();
        comment.setRankId(rankId);
        comment.setUserId(userId);
        comment.setComContent(content);
        comment.setParentId(parentId);
        workPublishMapper.insertComment(rankId,userId,content,parentId);
        return  comment;
    }

    @Override
    public List<CommentVo> selectComment(Integer rankId, Integer userId) {
        List<CommentVo> list=workPublishMapper.selectComment(rankId,userId);
        System.out.println("查询出的评论数："+list.size());
        return list;
    }

    @Override
    @Transactional
    public boolean deleteComment(Integer comId,Integer userId) {
        Comment comment=workPublishMapper.selectCommentById(comId);
        if(comment==null){
            return false;
        }
        if(comment.getUserId()!=userId){
            throw new RuntimeException("只能删除自己的评论");
        }
        int result= workPublishMapper.updateComment(comId,1);
        return result>0;
    }

    @Override
    @Transactional
    public boolean insertLike(Integer userId,Integer comId) {
        Comment comment=workPublishMapper.selectCommentById(comId);
        if(comment==null||comment.getIdDelete()==1){
            return false;
        }
        int result=workPublishMapper.insertLike(userId,comId);
        return result>0;
    }

    @Override
    @Transactional
    public boolean deleteLike(Integer comId, Integer userId) {
        Comment comment=workPublishMapper.selectCommentById(comId);
        if(comment==null||comment.getIdDelete()==1){
            return false;
        }
        int result=workPublishMapper.deleteLike(comId,userId);
        return result>0;
    }

}
