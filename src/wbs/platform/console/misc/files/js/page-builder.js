
function PageBuilder (
		pages) {

	var bigPage = 0;
	var littlePage = 0;

	function showPage (
			newBigPage,
			newLittlePage) {

		$("#pageHolder").html (
			pages [newBigPage] [newLittlePage]);

		$(".big-page-link-" + bigPage)
			.removeClass ("selected");

		$(".little-page-link-" + littlePage)
			.removeClass ("selected");

		bigPage =
			newBigPage;

		littlePage =
			newLittlePage;

		$(".big-page-link-" + bigPage)
			.addClass ("selected");

		$(".little-page-link-" + littlePage)
			.addClass ("selected");

	}

	function showBigPage (
			bigPage) {

		showPage (
			bigPage,
			littlePage);

	}

	function showLittlePage (
			littlePage) {

		showPage (
			bigPage,
			littlePage);

	}

	function init () {

		showPage (
			bigPage,
			littlePage);

	}

	this.showBigPage =
		showBigPage;

	this.showLittlePage =
		showLittlePage;

	this.init =
		init;

	return this;

}
