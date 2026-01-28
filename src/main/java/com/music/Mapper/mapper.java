package com.music.Mapper;

import org.apache.catalina.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper
public interface mapper {
    @Select("select user_id,username,password,user_create_time from user where username=#{username}")
    public User selectByUsername(String username);
    @Insert("insert into user (username, user_create_time, password) VALUES (#{username},#{createTime},#{password})")
    //占位符中的数据是前端传入的数据，注册时间要是user实体类中的名称
    public void insert(com.music.pojo.User newUser);
}
