package com.music.Mapper;

import com.music.pojo.Category;
import com.music.utils.Result;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    Result<List<Category>> selectAllCategoryId();//前端主页分类模块展示


     Category selectCategoryId(Integer categoryId);//用户选择具体类别并跳转

}
