
var chat = {};

var byId = function (id) {
	return getElementById (id);
}

// =============================================================== chat.modForm

chat.modForm = {};

chat.modForm.useTemplate = function () {
	var templateId = document.getElementById ("templateId");
	var text = document.getElementById ("message");
	if (templateId.value == "") return;
	var template = chatHelpTemplates [templateId.value];
	if (template) text.value = template;
};

chat.modForm.showApprove = function () {
	try {
		document.getElementById ("approveRow").style.display = "table-row";
	} catch (e) {
		document.getElementById ("approveRow").style.display = "block";
	}
	document.getElementById ("templateRow").style.display = "none";
	document.getElementById ("messageRow").style.display = "none";
	document.getElementById ("approveButton").style.display = "inline";
	document.getElementById ("rejectButton").style.display = "none";
};

chat.modForm.showReject = function () {
	document.getElementById ("approveRow").style.display = "none";
	try {
		document.getElementById ("templateRow").style.display = "table-row";
		document.getElementById ("messageRow").style.display = "table-row";
	} catch (e) {
		document.getElementById ("templateRow").style.display = "block";
		document.getElementById ("messageRow").style.display = "block";
	}
	document.getElementById ("approveButton").style.display = "none";
	document.getElementById ("rejectButton").style.display = "inline";
};

chat.modForm.useTemplate = function () {
	var templateId = document.getElementById ("templateId");
	var text = document.getElementById ("message");
	if (templateId.value == "") return;
	var template = chatHelpTemplates [templateId.value];
	if (template) text.value = template;
};

// ===================================================== .chatModForm

$(function () {

	$("form.chatModForm").each (function () {
		var form = $(this);

		// change form when approve/reject is pressed

		var showApprove = function () {
			form.find (".reject").hide ();
			form.find (".approve").show ();
		};

		var showReject = function () {
			form.find (".approve").hide ();
			form.find (".reject").show ();
		};

		form.find (".showApprove").click (showApprove);
		form.find (".showReject").click (showReject);

		// fill in template when ok is pressed

		form.find (".templateOk").click (function () {

			templateId = form.find (".templateId").val ();

			if (templateId == "")
				return;

			template = chatHelpTemplates [templateId];

			if (! template)
				return;

			form.find (".message").val (template);
		});

	});

});

// ===================================================== .chatSchemeKeywordForm

$(function () {

	$("form.chat_scheme_keyword").each (function () {
		var form = $(this);

		var update = function () {
			var value = form.find ("input:radio[name=keyword_type]:checked").val ();
			form.find (".join_row").toggle (value == "join");
			form.find (".command_row").toggle (value == "command");
		};

		form.find ("input:radio[name=keyword_type]").click (update);
		form.find ("td").click (update);
		update ();
	});

});
