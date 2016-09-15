$(function () {

	$("#templates .template").each (function () {

		var template = this;

		var selectTemplate = function () {

			// disable others

			$("#templates .template").removeClass ("selected");

			$("#templates .template-radio").prop ("checked", false);

			$("#templates .template-text").hide ();
			$("#templates .template-chars").hide ();

			$("#templates .template-submit").prop ("disabled", true);

			// enable this

			$(template).addClass ("selected");

			$(template).find (".template-radio").prop ("checked", true);

			$(template).find (".template-text").show ().focus ();

			$(template).find (".template-text").css ("width",
				+ $(template).find (".template-text").parent ().innerWidth ()
				+ $(template).find (".template-text").innerWidth ()
				- $(template).find (".template-text").outerWidth ()
				- 10);

			$(template).find (".template-chars").show ();

			$(template).find (".template-submit").prop ("disabled", false);

			updateCharCount ();

		};

		var updateCharCount = function () {

			var text = $(template).find (".template-text").val ();

			var length = text.length;

			var charCountText = [
				String (length),
				" characters",
			].join ("");

			var minimum = $(template).data ("minimum");
			var maximum = $(template).data ("maximum");

			var error = false;

			if (typeof minimum === "number" && length < minimum) {

				charCountText += [
					", minimum is ",
					minimum,
					", type ",
					minimum - length,
					" more characters",
				].join ("");

				error = true;

			} else if (typeof maximum === "number") {

				if (length < maximum) {

					charCountText += [
						", maximum is ",
						maximum,
						", you have ",
						maximum - length,
						" characters remaining",
					].join ("");

				} else {

					charCountText += [
						", maximum is ",
						maximum,
						", remove ",
						length - maximum,
						" characters",
					].join ("");

					error = true;

				}

			}

			$(template).find (".template-chars").text (charCountText);

			if (error) {
				$(template).find (".template-chars").addClass ("error");
			} else {
				$(template).find (".template-chars").removeClass ("error");
			}

		};

		$(template).click (selectTemplate);

		$(template).find (".template-text").keyup (updateCharCount);
		$(template).find (".template-text").keydown (updateCharCount);
		$(template).find (".template-text").change (updateCharCount);

	});

	$(".im-chat-customer-note-editable").editable (
		"imChat.pending.customerNoteUpdate",
		{
			type: "textarea",
			submit: "ok",
			cancel: "cancel",
			indicator: "(saving)",
			placeholder: "(click to add)",
			rows: 8,
			cols: 64,
			data: function (value, settings) {
				return value.replace (/<br[\s\/]?>/gi, "\n");
			}
		}
	);

});
