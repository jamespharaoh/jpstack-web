// on load

$(function () {

	// find dom items

	var namedNotesShowHideLink =
		$(".namedNotesShowHideLink");

	var generalNotesShowHideLink =
		$(".generalNotesShowHideLink");

	// get current status

	var namedNotesShow =
		localStorage.getItem ("namedNotesShowHide") != "hide";

	var generalNotesShow =
		localStorage.getItem ("generalNotesShowHide") != "hide";

	// main function

	var go = function () {

		makeNamedNotesEditable ();

		setCallbacks ();

		updateDom ();

		$(".peggedNoteRow").show ();

	};

	// make named notes editable

	var makeNamedNotesEditable = function () {

		$(".namedNote").editable ("chatMonitorInbox.namedNoteUpdate", {
			submit: "ok",
			cancel: "cancel",
			indicator: "(saving)",
			placeholder: "(click to add)",
			width: "none"
		});

	}

	// set callbacks

	var setCallbacks = function () {

		namedNotesShowHideLink.click (function () {

			namedNotesShow =
				! namedNotesShow;

			localStorage.setItem (
				"namedNotesShowHide",
				namedNotesShow ? "show" : "hide");

			updateDom ();

		});

		generalNotesShowHideLink.click (function () {

			generalNotesShow =
				! generalNotesShow;

			localStorage.setItem (
				"generalNotesShowHide",
				generalNotesShow ? "show" : "hide");

			updateDom ();

		});

	};

	// update dom

	var updateDom = function () {

		if (namedNotesShow) {

			namedNotesShowHideLink.text ("hide named");
			$(".namedNoteRow").show ();

		} else {

			namedNotesShowHideLink.text ("show named");
			$(".namedNoteRow").hide ();

		}

		if (generalNotesShow) {

			generalNotesShowHideLink.text ("hide general");
			$(".unpeggedNoteRow").show ();

		} else {

			generalNotesShowHideLink.text ("show general");
			$(".unpeggedNoteRow").hide ();

		}

	};

	// call go

	go ();

});