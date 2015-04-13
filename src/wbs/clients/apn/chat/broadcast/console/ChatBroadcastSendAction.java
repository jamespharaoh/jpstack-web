package wbs.clients.apn.chat.broadcast.console;

import static wbs.framework.utils.etc.Misc.allOf;
import static wbs.framework.utils.etc.Misc.joinWithoutSeparator;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.broadcast.logic.ChatBroadcastLogic;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastNumberObjectHelper;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastNumberRec;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastNumberState;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastObjectHelper;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastRec;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastState;
import wbs.clients.apn.chat.contact.model.ChatMessageMethod;
import wbs.clients.apn.chat.core.daemon.ProfileLogger;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserDao;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.param.CheckboxParamChecker;
import wbs.platform.console.param.EnumParamChecker;
import wbs.platform.console.param.IntegerParamChecker;
import wbs.platform.console.param.ParamChecker;
import wbs.platform.console.param.ParamCheckerSet;
import wbs.platform.console.param.RegexpParamChecker;
import wbs.platform.console.param.TimestampFromParamChecker;
import wbs.platform.console.param.TimestampToParamChecker;
import wbs.platform.console.param.YesNoParamChecker;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.service.console.ServiceConsoleHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.gsm.Gsm;
import wbs.sms.magicnumber.logic.MagicNumberLogic;
import wbs.sms.message.batch.logic.BatchLogic;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.message.batch.model.BatchSubjectRec;
import wbs.sms.number.core.console.NumberConsoleHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@Log4j
@PrototypeComponent ("chatBroadcastSendAction")
public
class ChatBroadcastSendAction
	extends ConsoleAction {

	// dependencies

	@Inject
	BatchObjectHelper batchHelper;

	@Inject
	BatchLogic batchLogic;

	@Inject
	ChatBroadcastObjectHelper chatBroadcastHelper;

	@Inject
	ChatBroadcastLogic chatBroadcastLogic;

	@Inject
	ChatBroadcastNumberObjectHelper chatBroadcastNumberHelper;

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatUserDao chatUserDao;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	MagicNumberLogic magicNumberLogic;

	@Inject
	NumberFormatLogic numberFormatLogic;

	@Inject
	NumberConsoleHelper numberHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	ServiceConsoleHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	UserObjectHelper userHelper;

	// details

	@Override
	protected
	Responder backupResponder () {

		if (requestContext.getForm ("send") != null) {

			return responder (
				"chatBroadcastVerifyResponder");

		}

		return responder (
			"chatBroadcastSendResponder");

	}

	// implementation

	@Override
	public
	Responder goReal () {

		ProfileLogger profileLogger =
			new ProfileLogger (
				log,
				"Broadcast send");

		try {

			// save params in session

			profileLogger.lap (
				"params");

			Map<String,String> sessionParams =
				requestContext.parameterMapSimple ();

			if (requestContext.getForm ("searchOn") != null) {

				sessionParams.put (
					"search",
					"true");

			}

			if (requestContext.getForm ("searchOff") != null) {

				sessionParams.put (
					"search",
					"false");

			}

			requestContext.session (
				"chatBroadcastParams",
				sessionParams);

			boolean verify =
				requestContext.getForm ("verify") != null;

			boolean send =
				requestContext.getForm ("send") != null;

System.out.println ("VERIFY: " + (verify? "yes": "no"));
System.out.println ("SEND: " + (send? "yes": "no"));
			if (verify || send) {

				// verify params

				profileLogger.lap ("verify params");

				ParamCheckerSet paramChecker =
					send
						? sendParamChecker
						: verifyParamChecker;

				Map<String,Object> params =
					paramChecker.apply (requestContext);

				if (params == null) {

					return responder (
						"chatBroadcastSendResponder");

				}

				boolean search =
					(Boolean) params.get ("search");

				// start transaction

				profileLogger.lap ("start transaction");

				@Cleanup
				Transaction transaction =
					database.beginReadWrite (
						this);

				ChatRec chat =
					chatHelper.find (
						requestContext.stuffInt ("chatId"));

				// lookup user

				profileLogger.lap ("lookup user");

				ChatUserRec fromChatUser =
					chatUserHelper.findByCode (
						chat,
						(String) params.get ("fromUserCode"));

				if (fromChatUser == null) {

					requestContext.addError (
						stringFormat (
							"Chat user not found: %s",
							params.get ("fromUserCode")));

					return responder ("chatBroadcastSendResponder");

				}

				if (fromChatUser.getType () != ChatUserType.monitor) {

					requestContext.addError (
						stringFormat (
							"Chat user is not a monitor: %s",
							fromChatUser.getCode ()));

					return responder ("chatBroadcastSendResponder");

				}

				// perform search

				List<Integer> allChatUserIds =
					new ArrayList<Integer> ();

				if (search) {

					profileLogger.lap ("perform search");

					Map<String,Object> searchMap =
						new LinkedHashMap<String,Object> ();

					searchMap.put ("chatId", chat.getId ());
					searchMap.put ("type", ChatUserType.user);
					searchMap.put ("notDeleted", true);
					searchMap.put ("number", "447_________");

					searchMap.put ("deliveryMethodIn",
						ImmutableList.<ChatMessageMethod>of (
							ChatMessageMethod.sms));

					// TODO block all?

					if (params.get ("searchLastActionFrom") != null) {

						searchMap.put (
							"lastActionAfter",
							params.get ("searchLastActionFrom"));

					}

					if (params.get ("searchLastActionTo") != null) {

						searchMap.put (
							"lastActionBefore",
							params.get ("searchLastActionTo"));

					}

					if (params.get ("searchGender") != null) {

						searchMap.put (
							"gender",
							params.get ("searchGender"));

					}

					if (params.get ("searchOrient") != null) {

						searchMap.put (
							"orient",
							params.get ("searchOrient"));

					}

					if (params.get ("searchPicture") != null) {

						searchMap.put (
							"hasImage",
							params.get ("searchPicture"));

					}

					if (params.get ("searchAdult") != null) {

						searchMap.put (
							"adultVerified",
							params.get ("searchAdult"));

					}

					if (params.get ("searchSpendMin") != null) {

						searchMap.put (
							"valueSinceEverGte",
							((Integer) params.get ("searchSpendMin")) * 100);

					}

					if (params.get ("searchSpendMax") != null) {

						searchMap.put (
							"valueSinceEverLte",
							((Integer) params.get ("searchSpendMax")) * 100);

					}

					allChatUserIds =
						chatUserHelper.searchIds (
							searchMap);

					log.debug (
						stringFormat (
							"Search returned %d users",
							allChatUserIds.size ()));

				}

				// check numbers

				if (! search) {

					profileLogger.lap ("check numbers");

					try {

						List<String> allNumbers =
							numberFormatLogic.parseLines (
								chat.getNumberFormat (),
								(String) requestContext.getForm ("numbers"));

						int loop0 = 0;

						for (String number
								: allNumbers) {

System.out.println ("NUMBER:"+number);
							NumberRec numberRec =
								numberHelper.findByCode (
									GlobalId.root,
									number);

							if (numberRec == null)
								continue;

System.out.println ("a");
							ChatUserRec chatUser =
								chatUserHelper.find (
									chat,
									numberRec);

							if (chatUser == null)
								continue;
System.out.println ("b");

							allChatUserIds.add (
								chatUser.getId ());

							if (++ loop0 % 128 == 0) {
								database.flush ();
								database.clear ();
							}

						}

					} catch (WbsNumberFormatException exception) {

						requestContext.addError (
							"Invalid mobile number");

						return responder ("chatBroadcastSendResponder");

					}

				}

				// purge numbers

				profileLogger.lap ("purge numbers");

				int removedNumbers = 0;

				List<Integer> remainingChatUserIds =
					new ArrayList<Integer> ();

				int loop1 = 0;

				boolean includeBlocked =
					allOf (
						requestContext.canContext ("chat.manage"),
						(Boolean) params.get ("includeBlocked"));

				boolean includeOptedOut =
					allOf (
						requestContext.canContext ("chat.manage"),
						(Boolean) params.get ("includeOptedOut"));

				for (
					Integer chatUserId
						: allChatUserIds
				) {

System.out.println ("ID:"+chatUserId);
					ChatUserRec chatUser =
						chatUserHelper.find (
							chatUserId);

					if (
						! chatBroadcastLogic.canSendToUser (
							chatUser,
							includeBlocked,
							includeOptedOut)
					) {
System.out.println ("skip");
						continue;
					}
System.out.println ("include");

					remainingChatUserIds.add (
						chatUserId);

					// don't use too much memory

					if (++ loop1 % 128 == 0) {

						database.flush ();

						database.clear ();

					}

				}

				if (includeBlocked) {

					requestContext.addWarning (
						"Blocked numbers will NOT be excluded");

				}

				if (removedNumbers > 0) {

					if (includeBlocked) {

						requestContext.addNotice (
							stringFormat (
								"Purged %d numbers due to barring",
								removedNumbers));

					} else {

						requestContext.addNotice (
							stringFormat (
								"Purged %d numbers due to blocking and/or barring",
								removedNumbers));

					}

				}

				// show verify page

				requestContext.request (
					"chatBroadcastParams",
					sessionParams);

				requestContext.request (
					"chatBroadcastChatUserIds",
					remainingChatUserIds);

				if (verify) {

					profileLogger.end ();

					return responder ("chatBroadcastVerifyResponder");

				}

				// perform send

				profileLogger.lap (
					"perform send");

				BatchSubjectRec batchSubject =
					batchLogic.batchSubject (
						chat,
						"broadcast");

				batchHelper.insert (
					new BatchRec ()

					.setSubject (
						batchSubject)

					.setCode (
						batchSubject.getCode ())

				);

				UserRec myUser =
					userHelper.find (
						requestContext.userId ());

				String messageString =
					joinWithoutSeparator (
						(String) params.get ("prefix"),
						(String) params.get ("message"));

				int messageLength =
					Gsm.length (messageString);

				if (messageLength > 160) {

					requestContext.addError (
						"Message is over 160 characters");

					requestContext.request (
						"chatBroadcastParams",
						sessionParams);

					requestContext.request (
						"chatBroadcastChatUserIds",
						remainingChatUserIds);

					return null;

				}

				TextRec text =
					textHelper.findOrCreate (
						messageString);

				ChatBroadcastRec chatBroadcast =
					new ChatBroadcastRec ()

					.setChat (
						chat)

					.setState (
						ChatBroadcastState.sending)

					.setCreatedUser (
						myUser)

					.setSentUser (
						myUser)

					.setCreatedTime (
						transaction.now ())

					.setSentTime (
						transaction.now ())

					.setChatUser (
						fromChatUser)

					.setText (
						text)

					.setNumAccepted (
						remainingChatUserIds.size ())

					.setSearch (
						search);

				if (search) {

					if (params.get ("searchLastActionFrom") != null) {

						chatBroadcast

							.setSearchLastActionFrom (
								(Date)
								params.get ("searchLastActionFrom"));

					}

					if (params.get ("searchLastActionTo") != null) {

						chatBroadcast

							.setSearchLastActionTo (
								(Date)
								params.get ("searchLastActionTo"));

					}

					if (params.get ("searchGender") != null) {

						chatBroadcast

							.setSearchGender (
								(Gender)
								params.get ("searchGender"));

					}

					if (params.get ("searchOrient") != null) {

						chatBroadcast

							.setSearchOrient (
								(Orient)
								params.get ("searchOrient"));

					}

					if (params.get ("searchPicture") != null) {

						chatBroadcast

							.setSearchPicture (
								(Boolean)
								params.get ("searchPicture"));

					}

					if (params.get ("searchAdult") != null) {

						chatBroadcast

							.setSearchAdult (
								(Boolean)
								params.get ("searchAdult"));

					}

					if (params.get ("searchSpendMin") != null) {

						chatBroadcast

							.setSearchSpendMin (
								(Integer)
								params.get ("searchSpendMin"));

					}

					if (params.get ("searchSpendMax") != null) {

						chatBroadcast

							.setSearchSpendMax (
								(Integer)
								params.get ("searchSpendMax"));

					}

				}

				chatBroadcast

					.setIncludeBlocked (
						includeBlocked)

					.setIncludeOptedOut (
						includeOptedOut);

				chatBroadcastHelper.insert (
					chatBroadcast);

				int loop3 = 0;

				for (
					Integer toChatUserId
						: remainingChatUserIds
				) {

					ChatUserRec toChatUser =
						chatUserHelper.find (
							toChatUserId);

					// record this number in the broadcast

					chatBroadcastNumberHelper.insert (
						new ChatBroadcastNumberRec ()

						.setChatBroadcast (
							chatBroadcast)

						.setChatUser (
							toChatUser)

						.setState (
							ChatBroadcastNumberState.accepted)

 						.setAddedByUser (
 							myUser)

					);

					// make sure we don't run out of memory

					if (++ loop3 % 128 == 0) {

						database.flushAndClear ();

					}

				}

				profileLogger.lap ("commit");

				transaction.commit ();

				profileLogger.end ();

				requestContext.addNotice (
					stringFormat (
						"Message sent to %d users",
						remainingChatUserIds.size ()));

				return responder ("chatBroadcastSendResponder");

			}

			return responder ("chatBroadcastSendResponder");

		} catch (RuntimeException exception) {

			profileLogger.error (exception);

			throw exception;

		}

	}

	// params

	final static
	ParamCheckerSet verifyParamChecker =
		new ParamCheckerSet (
			new ImmutableMap.Builder<String,ParamChecker<?>> ()

		.put (
			"search",
			new YesNoParamChecker (
				"Invalid search enabled",
				true))

		.put (
			"searchLastActionFrom",
			new TimestampFromParamChecker (
				"Invalid last action from",
				false))

		.put (
			"searchLastActionTo",
			new TimestampToParamChecker (
				"Invalid last action to",
				false))

		.put (
			"searchGender",
			new EnumParamChecker<Gender> (
				"Invalid gender",
				false,
				Gender.class))

		.put (
			"searchOrient",
			new EnumParamChecker<Orient> (
				"Invalid orient",
				false,
				Orient.class))

		.put (
			"searchPicture",
			new YesNoParamChecker (
				"Invalid picture",
				false))

		.put (
			"searchAdult",
			new YesNoParamChecker (
				"Invalid adult",
				false))

		.put (
			"searchSpendMin",
			new IntegerParamChecker (
				"Invalid search spend minimum",
				false))

		.put (
			"searchSpendMax",
			new IntegerParamChecker (
				"Invalid search spend maximum",
				false))

		.put (
			"fromUserCode",
			new RegexpParamChecker (
				"Invalid user code",
				"^\\d{6}$"))

		.put (
			"includeBlocked",
			new CheckboxParamChecker (
				"Internal error"))

		.put (
			"includeOptedOut",
			new CheckboxParamChecker (
				"Internal error"))

		.build ());

	final static
	ParamCheckerSet sendParamChecker =

		new ParamCheckerSet (
			new ImmutableMap.Builder<String,ParamChecker<?>> ()

		.putAll (
			verifyParamChecker.getParamCheckers ())

		.put (
			"prefix",
			new RegexpParamChecker (
				"Invalid prefix",
				"^From .+: $"))

		.put (
			"message",
			new RegexpParamChecker (
				"Invalid message",
				".+"))

		.build ());

}
