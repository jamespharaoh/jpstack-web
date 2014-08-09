package wbs.apn.chat.tv.moderation.console;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.action.ConsoleAction;

@PrototypeComponent ("chatTvModerationReplyAction")
public
class ChatTvModerationReplyAction
	extends ConsoleAction {

	/*
	@Inject
	ChatConsoleHelper chatHelper;

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
	ParamCheckerSet replyParams =
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
		return responder ("chatTvModerationReplyResponder");
	}

	@Override
	protected
	Responder goReal () {

		// handle skip

		if (requestContext.parameter ("postAsTextJockey") != null) {

			// process params

			Map<String,String> paramMap =
				requestContext.parameterMapSimple ();

			requestContext.session (
				"chatTvPostParams",
				paramMap);

			Map<String,Object> params =
				replyParams.apply (requestContext);

			if (params == null)
				return null;

			@Cleanup
			Transaction transaction =
				database.beginReadWrite ();

			ChatRec chat =
				chatHelper.find (
					requestContext.stuffInt ("chat"));

			// find chat user

			String fromUserCode =
				(String) params.get ("fromUserCode");

			ChatUserRec chatUser =
				chatUserHelper.findByCode (
					chat,
					fromUserCode);

			// error if user does not exist

			if (chatUser == null) {

				requestContext.addError (
					"Chat user not found " + fromUserCode);

				return null;

			}

			// send message

			ChatTvMessageRec message =
				chatTvLogic.postToScreen (
					userHelper.find (requestContext.userId ()),
					chatUser,
					(String) paramMap.get ("message"),
					true);

			transaction.commit ();

			// clear stored message from session

			paramMap.remove ("message");

			// add a notice

			requestContext.addNotice (sf ("Message posted as %s",
				message.getTextJockey () ? "text jockey" : "user"));

		}

		// forward to next moderation or else queue home

		return responder (
			requestContext.stuff ("chatTvModerationId") != null
				? "chatTvModerationFormResponder"
				: "queueHomeResponder");

	}
	*/

}
