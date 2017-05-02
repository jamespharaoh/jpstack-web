package wbs.apn.chat.broadcast.console;

import static wbs.utils.etc.LogicUtils.allOf;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.orNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.fromJavaInteger;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
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
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
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

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			// load form

			boolean verify =
				requestContext.formIsPresent (
					"verify");

			boolean send =
				requestContext.formIsPresent (
					"send");

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
					chatHelper.findFromContextRequired (
						transaction))

				.build ();

			UpdateResultSet updateResults =
				new UpdateResultSet ();

			formFieldLogic.update (
				transaction,
				requestContext,
				searchFields,
				verify
					? updateResults
					: new UpdateResultSet (),
				form,
				formHints,
				"send");

			formFieldLogic.update (
				transaction,
				requestContext,
				numbersFields,
				verify
					? updateResults
					: new UpdateResultSet (),
				form,
				formHints,
				"send");

			formFieldLogic.update (
				transaction,
				requestContext,
				commonFields,
				verify
					? updateResults
					: new UpdateResultSet (),
				form,
				formHints,
				"send");

			formFieldLogic.update (
				transaction,
				requestContext,
				messageUserFields,
				verify
					? updateResults
					: new UpdateResultSet (),
				form,
				formHints,
				"send");

			formFieldLogic.update (
				transaction,
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

				ChatRec chat =
					chatHelper.findFromContextRequired (
						transaction);

				// lookup user

				Optional <ChatUserRec> fromChatUserOptional =
					chatUserHelper.findByCode (
						transaction,
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
									userConsoleLogic.timezone (
										transaction),
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
							transaction,
							search);

					transaction.debugFormat (
						"Search returned %s users",
						integerToDecimalString (
							allChatUserIds.size ()));

				}

				// check numbers

				if (! form.search ()) {

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
									transaction,
									GlobalId.root,
									number);

							if (
								optionalIsNotPresent (
									numberOptional)
							) {
								continue;
							}

							Optional <ChatUserRec> chatUserOptional =
								chatUserHelper.find (
									transaction,
									chat,
									numberOptional.get ());

							if (
								optionalIsNotPresent (
									chatUserOptional)
							) {
								continue;
							}

							ChatUserRec chatUser =
								optionalGetRequired (
									chatUserOptional);

							allChatUserIds.add (
								chatUser.getId ());

							if (++ loop0 % 128 == 0) {

								transaction.flush ();

								transaction.clear ();

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
							transaction,
							chatUserId);

					if (
						! chatBroadcastLogic.canSendToUser (
							transaction,
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

						transaction.flush ();

						transaction.clear ();

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

					return responder (
						"chatBroadcastVerifyResponder");

				}

				// perform send

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
						transaction,
						messageString);

				ChatBroadcastRec chatBroadcast =
					chatBroadcastHelper.createInstance ()

					.setChat (
						chat)

					.setState (
						ChatBroadcastState.sending)

					.setCreatedUser (
						userConsoleLogic.userRequired (
							transaction))

					.setSentUser (
						userConsoleLogic.userRequired (
							transaction))

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
					transaction,
					chatBroadcast);

				// create batch

				BatchSubjectRec batchSubject =
					batchLogic.batchSubject (
						transaction,
						chat,
						"broadcast");

				batchHelper.insert (
					transaction,
					batchHelper.createInstance ()

					.setSubject (
						batchSubject)

					.setCode (
						batchSubject.getCode ())

					.setParentType (
						objectTypeHelper.findByCodeRequired (
							transaction,
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
							transaction,
							toChatUserId);

					// record this number in the broadcast

					chatBroadcastNumberHelper.insert (
						transaction,
						chatBroadcastNumberHelper.createInstance ()

						.setChatBroadcast (
							chatBroadcast)

						.setChatUser (
							toChatUser)

						.setState (
							ChatBroadcastNumberState.accepted)

						.setAddedByUser (
							userConsoleLogic.userRequired (
								transaction))

					);

					// make sure we don't run out of memory

					if (++ loop3 % 1024 == 0) {

						transaction.flush ();

						transaction.clear ();

					}

				}

				transaction.commit ();

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

		}

	}

}
