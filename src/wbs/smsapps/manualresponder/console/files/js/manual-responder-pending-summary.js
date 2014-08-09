// on load

$(function () {

	$(".mrNumberNoteEditable").editable (
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
