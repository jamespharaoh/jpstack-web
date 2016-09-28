package wbs.platform.user.console;

import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("userPasswordPart")
public
class UserPasswordPart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlBodyContent () {

		// form open

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/user.password"));

		// password

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"Enter new password<br>");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"password\"",
			" name=\"password_1\"",
			" size=\"32\"",
			">");

		htmlParagraphClose ();

		// password confirmation

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"Enter again to confirm<br>");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"password\"",
			" name=\"password_2\"",
			" size=\"32\"",
			">");

		htmlParagraphClose ();

		// form controls

		htmlParagraphOpen ();

		formatWriter.writeFormat (
			"<input",
			" type=\"submit\"",
			" value=\"save changes\"",
			">");

		htmlParagraphClose ();

		// form close

		htmlFormClose ();

	}

}
