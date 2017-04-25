$(function () {

	wbsStatus.handlerRegister ("manual-responder",
	function (data) {

		if (data.numToday || data.numThisHour) {

			$("#manualResponderCell").text ([
				"Messages answered: ",
				String (numToday),
				" today, ",
				String (numThisHour),
				" this hour",
			].join (""));

			$("#manualResponderRow").show ();

		} else {

			$("#manualResponderRow").hide ();

		}

	});

});

// ex: noet ts=4 filetype=javascript