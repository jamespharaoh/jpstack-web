// on load

$(function () {

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

			// select a specific template

			var selectTemplate = function () {

				if (template.hasClass ("selected")) {
					return;
				}

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

				if (updateCharCount) {
					updateCharCount ();
				}

			};

			template.click (selectTemplate);

			if (template.find (".template-radio").prop ("checked")) {
				selectTemplate.apply (template);
			}

			// get template id

			var templateId =
				template.data ("template-id");

			if (templateId == "ignore") {
				return;
			}

			// find text input and char count display

			var text =
				template.find (".template-text");

			var charCount =
				template.find (".template-chars");

			// get parameters from html data

			var templateMode =
				template.data ("template-mode");

			var minMessageParts = Number (
				template.data ("template-min-message-parts"));

			var maxMessageParts = Number (
				template.data ("template-max-messages"));

			var maxForSingleMessage = Number (
				template.data ("template-max-for-single-message"));

			var maxForMessagePart = Number (
				template.data ("template-max-for-message-part"));

			var templates = {

				single:
					template.data ("template-single"),

				first:
					template.data ("template-first"),

				middle:
					template.data ("template-middle"),

				last:
					template.data ("template-last"),

			};

			var templateFixeds = {

				single:
					gsmlen (
						templates.single
							.replace ("{message}", "")
							.replace ("{page}", "1")
							.replace ("{pages}", "1")),

				first:
					gsmlen (
						templates.first
							.replace ("{message}", "")
							.replace ("{page}", "1")
							.replace ("{pages}", "1")),

				middle:
					gsmlen (
						templates.middle
							.replace ("{message}", "")
							.replace ("{page}", "1")
							.replace ("{pages}", "1")),

				last:
					gsmlen (
						templates.last
							.replace ("{message}", "")
							.replace ("{page}", "1")
							.replace ("{pages}", "1")),

			};

			var templateFrees = {

				single:
					maxForSingleMessage - templateFixeds.single,

				first:
					maxForSingleMessage - templateFixeds.first,

				middle:
					maxForSingleMessage - templateFixeds.middle,

				last:
					maxForSingleMessage - templateFixeds.last,

			};

			var effectiveMinimum;

			if (minMessageParts <= 1) {

				effectiveMinimum = 25;

			} else if (templateMode == "join") {

				effectiveMinimum =
					+ maxForMessagePart * (minMessageParts - 1)
					+ 25;

			} else {

				effectiveMinimum =
					+ templateFrees.first
					+ templateFrees.middle * (minMessageParts - 2)
					+ 25;

			}

			var effectiveMaximum;

			if (maxMessageParts <= 1) {

				effectiveMaximum =
					templateFrees.single;

			} else if (templateMode == "join") {

				effectiveMaximum =
					+ maxForMessagePart * maxMessageParts;

			} else {

				effectiveMaximum =
					+ templateFrees.first
					+ templateFrees.last
					+ templateFrees.middle * (minMessageParts - 2);

			}

			// update the character counter

			var updateCharCount = function () {

				// do nothing if no text field

				if (text.length == 0) return;

				// get value

				var value =
					text.val ().trim ();

				// error if not valid

				if (! isGsm (value)) {

					charCount.text (
						"Message contains characters that can not be sent via SMS");

					charCount.removeClass ("warning");
					charCount.addClass ("error");

					return;

				}

				var length =
					gsmlen (value);

				var numParts;
				var effectiveLength;

				if (templateMode == "split") {

					// split message

					var messageParts =
						gsmMessageSplit (templates, value);

					numParts =
						messageParts.length;

					// calculate effective length

					effectiveLength = 0;

					for (
						var index = 0;
						index < messageParts.length;
						index ++
					) {

						var messagePart =
							messageParts [index];

						var partLength =
							gsmlen (messagePart);

						var partSpare =
							maxForSingleMessage - partLength;

						if (messageParts.length == 1) {

							effectiveLength +=
								templateFrees.single - partSpare;

						} else if (index == messageParts.length - 1) {

							effectiveLength +=
								templateFrees.last - partSpare;

						} else if (index == 0) {

							effectiveLength +=
								templateFrees.first;

						} else {

							effectiveLength +=
								templateFrees.middle;

						}

					}

				} else if (templateMode == "join") {

					if (length <= maxForSingleMessage) {

						numParts = 1;

					} else {

						numParts =
							+ Math.floor (
								(length - 1) / maxForMessagePart)
							+ 1;

					}

					effectiveLength = length;

				}

				var lengthNotice = [
					String (effectiveLength),
					" ",
					effectiveLength != 1 ? "characters" : "character",
					" in ",
					String (numParts),
					" ",
					numParts != 1 ? "parts" : "part",
				].join ("");

				var splitNotice;

				if (effectiveLength > length) {

					splitNotice = [
						" (lost ",
						effectiveLength - length,
						" ",
						(effectiveLength - length) != 1
							? "characters"
							: "character",
						")",
					].join ("");

				} else if (effectiveLength < length) {

					splitNotice = [
						" (gained ",
						length - effectiveLength,
						" ",
						(length - effectiveLength) != 1
							? "characters"
							: "character",
						")",
					].join ("")

				} else {

					splitNotice = "";

				}

				if (effectiveLength < effectiveMinimum) {

					// warn if too short

					var charsMore =
						effectiveMinimum - effectiveLength;

					charCount.text ([
						lengthNotice,
						", type ",
						String (charsMore),
						" more ",
						charsMore != 1 ? "characters" : "character",
						splitNotice,
					].join (""));

					charCount.addClass ("warning");
					charCount.removeClass ("error");

				} else if (effectiveLength > effectiveMaximum) {

					// error if too long

					var charsOver =
						effectiveLength - effectiveMaximum;

					charCount.text ([
						lengthNotice,
						", remove ",
						String (charsOver),
						" ",
						charsOver != 1 ? "characters" : "character",
						splitNotice,
					].join (""));

					charCount.removeClass ("warning");
					charCount.addClass ("error");

				} else {

					// info if length is right

					var charsLeft =
						effectiveMaximum - effectiveLength;

					charCount.text ([
						lengthNotice,
						", ",
						String (charsLeft),
						" ",
						charsLeft != 1 ? "characters" : "character",
						" left",
						splitNotice,
					].join (""));

					charCount.removeClass ("warning");
					charCount.removeClass ("error");

				}

			};

			template.find (".template-text").keyup (updateCharCount);
			template.find (".template-text").keydown (updateCharCount);
			template.find (".template-text").change (updateCharCount);

		});

	});

});
