package com.music.result;

import com.music.pojo.Song;
import lombok.AllArgsConstructor;
import lombok.Data;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class PagedTO<T> {
    private long pages;
    private long total;
    private List<T> list;

}
