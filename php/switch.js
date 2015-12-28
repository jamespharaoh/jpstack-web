$(function () {

	var cookieName = "txt2_console";
	var cookieDuration = 365;

	var idCounter = 0;

	var consoleValues = [
		"live",
		"test",
	];

	var consoleSelected =
		Cookies.get (cookieName) || "live";

	function main () {
		consoleSetup ();
	}

	function consoleSetup () {

		var consoleParagraph =
			$("p.console");

		consoleParagraph.empty ();

		consoleValues.forEach (function (consoleValue) {

			var thisId =
				idCounter ++;

			var consoleOption =
				$("<span>");

			var consoleInput = (
				$("<input>")
					.attr ("id", "id-" + thisId)
					.attr ("type", "radio")
					.attr ("name", "console")
					.attr ("value", consoleValue)
			);

			if (consoleSelected == consoleValue) {

				consoleInput
					.prop ("checked", true);

			}

			consoleOption.append (
				consoleInput);

			consoleOption.append (
				$("<label>")
					.attr ("for", "id-" + thisId)
					.text (consoleValue)
			);

			consoleOption.append (
				$("<br>"));

			consoleParagraph.append (
				consoleOption);

			consoleInput.change (
				consoleUpdate);

		});

	}

	function consoleUpdate () {

		consoleSelected =
			$(this).val ();

		Cookies.set (
			cookieName,
			consoleSelected,
			{ expires: cookieDuration });

	}

	main ();

});
