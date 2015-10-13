// on load

$(function () {

	var debugEnabled = true;

	var debugStart = function () {

		if (! debugEnabled) {
			return;
		}

		console.log ("========== MANUAL RESPONDER CHAR COUNT START ==========");

	};

	var debugStop = function () {

		if (! debugEnabled) {
			return;
		}

		console.log ("========== MANUAL RESPONDER CHAR COUNT END ==========");

	};

	var debugValue = function (name, value) {

		if (! debugEnabled) {
			return;
		}

		console.log ([
			String (name),
			": ",
			value,
		].join (""));

	};

	$(".manual-responder-request-pending-summary").each (function () {

		var summary = $(this);

		summary.find (".mrNumberNoteEditable").editable (
			"manualResponderRequest.pending.numberNoteUpdate",
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

	$(".manual-responder-request-pending-form").each (function () {

		var form = $(this);

		form.find (".template").each (function () {

			var template = $(this);

			var text = template.find (".template-text");
			var charCount = template.find (".template-chars");

			var selectTemplate = function () {

				// disable others

				form.find (".template").removeClass ("selected");

				form.find (".template-radio").prop ("checked", false);

				form.find (".template-text").hide ();
				form.find (".template-chars").hide ();

				form.find (".template-submit").prop ("disabled", true);

				// enable this

				template.addClass ("selected");

				template.find (".template-radio").prop ("checked", true);

				template.find (".template-text").show ().focus ();

				template.find (".template-text").each (function () {
					var length = $(this).val ().length;
					this.setSelectionRange (length, length);
				});

				template.find (".template-text").css ("width",
					+ template.find (".template-text").parent ().innerWidth ()
					+ template.find (".template-text").innerWidth ()
					- template.find (".template-text").outerWidth ()
					- 10);

				template.find (".template-chars").show ();

				template.find (".template-submit").prop ("disabled", false);

				updateCharCount ();

			};

			var updateCharCount = function () {

				debugStart ();

				// do nothing if no text field

				if (text.length == 0) return;

				var value = text.val ();

				// error if not valid

				if (! isGsm (value)) {

					charCount.text (
						"Message contains characters that can not be send via SMS");

					charCount.removeClass ("warning");
					charCount.addClass ("error");

					debugStop ();

					return;

				}

				// get parameters from html data

				var fixedLength = Number (
					template.data ("template-fixed-length"));

				debugValue ("fixedLength", fixedLength);

				var minMessageParts = Number (
					template.data ("template-min-message-parts"));

				debugValue ("minMessageParts", minMessageParts);

				var maxForSingleMessage = Number (
					template.data ("template-max-for-single-message"));

				debugValue ("maxForSingleMessage", maxForSingleMessage);

				var maxForMessagePart = Number (
					template.data ("template-max-for-message-part"));

				debugValue ("maxForMessagePart", maxForMessagePart);

				var maxMessages = Number (
					template.data ("template-max-messages"));

				debugValue ("maxMessages", maxMessages);

				// get length

				var length = gsmlen (value);
				debugValue ("length", length);

				var fullLength = length + fixedLength;
				debugValue ("fullLength", fullLength);

				// work out max characters

				var maxFullLength;

				if (maxMessages == 1) {

					maxFullLength = maxForSingleMessage;

				} else {

					maxFullLength = maxForMessagePart * maxMessages;

				}

				debugValue ("maxFullLength", maxFullLength);

				var maxLength = maxFullLength - fixedLength;
				debugValue ("maxLength", maxLength);

				// work out min characters

				var minFullLength;

				if (minMessageParts <= 1) {

					minFullLength = 1;

				} else if (minMessageParts == 2) {

					minFullLength = maxForSingleMessage + 1;

				} else {

					minFullLength = (
						maxForMessagePart * (minMessageParts - 1)
					) + 1;

				}

				debugValue ("minFullLength", minFullLength);

				var minLength = minFullLength - fixedLength;
				debugValue ("minLength", minLength);

				// work out number of parts

				var numParts;

				if (fullLength <= maxForSingleMessage) {
					numParts = 1;
				} else {
					numParts = Math.floor (
						(fullLength - 1) / maxForMessagePart
					) + 1;
				}

				debugValue ("numParts", numParts);

				// warn if too short

				if (numParts < minMessageParts) {

					var numMore = minLength - length;

					charCount.text ([
						String (length),
						" ",
						length != 1 ? "characters" : "character",
						" in ",
						String (numParts),
						" ",
						numParts != 1 ? "parts" : "part",
						", type ",
						String (numMore),
						" more ",
						numMore != 1 ? "characters" : "character",
					].join (""));

					charCount.addClass ("warning");
					charCount.removeClass ("error");

					debugStop ();

					return;

				}

				// error if too long

				if (fullLength > maxFullLength) {

					var numOver = length - maxLength;

					charCount.text ([
						String (length),
						" ",
						length != 1 ? "characters" : "character",
						", remove ",
						String (numOver),
					].join (""));

					charCount.removeClass ("warning");
					charCount.addClass ("error");

					debugStop ();

					return;

				}

				// length is ok

				var numLeft = maxLength - length;

				charCount.text ([
					String (length),
					" ",
					length > 1 ? "characters" : "character",
					" in ",
					String (numParts),
					" ",
					numParts > 1 ? "parts" : "part",
					", ",
					String (numLeft),
					" left",
				].join (""));

				charCount.removeClass ("warning");
				charCount.removeClass ("error");

				debugStop ();

			};

			template.click (selectTemplate);

			if (template.find (".template-radio").prop ("checked")) {
				selectTemplate.apply (template);
			}

			template.find (".template-text").keyup (updateCharCount);
			template.find (".template-text").keydown (updateCharCount);
			template.find (".template-text").change (updateCharCount);

		});

	});

});
