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
	_calls: undefined,

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
		async._calls = {};

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
		async._calls = undefined;

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

		//console.debug ("ASYNC RECEIVE: " + event.data);

		var endpoint = message.endpoint;
		var payload = message.payload;

		if (endpoint == "/authentication-error") {

			console.error (
				"Received authentication failure");

			window.top.location = "/";

			return;

		}

		if ("messageId" in message) {

			var messageId = message.messageId;

			if (! (messageId in async._calls)) {

				console.error (
					"Received message with unknown id: " + messageId);

			}

			callback = async._calls [messageId];

			delete async._calls [messageId];

			callback (payload);

		} else {

			if (! (endpoint in async._subscriptions)) {

				console.error (
					"Received message from unknown endpoint: " + endpoint);

				return;

			}

			callback = async._subscriptions [endpoint];

			callback (payload);

		}

	};

};

async.onConnect =
function onConnect (callback) {

	async._onConnectCallbacks.push (
		callback);

	if (async._state == "connected") {
		callback ();
	}

}

async.onDisconnect =
function onDisconnect (callback) {

	async._onDisconnectCallbacks.push (
		callback);

}

async.send =
function send (endpoint, payload) {

	if (async._state != "connected") {
		return;
	}

	var sessionId = Cookies.get ("wbs-session-id");
	var userId = Cookies.get ("wbs-user-id");

	if (! sessionId || ! userId) {

		console.error (
			"Authentication cookies not present");

		window.top.location = "/";

		return;

	}

	var messageId =
		async._generateRandomId ();

	var data = {
		sessionId: Cookies.get ("wbs-session-id"),
		userId: Number (Cookies.get ("wbs-user-id")),
		endpoint: endpoint,
		messageId: messageId,
		payload: payload,
	};

	var dataString =
		JSON.stringify (data);

	//console.debug ("ASYNC SEND: " + dataString);

	async._webSocket.send (
		dataString);

	return messageId;

};

async.call =
function call (endpoint, payload, callback) {

	var messageId =
		async.send (
			endpoint,
			payload);

	if (messageId) {

		async._calls [messageId] =
			callback;

	}

};

async.subscribe =
function subscribe (endpoint, handler) {

	if (endpoint in async._subscriptions) {

		throw new Error (
			"Duplicate subscription: " + endpoint);

	}

	async._subscriptions [endpoint] = handler;

	async.send (endpoint, {});

}

async._keepaliveLoop =
function keepaliveLoop () {

	async._keepaliveSend ();

	setTimeout (
		async._keepaliveLoop,
		async._keepaliveTime);

};

async._keepaliveSend =
function keepaliveSend () {

	async.send (
		"/status/keepalive",
		{});

};

async._generateRandomId =
function generateRandomId (length) {

	length = length || 20;

	var characters = "abcdefghijklmnopqrstuvwxyz";

	var randomId = "";

	for (var index = 0; index < length; index ++) {

		randomId +=
			characters.charAt (
				Math.floor (
					Math.random () * 26));

	}

	return randomId;

};

$(async._init);

// ex: noet ts=4 filetype=javascript