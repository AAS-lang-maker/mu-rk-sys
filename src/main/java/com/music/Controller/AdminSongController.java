package com.music.Controller;

import com.music.Service.AdminSongService;
import com.music.Service.SongService;
import com.music.dto.ApplySongVo;
import com.music.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
@Tag(name = "管理员歌曲管理", description = "管理员歌曲管理")
public class AdminSongController {
    @Autowired
     private AdminSongService adminSongService;
    @GetMapping("/redirect-song/{role}")
    public String redirectsong(@RequestParam("token")String token, @PathVariable("role")Integer role,
                               RedirectAttributes redirectAttributes){
        if(token==null||token==""){
            redirectAttributes.addFlashAttribute("请您先登录哦");
            return  "redirect:/login.html";
        }
        if(role!=1){
            redirectAttributes.addFlashAttribute("您当前还不是管理员呢");
            return "redirect:/login.html";
        }
        redirectAttributes.addFlashAttribute("role",role);
        redirectAttributes.addFlashAttribute("token",token);
        return "redirect:/selectsong.html?role="+role+"&token="+token;
    }
    @GetMapping("/selectsong")
    @Operation(summary = "查询所有待审核歌曲", description = "查询所有待审核歌曲")
    public Result<List<ApplySongVo>> selectsong(@RequestParam("token")String token,@RequestParam("role")Integer role) {
        if(token==null||token==""){
            return Result.error("token失效，请重新登录");
        }
        if(role!=1){
            return Result.error("您还不是管理员呢");
        }
        try{
        List<ApplySongVo> applysong=adminSongService.selectapplySong();
        return Result.success(applysong);
    }catch (Exception e){
        e.printStackTrace();
        return Result.error(e.getMessage());}
    }
    @PostMapping("/upload")
    @Operation(summary = "管理员上传歌曲", description = "管理员上传歌曲")
    public Result upload(@RequestParam("token")String token,@RequestParam("file") MultipartFile file,
                         @RequestParam("role")Integer role){
        if(token==null||token==""){
            return Result.error("token失效，请重新登录");
        }
        if(role!=1){
            return Result.error("您还不是管理员呢");
        }
        if(file.isEmpty()){
            return Result.error("上传文件为空");
        }
        String fileName = file.getOriginalFilename();
        if(fileName==null||!(
                fileName.endsWith(".mp3") ||
                        fileName.endsWith(".flac") ||
                        fileName.endsWith(".wav") ||
                        fileName.endsWith(".ncm")
        )){
            return Result.error("文件格式不对，请重新上传");
        }
        try{
            adminSongService.uploadadmin(file);
            return Result.success("文件上传成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
