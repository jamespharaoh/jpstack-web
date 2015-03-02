$(function () {

	$("#templates .template").click (function () {

		// disable others

		$("#templates .template").removeClass ("selected");

		$("#templates .template-radio").prop ("checked", false);

		$("#templates .template-text").hide ();

		$("#templates .template-submit").prop ("disabled", true);

		// enable this

		$(this).addClass ("selected");

		$(this).find (".template-radio").prop ("checked", true);

		$(this).find (".template-text").show ().focus ();

		$(this).find (".template-text").css ("width",
			+ $(this).find (".template-text").parent ().innerWidth ()
			+ $(this).find (".template-text").innerWidth ()
			- $(this).find (".template-text").outerWidth ()
			- 10);

		$(this).find (".template-submit").prop ("disabled", false);

	});

});
