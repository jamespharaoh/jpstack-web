// ensure we are not in a frame

if (window.parent != window) {
	window.parent.location = window.location;
}

$(function () {

	$(".login-buttons .login-button").each (function () {

		var sliceCode =
			$(this).data ("slice-code");

		var userCode =
			$(this).data ("user-code");

		$(this).click (function () {

			$("#login-form .slice-input").val (sliceCode);
			$("#login-form .username-input").val (userCode);
			$("#login-form .password-input").val ("**********");

			$(".login-buttons .login-button").attr ("disabled", "disabled");

			$("#login-form").submit ();

		});

		$(this).removeAttr ("disabled");

	});

});