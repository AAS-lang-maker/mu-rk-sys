package com.music.Service.impl;

import com.github.pagehelper.PageInfo;
import com.music.Mapper.AdminCommentMapper;
import com.music.Service.AdminCommentService;
import com.music.Service.AiAuditService;
import com.music.dto.CommentVo;
import com.music.pojo.Comment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
public class AdminCommentServiceImpl implements AdminCommentService {
    @Autowired
    private AdminCommentMapper adminCommentMapper;
    @Autowired
    private AiAuditService aiAuditService;


    @Override
    public PageInfo<Comment> selectAllComment(Integer pageSize, Integer pageNum, Integer offset) {
        List<Comment> Allcomment=adminCommentMapper.selectAllComment(pageSize,offset);
        PageInfo<Comment> pageInfo=new PageInfo<>();
        Integer total=adminCommentMapper.selectAll();
        Integer pages=(pageSize+total-1)/pageSize;
        pageInfo.setList(Allcomment);
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setPages(pages);
        pageInfo.setTotal(total);
        return pageInfo;
    }

    @Override
    public PageInfo<Comment> selectAllComment1(Integer pageNum, Integer pageSize, Integer offset) {
        List<Comment> Allcomment=adminCommentMapper.selectAllComment1(pageSize,offset);
        PageInfo<Comment> pageInfo=new PageInfo<>();
        Integer total=adminCommentMapper.selectAll1();
        Integer pages=(pageSize+total-1)/pageSize;
        pageInfo.setList(Allcomment);
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setPages(pages);
        pageInfo.setTotal(total);
        return pageInfo;
    }

    @Override
    public void aiScan() {
        System.out.println("=====> AI审计开始运行");
        System.out.println("=====================================");

        // 1. 查询【未审核、未删除】的评论
        List<Comment> all = adminCommentMapper.selectAllCom();
        System.out.println("查到评论数量：" + all.size());

        for (Comment comment : all) {

            // ==============================================
            // 🔥 关键打印：看看到底拿到内容没！
            // ==============================================
            System.out.println("评论ID: " + comment.getComId());
            System.out.println("评论内容: " + comment.getComContent()); // 这里必须有值！

            // AI打分
            int score = aiAuditService.getScore(comment);

            // ==============================================
            // 🔥 最重要：无论怎样，都要更新数据库！
            // 这里我给你写死，绝对入库！
            // ==============================================
            comment.setRiskScore(score);

            if (score >= 80) {
                // 高风险 → 拦截 + 删除
                comment.setIdDelete(1);
                comment.setStatus(1);
                comment.setComContent(comment.getComContent()); // 不清空！
                adminCommentMapper.updateScore1(comment);
                log.info("🚨 高风险拦截，ID={}", comment.getComId());
            }
            else if (score >= 50) {
                // 中风险 → 标记，不通过
                comment.setStatus(0);
                adminCommentMapper.updateScore2(comment);
                log.info("⚠️ 中风险标记，ID={}", comment.getComId());
            }
            else {
                // 低风险 → 自动通过
                comment.setStatus(1);
                adminCommentMapper.updateScore3(comment);
                log.info("✅ 低风险通过，ID={}", comment.getComId());
            }

            // ==============================================
            // 🔥 强制打印：确认执行了哪一段
            // ==============================================
            System.out.println("最终状态：status=" + comment.getStatus() + " idDelete=" + comment.getIdDelete());
        }

        log.info("AI审计完成");
    }

    @Override
    public boolean updateComment1(Integer comId) {
        int rows=adminCommentMapper.deleteComment1(comId);
        if(rows>0){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean update2Comment(Integer comId) {
        int rows=adminCommentMapper.deleteComment2(comId);
        if(rows>0){
            return true;
        }else{
            return false;
        }
    }
}
