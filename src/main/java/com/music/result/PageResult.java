package com.music.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private long total; //总记录数

    private List<?> records; //当前页数据集合

}

