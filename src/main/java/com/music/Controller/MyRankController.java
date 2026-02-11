package com.music.Controller;

import com.github.pagehelper.PageInfo;
import com.music.Service.MyRankService;
import com.music.dto.EditRank;
import com.music.dto.MyRankWithSong;
import com.music.pojo.Singer;
import com.music.pojo.Song;
import com.music.utils.JwtUtils;
import com.music.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

//数据渲染（查询回显）和页面重定向跳转分开写
@Controller
@RequestMapping("/api/mine")
public class MyRankController {
    @Autowired
    private MyRankService myRankService;


    @GetMapping("/my")//没有调用Service，只负责页面跳转，没有要操作数据库的逻辑
    public String myRank(@RequestParam(value = "token",required = true)String token,@RequestParam(value = "userId",required = true)Integer userId,
                         RedirectAttributes redirectAttributes){
    System.out.println("========/my接口被调用了========" );
    Integer paramuserId=validateToken(token,redirectAttributes);
    if(userId==null||!userId.equals(paramuserId)){
        return "redirect:/login";
    }

       redirectAttributes.addFlashAttribute("userId", userId);
       redirectAttributes.addFlashAttribute("token", token);
        return "redirect:/api/mine/myrank?token="+token+"&userId="+userId;
    }

    @GetMapping("/myrank")
    //可以保证在个人榜单中心页面刷新的时候数据不丢失，因为每次刷新都是在调用上面的三层架构重新查询后再次重定向
    //在此页面需要重新校验token以及userId
    //pageNum 页码数，pageSize一个页面的展示数
    public String myrank(@RequestParam(value = "token",required = false)String token,
                         @RequestParam("userId")Integer paramuserId,RedirectAttributes redirectAttributes,Model model,
                         @RequestParam(defaultValue="1")Integer pageNum, @RequestParam(defaultValue = "3")Integer pageSize){//userId要随着重定向和转发跟随到下一个新页面

       Integer userId=validateToken(token,redirectAttributes);
       if(userId==null){
           return "redirect:/login.html";
       }
       if(!userId.equals(paramuserId)){
           redirectAttributes.addFlashAttribute("errormessage","无权访问他人数据");
           return "redirect:/login.html";
       }
       Integer offset = (pageNum-1)*pageSize;
        PageInfo<MyRankWithSong> personalRanks=myRankService.selectMyrank(pageNum,pageSize,offset,userId);
        // 新增：打印PageInfo的所有关键字段
       System.out.println(personalRanks.getList());
        model.addAttribute("personalRanks",personalRanks);//把这两个必要数据传进URL而不是一次性的Flash，重定向的时候数据也进去了
        model.addAttribute("userId", userId);
        return "myrank-page";
    }

    //编辑功能种重定向和查询回显用的视图需要分开写两个@Getmapping，model在重定向时会消失
    //编辑功能继续阴我，已经修改了10次前后端联调失败就又来改Controller层。
    @GetMapping("/edit/{rankId}")//负责重定向
    public String edit(@PathVariable("rankId") Integer rankId, @RequestParam("token")String token, RedirectAttributes redirectAttributes
                       ) throws Exception {
        Integer paramuserId=validateToken(token,redirectAttributes);
        if(paramuserId==null){
            return "redirect:/login.html";
        }
        return "redirect:/edit.html?token="+token+"&rankId="+rankId;
    }
    //负责前端数据渲染的视图
    @GetMapping("/edit-page")
    public String editPage(@RequestParam("token")String token,Model model,@RequestParam("rankId")Integer rankId) throws Exception {
        //看不懂思密达，全都看不懂啥意思，豆包屎个角色
        if(token==null||token.isEmpty()){
            return "redirect:/login.html";
        }
        Integer userId=JwtUtils.getUserIdFromToken(token);
        if(model.containsAttribute("newRank")){
         MyRankWithSong newRank=(MyRankWithSong) model.asMap().get("newRank");
        model.addAttribute("newRank",newRank);
        model.addAttribute("rankId",rankId);}
      return  "redirect:/edit.html?token="+token+"&rankId="+rankId+"&userId="+userId;
    }
    @GetMapping("/getRank/{rankId}")
    @ResponseBody
    public MyRankWithSong getRank(@RequestParam("token")String token,@PathVariable("rankId")Integer rankId,
                                  RedirectAttributes redirectAttributes)throws Exception{
        Integer paramuserId=validateToken(token,redirectAttributes);
        if(paramuserId==null){
            throw new RuntimeException("token不能为空");
        }
        return myRankService.getRank(rankId);
    }

    @GetMapping("/singer")
    @ResponseBody
    //点击添加歌曲的按钮不涉及重定向，URL没有变化，页面也没有刷新
    public List<Singer> singer(@RequestParam("token") String token, @RequestParam("userId")Integer userId,// 接收前端的token
                               @RequestParam("categoryId") Integer categoryId,RedirectAttributes redirectAttributes){
        List<Singer> singers=myRankService.selectSinger(categoryId);
        return singers;
    }
    @GetMapping("/song")
    @ResponseBody
    public List<Song>  song(@RequestParam("token") String token,
                            @RequestParam("userId") Integer userId,
                            @RequestParam("singerId") Integer singerId
    ){
        List<Song> songs=myRankService.selectSong(singerId);
        return songs;
    }
    private Integer validateToken(String token,RedirectAttributes redirectAttributes) {
        if (token == null || token.isEmpty()) {
            redirectAttributes.addFlashAttribute("errormessage", "token已失效");
            return null;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer userId;
        try {
            userId = JwtUtils.getUserIdFromToken(token);
            return userId;
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errormessage", "token不见啦，请重新登录");
            return null;
        }
    }
    @PostMapping("/save")
    @ResponseBody
    //和前端配合，不用重定向，实现弹窗关闭
    public Result<String> save(@RequestBody EditRank dto,RedirectAttributes redirectAttributes,@RequestParam("token")
                              String token ){
        Integer userId=validateToken(token,redirectAttributes);
        if(userId==null){
            return Result.error("userId不能为空");
        }
        myRankService.insertNewRank(dto);
        redirectAttributes.addFlashAttribute("success","榜单重新编辑成功，已经保存");
        return Result.success("榜单编辑成功");
    }
    @GetMapping("/lv")
    public String lv(@RequestParam("userId")Integer userId,@RequestParam("token")String token, RedirectAttributes redirectAttributes){
        Integer paramuserId=validateToken(token,redirectAttributes);
        if(paramuserId==null){
            return "redirect:/login.html";
        }
        redirectAttributes.addFlashAttribute("userId", userId);
        redirectAttributes.addFlashAttribute("token", token);
        return "redirect:/api/mine/mylove?token="+token+"&userId="+paramuserId;
    }
    @GetMapping("/mylove")
    public String mylove(@RequestParam("token")String token,@RequestParam("userId")Integer userId,Model model,
                         RedirectAttributes redirectAttributes,@RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                         @RequestParam(value = "pageSize",defaultValue = "3")Integer pageSize){
        Integer paramuserId=validateToken(token,redirectAttributes);
        if(userId.equals(paramuserId)){
            redirectAttributes.addFlashAttribute("errormessage","userId怎么能不一致");
        }
        Integer offset = (pageNum-1)*pageSize;
        PageInfo<MyRankWithSong> myloveSongs=myRankService.selectMyLoverank(pageNum,pageSize,offset,userId);
        model.addAttribute("myloveSongs",myloveSongs);
        model.addAttribute("token",token);
        model.addAttribute("userId", userId);
        return "mylove-page";
    }
}



















