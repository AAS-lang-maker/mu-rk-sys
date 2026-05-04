class MusicAIAgent {
    constructor(config) {
        this.inputEl = document.querySelector(config.input);
        this.sendBtnEl = document.querySelector(config.sendButton);
        this.chatAreaEl = document.querySelector(config.messageContainer);
        this.isStreaming = false;
        this.typeQueue = [];     // 打字队列
        this.isTyping = false;  // 是否正在打字
        this.currentBubble = null; // 当前AI气泡

        if (!this.inputEl || !this.sendBtnEl || !this.chatAreaEl) {
            console.error('MusicAIAgent: DOM element not found', config);
            return;
        }

        this.bindEvents();
    }

    bindEvents() {
        this.sendBtnEl.addEventListener('click', () => this.sendMessage());
        this.inputEl.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.isComposing) {
                e.preventDefault();
                this.sendMessage();
            }
        });
    }

    sendMessage() {
        if (this.isStreaming) return;

        const userMessage = this.inputEl.value.trim();
        if (!userMessage) return;

        this.renderBuffer = '';
        this.typeQueue = [];
        this.isTyping = false;

        this.appendUserBubble(userMessage);
        this.inputEl.value = '';
        this.setInputsDisabled(true);
        this.isStreaming = true;

        const token = localStorage.getItem('token');
        const aiBubble = this.appendAIBubble('');
        this.currentBubble = aiBubble;
        const self = this;

        console.log('[MusicAIAgent] 发送消息:', userMessage);

        fetch('/music/api/agent/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'token': token || ''
            },
            body: new URLSearchParams({ token: token || '', userMessage })
        }).then(response => {
            console.log('[MusicAIAgent] 响应状态:', response.status);
            if (response.status === 401) {
                throw new Error('UNAUTHORIZED');
            }
            if (!response.ok) {
                throw new Error('SERVER_ERROR');
            }

            return response.clone().text().then(text => {
                const trimmed = text.trim();
                if (trimmed.startsWith('{') || trimmed.startsWith('[')) {
                    console.log('[MusicAIAgent] 检测到JSON响应, 内容:', trimmed.slice(0, 200));
                    self.handleJSONError(text, aiBubble);
                    return null;
                }
                console.log('[MusicAIAgent] 检测到SSE流响应');
                return response.body.getReader();
            });
        }).then(reader => {
            if (!reader) return;

            const decoder = new TextDecoder();
            let buffer = '';
            let chunkCount = 0;

            console.log('[MusicAIAgent] 开始读取流');

            function readChunk() {
                reader.read().then(({ done, value }) => {
                    if (done) {
                        console.log('[MusicAIAgent] 流读取完毕, 总chunk数:', chunkCount + ', buffer剩余:', buffer.length);
                        console.log('[MusicAIAgent] buffer原始内容(前500字符):', JSON.stringify(buffer.slice(0, 500)));
                        if (buffer.trim()) {
                            self.parseSSEEvents(buffer, aiBubble);
                        }
                        self.onStreamEnd();
                        return;
                    }

                    chunkCount++;
                    const chunkText = decoder.decode(value, { stream: true });
                    buffer += chunkText;

                    if (chunkCount <= 3 || chunkCount % 10 === 0) {
                        console.log('[MusicAIAgent] chunk#' + chunkCount + ', 大小:' + chunkText.length + ', buffer总长:' + buffer.length);
                    }

                    // self.parseSSEEvents(buffer, aiBubble);

                    const lastDoubleNl = buffer.lastIndexOf('\n\n');
                    if (lastDoubleNl !== -1) {
                        const completeEvents = buffer.slice(0, lastDoubleNl + 2);
                        // 只解析这些完整的事件
                        self.parseSSEEvents(completeEvents, aiBubble);
                        // 然后把已经解析掉的部分从 buffer 中删掉，防止下次重复解析
                        buffer = buffer.slice(lastDoubleNl + 2);
                    }

                    readChunk();
                }).catch(err => {
                    console.error('[MusicAIAgent] 流读取错误:', err);
                    self.updateAIBubble(aiBubble, '回复过程中出现错误，请重试。');
                    self.onStreamEnd();
                });
            }

            readChunk();
        }).catch(err => {
            console.error('[MusicAIAgent] 请求错误:', err);
            if (err.message === 'UNAUTHORIZED') {
                alert('登录已过期，请重新登录');
            } else if (err.message === 'SERVER_ERROR') {
                alert('系统繁忙，请稍后重试');
            } else {
                alert('网络连接失败，请检查网络');
            }
            self.updateAIBubble(aiBubble, '抱歉，回复出现问题，请稍后重试。');
            self.onStreamEnd();
        });
    }

    handleJSONError(text, aiBubble) {
        console.log('[MusicAIAgent] 处理JSON错误响应');
        try {
            const json = JSON.parse(text);
            let errorMsg = 'AI 助手暂时不可用，请稍后再试 ~';
            if (json.msg) {
                if (json.msg.includes('getUserId') || json.msg.includes('null')) {
                    errorMsg = '🔑 登录状态已失效，请重新登录后再试 ~';
                } else if (json.code === 500) {
                    errorMsg = '⚠️ AI 助手正在休息中，稍后再来找我吧 ~';
                }
            }
            this.updateAIBubble(aiBubble, errorMsg);
        } catch (e) {
            this.updateAIBubble(aiBubble, 'AI 助手暂时不可用，请稍后再试 ~');
        }
        this.onStreamEnd();
    }

    parseSSEEvents(data, aiBubble) {
        const events = data.split('\n\n');
        let matchedCount = 0;

        for (const event of events) {
            if (!event.trim()) continue;

            for (const line of event.split('\n')) {
                const trimmed = line.trim();

                if (trimmed.startsWith('data:')) {
                    let content = trimmed.slice(5).trim();
                    if (content && content !== '[DONE]') {
                        if (matchedCount < 3) {
                            console.log('[MusicAIAgent] 匹配到data行, 内容:', JSON.stringify(content));
                        }
                        this.updateAIBubble(aiBubble, content);
                        matchedCount++;
                    }
                }
            }
        }

        if (data.trim() && matchedCount === 0) {
            console.log('[MusicAIAgent] parseSSEEvents未匹配任何内容, 数据预览:', JSON.stringify(data.slice(0, 200)));
        }
    }

    appendUserBubble(message) {
        const bubble = document.createElement('div');
        bubble.className = 'ai-bubble user-bubble';
        bubble.innerHTML = `<p>${this.escapeHtml(message)}</p>`;
        this.chatAreaEl.appendChild(bubble);
        this.scrollToBottom();
    }

    appendAIBubble(initialContent) {
        const bubble = document.createElement('div');
        bubble.className = 'ai-bubble';

        bubble.innerHTML = `
        <p></p>
        <span class="typing-cursor">▋</span>
    `;

        this.chatAreaEl.appendChild(bubble);
        this.scrollToBottom();
        return bubble;
    }

    updateAIBubble(bubble, content) {

        // 把新文字放进队列
        this.typeQueue.push(...content.split(''));

        // 如果没在打字，就开始
        if (!this.isTyping) {
            this.startTyping();
        }
    }

    startTyping() {

        if (this.typeQueue.length === 0) {
            this.isTyping = false;
            return;
        }

        this.isTyping = true;

        const char = this.typeQueue.shift();

        const textEl = this.currentBubble.querySelector('p');

        if (textEl) {
            this.renderBuffer = (this.renderBuffer || '') + char;

            // 打字阶段显示纯文本
            textEl.textContent = this.renderBuffer;

            this.scrollToBottom();
        }

        let speed = 25;

        if (char === '，' || char === '。' || char === '！') {
            speed = 80;
        }

        if (char === ' ') {
            speed = 10;
        }

        setTimeout(() => this.startTyping(), speed);
    }

    setInputsDisabled(disabled) {
        this.inputEl.disabled = disabled;
        this.sendBtnEl.disabled = disabled;
    }

    scrollToBottom() {
        requestAnimationFrame(() => {
            this.chatAreaEl.scrollTop = this.chatAreaEl.scrollHeight;
        });
    }

    onStreamEnd() {

        const waitFinish = () => {

            if (this.typeQueue.length === 0 && !this.isTyping) {

                const textEl = this.currentBubble.querySelector('p');

                if (textEl) {

                    let finalText = this.renderBuffer || '';

// =======================
// AI 输出格式修复器
// =======================

// 1. 标题 ### 后补空格
                    finalText = finalText.replace(/(#{1,6})([^\s#])/g, '$1 $2');

// 2. 标题前强制换行
                    finalText = finalText.replace(/([^\n])(#{1,6}\s)/g, '$1\n\n$2');

// 3. 数字列表前换行
                    finalText = finalText.replace(/(\d+\.)/g, '\n$1');

// 4. 列表项后面补空格
                    finalText = finalText.replace(/(\d+\.)([^\s])/g, '$1 $2');

// 5. - 前换行
                    finalText = finalText.replace(/([^\n])-\s/g, '$1\n- ');

// 6. 加粗修复 **文字**
                    finalText = finalText.replace(/\*\*([^\*]+)\*\*/g, '**$1**');

// 7. 英文歌名连写修复（TakeFive → Take Five）
                    finalText = finalText.replace(/([a-z])([A-Z])/g, '$1 $2');

// 8. 多余空行压缩
                    finalText = finalText.replace(/\n{3,}/g, '\n\n');

                    textEl.innerHTML = marked.parse(finalText);
                }

                const cursor = this.currentBubble.querySelector('.typing-cursor');
                if (cursor) cursor.remove();

                this.isStreaming = false;
                this.setInputsDisabled(false);
                this.inputEl.focus();
                return;
            }

            setTimeout(waitFinish, 100);
        };

        waitFinish();
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// ====== 使用示例 ======

// A 页面（mylove-page）初始化示例
// const myloveAgent = new MusicAIAgent({
//   input: '.ai-input',
//   sendButton: '.send-btn',
//   messageContainer: '.ai-chat-area'
// });

// B 页面（search-page）初始化示例  
// const searchAgent = new MusicAIAgent({
//   input: '.chat-input-field',
//   sendButton: '.chat-send-btn',
//   messageContainer: '.chat-messages-area'
// });
