package wbs.apn.chat.user.image.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.Misc.toEnum;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;

import lombok.NonNull;

import wbs.console.forms.core.ConsoleForm;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.user.image.model.ChatUserImageType;

@PrototypeComponent ("chatUserImageUploadPart")
public
class ChatUserImageUploadPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@NamedDependency ("chatUserImageUploadFormType")
	ConsoleFormType <ChatUserImageUploadForm> formType;

	@ClassSingletonDependency
	LogContext logContext;

	// state

	ConsoleForm <ChatUserImageUploadForm> form;

	ChatUserImageType chatUserImageType;

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			chatUserImageType =
				toEnum (
					ChatUserImageType.class,
					requestContext.stuffString (
						"chatUserImageType"));

			form =
				formType.buildResponse (
					transaction,
					emptyMap ());

			if (requestContext.post ()) {

				form.update (
					transaction);

			}

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlParagraphWrite (
				"Please upload the photo or video.");

			form.outputFormTable (
				transaction,
				"post",
				requestContext.resolveLocalUrlFormat (
					"/chatUser.%s.upload",
					chatUserImageType.name ()),
				"upload file");

		}

	}

}
