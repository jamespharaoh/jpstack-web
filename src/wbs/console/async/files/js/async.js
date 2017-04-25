var async = {

	_state: "none",

	_webSocketUrl: [
		{
			"http:": "ws:",
			"https:": "wss:",
		} [location.protocol],
		"//",
		location.hostname,
		"/_async",
	].join (''),

	_keepaliveTime: 1000,
	_errorWaitTime: 5000,

	_webSocket: undefined,

	_onConnectCallbacks: [],
	_onDisconnectCallbacks: [],

	_subscriptions: undefined,

};

async._init =
function asyncInit () {

	setTimeout (async._connect);

	setTimeout (async._keepaliveLoop);

};

async._connect =
function asyncConnect () {

	if (async._state != "none") {

		throw new Error (
			"Unable to connect in state: " + async._state);

	}

	async._state = "connecting";

	console.log (
		"Connection to " + async._webSocketUrl);

	async._webSocket =
		new WebSocket (
			async._webSocketUrl);

	async._webSocket.onerror = function () {

		console.error (
			"Web socket connection failed");

	};

	async._webSocket.onopen = function () {

		if (async._state != "connecting") {
			throw new Error (
				"Invalid state: " + async._state);
		}

		async._state = "connected";

		console.log (
			"Web socket connected");

		async._subscriptions = {};

		async._onConnectCallbacks.forEach (
			function (onConnectCallback) {
				onConnectCallback ();
			}
		);

	};

	async._webSocket.onclose = function () {

		if (async._state == "none") {

			throw new Error (
				"Invalid state: " + async._state);

		}

		async._state = "none";

		console.warn (
			"Web socket closed");

		async._subscriptions = undefined;

		async._onDisconnectCallbacks.forEach (
			function (onDisconnectCallback) {
				onDisconnectCallback ();
			}
		);

		setTimeout (
			async._connect,
			async._errorWaitTime);

	};

	async._webSocket.onmessage = function (event) {

		var message = JSON.parse (event.data);

		var endpoint = message.endpoint;
		var payload = message.payload;

		if (! (endpoint in async._subscriptions)) {

			console.error (
				"Received message from unknown endpoint: " + endpoint);

			return;

		}

		callback = async._subscriptions [endpoint];

		callback (payload);

	};

};

async.onConnect = function asyncOnConnect (callback) {

	async._onConnectCallbacks.push (
		callback);

	if (async._state == "connected") {
		callback ();
	}

}

async.onDisconnect = function asyncOnDisconnect (callback) {

	async._onDisconnectCallbacks.push (
		callback);

}

async.send = function asyncSend (endpoint, payload) {

	if (async._state != "connected") {
		return;
	}

	async._webSocket.send (
		JSON.stringify ({
			sessionId: Cookies.get ("wbs-session-id"),
			userId: Number (Cookies.get ("wbs-user-id")),
			endpoint: endpoint,
			payload: payload,
		})
	);

}

async.subscribe = function asyncSubscribe (endpoint, handler) {

	if (endpoint in async._subscriptions) {

		throw new Error (
			"Duplicate subscription: " + endpoint);

	}

	async._subscriptions [endpoint] = handler;

	async.send (endpoint, {});

}

async._keepaliveLoop =
function asyncKeepaliveLoop () {

	async._keepaliveSend ();

	setTimeout (
		async._keepaliveLoop,
		async._keepaliveTime);

};

async._keepaliveSend =
function asyncSendKeepalive () {

	async.send (
		"/status/keepalive",
		{});

};

$(async._init);

// ex: noet ts=4 filetype=javascript