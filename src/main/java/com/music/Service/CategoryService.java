package com.music.Service;

import com.music.pojo.Category;
import com.music.utils.Result;

import java.util.List;

public interface CategoryService {
    Result<List<Category>> selectAllCategoryId();

    Result<Category> selectCategoryId(Integer categoryId);
}
