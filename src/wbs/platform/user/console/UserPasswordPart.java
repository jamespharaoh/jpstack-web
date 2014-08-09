package wbs.platform.user.console;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("userPasswordPart")
public
class UserPasswordPart
	extends AbstractPagePart {

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/user.password"),
			">\n");

		printFormat (
			"<p>Enter new password<br>\n",

			"<input",
			" type=\"password\"",
			" name=\"password_1\"",
			" size=\"32\"",
			"></p>\n");

		printFormat (
			"<p>Enter again to confirm<br>\n",

			"<input",
			" type=\"password\"",
			" name=\"password_2\"",
			" size=\"32\"",
			"></p>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"save changes\"",
			"></p>\n");

		printFormat (
			"</form>\n");

	}

}
