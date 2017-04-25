$(function () {

	wbsStatus.handlerRegister ("manual-responder",
	function (data) {

		if (data.numToday || data.numThisHour) {

			$("#manual-responder-cell").text ([
				"Messages answered: ",
				String (numToday),
				" today, ",
				String (numThisHour),
				" this hour",
			].join (""));

			$("#manual-responder-row").show ();

		} else {

			$("#manual-responder-row").hide ();

		}

	});

});

// ex: noet ts=4 filetype=javascript