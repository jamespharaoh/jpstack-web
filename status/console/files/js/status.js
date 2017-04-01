// status

var statusRequest;

// sets up the request

function statusRequestGo () {

	if (window.XMLHttpRequest) {
		statusRequest = new XMLHttpRequest ();
	} else if (window.ActiveXObject) {
		statusRequest = new ActiveXObject ("Microsoft.XMLHTTP");
	} else return;

	statusRequest.onreadystatechange = statusRequestChange;
	statusRequest.open ("GET", statusRequestUrl, true);
	statusRequest.send (null);

}

// handles the request status change events

function statusRequestChange () {

	if (statusRequest.readyState != 4) return;

	if (statusRequest.status != 200) {
		document.getElementById ('headerCell').firstChild.data =
		 'Status (' + statusRequest.status + '!)';
		statusRequestSchedule ();
		return;
	}

	try {

		var statusDiv = document.getElementById ('statusDiv');
		var response = statusRequest.responseXML.documentElement;
		eval (response.getElementsByTagName ('javascript') [0].firstChild.data);

		document.getElementById ('headerCell').firstChild.data = 'Status';

		var loadingRow = document.getElementById ('loadingRow');
		showTableRow (loadingRow, false);

	} catch (e) { }

	statusRequestSchedule ();

}

// sets a timer for the request

function statusRequestSchedule () {

	setTimeout (
		statusRequestGo,
		statusRequestTime);

}

// shows or hides the given table row

function showTableRow (row, show) {

	if (show && row.style.display != 'table-row' && row.style.display != 'block') {
		try { row.style.display = 'table-row'; }
		catch (e) { row.style.display = 'block'; }
	}

	if (! show && row.style.display != 'none') {
		row.style.display = 'none';
	}

}

function updateTimestamp (timestamp) {

	var timeCell = document.getElementById ('timeCell');
	var timeRow = document.getElementById ('timeRow');

	timeCell.firstChild.data = timestamp;
	showTableRow (timeRow, true);

}

function updateNotice (notice) {

	if (notice) {

		$("#noticeCell").html (notice);
		$("#noticeRow").show ();

	} else {

		$("#noticeRow").hide ();

	}

}

$(function () {

	$("#timeRow").hover (
		function () { $(this).addClass ("hover") },
		function () { $(this).removeClass ("hover") });

	$("#timeRow").click (function () {
		window.parent.frames.main.location = "/coreSystem";
	});

});

// ex: noet ts=4 filetype=javascript