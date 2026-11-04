let socket = null;
let msgNum = 0;

function sendApplMsg(msgId, msgType, sender, receiver, content) {
    if (socket && socket.readyState === WebSocket.OPEN) {
        msgNum++;
        const msgStr = `msg(${msgId},${msgType},${sender},${receiver},${content},${msgNum})`;
        console.log("Invio IApplMessage:", msgStr);
        socket.send(msgStr);
    } else {
        console.error("WebSocket non connesso.");
    }
}

window.addEventListener('DOMContentLoaded', () => {
    initWebSocket();
    setupEventListeners();
});

function initWebSocket() {
    const host = window.location.host;
    socket = new WebSocket(`ws://${host}/ws/ioport`);

    socket.onopen = () => {
        console.log("Connessione WebSocket stabilita.");
        document.getElementById('dot').classList.add('connected');
        document.getElementById('status').innerText = "Connesso";
    };

    socket.onmessage = (event) => {
        const rawMsg = event.data;
        console.log("Ricevuto IApplMessage:", rawMsg);

        const regex = /msg\(\s*(\w+)\s*,\s*\w+\s*,\s*\w+\s*,\s*\w+\s*,\s*(.+)\s*,\s*\d+\s*\)/;
        const match = rawMsg.match(regex);

        if (match) {
            const msgId = match[1];
            const content = match[2];

			if (msgId === 'hold_status') {
			    const matchArgs = content.match(/hold_info\(\s*'([^']*)'\s*,\s*'([^']*)'\s*,\s*'([^']*)'\s*\)/);
			    
			    if (matchArgs) {
			        document.getElementById('line-state').innerText = matchArgs[1] || 'IDLE';
			        document.getElementById('line-hold').innerText  = matchArgs[2] || '[]';
			        document.getElementById('line-msg').innerText   = matchArgs[3] || '';
			    } else {
			        console.warn("Formato hold_status non standard, tento parsing generico:", content);
			        const rawArgs = content.replace(/hold_info\(|\)/g, '').split("', '").map(s => s.replace(/'/g, '').trim());
			        document.getElementById('line-state').innerText = rawArgs[0] || 'IDLE';
			        document.getElementById('line-hold').innerText  = rawArgs[1] || '[]';
			        document.getElementById('line-msg').innerText   = rawArgs[2] || '';
			    }
			} 
            else if (msgId === 'working_state') {
                const state = content.replace(/'/g, '').trim();
                const serviceStatusEl = document.getElementById('service-status');

                if (state === 'OUT_OF_SERVICE') {
                    serviceStatusEl.innerText = "OUT OF SERVICE";
                    serviceStatusEl.classList.add('out');
                } else {
                    serviceStatusEl.innerText = "SERVICE WORKING";
                    serviceStatusEl.classList.remove('out');
                }
            }
            // NUOVO: Gestione dello stato del sensore IOPort (updateioport(true/false))
            else if (msgId === 'updateioport') {
                const isOccupied = content.includes('true');
                const sensorBadge = document.getElementById('sensor-badge');

                if (sensorBadge) {
                    if (isOccupied) {
                        sensorBadge.innerText = "SENSORE: OCCUPATO";
                        sensorBadge.className = "sensor-badge badge-occupied";
                    } else {
                        sensorBadge.innerText = "SENSORE: LIBERO";
                        sensorBadge.className = "sensor-badge badge-free";
                    }
                }
            }
        }
    };

    socket.onclose = () => {
        document.getElementById('dot').classList.remove('connected');
        document.getElementById('status').innerText = "Disconnesso";
        setTimeout(initWebSocket, 2000);
    };

    socket.onerror = (err) => {
        console.error("Errore WebSocket:", err);
    };
}

function setupEventListeners() {
    const pushBtn = document.getElementById('btn-push');
    if (pushBtn) {
        pushBtn.addEventListener('click', () => {
            sendApplMsg("button_pushed", "dispatch", "webgui", "ioport", "1");
        });
    }
}