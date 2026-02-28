// 音乐播放器模块
class MusicPlayer {
    constructor(options = {}) {
        this.options = {
            container: 'music-player-container',
            token: '',
            userId: '',
            ...options
        };

        this.audioElement = null;
        this.isPlaying = false;
        this.currentSong = null; // 核心：全程保留当前歌曲信息
        this.progressInterval = null;
        this.container = document.getElementById(this.options.container);

        this.init();
    }

    init() {
        if (!this.container) {
            console.error('播放器容器不存在:', this.options.container);
            return;
        }

        this.createPlayerUI();
        this.bindEvents();
    }

    createPlayerUI() {
        const playerHTML = `
            <div class="music-player">
                <div class="player-header">
                    <h3>音乐播放器</h3>
                </div>
                
                <div class="player-content">
                    <div class="song-info">
                        <div class="song-image">
                            <img id="player-song-img" src="" alt="歌曲封面">
                        </div>
                        <div class="song-details">
                            <h4 id="player-song-name">未播放</h4>
                            <p id="player-singer-name">-</p>
                        </div>
                    </div>
                    
                    <div class="player-controls">
                        <div class="progress-bar">
                            <div class="progress-track" id="player-progress-track">
                                <div class="progress-fill" id="player-progress-fill"></div>
                                <div class="progress-handle" id="player-progress-handle"></div>
                            </div>
                            <div class="time-info">
                                <span id="player-current-time">0:00</span>
                                <span id="player-duration">0:00</span>
                            </div>
                        </div>
                        
                        <!-- 只保留播放/暂停按钮 -->
                        <div class="control-buttons">
                            <button class="control-btn play-btn" id="player-play-btn">▶</button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        this.container.innerHTML = playerHTML;

        // 创建音频元素
        this.audioElement = document.createElement('audio');
        this.audioElement.style.display = 'none';
        this.container.appendChild(this.audioElement);
    }

    bindEvents() {
        const playBtn = document.getElementById('player-play-btn');
        if (!playBtn) return;

        // 播放/暂停按钮（核心修复：加固逻辑）
        playBtn.addEventListener('click', () => {
            // 1. 没有歌曲时，直接提示，不执行任何操作
            console.log("播放/暂停按钮被点击了！");
            console.log("当前 this.isPlaying:", this.isPlaying);
            if (!this.currentSong || !this.currentSong.songId) {
                console.log('暂无播放中的歌曲，请先在列表中选择一首歌曲播放');
                return;
            }
            // 2. 有歌曲时，正常切换播放/暂停
            if (this.isPlaying) {
                console.log("调用 this.pause()");
                this.pause();
            } else {
                console.log("调用 this.play()");
                this.play();
            }
        });

        // 进度条点击
        const progressTrack = document.getElementById('player-progress-track');
        if (progressTrack) {
            progressTrack.addEventListener('click', (e) => {
                if (!this.currentSong || !this.audioElement) return;

                const track = e.currentTarget;
                const rect = track.getBoundingClientRect();
                const pos = (e.clientX - rect.left) / rect.width;
                const seekTime = pos * this.audioElement.duration;
                this.audioElement.currentTime = seekTime;
            });
        }

        // 音频元素事件（加固：防止currentSong丢失）
        this.audioElement.addEventListener('timeupdate', () => {
            this.updateProgress();
        });

        this.audioElement.addEventListener('ended', () => {
            // 歌曲播放完，只改状态，不重置currentSong
            this.isPlaying = false;
            if (playBtn) playBtn.textContent = '▶';
        });

        this.audioElement.addEventListener('loadedmetadata', () => {
            this.updateDuration();
        });

        // 防止音频出错导致currentSong丢失
        this.audioElement.addEventListener('error', (e) => {
            console.error('音频播放出错:', e);
            this.isPlaying = false;
            if (playBtn) playBtn.textContent = '▶';
            alert('歌曲播放出错，请换一首试试');
        });
    }

    // 播放歌曲（核心：全程保留currentSong）
    playSong(song) {
        if (!song || !song.songId) {
            console.error('无效的歌曲信息');
            alert('歌曲信息错误，无法播放');
            return;
        }

        // 关键：赋值后全程不重置，直到播放下一首
        this.currentSong = { ...song }; // 深拷贝，防止原对象被修改

        // 更新UI
        const songNameEl = document.getElementById('player-song-name');
        const singerNameEl = document.getElementById('player-singer-name');
        const songImgEl = document.getElementById('player-song-img');

        if (songNameEl) songNameEl.textContent = song.songName || '未知歌曲';
        if (singerNameEl) singerNameEl.textContent = song.singerName || '未知歌手';
        if (songImgEl) {
            songImgEl.src = song.songImg || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=music%20album%20cover%20placeholder&image_size=square';
        }

        // 创建音频源URL
        const audioUrl = `/music/api/songGo/play/${song.songId}?token=${encodeURIComponent(this.options.token)}`;
        this.audioElement.src = audioUrl;

        // 播放
        this.audioElement.play().then(() => {
            this.isPlaying = true;
            const playBtn = document.getElementById('player-play-btn');
            if (playBtn) playBtn.textContent = '⏸';
        }).catch(error => {
            console.error('播放失败:', error);
            alert('播放失败：' + (error.message || '网络异常或歌曲不存在'));
        });
    }

    // 播放（加固：只操作状态，不依赖重新赋值）
    play() {
        if (!this.currentSong || !this.audioElement) return;

        this.audioElement.play().then(() => {
            this.isPlaying = true;
            const playBtn = document.getElementById('player-play-btn');
            if (playBtn) playBtn.textContent = '⏸';
        }).catch(error => {
            console.error('播放失败:', error);
            alert('继续播放失败：' + error.message);
        });
    }

    // 暂停（核心修复：只改状态，不重置currentSong）
    pause() {
        console.log("pause() 方法被调用了！");
        console.log("当前 this.currentSong:", this.currentSong);
        console.log("当前 this.isPlaying:", this.isPlaying);
        console.log("当前 this.audioElement:", this.audioElement);
        if (!this.currentSong || !this.audioElement || !this.isPlaying) return;
        console.log("执行 this.audioElement.pause()");
        this.audioElement.pause();
        this.isPlaying = false;
        const playBtn = document.getElementById('player-play-btn');
        if (playBtn) {
            playBtn.textContent = '▶';
            console.log("按钮文字已改为 ▶");
        }
    }

    updateProgress() {
        if (!this.audioElement || !this.audioElement.duration) return;

        const currentTime = this.audioElement.currentTime;
        const duration = this.audioElement.duration;
        const progress = (currentTime / duration) * 100;

        const progressFill = document.getElementById('player-progress-fill');
        const progressHandle = document.getElementById('player-progress-handle');
        const currentTimeEl = document.getElementById('player-current-time');

        if (progressFill) progressFill.style.width = `${progress}%`;
        if (progressHandle) progressHandle.style.left = `${progress}%`;
        if (currentTimeEl) currentTimeEl.textContent = this.formatTime(currentTime);
    }

    updateDuration() {
        if (!this.audioElement || !this.audioElement.duration) return;

        const durationEl = document.getElementById('player-duration');
        if (durationEl) durationEl.textContent = this.formatTime(this.audioElement.duration);
    }

    formatTime(seconds) {
        if (isNaN(seconds)) return '0:00';
        const mins = Math.floor(seconds / 60);
        const secs = Math.floor(seconds % 60);
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    }

    setToken(token) {
        this.options.token = token;
    }

    setUserId(userId) {
        this.options.userId = userId;
    }
}

// 全局变量和初始化函数（保持不变）
let musicPlayer = null;

function initMusicPlayer(options) {
    const defaultOptions = {
        container: 'music-player-container',
        token: window.token || '',
        userId: window.userId || ''
    };
    const finalOptions = { ...defaultOptions, ...options };

    if (!musicPlayer) {
        musicPlayer = new MusicPlayer(finalOptions);
        window.musicPlayer = musicPlayer;
    }
    return musicPlayer;
}

function playSong(song, options = {}) {
    if (!musicPlayer) {
        musicPlayer = initMusicPlayer(options);
    }
    musicPlayer.playSong(song);
}

// 挂载到window，确保全局可访问
window.MusicPlayer = MusicPlayer;
window.initMusicPlayer = initMusicPlayer;
window.playSong = playSong;