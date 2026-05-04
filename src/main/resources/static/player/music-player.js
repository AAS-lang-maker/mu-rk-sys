// ===============================
// MusicPlayer（方案C最终版）
// 保留你原UI / 进度条 / 30秒试听
// 切页恢复：歌名、歌手、封面、进度、播放状态
// ===============================

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
        this._currentSong = null;
        this.container = document.getElementById(this.options.container);
        this.hasReachedMax = false;

        this.playerChannel = new BroadcastChannel('music_social_sync');

        this.playerChannel.onmessage = (event) => {
            if (event.data.type === 'SYNC_PLAY') {
                this._executePlay(event.data.song);
            }
        };

        if (!this.container && !document.body) {
            document.addEventListener('DOMContentLoaded', () => {
                this.container = document.createElement('div');
                this.container.id = this.options.container;
                document.body.appendChild(this.container);
                this.init();
            });
            return;
        }

        if (!this.container) {
            this.container = document.createElement('div');
            this.container.id = this.options.container;
            document.body.appendChild(this.container);
        }

        this.init();
    }

    // ===============================
    // 初始化
    // ===============================
    init() {
        this.createPlayerUI();
        this.bindEvents();

        setTimeout(() => {
            this.syncCurrentAudioState();
        }, 300);
    }

    // ===============================
    // UI（100%保留你原版）
    // ===============================
    createPlayerUI() {
        const playerHTML = `
            <div class="music-player">

                <div class="player-left">
                    <div class="song-cover">
                        <img id="player-song-img" src="" alt="封面">
                    </div>

                    <div class="song-info-text">
                        <h4 id="player-song-name" class="song-title">未播放</h4>
                        <p id="player-singer-name" class="song-artist">-</p>
                    </div>
                </div>

                <div class="player-center">

                    <div class="controls-row">
                        <button class="control-btn" id="player-prev-btn">
                            <span class="material-symbols-outlined">skip_previous</span>
                        </button>

                        <button class="play-pause-btn control-btn"
                                id="player-play-btn"
                                style="background: linear-gradient(135deg, #f0c64d 0%, #ffe4a4 100%);">
                            <span class="material-symbols-outlined" style="color:black;">
                                play_arrow
                            </span>
                        </button>

                        <button class="control-btn" id="player-next-btn">
                            <span class="material-symbols-outlined">skip_next</span>
                        </button>
                    </div>

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

                <div class="player-right" style="display:none;"></div>

            </div>
        `;

        this.container.innerHTML = playerHTML;

        this.audioElement = document.createElement('audio');
        this.container.appendChild(this.audioElement);
    }

    // ===============================
    // 绑定事件
    // ===============================
    bindEvents() {
        const playBtn = document.getElementById('player-play-btn');
        const progressTrack = document.getElementById('player-progress-track');
        const that = this;

        // 播放暂停
        if (playBtn) {
            playBtn.addEventListener('click', function () {

                if (!that._currentSong || !that._currentSong.songId) {
                    alert('请先选择一首歌曲播放！');
                    return;
                }

                if (
                    that.hasReachedMax &&
                    that.audioElement.currentTime >= that.options.maxPlaySeconds
                ) {
                    that.audioElement.currentTime = 0;
                    that.hasReachedMax = false;
                }

                if (that.isPlaying) {

                    that.audioElement.pause();
                    that.isPlaying = false;

                    playBtn.querySelector(
                        '.material-symbols-outlined'
                    ).textContent = 'play_arrow';

                } else {

                    that.audioElement.play().then(() => {
                        that.isPlaying = true;

                        playBtn.querySelector(
                            '.material-symbols-outlined'
                        ).textContent = 'pause';

                    }).catch(() => {});
                }

                that.savePlayerState();
            });
        }

        // 点击进度条
        if (progressTrack) {
            progressTrack.addEventListener('click', (e) => {

                if (!that.audioElement.duration) return;

                const rect = progressTrack.getBoundingClientRect();

                let percent =
                    (e.clientX - rect.left) / rect.width;

                if (percent < 0) percent = 0;
                if (percent > 1) percent = 1;

                let newTime =
                    percent * that.audioElement.duration;

                if (newTime > that.options.maxPlaySeconds) {
                    newTime = that.options.maxPlaySeconds;
                }

                that.audioElement.currentTime = newTime;

                that.updateProgress();
                that.savePlayerState();
            });
        }

        // 播放进度
        this.audioElement.ontimeupdate = () => {

            const currentTime = this.audioElement.currentTime;

            if (currentTime >= this.options.maxPlaySeconds) {

                this.audioElement.pause();
                this.audioElement.currentTime =
                    this.options.maxPlaySeconds;

                this.isPlaying = false;
                this.hasReachedMax = true;

                if (playBtn) {
                    playBtn.querySelector(
                        '.material-symbols-outlined'
                    ).textContent = 'play_arrow';
                }
            }

            this.updateProgress();
            this.savePlayerState();
        };

        // 加载完成
        this.audioElement.onloadedmetadata = () => {
            this.updateDuration();
        };

        // 播放结束
        this.audioElement.onended = () => {
            this.isPlaying = false;

            if (playBtn) {
                playBtn.querySelector(
                    '.material-symbols-outlined'
                ).textContent = 'play_arrow';
            }
        };
    }

    // ===============================
    // 播放歌曲
    // ===============================
    playSong(song) {

        if (!song || !song.songId) {
            alert('歌曲ID不能为空！');
            return;
        }

        this._executePlay(song);

        this.playerChannel.postMessage({
            type: 'SYNC_PLAY',
            song: song
        });

        this.savePlayerState();
    }

    _executePlay(song) {

        this._currentSong = { ...song };
        this.isPlaying = false;
        this.hasReachedMax = false;

        const songNameEl =
            document.getElementById('player-song-name');

        const singerNameEl =
            document.getElementById('player-singer-name');

        const songImgEl =
            document.getElementById('player-song-img');

        const playBtn =
            document.getElementById('player-play-btn');

        if (songNameEl)
            songNameEl.textContent =
                song.songName || '未知歌曲';

        if (singerNameEl)
            singerNameEl.textContent =
                song.singerName || '未知歌手';

        if (songImgEl)
            songImgEl.src =
                song.songImg || 'https://picsum.photos/50/50';

        const audioUrl =
            `/music/api/songGo/play/${song.songId}?token=${encodeURIComponent(this.options.token)}`;

        this.audioElement.src = audioUrl;

        this.audioElement.play().then(() => {

            this.isPlaying = true;

            if (playBtn) {
                playBtn.querySelector(
                    '.material-symbols-outlined'
                ).textContent = 'pause';
            }

            this.savePlayerState();

        }).catch(() => {});
    }

    // ===============================
    // 切页恢复
    // ===============================
    syncCurrentAudioState() {

        const saved =
            localStorage.getItem('global_player_state');

        if (!saved) return;

        const state = JSON.parse(saved);

        if (!state.song) return;

        this._currentSong = state.song;

        const songNameEl =
            document.getElementById('player-song-name');

        const singerNameEl =
            document.getElementById('player-singer-name');

        const songImgEl =
            document.getElementById('player-song-img');

        const playBtn =
            document.getElementById('player-play-btn');

        if (songNameEl)
            songNameEl.textContent =
                state.song.songName || '未知歌曲';

        if (singerNameEl)
            singerNameEl.textContent =
                state.song.singerName || '未知歌手';

        if (songImgEl)
            songImgEl.src =
                state.song.songImg ||
                'https://picsum.photos/50/50';

        const audioUrl =
            `/music/api/songGo/play/${state.song.songId}?token=${encodeURIComponent(this.options.token)}`;

        this.audioElement.src = audioUrl;

        this.audioElement.onloadedmetadata = () => {

            this.audioElement.currentTime =
                state.currentTime || 0;

            this.updateDuration();
            this.updateProgress();

            if (state.isPlaying) {

                this.audioElement.play()
                    .then(() => {

                        this.isPlaying = true;

                        if (playBtn) {
                            playBtn.querySelector(
                                '.material-symbols-outlined'
                            ).textContent = 'pause';
                        }

                    }).catch(() => {
                    this.isPlaying = false;
                });
            }
        };
    }

    // ===============================
    // 保存状态
    // ===============================
    savePlayerState() {

        if (!this._currentSong) return;

        localStorage.setItem(
            'global_player_state',
            JSON.stringify({
                song: this._currentSong,
                currentTime:
                    this.audioElement.currentTime || 0,
                isPlaying:
                    !this.audioElement.paused
            })
        );
    }

    // ===============================
    // UI更新
    // ===============================
    updateProgress() {

        if (!this.audioElement.duration) return;

        const progressFill =
            document.getElementById('player-progress-fill');

        const progressHandle =
            document.getElementById('player-progress-handle');

        const currentTimeEl =
            document.getElementById('player-current-time');

        const progress =
            (this.audioElement.currentTime /
                this.audioElement.duration) * 100;

        if (progressFill)
            progressFill.style.width =
                `${progress}%`;

        if (progressHandle)
            progressHandle.style.left =
                `${progress}%`;

        if (currentTimeEl)
            currentTimeEl.textContent =
                this.formatTime(
                    this.audioElement.currentTime
                );
    }

    updateDuration() {

        if (!this.audioElement.duration) return;

        const durationEl =
            document.getElementById('player-duration');

        if (durationEl) {
            durationEl.textContent =
                this.formatTime(
                    this.audioElement.duration
                );
        }
    }

    formatTime(seconds) {

        if (isNaN(seconds)) return '0:00';

        const mins = Math.floor(seconds / 60);
        const secs = Math.floor(seconds % 60);

        return `${mins}:${(secs < 10 ? '0' : '') + secs}`;
    }
}