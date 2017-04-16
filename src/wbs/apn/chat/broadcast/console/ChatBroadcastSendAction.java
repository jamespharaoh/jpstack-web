package wbs.apn.chat.broadcast.console;

import static wbs.utils.etc.LogicUtils.allOf;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.orNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.fromJavaInteger;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.apache.commons.lang3.Range;

import wbs.console.action.ConsoleAction;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleModule;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.LogSeverity;
import wbs.framework.logging.TaskLogger;

import wbs.platform.object.core.console.ObjectTypeConsoleHelper;
import wbs.platform.service.console.ServiceConsoleHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.gsm.GsmUtils;
import wbs.sms.magicnumber.logic.MagicNumberLogic;
import wbs.sms.message.batch.logic.BatchLogic;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.batch.model.BatchSubjectRec;
import wbs.sms.number.core.console.NumberConsoleHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;

import wbs.utils.etc.ProfileLogger;
import wbs.utils.time.TextualInterval;

import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.broadcast.logic.ChatBroadcastLogic;
import wbs.apn.chat.broadcast.model.ChatBroadcastNumberObjectHelper;
import wbs.apn.chat.broadcast.model.ChatBroadcastNumberState;
import wbs.apn.chat.broadcast.model.ChatBroadcastObjectHelper;
import wbs.apn.chat.broadcast.model.ChatBroadcastRec;
import wbs.apn.chat.broadcast.model.ChatBroadcastState;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserDao;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserSearch;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.web.responder.Responder;

