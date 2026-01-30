package com.music.Service.impl;

import com.music.Mapper.CategoryMapper;
import com.music.Service.CategoryService;
import com.music.pojo.Category;
import com.music.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class CategoryServiceImpl implements CategoryService {
   @Autowired
   private CategoryMapper categoryMapper;
    @Override
    public Result<List<Category>> selectAllCategoryId() {
        return categoryMapper.selectAllCategoryId();
    }

    @Override
    public Result<Category> selectCategoryId(Integer categoryId) {
        if(categoryId==null||categoryId==0){
            return Result.error("不能没有选择");
        }
        Category category= categoryMapper.selectCategoryId(categoryId);//转化为Category实体类
        if(category==null){
            return Result.error("选择类别失败，重新选择");
        }else{
            return Result.success(category);
        }
    }
}
