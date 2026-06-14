package com.music.Controller;

import com.music.Service.CategoryService;
import com.music.pojo.Category;
import com.music.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
@Controller
@RequestMapping("/api/category")
@Tag(name = "CategoryController", description = "分类管理模块")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")//方法一：前端展示的后端逻辑，直接调用Service层,后续有可能增加其他分类模块，就只需要在前端动态增加按钮
    public Result<List<Category>> selectAllCategoryId() {
        return categoryService.selectAllCategoryId();
    }



        @GetMapping("/redirect")
        @Operation(summary = "根据分类ID重定向", description = "根据分类ID重定向")
        public String selectAllCategoryId(@RequestParam("categoryId") Integer categoryId,
                                          // 从请求头/参数中获取前端传递的Token和UserID（根据你的验证逻辑，从实际存储位置取）
                                          @RequestParam(value = "token", required = true) String token,
                                          @RequestParam(value = "userId", required = true) Integer userId,
                                          RedirectAttributes redirectAttributes) {
        if(token==null||token==""){
            redirectAttributes.addFlashAttribute("errormessage","token不能为空");
            return "redirect:/login.html";
        }
        if(userId==null||userId==0){
            redirectAttributes.addFlashAttribute("errormessage","userId不能为空");
            return "redirect:/login.html";
        }
            Result<Category> result = categoryService.selectCategoryId(categoryId);
            if (result.getCode() == 200) {
                redirectAttributes.addAttribute("success", "即将为您跳转至榜单发布页面");
                return "redirect:/api/rank/publish?token=" + token + "&userId=" + userId + "&category=" + categoryId;
            } else {
                redirectAttributes.addAttribute("errormessage", "点击失败，请重新选择类别");
                return "redirect:/index.html?token=" + token + "&userId=" + userId;
            }
        }
    }