@PrototypeComponent ("chatBroadcastSendAction")
public
class ChatBroadcastSendAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	BatchObjectHelper batchHelper;

	@SingletonDependency
	BatchLogic batchLogic;

	@SingletonDependency
	@Named
	ConsoleModule chatBroadcastConsoleModule;

	@SingletonDependency
	ChatBroadcastObjectHelper chatBroadcastHelper;

	@SingletonDependency
	ChatBroadcastLogic chatBroadcastLogic;

	@SingletonDependency
	ChatBroadcastNumberObjectHelper chatBroadcastNumberHelper;

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	ChatUserDao chatUserDao;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MagicNumberLogic magicNumberLogic;

	@SingletonDependency
	NumberFormatLogic numberFormatLogic;

	@SingletonDependency
	NumberConsoleHelper numberHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ObjectTypeConsoleHelper objectTypeHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ServiceConsoleHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// details

	@Override
	protected
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		if (
			optionalIsPresent (
				requestContext.form (
					"send"))
		) {

			return responder (
				"chatBroadcastVerifyResponder");

		}

		return responder (
			"chatBroadcastSendResponder");

	}

	// implementation

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goReal");

		boolean verify =
			requestContext.formIsPresent (
				"verify");

		boolean send =
			requestContext.formIsPresent (
				"send");

		ProfileLogger profileLogger =
			new ProfileLogger (
				taskLogger,
				LogSeverity.debug,
				"Broadcast send");

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"ChatBroadcastSendAction.goReal ()",
					this);

		) {

			// load form

			FormFieldSet <?> searchFields =
				chatBroadcastConsoleModule.formFieldSets ().get (
					"send-search");

			FormFieldSet <?> numbersFields =
				chatBroadcastConsoleModule.formFieldSets ().get (
					"send-numbers");

			FormFieldSet <?> commonFields =
				chatBroadcastConsoleModule.formFieldSets ().get (
					"send-common");

			FormFieldSet <?> messageUserFields =
				chatBroadcastConsoleModule.formFieldSets ().get (
					"send-message-user");

			FormFieldSet <?> messageMessageFields =
				chatBroadcastConsoleModule.formFieldSets ().get (
					"send-message-message");

			ChatBroadcastSendForm form =
				new ChatBroadcastSendForm ()

				.includeBlocked (
					false)

				.includeOptedOut (
					false);

			Map <String, Object> formHints =
				ImmutableMap.<String, Object> builder ()

				.put (
					"chat",
					chatHelper.findFromContextRequired ())

				.build ();

			UpdateResultSet updateResults =
				new UpdateResultSet ();

			formFieldLogic.update (
				taskLogger,
				requestContext,
				searchFields,
				verify
					? updateResults
					: new UpdateResultSet (),
				form,
				formHints,
				"send");

			formFieldLogic.update (
				taskLogger,
				requestContext,
				numbersFields,
				verify
					? updateResults
					: new UpdateResultSet (),
				form,
				formHints,
				"send");

			formFieldLogic.update (
				taskLogger,
				requestContext,
				commonFields,
				verify
					? updateResults
					: new UpdateResultSet (),
				form,
				formHints,
				"send");

			formFieldLogic.update (
				taskLogger,
				requestContext,
				messageUserFields,
				verify
					? updateResults
					: new UpdateResultSet (),
				form,
				formHints,
				"send");

			formFieldLogic.update (
				taskLogger,
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
				requestContext.formIsPresent (
					"searchOn")
			) {

				form.search (
					true);

				requestContext.formData (
					"send-search",
					"yes");

				return null;

			}

			if (
				requestContext.formIsPresent (
					"searchOff")
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
					chatHelper.findFromContextRequired ();

				// lookup user

				profileLogger.lap (
					"lookup user");

				Optional <ChatUserRec> fromChatUserOptional =
					chatUserHelper.findByCode (
						chat,
						form.fromUser ());

				if (
					optionalIsNotPresent (
						fromChatUserOptional)
				) {

					requestContext.addError (
						stringFormat (
							"Chat user not found: %s",
							form.fromUser ()));

					return responder (
						"chatBroadcastSendResponder");

				}

				ChatUserRec fromChatUser =
					fromChatUserOptional.get ();

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

				List <Long> allChatUserIds =
					new ArrayList<> ();

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

						.deliveryMethodIn (
							ImmutableList.of (
								ChatMessageMethod.sms))

						// TODO block all? done later...

						.lastAction (
							orNull (
								TextualInterval.forInterval (
									userConsoleLogic.timezone (),
									Optional.fromNullable (
										form.lastAction ()))))

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

					taskLogger.debugFormat (
						"Search returned %s users",
						integerToDecimalString (
							allChatUserIds.size ()));

				}

				// check numbers

				if (! form.search ()) {

					profileLogger.lap ("check numbers");

					try {

						List <String> allNumbers =
							numberFormatLogic.parseLines (
								chat.getNumberFormat (),
								form.numbers ());

						int loop0 = 0;

						for (
							String number
								: allNumbers
						) {

							Optional <NumberRec> numberOptional =
								numberHelper.findByCode (
									GlobalId.root,
									number);

							if (
								optionalIsNotPresent (
									numberOptional)
							) {
								continue;
							}

							ChatUserRec chatUser =
								chatUserHelper.find (
									chat,
									numberOptional.get ());

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

				List <Long> remainingChatUserIds =
					new ArrayList<> ();

				int loop1 = 0;

				boolean includeBlocked = allOf (

					() -> requestContext.canContext (
						"chat.manage"),

					() -> form.includeBlocked ()

				);

				boolean includeOptedOut = allOf (

					() -> requestContext.canContext (
						"chat.manage"),

					() -> form.includeOptedOut ()

				);

				for (
					Long chatUserId
						: allChatUserIds
				) {

					ChatUserRec chatUser =
						chatUserHelper.findRequired (
							chatUserId);

					if (
						! chatBroadcastLogic.canSendToUser (
							taskLogger,
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

						requestContext.addNoticeFormat (
							"Purged %s numbers due to barring",
							integerToDecimalString (
								removedNumbers));

					} else {

						requestContext.addNoticeFormat (
							"Purged %s numbers ",
							integerToDecimalString (
								removedNumbers),
							"due to blocking and/or barring");

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

				String messageString =
					joinWithoutSeparator (
						form.prefix (),
						form.message ());

				long messageLength =
					GsmUtils.gsmStringLength (
						messageString);

				if (messageLength > 160) {

					requestContext.addError (
						"Message is over 160 characters");

					return null;

				}

				TextRec text =
					textHelper.findOrCreate (
						taskLogger,
						messageString);

				ChatBroadcastRec chatBroadcast =
					chatBroadcastHelper.createInstance ()

					.setChat (
						chat)

					.setState (
						ChatBroadcastState.sending)

					.setCreatedUser (
						userConsoleLogic.userRequired ())

					.setSentUser (
						userConsoleLogic.userRequired ())

					.setCreatedTime (
						transaction.now ())

					.setSentTime (
						transaction.now ())

					.setChatUser (
						fromChatUser)

					.setText (
						text)

					.setNumAccepted (
						fromJavaInteger (
							remainingChatUserIds.size ()))

					.setSearch (
						form.search ());

				if (form.search ()) {

					chatBroadcast

						.setSearchLastActionFrom (
							form.lastAction () != null
								? form.lastAction ().getStart ()
								: null)

						.setSearchLastActionTo (
							form.lastAction () != null
								? form.lastAction ().getEnd ()
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
					taskLogger,
					chatBroadcast);

				// create batch

				BatchSubjectRec batchSubject =
					batchLogic.batchSubject (
						taskLogger,
						chat,
						"broadcast");

				batchHelper.insert (
					taskLogger,
					batchHelper.createInstance ()

					.setSubject (
						batchSubject)

					.setCode (
						batchSubject.getCode ())

					.setParentType (
						objectTypeHelper.findByCodeRequired (
							GlobalId.root,
							"chat_broadcast"))

					.setParentId (
						chatBroadcast.getId ())

				);

				// store broadcast numbers

				int loop3 = 0;

				for (
					Long toChatUserId
						: remainingChatUserIds
				) {

					ChatUserRec toChatUser =
						chatUserHelper.findRequired (
							toChatUserId);

					// record this number in the broadcast

					chatBroadcastNumberHelper.insert (
						taskLogger,
						chatBroadcastNumberHelper.createInstance ()

						.setChatBroadcast (
							chatBroadcast)

						.setChatUser (
							toChatUser)

						.setState (
							ChatBroadcastNumberState.accepted)

						.setAddedByUser (
							userConsoleLogic.userRequired ())

					);

					// make sure we don't run out of memory

					if (++ loop3 % 1024 == 0) {

						database.flushAndClear ();

					}

				}

				profileLogger.lap ("commit");

				transaction.commit ();

				profileLogger.end ();

				requestContext.addNoticeFormat (
					"Message sent to %s users",
					integerToDecimalString (
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
