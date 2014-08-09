package wbs.psychic.contact.console;

import static wbs.framework.utils.etc.Misc.toInteger;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.StringSubstituter;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.psychic.contact.model.PsychicContactRec;
import wbs.psychic.core.model.PsychicRec;
import wbs.psychic.profile.model.PsychicProfileRec;
import wbs.psychic.request.model.PsychicRequestObjectHelper;
import wbs.psychic.request.model.PsychicRequestRec;
import wbs.psychic.send.logic.PsychicSendLogic;
import wbs.psychic.template.console.PsychicTemplateConsoleHelper;
import wbs.psychic.template.model.PsychicTemplateRec;
import wbs.psychic.user.core.model.PsychicUserRec;
import wbs.sms.gsm.MessageSplitter;
import wbs.sms.message.core.model.MessageRec;

@PrototypeComponent ("psychicContactFormAction")
public
class PsychicContactFormAction
	extends ConsoleAction {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	PsychicContactConsoleHelper psychicContactHelper;

	@Inject
	PsychicRequestObjectHelper psychicRequestHelper;

	@Inject
	PsychicSendLogic psychicSendLogic;

	@Inject
	PsychicTemplateConsoleHelper psychicTemplateHelper;

	@Inject
	QueueLogic queueLogic;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	UserObjectHelper userHelper;

	UserRec myUser;

	PsychicContactRec contact;
	PsychicRequestRec request;
	PsychicProfileRec profile;
	PsychicUserRec user;
	PsychicRec psychic;

	@Override
	public
	Responder backupResponder () {
		return responder ("psychicContactFormResponder");
	}

	@Override
	protected
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		myUser =
			userHelper.find (
				requestContext.userId ());

		contact =
			psychicContactHelper.find (
				requestContext.stuffInt ("psychicContactId"));

		request =
			psychicRequestHelper.find (
				toInteger (requestContext.getForm ("psychicRequestId")));

		profile =
			contact.getPsychicProfile ();

		user =
			contact.getPsychicUser ();

		psychic =
			user.getPsychic ();

		// split message

		PsychicTemplateRec responseSingleTemplate =
			psychicTemplateHelper.findByCode (
				psychic,
				"response_single");

		String responseSingleText =
			new StringSubstituter ()

				.param (
					"name",
					profile.getName ())

				.param (
					"page",
					"{page}")

				.param (
					"pages",
					"{pages}")

				.param (
					"message",
					"{message}")

				.substitute (
					responseSingleTemplate
						.getTemplateText ()
						.getText ());

		PsychicTemplateRec responseFirstTemplate =
			psychicTemplateHelper.findByCode (
				psychic,
				"response_first");

		String responseFirstText =
			new StringSubstituter ()

				.param (
					"name",
					profile.getName ())

				.param (
					"page",
					"{page}")

				.param (
					"pages",
					"{pages}")

				.param (
					"message",
					"{message}")

				.substitute (
					responseFirstTemplate
						.getTemplateText ()
						.getText ());

		PsychicTemplateRec responseMiddleTemplate =
			psychicTemplateHelper.findByCode (
				psychic,
				"response_middle");

		String responseMiddleText =
			new StringSubstituter ()

				.param (
					"name",
					profile.getName ())

				.param (
					"page",
					"{page}")

				.param (
					"pages",
					"{pages}")

				.param (
					"message",
					"{message}")

				.substitute (
					responseMiddleTemplate
						.getTemplateText ()
						.getText ());

		PsychicTemplateRec responseLastTemplate =
			psychicTemplateHelper.findByCode (
				psychic,
				"response_last");

		String responseLastText =
			new StringSubstituter ()

				.param (
					"name",
					profile.getName ())

				.param (
					"page",
					"{page}")

				.param (
					"pages",
					"{pages}")

				.param (
					"message",
					"{message}")

				.substitute (
					responseLastTemplate
						.getTemplateText ()
						.getText ());

		MessageSplitter.Templates splitterTemplates =
			new MessageSplitter.Templates (
				responseSingleText,
				responseFirstText,
				responseMiddleText,
				responseLastText);

		List<String> messageBodies =
			MessageSplitter.split (
				requestContext.getForm ("text"),
				splitterTemplates);

		// send message(s)

		List<MessageRec> messages =
			new ArrayList<MessageRec> ();

		for (String messageBody : messageBodies) {

			messages.add (
				psychicSendLogic.sendMagic (
					user,
					"send_to_profile",
					profile.getId (),
					request.getRequestMessage ().getThreadId (),
					messageBody,
					"request"));

		}

		// update request

		request
			.setResponseMessage (messages.get (0))
			.setResponseTime (transaction.now ())
			.setResponseText (
				textHelper.findOrCreate (requestContext.getForm ("text")))
			.setUser (myUser);

		// update contact

		contact
			.setLastResponse (transaction.now ())
			.setNumResponses (contact.getNumResponses () + 1);

		if (contact.getFirstResponse () == null)
			contact.setFirstResponse (transaction.now ());

		// update queue

		PsychicRequestRec nextRequest =
			contact.getRequestsByIndex ().get (
				contact.getNumResponses ());

		if (nextRequest != null) {

			contact.getQueueItem ().setDetails (
				nextRequest.getRequestText ().getText ());

		} else {

			queueLogic.processQueueItem (
				contact.getQueueItem (),
				myUser);

			contact.setQueueItem (null);

		}

		// and finish off

		transaction.commit ();

		requestContext.addNotice ("Message sent");

		if (nextRequest != null) {
			return responder ("psychicContactFormResponder");
		} else {
			return responder ("queueHomeResponder");
		}

	}

}
