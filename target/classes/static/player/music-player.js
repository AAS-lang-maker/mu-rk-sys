// 音乐播放器模块 - 极简排障版（仅保留核心播放/暂停，无本地存储）
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
        this._currentSong = null; // 核心：仅内存存储，不做本地恢复
        this.container = document.getElementById(this.options.container);

        // 核心修复：如果 document.body 不存在，延迟初始化
        if (!this.container && !document.body) {
            console.warn("document.body 未加载，延迟初始化播放器...");
            document.addEventListener('DOMContentLoaded', () => {
                this.container = document.createElement('div');
                this.container.id = this.options.container;
                document.body.appendChild(this.container);
                this.init();
            });
            return; // 立即返回，避免在 body 不存在时执行后续代码
        }

        // 创建容器
        if (!this.container) {
            this.container = document.createElement('div');
            this.container.id = this.options.container;
            document.body.appendChild(this.container);
        }

        this.init();
    }

    init() {
        this.createPlayerUI();
        this.bindEvents();
    }

    createPlayerUI() {
        const playerHTML = `
            <div class="music-player" style="padding: 10px; border: 1px solid #ccc; border-radius: 8px;">
                <div class="song-info" style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
                    <img id="player-song-img" src="" alt="封面" style="width: 50px; height: 50px; border-radius: 50%;">
                    <div>
                        <h4 id="player-song-name" style="margin: 0; font-size: 14px;">未播放</h4>
                        <p id="player-singer-name" style="margin: 0; font-size: 12px; color: #666;">-</p>
                    </div>
                </div>
                <div class="player-controls">
                    <div class="progress-bar" style="margin-bottom: 8px;">
                        <div id="player-progress-track" style="height: 6px; background: #eee; border-radius: 3px; cursor: pointer; position: relative;">
                            <div id="player-progress-fill" style="position: absolute; left: 0; top: 0; height: 100%; width: 0%; background: #2196F3; border-radius: 3px;"></div>
                            <div id="player-progress-handle" style="position: absolute; left: 0%; top: 50%; width: 12px; height: 12px; background: #2196F3; border-radius: 50%; transform: translate(-50%, -50%);"></div>
                        </div>
                        <div class="time-info" style="display: flex; justify-content: space-between; font-size: 12px;">
                            <span id="player-current-time">0:00</span>
                            <span id="player-duration">0:00</span>
                        </div>
                    </div>
                    <button id="player-play-btn" style="padding: 8px 16px; border: none; border-radius: 4px; background: #2196F3; color: white; cursor: pointer;">▶ 播放</button>
                </div>
            </div>
        `;

        this.container.innerHTML = playerHTML;

        // 创建音频元素
        this.audioElement = document.createElement('audio');
        this.container.appendChild(this.audioElement);
    }

    bindEvents() {
        // 增加DOM元素存在性判断，避免null调用addEventListener
        const playBtn = document.getElementById('player-play-btn');
        const progressTrack = document.getElementById('player-progress-track');

        // 核心：强制绑定 this，确保永远拿到正确的播放器实例
        const that = this;

        // 播放/暂停按钮 - 极简核心逻辑（增加playBtn存在性判断）
        if (playBtn) {
            playBtn.addEventListener('click', function() {
                // 第一步：如果没有当前歌曲，直接提示（排除ID为空）
                if (!that._currentSong || !that._currentSong.songId) {
                    alert('请先选择一首歌曲播放！');
                    return;
                }

                // 第二步：根据状态执行播放/暂停
                if (that.isPlaying) {
                    // 暂停逻辑
                    that.audioElement.pause();
                    that.isPlaying = false;
                    playBtn.textContent = '▶ 播放';
                } else {
                    // 播放逻辑（复用已有的音频源）
                    that.audioElement.play().then(() => {
                        that.isPlaying = true;
                        playBtn.textContent = '⏸ 暂停';
                    }).catch(error => {
                        alert('播放失败：' + error.message);
                        console.error('播放异常：', error);
                    });
                }
            });
        }

        // 进度条逻辑（增加progressTrack存在性判断）
        if (progressTrack) {
            progressTrack.addEventListener('click', (e) => {
                if (!that._currentSong || !that.audioElement) return;
                const rect = progressTrack.getBoundingClientRect();
                const pos = (e.clientX - rect.left) / rect.width;
                that.audioElement.currentTime = pos * that.audioElement.duration;
            });
        }

        // 音频事件（增加audioElement存在性判断）
        if (this.audioElement) {
            this.audioElement.addEventListener('timeupdate', () => that.updateProgress());
            this.audioElement.addEventListener('ended', () => {
                that.isPlaying = false;
                if (playBtn) { // 增加playBtn存在性判断
                    playBtn.textContent = '▶ 播放';
                }
            });
            this.audioElement.addEventListener('loadedmetadata', () => that.updateDuration());
        }
    }

    // 核心方法：播放指定歌曲（强制赋值 _currentSong）
    playSong(song) {
        // 严格校验：确保传入的歌曲有 ID
        if (!song || !song.songId) {
            alert('歌曲ID不能为空！');
            return;
        }

        // 1. 强制赋值：这一步是关键！确保 _currentSong 绝对有值
        this._currentSong = { ...song };
        // 2. 重置播放状态
        this.isPlaying = false;

        // 所有DOM操作前增加存在性判断，避免null调用
        const songNameEl = document.getElementById('player-song-name');
        const singerNameEl = document.getElementById('player-singer-name');
        const songImgEl = document.getElementById('player-song-img');
        const playBtn = document.getElementById('player-play-btn');

        if (songNameEl) {
            songNameEl.textContent = song.songName || '未知歌曲';
        }
        if (singerNameEl) {
            singerNameEl.textContent = song.singerName || '未知歌手';
        }
        if (songImgEl) {
            songImgEl.src = song.songImg || 'https://picsum.photos/50/50';
        }

        // 3. 设置音频源（带 /music 上下文路径）
        const audioUrl = `/music/api/songGo/play/${song.songId}?token=${encodeURIComponent(this.options.token)}`;
        if (this.audioElement) {
            this.audioElement.src = audioUrl;

            // 4. 自动播放（增加audioElement存在性判断）
            this.audioElement.play().then(() => {
                this.isPlaying = true;
                if (playBtn) { // 增加playBtn存在性判断
                    playBtn.textContent = '⏸ 暂停';
                }
            }).catch(error => {
                alert('播放失败：请检查后端接口是否正常！');
                console.error('接口异常：', audioUrl); // 打印URL，方便你排查后端
            });
        }
    }

    // 辅助方法：更新进度（增加DOM元素存在性判断）
    updateProgress() {
        if (!this.audioElement || !this.audioElement.duration) return;

        const progressFill = document.getElementById('player-progress-fill');
        const progressHandle = document.getElementById('player-progress-handle');
        const currentTimeEl = document.getElementById('player-current-time');

        const progress = (this.audioElement.currentTime / this.audioElement.duration) * 100;

        if (progressFill) {
            progressFill.style.width = `${progress}%`;
        }
        if (progressHandle) {
            progressHandle.style.left = `${progress}%`;
        }
        if (currentTimeEl) {
            currentTimeEl.textContent = this.formatTime(this.audioElement.currentTime);
        }
    }

    // 辅助方法：更新时长（增加DOM元素存在性判断）
    updateDuration() {
        if (!this.audioElement || !this.audioElement.duration) return;

        const durationEl = document.getElementById('player-duration');
        if (durationEl) {
            durationEl.textContent = this.formatTime(this.audioElement.duration);
        }
    }

    // 辅助方法：格式化时间（修复padStart兼容问题）
    formatTime(seconds) {
        if (isNaN(seconds)) return '0:00';
        const mins = Math.floor(seconds / 60);
        const secs = Math.floor(seconds % 60);
        // 替换padStart为兼容写法，避免低版本浏览器报错
        return `${mins}:${(secs < 10 ? '0' : '') + secs}`;
    }
}

// 移除这里的全局实例化，完全交给页面的 initMusicPlayer() 来控制
// window.musicPlayer = new MusicPlayer({
//     token: window.token || '',
//     userId: window.userId || ''
// });

// 全局调用方法：前端列表点击时，直接调用这个方法
window.playSong = function(song) {
    if (window.musicPlayer) {
        window.musicPlayer.playSong(song);
    } else {
        console.error("播放器未初始化");
    }
};