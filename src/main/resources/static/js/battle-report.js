/* =========================
   黑金战报系统（全局版）
========================= */

(function () {
    if (window.__battleReportLoaded__) return;
    window.__battleReportLoaded__ = true;

    // ---------- 自动插入 DOM ----------
    document.body.insertAdjacentHTML(
        "beforeend",
        `
    <div class="overlay" id="battleOverlay"></div>

    <div class="modal-container" id="battleContainer">
      <div class="modal-card" id="battleCard">
        <div class="modal-icon">🏆</div>
        <h2 class="modal-title" id="battleTitle">战报标题</h2>
        <div class="modal-body" id="battleContent"></div>
        <button class="battle-btn" id="battleCloseBtn">我知道了</button>
      </div>
    </div>

    <div id="particle-container"></div>
  `
    );

    // ---------- 元素 ----------
    const container = document.getElementById("battleContainer");
    const card = document.getElementById("battleCard");
    const overlay = document.getElementById("battleOverlay");
    const closeBtn = document.getElementById("battleCloseBtn");
    const particleArea = document.getElementById("particle-container");

    let autoCloseTimer = null;
    let ws = null;

    // ---------- 获取 userId ----------
    function getUserId() {
        return (
            localStorage.getItem("user_id") ||
            sessionStorage.getItem("user_id") ||
            localStorage.getItem("userId") ||
            sessionStorage.getItem("userId") ||
            "1001"
        );
    }

    // ---------- 显示战报 ----------
    function showBattleReport(data) {
        document.getElementById("battleTitle").innerText =
            data.title || "系统战报";

        document.getElementById("battleContent").innerHTML = `
      <p>当前排名：<span class="report-rank">第 ${data.rank || "--"} 名</span></p>
      <p>上升了 ${data.up || "--"} 位</p>
    `;

        container.classList.add("active");
        overlay.classList.add("active");

        burstParticles(
            window.innerWidth / 2,
            window.innerHeight / 2,
            28,
            "#fcf6ba"
        );

        clearTimeout(autoCloseTimer);
        autoCloseTimer = setTimeout(closeBattleReport, 5000);
    }

    // 暴露全局方法（方便你手动测试）
    window.showBattleReport = showBattleReport;

    // ---------- 关闭 ----------
    function closeBattleReport() {
        card.style.animation = "battleGoOut .45s ease forwards";
        overlay.classList.remove("active");

        setTimeout(() => {
            container.classList.remove("active");
            card.style.animation = "";
            card.style.transform = "rotateX(0deg) rotateY(0deg)";
        }, 450);
    }

    closeBtn.addEventListener("click", closeBattleReport);

    container.addEventListener("mouseenter", () => {
        clearTimeout(autoCloseTimer);
    });

    container.addEventListener("mouseleave", () => {
        autoCloseTimer = setTimeout(closeBattleReport, 5000);
    });

    // ---------- 鼠标 3D 视差 ----------
    document.addEventListener("mousemove", (e) => {
        if (!container.classList.contains("active")) return;

        const rect = card.getBoundingClientRect();
        const centerX = rect.left + rect.width / 2;
        const centerY = rect.top + rect.height / 2;

        const angleX = (centerY - e.clientY) / 16;
        const angleY = (e.clientX - centerX) / 22;

        card.style.transform = `rotateX(${angleX}deg) rotateY(${angleY}deg)`;
    });

    // ---------- 粒子 ----------
    function burstParticles(x, y, count, color) {
        for (let i = 0; i < count; i++) {
            const p = document.createElement("div");
            p.className = "particle";

            const size = Math.random() * 8 + 2;
            p.style.width = size + "px";
            p.style.height = size + "px";
            p.style.background = color;
            p.style.left = x + "px";
            p.style.top = y + "px";
            p.style.opacity = "1";

            particleArea.appendChild(p);

            const dx = (Math.random() - 0.5) * 320;
            const dy = (Math.random() - 0.5) * 320;

            p.animate(
                [
                    { transform: "translate(0,0)", opacity: 1 },
                    { transform: `translate(${dx}px,${dy}px)`, opacity: 0 }
                ],
                {
                    duration: 900 + Math.random() * 600,
                    easing: "ease-out"
                }
            ).onfinish = () => p.remove();
        }
    }

    // ---------- websocket ----------
    function connectBattleWs() {
        const userId = getUserId();

        ws = new WebSocket(
            `ws://${location.host}/music/ws-endpoint/${userId}`
        );

        ws.onopen = function () {
            console.log("战报 websocket 已连接");
        };

        ws.onmessage = function (event) {
            console.log("收到战报:", event.data);

            try {
                const data = JSON.parse(event.data);
                showBattleReport(data);
            } catch (e) {
                showBattleReport({
                    title: event.data,
                    rank: "--",
                    up: "--"
                });
            }
        };

        ws.onclose = function () {
            console.log("战报连接关闭，3秒后重连");
            setTimeout(connectBattleWs, 3000);
        };

        ws.onerror = function () {
            console.log("战报连接异常");
        };
    }

    connectBattleWs();
})();