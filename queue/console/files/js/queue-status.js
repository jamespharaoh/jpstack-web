function updateQueueItems (numQueue, numClaimed) {

	var queueRow = $("#queue-row");
	var queueCell = $("#queue-cell");

	if (numQueue > 0 && numClaimed > 0) {

		queueCell.text ([
			String (numQueue),
			" ",
			numQueue > 1 ? "items" : "items",
			" queueing and ",
			String (numClaimed),
			" claimed",
		].join (""));

	} else if (numQueue > 0) {

		queueCell.text ([
			String (numQueue),
			" ",
			numQueue > 1 ? "items" : "items",
			" queueing",
		].join (""));

	} else if (numClaimed > 0) {

		queueCell.text ([
			String (numClaimed),
			" claimed queue ",
			numClaimed > 1 ? "items" : "item",
		].join (""));

	}

	showTableRow (
		queueRow [0],
		numQueue > 0 || numClaimed > 0);

}