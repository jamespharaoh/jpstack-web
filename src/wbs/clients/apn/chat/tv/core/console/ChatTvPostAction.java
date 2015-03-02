package wbs.clients.apn.chat.tv.core.console;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.action.ConsoleAction;

@PrototypeComponent ("chatTvPostAction")
public
class ChatTvPostAction
	extends ConsoleAction {

	/*
	@Inject
	ChatTvConsoleHelper chatTvHelper;

	@Inject
	ChatTvLogic chatTvLogic;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	UserObjectHelper userHelper;

	final static
	ParamCheckerSet postParams =
		new ParamCheckerSet (

			new ImmutableMap.Builder<String,ParamChecker<?>> ()

				.put ("fromUserCode",
					new RegexpParamChecker (
						"Chat user code must be six digits",
						"\\d{6}"))

				.put ("message",
					new RegexpParamChecker (
						"Message must be filled in",
						".+"))

				.build ());

	@Override
	public
	Responder backupResponder () {
		return responder ("chatTvPostResponder");
	}

	@Override
	protected
	Responder goReal () {

		// proces params

		Map<String,String> paramMap =
			requestContext.parameterMapSimple ();

		requestContext.session (
			"chatTvPostParams",
			paramMap);

		Map<String,Object> params =
			postParams.apply (
				requestContext);

		if (params == null)
			return null;

		// start a transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		// find some basic stuff

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		ChatTvRec chatTv =
			chatTvHelper.find (
				requestContext.stuffInt ("chatId"));

		ChatRec chat =
			chatTv.getChat ();

		// find chat user

		String fromUserCode =
			(String) params.get ("fromUserCode");

		ChatUserRec chatUser =
			chatUserHelper.findByCode (
				chat,
				fromUserCode);

		if (chatUser == null) {
			requestContext.addError ("Chat user not found " + fromUserCode);
			return null;
		}

		// check text jockey flag

		boolean textJockey;
		if (requestContext.parameter ("postAsTextJockey") != null) {
			textJockey = true;
		} else if (requestContext.parameter ("postAsUser") != null) {
			textJockey = false;
		} else {
			throw new RuntimeException ();
		}

		// send message

		ChatTvMessageRec message =
			chatTvLogic.postToScreen (
				myUser,
				chatUser,
				(String) params.get ("message"),
				textJockey);

		// commit transaction

		transaction.commit ();

		// update session

		paramMap.remove ("message");

		// add a notice

		requestContext.addNotice (sf (
			"Message posted as %s",
			message.getTextJockey () ? "text jockey" : "user"));

		return null;

	}
	*/

}
