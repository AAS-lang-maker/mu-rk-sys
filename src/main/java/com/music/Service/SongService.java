package com.music.Service;

import jakarta.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;

public interface SongService {
 void playSong(Integer songId, HttpServletResponse response) throws UnsupportedEncodingException;
}
