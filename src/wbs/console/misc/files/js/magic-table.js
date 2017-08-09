var magicTable = {};

magicTable.applyMagicTableRow =
function applyMagicTableRow (
		magicTableRow) {

	var magicTableRow = $(this);

	var targetHref =
		magicTableRow.data ("target-href");

	var targetFrame =
		magicTableRow.data ("target-frame");

	var rowsClass =
		magicTableRow.data ("rows-class");

	var magicTableRows =
		rowsClass
			? $("." + rowsClass)
			: magicTableRow;

	magicTableRow.mouseenter (function () {
		magicTableRows.addClass ("hover");
	});

	magicTableRow.mouseleave (function () {
		magicTableRows.removeClass ("hover");
	});

	magicTableRow.click (function () {

		if (targetFrame) {
			top.frames [targetFrame].location = targetHref;
		} else {
			window.location = targetHref;
		}

	});

};

magicTable.setupMagicHandlers =
function setupMagicHandlers (
		context) {

	context.find (".magic-table-row").each (
		magicTable.applyMagicTableRow);

};

$(function () {

	magicTable.setupMagicHandlers (
		$(document));

});