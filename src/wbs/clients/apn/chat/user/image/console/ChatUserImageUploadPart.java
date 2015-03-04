package wbs.clients.apn.chat.user.image.console;

import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.toEnum;

import javax.inject.Inject;
import javax.inject.Named;

import wbs.clients.apn.chat.user.image.model.ChatUserImageType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.module.ConsoleModule;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("chatUserImageUploadPart")
public
class ChatUserImageUploadPart
	extends AbstractPagePart {

	// dependencies

	@Inject @Named
	ConsoleModule chatUserImageConsoleModule;

	@Inject
	FormFieldLogic formFieldLogic;

	// state

	FormFieldSet formFieldSet;

	ChatUserImageType chatUserImageType;

	ChatUserImageUploadForm uploadForm;

	// implementation

	@Override
	public
	void prepare () {

		formFieldSet =
			chatUserImageConsoleModule.formFieldSets ().get (
				"uploadForm");

		chatUserImageType =
			toEnum (
				ChatUserImageType.class,
				(String) requestContext.stuff ("chatUserImageType"));

		uploadForm =
			new ChatUserImageUploadForm ();

		if (requestContext.post ()) {

			formFieldLogic.update (
				formFieldSet,
				uploadForm);

		}

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<p>Please upload the photo or video.</p>\n");

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				stringFormat (
					"/chatUser.%s.upload",
					chatUserImageType.name ())),
			" enctype=\"multipart/form-data\"",
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputFormRows (
			out,
			formFieldSet,
			uploadForm);

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"upload file\"",
			"></p>\n");

		printFormat (
			"</form>\n");

	}

}
