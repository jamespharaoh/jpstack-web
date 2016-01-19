package wbs.clients.apn.chat.user.image.console;

import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.toEnum;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.base.Optional;

import wbs.clients.apn.chat.user.image.model.ChatUserImageType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;

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
				requestContext,
				formFieldSet,
				uploadForm);

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

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
			requestContext,
			formatWriter,
			formFieldSet,
			Optional.<UpdateResultSet>absent (),
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
