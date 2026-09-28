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
			    // Estrae i tre parametri delimitati da virgola dentro hold_info(...)
			    const matchArgs = content.match(/hold_info\(\s*'([^']*)'\s*,\s*'([^']*)'\s*,\s*'([^']*)'\s*\)/);
			    
			    if (matchArgs) {
			        document.getElementById('line-state').innerText = matchArgs[1] || 'IDLE';
			        document.getElementById('line-hold').innerText  = matchArgs[2] || '[]';
			        document.getElementById('line-msg').innerText   = matchArgs[3] || '';
			    } else {
			        // Fallback tramite split se la tupla ha formato grezzo
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
            else if (msgId === 'led') {
                const ledState = content.replace(/'/g, '').trim();
                setLedState(ledState);
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

	const containerSensorBtn = document.getElementById('btn-mock-container');
	if (containerSensorBtn) {
	    let isOccupied = false;
	    containerSensorBtn.addEventListener('click', () => {
	        isOccupied = !isOccupied;
	        
	        containerSensorBtn.innerText = isOccupied ? "Sensore: PRESENTE (true)" : "Sensore: LIBERO (false)";
	        containerSensorBtn.classList.toggle('occupied', isOccupied);
	        
	        // Racchiudiamo il valore booleano tra apici: 'true' / 'false'
	        const payload = isOccupied ? "true" : "false";
	        sendApplMsg("containerSensed", "dispatch", "webgui", "ioport", payload);
	    });
	}
	
	const faultBtn = document.getElementById('btn-mock-fault');

	if (faultBtn) {
	    faultBtn.addEventListener('click', () => {
	        console.log("Pulsante Simula Guasto premuto");
	        
	        // Invia il dispatch inoltrando la tupla outOfService(...)
	        sendApplMsg("outOfService", "dispatch", "webgui", "ioport", "outOfService('sensor_fault')");
	    });
	} else {
	    console.error("Elemento 'btn-mock-fault' non trovato nel DOM!");
	}
}

function setLedState(state) {
    const ledEl = document.getElementById('led-indicator');
    if (!ledEl) return;

    ledEl.classList.remove('off', 'blinking', 'red');

    if (state === 'blinking') {
        ledEl.classList.add('blinking');
    } else if (state === 'red') {
        ledEl.classList.add('red');
    } else {
        ledEl.classList.add('off');
    }
}