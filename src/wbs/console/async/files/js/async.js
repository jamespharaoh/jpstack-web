var async = {

	state: "none",

	webSocketUrl: [
		{
			"http:": "ws:",
			"https:": "wss:",
		} [location.protocol],
		"//",
		location.hostname,
		"/_async",
	].join (''),

	_webSocket: undefined,

	_onConnectCallbacks: [],
	_onDisconnectCallbacks: [],

	_subscriptions: undefined,

};

async._connect = function asyncConnect () {

	if (async.state != "none") {

		throw new Error (
			"Unable to connect in state: " + async.state);

	}

	async.state = "connecting";

	console.log (
		"Connection to " + async.webSocketUrl);

	async._webSocket =
		new WebSocket (
			async.webSocketUrl);

	async._webSocket.onerror = function () {

		console.error (
			"Web socket connection failed");

	};

	async._webSocket.onopen = function () {

		if (async.state != "connecting") {
			throw new Error (
				"Invalid state: " + async.state);
		}

		async.state = "connected";

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

		if (async.state == "none") {

			throw new Error (
				"Invalid state: " + async.state);

		}

		async.state = "none";

		console.warn (
			"Web socket closed");

		async._subscriptions = undefined;

		async._onDisconnectCallbacks.forEach (
			function (onDisconnectCallback) {
				onDisconnectCallback ();
			}
		);

		setTimeout (async._connect, 5000);

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

	if (async.state == "connected") {
		callback ();
	}

}

async.onDisconnect = function asyncOnDisconnect (callback) {

	async._onDisconnectCallbacks.push (
		callback);

}

async.send = function asyncSend (endpoint, payload) {

	if (async.state != "connected") {
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

$(async._connect);

// ex: noet ts=4 filetype=javascript