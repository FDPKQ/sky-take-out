package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    void update(Category category);


    Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    void save(Category categoryDTO);

    void deleteById(Long id);

    List<Category> getByType(Integer type);
}
