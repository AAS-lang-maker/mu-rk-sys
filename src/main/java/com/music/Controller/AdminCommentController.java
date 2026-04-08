package com.music.Controller;

import com.github.pagehelper.PageInfo;
import com.music.Service.AdminCommentService;
import com.music.pojo.Comment;
import com.music.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api/adminc")
public class AdminCommentController {
    @Autowired
    private AdminCommentService adminCommentService;
    @GetMapping("/redirect-comment/{role}")
    public String redirect(@RequestParam("token")String token, RedirectAttributes redirectAttributes,
                         @PathVariable("role") Integer role) {
        if(token==null||token.equals("")){
            redirectAttributes.addFlashAttribute("请先登录哦");
            return  "redirect:/login.html";
        }
        if(role==1){
            return "redirect:/adminc/admining?token="+token+"&role="+role;
        }else{
            redirectAttributes.addFlashAttribute("您当前不是管理员，禁止进入");
            return "reirect:/login.html";
        }
    }

    @GetMapping("/adminnew/{role}")
    @ResponseBody
    public Result<PageInfo<Comment>> adminnew(@RequestParam("token") String token,@PathVariable("role") Integer role,
                           RedirectAttributes redirectAttributes, @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                           @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize, Model model) {
        if (token == null) {
            return Result.error("请先登录哦" );
        }
        if (role != 1) {
            return Result.error("您当前不是管理员，禁止进入");
        }
        Integer offset = (pageNum - 1) * pageSize;
        PageInfo<Comment> commentVoPage = adminCommentService.selectAllComment(pageSize,pageNum,offset);
        model.addAttribute("commentVoPage", commentVoPage);
        model.addAttribute("pageNum", pageNum);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("offset", offset);
        return Result.success(commentVoPage);
    }
    @GetMapping("/adminold/{role}")
    @ResponseBody
    public Result<PageInfo<Comment>> adminold(@RequestParam("token") String token, @PathVariable("role") Integer role,
                                              RedirectAttributes redirectAttributes, @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                              @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize, Model model) {
        if (token == null) {
            return Result.error("请先登录哦" );
        }
        if (role != 1) {
            return Result.error("您当前不是管理员，禁止进入");
        }
        Integer offset = (pageNum - 1) * pageSize;
        PageInfo<Comment> commentVoPage = adminCommentService.selectAllComment1(pageNum,pageSize,offset);
        model.addAttribute("commentVoPage", commentVoPage);
        model.addAttribute("pageNum", pageNum);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("offset", offset);
        return Result.success(commentVoPage);
    }
    @GetMapping("/testAi")
    @ResponseBody
    public Result<String> testAi() {
        adminCommentService.aiScan();
        return Result.success("测试AI审计");
    }
    @PostMapping("/ai-scan")
    @ResponseBody
    public Result aiscan(@RequestParam("token")String token,@RequestParam("role")Integer role){
       if(token==null||token.equals("")){
           return Result.error("请您先登录");
       }
       if(role!=1){
           return Result.error("您当前不是管理员，不可执行该操作哦");
       }

        adminCommentService.aiScan();
       return Result.success("AI审计已完成，已自动为您拦截高风险评论");
    }
    @DeleteMapping("/tongguoComment")
    @ResponseBody
    public Result tongguoComment(@RequestParam("token")String token,@RequestParam("comId")Integer comId,
                                @RequestParam("role")Integer role) {
        if (token == null || token.equals("")) {
            return Result.error("请您先登录");
        }
        if (role != 1) {
            return Result.error("您当前不是管理员，不可执行该操作哦");
        }
        if (comId == null) {
            return Result.error("请先选择要删除的评论");
        }
        boolean falg = adminCommentService.updateComment1(comId);
        if (falg) {
            return Result.success();
        }else{
            return Result.error("删除操作失败");
        }
    }
    @DeleteMapping("/bohuiComment")
    @ResponseBody
    public Result bohuiComment (@RequestParam("token")String token,@RequestParam("comId")Integer comId,
                                @RequestParam("role")Integer role) {
        if (token == null || token.equals("")) {
            return Result.error("请您先登录");
        }
        if (role != 1) {
            return Result.error("您当前不是管理员，不可执行该操作哦");
        }
        if (comId == null) {
            return Result.error("请先选择要删除的评论");
        }
        boolean falg = adminCommentService.update2Comment(comId);
        if (falg) {
            return Result.success();
        }else{
            return Result.error("删除操作失败");
        }
    }
}