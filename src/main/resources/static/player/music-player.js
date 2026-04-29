// 音乐播放器模块 - 极简排障版（仅保留核心播放/暂停，无本地存储）
class MusicPlayer {
    constructor(options = {}) {
        this.options = {
            container: 'music-player-container',
            token: '',
            userId: '',
            maxPlaySeconds: 30,
            ...options
        };

        this.audioElement = null;
        this.isPlaying = false;
        this._currentSong = null; // 核心：仅内存存储，不做本地恢复
        this.container = document.getElementById(this.options.container);

        // 【新增】标记是否已触发过 30 秒上限
        this.hasReachedMax = false;

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
            <div class="music-player">
                <!-- 左侧：歌曲信息 -->
                <div class="player-left">
                    <div class="song-cover">
                        <img id="player-song-img" src="" alt="封面">
                    </div>
                    <div class="song-info-text">
                        <h4 id="player-song-name" class="song-title">未播放</h4>
                        <p id="player-singer-name" class="song-artist">-</p>
                    </div>
                </div>

                <!-- 中间：播放控制 + 进度条（垂直两行） -->
                <div class="player-center">
                    <!-- 第一行：控制按钮 -->
                    <div class="controls-row">
                        <button class="control-btn" id="player-prev-btn">
                            <span class="material-symbols-outlined">skip_previous</span>
                        </button>
                        <button class="play-pause-btn control-btn" id="player-play-btn" style="background: linear-gradient(135deg, #f0c64d 0%, #ffe4a4 100%);">
                            <span class="material-symbols-outlined" style="color:black;">play_arrow</span>
                        </button>
                        <button class="control-btn" id="player-next-btn">
                            <span class="material-symbols-outlined">skip_next</span>
                        </button>
                    </div>

                    <!-- 第二行：进度条 -->
                    <div class="progress-row">
                        <span id="player-current-time" class="time-display">0:00</span>
                        <div class="progress-container">
                            <div id="player-progress-track" class="progress-bar-wrapper">
                                <div id="player-progress-fill" class="progress-fill"></div>
                                <div id="player-progress-handle" class="progress-handle"></div>
                            </div>
                        </div>
                        <span id="player-duration" class="time-display">0:00</span>
                    </div>
                </div>

                <!-- 右侧：隐藏 -->
                <div class="player-right" style="display: none;">
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
                // 第一步：如果没有当前歌曲，直接提示
                if (!that._currentSong || !that._currentSong.songId) {
                    alert('请先选择一首歌曲播放！');
                    return;
                }

                // 👈【新增】检查是否已播放满30秒，如果是则重置
                if (that.hasReachedMax && that.audioElement.currentTime >= that.options.maxPlaySeconds) {
                    that.audioElement.currentTime = 0;
                    that.hasReachedMax = false;
                    // 更新UI到起始位置
                    const progressFill = document.getElementById('player-progress-fill');
                    const progressHandle = document.getElementById('player-progress-handle');
                    const currentTimeEl = document.getElementById('player-current-time');
                    if (progressFill) progressFill.style.width = '0%';
                    if (progressHandle) progressHandle.style.left = '0%';
                    if (currentTimeEl) currentTimeEl.textContent = '0:00';
                }

                // 第二步：根据状态执行播放/暂停
                if (that.isPlaying) {
                    that.audioElement.pause();
                    that.isPlaying = false;
                    document.getElementById('player-song-img').style.animationPlayState = 'paused';
                    playBtn.querySelector('.material-symbols-outlined').textContent = 'play_arrow';
                } else {
                    that.audioElement.play().then(() => {
                        that.isPlaying = true;
                        document.getElementById('player-song-img').style.animationPlayState = 'running';
                        playBtn.querySelector('.material-symbols-outlined').textContent = 'pause';
                    }).catch(error => {
                        alert('播放失败：' + error.message);
                        console.error('播放异常：', error);
                    });
                }
            });
        }

        // 进度条逻辑（增加progressTrack存在性判断）
    // 改动 进度条点击 + 拖拽逻辑
        if (progressTrack) {
            let isDragging = false;

            // 点击跳转（限制在 maxPlaySeconds 内）
            progressTrack.addEventListener('click', (e) => {
                if (!that._currentSong || !that.audioElement) return;
                const rect = progressTrack.getBoundingClientRect();
                let pos = (e.clientX - rect.left) / rect.width;
                let newTime = pos * that.audioElement.duration;

                // 限制不能超过最大播放时长
                if (newTime > that.options.maxPlaySeconds) {
                    newTime = that.options.maxPlaySeconds;
                    pos = newTime / that.audioElement.duration;
                }

                that.audioElement.currentTime = newTime;

                const progressFill = document.getElementById('player-progress-fill');
                const progressHandle = document.getElementById('player-progress-handle');
                const currentTimeEl = document.getElementById('player-current-time');

                if (progressFill) progressFill.style.width = `${pos * 100}%`;
                if (progressHandle) progressHandle.style.left = `${pos * 100}%`;
                if (currentTimeEl) currentTimeEl.textContent = that.formatTime(newTime);
            });

            // 拖拽开始
            const progressHandle = document.getElementById('player-progress-handle');
            if (progressHandle) {
                progressHandle.addEventListener('mousedown', (e) => {
                    // 👈【改动6】可选：30秒后禁止拖拽
                    if (that.audioElement.currentTime >= that.options.maxPlaySeconds) {
                        return;
                    }
                    isDragging = true;
                    e.preventDefault();
                });
            }

            // 拖拽移动
            document.addEventListener('mousemove', (e) => {
                if (!isDragging || !that.audioElement || !that._currentSong) return;
                const rect = progressTrack.getBoundingClientRect();
                let pos = (e.clientX - rect.left) / rect.width;
                if (pos < 0) pos = 0;
                if (pos > 1) pos = 1;

                let newTime = pos * that.audioElement.duration;
                if (newTime > that.options.maxPlaySeconds) {
                    newTime = that.options.maxPlaySeconds;
                    pos = newTime / that.audioElement.duration;
                }

                that.audioElement.currentTime = newTime;

                const progressFill = document.getElementById('player-progress-fill');
                const handle = document.getElementById('player-progress-handle');
                const currentTimeEl = document.getElementById('player-current-time');

                if (progressFill) progressFill.style.width = `${pos * 100}%`;
                if (handle) handle.style.left = `${pos * 100}%`;
                if (currentTimeEl) currentTimeEl.textContent = that.formatTime(newTime);
            });

            // 拖拽结束
            document.addEventListener('mouseup', () => {
                isDragging = false;
            });
        }

        // 音频事件（增加audioElement存在性判断）
        if (this.audioElement) {
            this.audioElement.addEventListener('timeupdate', () => {
                const currentTime = this.audioElement.currentTime;
                const duration = this.audioElement.duration;

                // 👈【修复】首先检查是否已超限，如果是则直接返回，不重置标志
                if (duration && !isNaN(duration)) {
                    if (currentTime >= this.options.maxPlaySeconds) {
                        if (!this.hasReachedMax) {
                            this.hasReachedMax = true;
                            this.audioElement.pause();
                            this.isPlaying = false;

                            const cover = document.getElementById('player-song-img');
                            if (cover) cover.style.animationPlayState = 'paused';

                            const playBtn = document.getElementById('player-play-btn');
                            if (playBtn) {
                                playBtn.querySelector('.material-symbols-outlined').textContent = 'play_arrow';
                            }

                            this.updateProgressToMax();
                        }
                        // 👈【修复】移除 return，改为在外部统一处理
                    }
                }

                // 👈【修复】只有当未超限时才重置标志并更新进度
                if (currentTime < this.options.maxPlaySeconds) {
                    this.hasReachedMax = false;
                    this.updateProgress();
                }
            });

            this.audioElement.addEventListener('ended', () => {
                that.isPlaying = false;
                if (playBtn) {
                    document.getElementById('player-song-img').style.animationPlayState = 'paused'
                    playBtn.querySelector('.material-symbols-outlined').textContent = 'play_arrow'
                }
            });
            this.audioElement.addEventListener('loadedmetadata', () => that.updateDuration());
        }
    }

    // 新增方法：强制进度条停在最大限制位置（30秒）
    updateProgressToMax() {
        const progressFill = document.getElementById('player-progress-fill');
        const progressHandle = document.getElementById('player-progress-handle');
        const currentTimeEl = document.getElementById('player-current-time');

        const maxTime = this.options.maxPlaySeconds;
        const duration = this.audioElement.duration;

        if (!duration || isNaN(duration)) return;

        const progress = (maxTime / duration) * 100;

        if (progressFill) progressFill.style.width = `${progress}%`;
        if (progressHandle) progressHandle.style.left = `${progress}%`;
        if (currentTimeEl) currentTimeEl.textContent = this.formatTime(maxTime);

        // 👈【新增】确保音频当前时间也被限制在最大值
        if (this.audioElement.currentTime > maxTime) {
            this.audioElement.currentTime = maxTime;
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
                if (playBtn) {
                    document.getElementById('player-song-img').style.animationPlayState = 'running';
                    playBtn.querySelector('.material-symbols-outlined').textContent = 'pause'
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
// window.playSong = function(song) {
//     if (window.musicPlayer) {
//         window.musicPlayer.playSong(song);
//     } else {
//         console.error("播放器未初始化");
//     }
// };
// const songs = ['纤维', '向日葵朝着夜追', '晴']
//
// const audioFiles = [
//     'https://music.163.com/song/media/outer/url?id=2114419823.mp3',
//     'https://music.163.com/song/media/outer/url?id=3333130860.mp3',
//     'https://music.163.com/song/media/outer/url?id=3330453731.mp3'
// ]
//
// const images = [
//     'http://p2.music.126.net/1uuM5dk6JABG7ksG91osnw==/109951169235274408.jpg?param=300x300',
//     'http://p2.music.126.net/V34GxyIJdz7vcdptDI_g-g==/109951172495731859.jpg?param=300x300',
//     'http://p1.music.126.net/9PJAD4ohuWJYr6_WjNATlg==/109951172458960478.jpg?param=300x300'
// ]
//
// const musicInfo = document.querySelector('.music-info')
// const musicName = document.querySelector('.music-name')
// const cover = document.querySelector('.cover')
// const coverImg = document.querySelector('.cover img')
// const progressContainer = document.querySelector('.progress')
// const progressLine = document.querySelector('.progress-line')
// const audio = document.querySelector('audio')
// const playButton = document.getElementById('play')
// const prevButton = document.getElementById('prev')
// const nextButton = document.getElementById('next')
//
// let currentIndex = 0
// let isPlaying = false
//
// //加载歌曲函数 加载歌曲名，地址，封面
// function loadMusic(index) {
//     currentIndex = index
//     musicName.textContent = songs[index]
//     coverImg.src = images[index]
//     audio.src = audioFiles[index]
//
//     // 重置进度条
//     progressLine.style.width = '0%'
//
//     // 重置动画状态
//     cover.style.animationPlayState = 'paused'
//
//     // 如果正在播放，继续播放新歌曲
//     if (isPlaying) {
//         // 延迟一下确保音频加载完成
//         setTimeout(() => {
//             audio.play().then(() => {
//                 // 播放成功后开始旋转动画
//                 cover.style.animationPlayState = 'running'
//             }).catch(e => {
//                 console.log('播放失败:', e)
//                 // 如果播放失败，自动播放下一首
//                 nextSong()
//             })
//         }, 300)
//     }
// }
//
// loadMusic(currentIndex)
//
// function playMusic() {
//     audio.play().then(() => {
//         isPlaying = true
//         cover.style.animationPlayState = 'running'
//         musicInfo.classList.add('play')
//         playButton.classList.remove('icon-a-368793440')
//         playButton.classList.add('icon-Playerpause')
//     })
// }
//
// function pauseMusic() {
//     audio.pause()
//     isPlaying = false
//     cover.style.animationPlayState = 'paused'
//     musicInfo.classList.remove('play')
//     playButton.classList.remove('icon-Playerpause')
//     playButton.classList.add('icon-a-368793440')
// }
//
// //音乐的暂停/播放
// function togglePlay() {
//     if (isPlaying) {
//         pauseMusic()
//     } else {
//         playMusic()
//     }
// }
//
// function updateProgress(e) {
//     // 1. 从事件对象中解构出 duration 和 currentTime
//     const { duration, currentTime } = e.srcElement
//
//     // 2. 检查 duration 是否存在且是有效数字
//     if (duration && !isNaN(duration)) {
//         // 3. 计算播放进度的百分比
//         const progressPercent = (currentTime / duration) * 100
//
//         // 4. 更新进度条的宽度
//         progressLine.style.width = `${progressPercent}%`
//     }
// }
//
// function setProgress(e) {
//     // 1. 获取进度条元素的位置和大小信息
//     const rect = this.getBoundingClientRect();
//
//     // 2. 计算点击位置相对于进度条左侧的距离
//     const clickX = e.clientX - rect.left;
//
//     // 3. 获取进度条的总宽度
//     const width = rect.width;
//
//     // 4. 获取音频的总时长
//     const duration = audio.duration;
//
//     // 5. 如果音频时长有效，计算并设置新的播放时间
//     if (duration && !isNaN(duration)) {
//         audio.currentTime = (clickX / width) * duration;
//     }
// }
//
// function prevSong() {
//     currentIndex--
//     if (currentIndex < 0) {
//         currentIndex = songs.length - 1
//     }
//     loadMusic(currentIndex)
// }
//
// function nextSong() {
//     currentIndex++
//     if (currentIndex > songs.length - 1) {
//         currentIndex = 0
//     }
//     loadMusic(currentIndex)
// }
//
// playButton.addEventListener('click', togglePlay)
// prevButton.addEventListener('click', prevSong)
// nextButton.addEventListener('click', nextSong)
// audio.addEventListener('timeupdate', updateProgress)
// progressContainer.addEventListener('click', setProgress)
//
//
// audio.addEventListener('ended', () => {
//     nextSong()
// })
//
