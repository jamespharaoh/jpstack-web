package wbs.platform.user.console;

import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("userPasswordPart")
public
class UserPasswordPart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

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
