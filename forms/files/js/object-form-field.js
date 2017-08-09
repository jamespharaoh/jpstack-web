var objectFormField = {

	_activeField: undefined,
	_dropdown: undefined,
	_searchText: undefined,

};

objectFormField._init =
function init () {

	async.onConnect (
		objectFormField._handleConnect);

	async.onDisconnect (
		objectFormField._handleDisconnect);

	$(".form-object-search-field").focus (
		objectFormField._fieldFocus);

	$(".form-object-search-field").keypress (
		objectFormField._fieldKeypress);

	$(".form-object-search-field").blur (
		objectFormField._fieldBlur);

};

objectFormField._handleConnect =
function handleConnect () {

	console.log ("CONNECTED");

	objectFormField._update ();

};

objectFormField._handleDisconnect =
function handleDisconnect () {

	console.log ("DISCONNECTED");

};

objectFormField._fieldFocus =
function fieldFocus () {

	console.log ("FOCUS");

	objectFormField._activeField = $(this);

	objectFormField._dropdown =
		$("<div>")
			.addClass ("form-object-search-dropdown")
			.css ("left", $(this).offset ().left)
			.css ("right", "5px")
			.css ("display", "none");

	$(this).parent ().append (
		objectFormField._dropdown);

	objectFormField._update ();

};

objectFormField._fieldKeypress =
function fieldKeypress () {

	console.log ("KEY PRESS");

	setTimeout (objectFormField._update, 0);

};

objectFormField._fieldBlur =
function fieldBlur () {

	console.log ("BLUR");

	objectFormField._activeField = undefined;

	objectFormField._dropdown.remove ();
	objectFormField._dropdown = undefined;

	objectFormField._searchText = undefined;

};

objectFormField._update =
function update () {

	console.log ("UPDATE");

	var activeField = objectFormField._activeField;

	if (! activeField) {
		return;
	}

	var searchText = activeField.val ();

	if (searchText == objectFormField._searchText) {
		return;
	}

	objectFormField._searchText = searchText;

	if (searchText == "") {

		var hiddenField = $("#" + activeField.data ("search-field-id"));

		hiddenField.value = "none";

		return;

	}

	var fieldId = activeField.attr ("id");
	var objectTypeId = activeField.data ("search-object-type-id");
	var rootObjectTypeId = activeField.data ("search-root-object-type-id");
	var rootObjectId = activeField.data ("search-root-object-id");

	console.log ("SEARCH: " + searchText);

	var payload = {
		fieldId: fieldId,
		objectTypeId: objectTypeId,
		rootObjectTypeId: rootObjectTypeId,
		rootObjectId: rootObjectId,
		searchText: searchText,
	};

	var callback = function (payload) {

		objectFormField._callback (
			objectFormField._activeField,
			payload);

	};

	async.call ("/forms/object-search", payload, callback);

};

objectFormField._callback =
function callback (activeField, data) {

	var activeField = objectFormField._activeField;
	var dropdown = objectFormField._dropdown;

	if (! activeField) {
		return;
	}

	if (activeField.attr ("id") != data.fieldId) {
		return;
	}

	console.log ("CALLBACK " + data.items.length);

	dropdown
		.empty ()
		.css ("display", "block");

	data.items.forEach (function (item) {

		var itemText = item.path;

		if (item.name) {
			itemText += " — " + item.name;
		}

		if (item.description) {
			itemText += " (" + item.description + ")";
		}

		var itemDiv =
			$("<div>")
				.text (itemText)
				.mouseover (function () {
					$(this).addClass ("hover")
				})
				.mouseout (function () {
					$(this).removeClass ("hover")
				})
				.mousedown (function (event) {
					event.preventDefault ();
				})
				.click (function (event) {
					objectFormField._select (activeField, item);
				});

		dropdown.append (itemDiv);

	});

};

objectFormField._select =
function select (activeField, item) {

	console.log ("SELECT");

	activeField.val (item.path);

	console.log (activeField.data ("search-field-id"));

	var hiddenField =
		$("#" + activeField.data ("search-field-id")
			.replace (/\./g, "\\."));

	console.log (hiddenField [0]);

	hiddenField.val (String (item.objectId));

	if (activeField != objectFormField._activeField) {
		return;
	}

	objectFormField._dropdown
		.css ("display", "none");

};

$(objectFormField._init);

// ex: noet ts=4 filetype=javascript