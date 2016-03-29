package wbs.clients.apn.chat.broadcast.console;

import static wbs.framework.utils.etc.Misc.allOf;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.joinWithoutSeparator;
import static wbs.framework.utils.etc.Misc.moreThanZero;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import org.apache.commons.lang3.Range;
import org.apache.log4j.Level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.broadcast.logic.ChatBroadcastLogic;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastNumberObjectHelper;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastNumberState;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastObjectHelper;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastRec;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastState;
import wbs.clients.apn.chat.contact.model.ChatMessageMethod;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserDao;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserSearch;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.console.action.ConsoleAction;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.misc.TimeFormatter;
import wbs.console.module.ConsoleModule;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
import wbs.framework.utils.TextualInterval;
import wbs.framework.utils.etc.ProfileLogger;
import wbs.framework.web.Responder;
import wbs.platform.object.core.console.ObjectTypeConsoleHelper;
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
import wbs.sms.message.batch.model.BatchSubjectRec;
import wbs.sms.number.core.console.NumberConsoleHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;

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

	@Inject @Named
	ConsoleModule chatBroadcastConsoleModule;

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
	FormFieldLogic formFieldLogic;

	@Inject
	MagicNumberLogic magicNumberLogic;

	@Inject
	NumberFormatLogic numberFormatLogic;

	@Inject
	NumberConsoleHelper numberHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ObjectTypeConsoleHelper objectTypeHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	ServiceConsoleHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	TimeFormatter timeFormatter;

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

		boolean verify =
			requestContext.getForm ("verify") != null;

		boolean send =
			requestContext.getForm ("send") != null;

		ProfileLogger profileLogger =
			new ProfileLogger (
				log,
				Level.DEBUG,
				"Broadcast send");

		try {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					this);

			// load form

			FormFieldSet searchFields =
				chatBroadcastConsoleModule.formFieldSets ().get (
					"send-search");

			FormFieldSet numbersFields =
				chatBroadcastConsoleModule.formFieldSets ().get (
					"send-numbers");

			FormFieldSet commonFields =
				chatBroadcastConsoleModule.formFieldSets ().get (
					"send-common");

			FormFieldSet messageUserFields =
				chatBroadcastConsoleModule.formFieldSets ().get (
					"send-message-user");

			FormFieldSet messageMessageFields =
				chatBroadcastConsoleModule.formFieldSets ().get (
					"send-message-message");

			ChatBroadcastSendForm form =
				new ChatBroadcastSendForm ()

				.includeBlocked (
					false)

				.includeOptedOut (
					false);

			Map<String,Object> formHints =
				ImmutableMap.<String,Object>builder ()

				.put (
					"chat",
					chatHelper.find (
						requestContext.stuffInt (
							"chatId")))

				.build ();

			UpdateResultSet updateResults =
				new UpdateResultSet ();

			formFieldLogic.update (
				requestContext,
				searchFields,
				verify
					? updateResults
					: new UpdateResultSet (),
				form,
				formHints,
				"send");

			formFieldLogic.update (
				requestContext,
				numbersFields,
				verify
					? updateResults
					: new UpdateResultSet (),
				form,
				formHints,
				"send");

			formFieldLogic.update (
				requestContext,
				commonFields,
				verify
					? updateResults
					: new UpdateResultSet (),
				form,
				formHints,
				"send");

			formFieldLogic.update (
				requestContext,
				messageUserFields,
				verify
					? updateResults
					: new UpdateResultSet (),
				form,
				formHints,
				"send");

			formFieldLogic.update (
				requestContext,
				messageMessageFields,
				send
					? updateResults
					: new UpdateResultSet (),
				form,
				formHints,
				"send");

			requestContext.request (
				"chatBroadcastForm",
				form);

			// enable or disable search

			if (
				isNotNull (
					requestContext.getForm (
						"searchOn"))
			) {

				form.search (
					true);

				requestContext.formData (
					"send-search",
					"yes");

				return null;

			}

			if (
				isNotNull (
					requestContext.getForm (
						"searchOff"))
			) {

				form.search (
					false);

				requestContext.formData (
					"send-search",
					"no");

				return null;

			}

			// report errors

			if (
				moreThanZero (
					updateResults.errorCount ())
			) {

				formFieldLogic.reportErrors (
					requestContext,
					updateResults,
					"send");

				return null;

			}

			// do some work

			if (send || verify) {

				// start transaction

				profileLogger.lap (
					"start transaction");

				ChatRec chat =
					chatHelper.find (
						requestContext.stuffInt ("chatId"));

				// lookup user

				profileLogger.lap ("lookup user");

				ChatUserRec fromChatUser =
					chatUserHelper.findByCode (
						chat,
						form.fromUser ());

				if (fromChatUser == null) {

					requestContext.addError (
						stringFormat (
							"Chat user not found: %s",
							form.fromUser ()));

					return responder (
						"chatBroadcastSendResponder");

				}

				form.prefix (
					fromChatUser.getName () != null
						? stringFormat (
							"From %s %s: ",
							fromChatUser.getName (),
							fromChatUser.getCode ())
						: stringFormat (
							"From %s: ",
							fromChatUser.getCode ()));

				if (fromChatUser.getType () != ChatUserType.monitor) {

					requestContext.addError (
						stringFormat (
							"Chat user is not a monitor: %s",
							fromChatUser.getCode ()));

					return responder (
						"chatBroadcastSendResponder");

				}

				// perform search

				List<Integer> allChatUserIds =
					new ArrayList<Integer> ();

				if (form.search ()) {

					profileLogger.lap ("perform search");

					ChatUserSearch search =
						new ChatUserSearch ()

						.chatId (
							chat.getId ())

						.type (
							ChatUserType.user)

						.deleted (
							false)

						.numberLike (
							"447_________")

						.deliveryMethodIn (
							ImmutableList.of (
								ChatMessageMethod.sms))

						// TODO block all? done later...

						.lastAction (
							TextualInterval.forInterval (
								timeFormatter.defaultTimezone (),
								form.lastAction ()))

						.gender (
							form.gender ())

						.orient (
							form.orient ())

						.hasCategory (
							isNotNull (
								form.categoryId))

						.categoryId (
							form.categoryId ())

						.hasPicture (
							form.hasPicture ())

						.adultVerified (
							form.isAdult ())

						.valueSinceEver (
							Range.between (
								ifNull (
									form.minimumSpend (),
									Long.MIN_VALUE),
								ifNull (
									form.maximumSpend (),
									Long.MAX_VALUE)));

					allChatUserIds =
						chatUserHelper.searchIds (
							search);

					log.debug (
						stringFormat (
							"Search returned %d users",
							allChatUserIds.size ()));

				}

				// check numbers

				if (! form.search ()) {

					profileLogger.lap ("check numbers");

					try {

						List<String> allNumbers =
							numberFormatLogic.parseLines (
								chat.getNumberFormat (),
								form.numbers ());

						int loop0 = 0;

						for (
							String number
								: allNumbers
						) {

							NumberRec numberRec =
								numberHelper.findByCode (
									GlobalId.root,
									number);

							if (numberRec == null)
								continue;

							ChatUserRec chatUser =
								chatUserHelper.find (
									chat,
									numberRec);

							if (chatUser == null)
								continue;

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

						return responder (
							"chatBroadcastSendResponder");

					}

				}

				// purge numbers

				profileLogger.lap (
					"purge numbers");

				int removedNumbers = 0;

				List<Integer> remainingChatUserIds =
					new ArrayList<Integer> ();

				int loop1 = 0;

				boolean includeBlocked =
					allOf (
						requestContext.canContext (
							"chat.manage"),
						form.includeBlocked ());

				boolean includeOptedOut =
					allOf (
						requestContext.canContext (
							"chat.manage"),
						form.includeOptedOut ());

				for (
					Integer chatUserId
						: allChatUserIds
				) {

					ChatUserRec chatUser =
						chatUserHelper.find (
							chatUserId);

					if (
						! chatBroadcastLogic.canSendToUser (
							chatUser,
							includeBlocked,
							includeOptedOut)
					) {
						continue;
					}

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
					"chatBroadcastChatUserIds",
					remainingChatUserIds);

				if (verify) {

					profileLogger.end ();

					return responder (
						"chatBroadcastVerifyResponder");

				}

				// perform send

				profileLogger.lap (
					"perform send");

				UserRec myUser =
					userHelper.find (
						requestContext.userId ());

				String messageString =
					joinWithoutSeparator (
						form.prefix (),
						form.message ());

				int messageLength =
					Gsm.length (
						messageString);

				if (messageLength > 160) {

					requestContext.addError (
						"Message is over 160 characters");

					return null;

				}

				TextRec text =
					textHelper.findOrCreate (
						messageString);

				ChatBroadcastRec chatBroadcast =
					chatBroadcastHelper.createInstance ()

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
						(long)
						remainingChatUserIds.size ())

					.setSearch (
						form.search ());

				if (form.search ()) {

					chatBroadcast

						.setSearchLastActionFrom (
							form.lastAction () != null
								? instantToDate (
									form.lastAction ().getStart ())
								: null)

						.setSearchLastActionTo (
							form.lastAction () != null
								? instantToDate (
									form.lastAction ().getEnd ())
								: null)

						.setSearchGender (
							form.gender ())

						.setSearchOrient (
							form.orient ())

						.setSearchPicture (
							form.hasPicture ())

						.setSearchAdult (
							form.isAdult ())

						.setSearchSpendMin (
							form.minimumSpend ())

						.setSearchSpendMax (
							form.maximumSpend ());

				}

				chatBroadcast

					.setIncludeBlocked (
						includeBlocked)

					.setIncludeOptedOut (
						includeOptedOut);

				chatBroadcastHelper.insert (
					chatBroadcast);

				// create batch

				BatchSubjectRec batchSubject =
					batchLogic.batchSubject (
						chat,
						"broadcast");

				batchHelper.insert (
					batchHelper.createInstance ()

					.setSubject (
						batchSubject)

					.setCode (
						batchSubject.getCode ())

					.setParentType (
						objectTypeHelper.findByCode (
							GlobalId.root,
							"chat_broadcast"))

					.setParentId (
						chatBroadcast.getId ())

				);

				// store broadcast numbers

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
						chatBroadcastNumberHelper.createInstance ()

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

					if (++ loop3 % 1024 == 0) {

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

				requestContext.request (
					"chatBroadcastForm",
					new ChatBroadcastSendForm ()

					.includeBlocked (
						false)

					.includeOptedOut (
						false)

				);

				requestContext.setEmptyFormData ();

				return responder (
					"chatBroadcastSendResponder");

			}

			return responder (
				"chatBroadcastSendResponder");

		} catch (RuntimeException exception) {

			profileLogger.error (exception);

			throw exception;

		}

	}

}
