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
        this.currentSong = null;
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
                        
                        <div class="control-buttons">
                            <button class="control-btn" id="player-volume-btn">🔊</button>
                            <button class="control-btn play-btn" id="player-play-btn">▶</button>
                            <button class="control-btn" id="player-next-btn">⏭</button>
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
        // 播放/暂停按钮
        document.getElementById('player-play-btn').addEventListener('click', () => {
            if (this.isPlaying) {
                this.pause();
            } else {
                this.play();
            }
        });

        // 进度条点击
        document.getElementById('player-progress-track').addEventListener('click', (e) => {
            const track = e.currentTarget;
            const rect = track.getBoundingClientRect();
            const pos = (e.clientX - rect.left) / rect.width;
            const seekTime = pos * this.audioElement.duration;
            this.audioElement.currentTime = seekTime;
        });

        // 音频元素事件
        this.audioElement.addEventListener('timeupdate', () => {
            this.updateProgress();
        });

        this.audioElement.addEventListener('ended', () => {
            this.isPlaying = false;
            document.getElementById('player-play-btn').textContent = '▶';
        });

        this.audioElement.addEventListener('loadedmetadata', () => {
            this.updateDuration();
        });
    }

    playSong(song) {
        if (!song || !song.songId) {
            console.error('无效的歌曲信息');
            return;
        }

        this.currentSong = song;

        // 更新UI
        document.getElementById('player-song-name').textContent = song.songName || '未知歌曲';
        document.getElementById('player-singer-name').textContent = song.singerName || '未知歌手';
        document.getElementById('player-song-img').src = song.songImg || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=music%20album%20cover%20placeholder&image_size=square';

        // 创建音频源URL
        const audioUrl = `/music/api/songGo/play/${song.songId}?token=${encodeURIComponent(this.options.token)}`;
        this.audioElement.src = audioUrl;

        // 播放
        this.audioElement.play().then(() => {
            this.isPlaying = true;
            document.getElementById('player-play-btn').textContent = '⏸';
        }).catch(error => {
            console.error('播放失败:', error);
            alert('播放失败，请检查网络连接或歌曲文件是否存在');
        });
    }

    play() {
        if (this.audioElement && this.currentSong) {
            this.audioElement.play().then(() => {
                this.isPlaying = true;
                document.getElementById('player-play-btn').textContent = '⏸';
            }).catch(error => {
                console.error('播放失败:', error);
            });
        }
    }

    pause() {
        if (this.audioElement) {
            this.audioElement.pause();
            this.isPlaying = false;
            document.getElementById('player-play-btn').textContent = '▶';
        }
    }

    updateProgress() {
        if (!this.audioElement || !this.audioElement.duration) return;

        const currentTime = this.audioElement.currentTime;
        const duration = this.audioElement.duration;
        const progress = (currentTime / duration) * 100;

        document.getElementById('player-progress-fill').style.width = `${progress}%`;
        document.getElementById('player-progress-handle').style.left = `${progress}%`;
        document.getElementById('player-current-time').textContent = this.formatTime(currentTime);
    }

    updateDuration() {
        if (this.audioElement && this.audioElement.duration) {
            document.getElementById('player-duration').textContent = this.formatTime(this.audioElement.duration);
        }
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
// ========== 核心修改点 ==========
// 1. 全局变量名改为和HTML一致的 musicPlayer（原globalMusicPlayer）
let musicPlayer = null;

// 2. 初始化函数适配HTML的调用逻辑（无参数也能初始化）
function initMusicPlayer(options) {
    // 如果HTML不传参数，用默认值（token和userId从HTML的全局变量取）
    const defaultOptions = {
        container: 'music-player-container',
        token: window.token || '',  // 取HTML里的全局token
        userId: window.userId || '' // 取HTML里的全局userId
    };
    const finalOptions = { ...defaultOptions, ...options };

    if (!musicPlayer) {
        musicPlayer = new MusicPlayer(finalOptions);
        // 在这里更新 window.musicPlayer，确保它指向新创建的实例
        window.musicPlayer = musicPlayer;
    }
    return musicPlayer;
}

// 3. 保留原playSong函数，同时兼容HTML的调用
function playSong(song, options = {}) {
    if (!musicPlayer) {
        musicPlayer = initMusicPlayer(options);
    }
    musicPlayer.playSong(song);
}

// 4. 移除export（避免浏览器环境报错，同时把方法挂载到window，确保HTML能访问）
// 把核心方法挂载到window，确保HTML能全局访问
window.MusicPlayer = MusicPlayer;
window.initMusicPlayer = initMusicPlayer;
window.playSong = playSong;
// 这里不再直接赋值 null，而是在 initMusicPlayer 中动态更新
// window.musicPlayer = musicPlayer;