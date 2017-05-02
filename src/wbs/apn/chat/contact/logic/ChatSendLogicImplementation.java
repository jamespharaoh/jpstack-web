package wbs.apn.chat.contact.logic;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalMapRequiredOrDefault;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectManager;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.misc.MapStringSubstituter;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserRec;

import wbs.sms.command.logic.CommandLogic;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.keyword.logic.KeywordLogic;
import wbs.sms.magicnumber.logic.MagicNumberLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;
import wbs.sms.route.router.model.RouterRec;

import wbs.utils.etc.NumberUtils;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.apn.chat.help.logic.ChatHelpTemplateLogic;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;

@SingletonComponent ("chatSendLogic")
public
class ChatSendLogicImplementation
	implements ChatSendLogic {

	// singleton dependency

	@SingletonDependency
	ChatHelpLogLogic chatHelpLogLogic;

	@SingletonDependency
	ChatHelpTemplateLogic chatTemplateLogic;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	CommandLogic commandLogic;

	@SingletonDependency
	KeywordLogic keywordLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MagicNumberLogic magicNumberLogic;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSenderProvider;

	/**
	 * Sends a message to a user using the "free" reverse bill route.
	 *
	 * @param chatUser
	 *            the chat user to send to
	 * @param threadId
	 *            the message thread to associate with, or null
	 * @param service
	 *            the service to associate with
	 * @param message
	 *            the message to send
	 * @return the created message record
	 */
	@Override
	public
	MessageRec sendMessageRbFree (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull Optional<Long> threadId,
			@NonNull ServiceRec service,
			@NonNull String message) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendMessageRbFree");

		) {

			if (chatUser.getNumber () == null) {

				throw new NullPointerException (
					stringFormat (
						"%s has no number",
						objectManager.objectPath (
							transaction,
							chatUser)));

			}

			ChatSchemeRec chatScheme =
				chatUser.getChatScheme ();

			if (chatScheme == null) {

				throw new NullPointerException (
					stringFormat (
						"%s has no chat scheme",
						objectManager.objectPath (
							transaction,
							chatUser)));

			}

			AffiliateRec affiliate =
				chatUserLogic.getAffiliate (
					transaction,
					chatUser);

			return messageSenderProvider.get ()

				.threadId (
					threadId.orNull ())

				.number (
					chatUser.getNumber ())

				.messageString (
					transaction,
					message)

				.numFrom (
					chatScheme.getRbNumber ())

				.routerResolve (
					transaction,
					chatScheme.getRbFreeRouter ())

				.service (
					service)

				.affiliate (
					affiliate)

				.send (
					transaction);

		}

	}

	/**
	 * Sends a chat user a system message and logs it.
	 *
	 * @param chatUser
	 *            ChatUserRec of user to send to
	 * @param threadId
	 *            threadId to associate with, or null for new thread
	 * @param templateCode
	 *            code to find the ChatHelpTemplateRec with
	 * @param route
	 *            route to send on
	 * @param numFrom
	 *            number to send from
	 * @return newly created MessageRec
	 */
	@Override
	public
	Optional <MessageRec> sendSystem (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull Optional<Long> threadId,
			@NonNull String templateCode,
			@NonNull RouterRec router,
			@NonNull String numFrom,
			@NonNull Set<String> tags,
			@NonNull Optional<String> deliveryTypeCode,
			@NonNull String serviceCode,
			@NonNull TemplateMissing templateMissing,
			@NonNull Map<String,String> suppliedParams) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendSystem");

		) {

			ChatRec chat =
				chatUser.getChat ();

			// find the template

			ChatHelpTemplateRec chatHelpTemplate =
				chatTemplateLogic.findChatHelpTemplate (
					transaction,
					chatUser,
					"system",
					templateCode);

			if (chatHelpTemplate == null) {

				if (templateMissing == TemplateMissing.error) {

					throw new RuntimeException (
						stringFormat (
							"System template %s not found for chat %s",
							templateCode,
							objectManager.objectPathMini (
								transaction,
								chat)));

				} else {

					return optionalAbsent ();

				}

			}

			// substitute the params

			Map <String, String> allParams =
				addDefaultParams (
					transaction,
					chatUser,
					suppliedParams);

			String originalText =
				chatHelpTemplate.getText ();

			String finalText =
				MapStringSubstituter.substitute (
					originalText,
					allParams);

			ServiceRec service =
				serviceHelper.findByCodeRequired (
					transaction,
					chat,
					serviceCode);

			AffiliateRec affiliate =
				chatUserLogic.getAffiliate (
					transaction,
					chatUser);

			// send the message

			MessageRec message =
				messageSenderProvider.get ()

				.threadId (
					threadId.orNull ())

				.number (
					chatUser.getNumber ())

				.messageString (
					transaction,
					finalText)

				.numFrom (
					numFrom)

				.routerResolve (
					transaction,
					router)

				.service (
					service)

				.affiliate (
					affiliate)

				.deliveryTypeCode (
					transaction,
					deliveryTypeCode)

				.ref (
					chatUser.getId ())

				.tags (
					tags)

				.send (
					transaction);

			// log it

			chatHelpLogLogic.createChatHelpLogOut (
				transaction,
				chatUser,
				optionalAbsent (),
				optionalAbsent (),
				message,
				optionalAbsent (),
				finalText,
				optionalAbsent ());

			return Optional.of (
				message);

		}

	}

	@Override
	public
	Optional <MessageRec> sendSystemRbFree (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull Optional<Long> threadId,
			@NonNull String templateCode,
			@NonNull TemplateMissing templateMissing,
			@NonNull Map<String,String> suppliedParams) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendSystemRbFree");

		) {

			ChatRec chat =
				chatUser.getChat ();

			// find the template

			ChatHelpTemplateRec chatHelpTemplate =
				chatTemplateLogic.findChatHelpTemplate (
					transaction,
					chatUser,
					"system",
					templateCode);

			if (chatHelpTemplate == null) {

				if (templateMissing == TemplateMissing.error) {

					throw new RuntimeException (
						"System template not found: " + templateCode);

				} else {

					return Optional.absent ();

				}

			}

			// substitute the params

			String originalText =
				chatHelpTemplate.getText ();

			Map <String, String> allParams =
				addDefaultParams (
					transaction,
					chatUser,
					suppliedParams);

			String finalText =
				MapStringSubstituter.substitute (
					originalText,
					allParams);

			// send the message

			MessageRec message =
				sendMessageRbFree (
					transaction,
					chatUser,
					threadId,
					serviceHelper.findByCodeRequired (
						transaction,
						chat,
						"system"),
					finalText);

			// log it

			chatHelpLogLogic.createChatHelpLogOut (
				transaction,
				chatUser,
				optionalAbsent (),
				optionalAbsent (),
				message,
				optionalAbsent (),
				finalText,
				optionalAbsent ());

			// and return

			return optionalOf (
				message);

		}

	}

	@Override
	public
	MessageRec sendMessageMmsFree (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull Optional<Long> threadId,
			@NonNull String message,
			@NonNull CommandRec command,
			@NonNull ServiceRec service) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendMessageMmsFree");

		) {

			ChatSchemeRec chatScheme =
				chatUser.getChatScheme ();

			// send the message

			AffiliateRec affiliate =
				chatUserLogic.getAffiliate (
					transaction,
					chatUser);

			MessageRec ret =
				messageSenderProvider.get ()

				.threadId (
					threadId.orNull ())

				.number (
					chatUser.getNumber ())

				.messageString (
					transaction,
					message)

				.numFrom (
					chatScheme.getMmsNumber ())

				.routerResolve (
					transaction,
					chatScheme.getMmsFreeRouter ())

				.service (
					service)

				.affiliate (
					affiliate)

				.send (
					transaction);

			// set the fallback keyword on the mms thing to handle a reply with no
			// keyword

			if (chatScheme.getMmsKeywordSet () != null) {

				keywordLogic.createOrUpdateKeywordSetFallback (
					transaction,
					chatScheme.getMmsKeywordSet (),
					chatUser.getNumber (),
					command);

			}

			// and return

			return ret;

		}

	}

	@Override
	public
	Optional <MessageRec> sendSystemMmsFree (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull Optional<Long> threadId,
			@NonNull String templateCode,
			@NonNull CommandRec command,
			@NonNull TemplateMissing templateMissing) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendSystemMmsFree");

		) {

			ChatRec chat =
				chatUser.getChat ();

			// look up the template

			ChatHelpTemplateRec chatHelpTemplate =
				chatTemplateLogic.findChatHelpTemplate (
					transaction,
					chatUser,
					"system",
					templateCode);

			if (chatHelpTemplate == null) {

				if (templateMissing == TemplateMissing.error) {

					throw new RuntimeException (
						stringFormat (
							"System template %s not found for chat %s.%s",
							templateCode,
							chat.getSlice ().getCode (),
							chat.getCode ()));

				} else {

					return Optional.absent ();

				}

			}

			// send it

			ServiceRec systemService =
				serviceHelper.findByCodeRequired (
					transaction,
					chat,
					"system");

			MessageRec message =
				sendMessageMmsFree (
					transaction,
					chatUser,
					threadId,
					chatHelpTemplate.getText (),
					command,
					systemService);

			// log it

			chatHelpLogLogic.createChatHelpLogOut (
				transaction,
				chatUser,
				optionalAbsent (),
				optionalAbsent (),
				message,
				optionalAbsent (),
				chatHelpTemplate.getText (),
				optionalAbsent ());

			// and return

			return Optional.of (
				message);

		}

	}

	@Override
	public
	MessageRec sendMessageMagic (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull Optional<Long> threadId,
			@NonNull TextRec message,
			@NonNull CommandRec magicCommand,
			@NonNull ServiceRec service,
			@NonNull Long magicRef) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendMessageMagic");

		) {

			ChatSchemeRec chatScheme =
				chatUser.getChatScheme ();

			// sanity check

			if (chatScheme.getMagicNumberSet () == null) {

				throw new NullPointerException (
					stringFormat (
						"Error sending to chat user %s: " +
						"Chat scheme %s has no magic number set",
						objectManager.objectPath (
							transaction,
							chatUser),
						objectManager.objectPath (
							transaction,
							chatScheme)));

			}

			// send the message and return

			return magicNumberLogic.sendMessage (
				transaction,
				chatScheme.getMagicNumberSet (),
				chatUser.getNumber (),
				magicCommand,
				magicRef,
				threadId,
				message,
				chatScheme.getMagicRouter (),
				service,
				optionalAbsent (),
				chatUserLogic.getAffiliate (
					transaction,
					chatUser),
				optionalAbsent ());

		}

	}

	@Override
	public
	Optional <MessageRec> sendSystemMagic (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull Optional <Long> threadId,
			@NonNull String templateCode,
			@NonNull CommandRec magicCommand,
			@NonNull Long magicRef,
			@NonNull TemplateMissing templateMissing,
			@NonNull Map <String, String> suppliedParams) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendSystemMagic");

		) {

			ChatRec chat =
				chatUser.getChat ();

			// lookup template

			ChatHelpTemplateRec chatHelpTemplate =
				chatTemplateLogic.findChatHelpTemplate (
					transaction,
					chatUser,
					"system",
					templateCode);

			if (chatHelpTemplate == null) {

				if (templateMissing == TemplateMissing.error) {

					throw new RuntimeException (
						stringFormat (
							"System template not found: %s ",
							templateCode,
							"for chat %s, ",
							integerToDecimalString (
								chatUser.getChat ().getId ()),
							"user %s ",
							integerToDecimalString (
								chatUser.getId ()),
							"and thread %s ",
							optionalMapRequiredOrDefault (
								NumberUtils::integerToDecimalString,
								threadId,
								"(none)"),
							"and template code %s",
							templateCode));

				} else {

					return Optional.absent ();

				}

			}

			// substitude params

			String originalText =
				chatHelpTemplate.getText ();

			Map<String,String> allParams =
				addDefaultParams (
					transaction,
					chatUser,
					suppliedParams);

			String finalText =
				MapStringSubstituter.substitute (
					originalText,
					allParams);

			TextRec text =
				textHelper.findOrCreate (
					transaction,
					finalText);

			// send message

			MessageRec message =
				sendMessageMagic (
					transaction,
					chatUser,
					threadId,
					text,
					magicCommand,
					serviceHelper.findByCodeRequired (
						transaction,
						chat,
						"system"),
					magicRef);

			// log it

			chatHelpLogLogic.createChatHelpLogOut (
				transaction,
				chatUser,
				optionalAbsent (),
				optionalAbsent (),
				message,
				optionalAbsent (),
				finalText,
				optionalOf (
					ifThenElse (
						referenceEqualWithClass (
							CommandRec.class,
							magicCommand,
							commandHelper.findByCodeRequired (
								transaction,
								chat,
								"magic")),
						() -> commandHelper.findRequired (
							transaction,
							magicRef),
						() -> magicCommand)));

			return optionalOf (
				message);

		}

	}

	@Override
	public
	Long sendMessageMagic (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull Optional <Long> threadId,
			@NonNull Collection <TextRec> parts,
			@NonNull CommandRec magicCommand,
			@NonNull ServiceRec service,
			@NonNull Long magicRef,
			@NonNull Optional <UserRec> user) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendMessageMagic");

		) {

			ChatSchemeRec chatScheme =
				chatUser.getChatScheme ();

			return magicNumberLogic.sendMessage (
				transaction,
				chatScheme.getMagicNumberSet (),
				chatUser.getNumber (),
				magicCommand,
				magicRef,
				threadId,
				parts,
				chatScheme.getMagicRouter (),
				service,
				optionalAbsent (),
				chatUserLogic.getAffiliate (
					transaction,
					chatUser),
				user);

		}

	}

	@Override
	public
	Map <String, String> addDefaultParams (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull Map <String, String> params) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"addDefaultParams");

		) {

			return ImmutableMap.<String, String> builder ()

				.putAll (
					params)

				.put (
					"brandName",
					chatUserLogic.getBrandName (
						chatUser))

				.build ();

		}

	}

	@Override
	public
	String renderTemplate (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull String templateTypeCode,
			@NonNull String templateCode,
			@NonNull Map<String,String> suppliedParams) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderTemplate");

		) {

			Map <String, String> allParams =
				addDefaultParams (
					transaction,
					chatUser,
					suppliedParams);

			ChatHelpTemplateRec template =
				chatTemplateLogic.findChatHelpTemplate (
					transaction,
					chatUser,
					templateTypeCode,
					templateCode);

			String originalText =
				template.getText ();

			String finalText =
				MapStringSubstituter.substitute (
					originalText,
					allParams);

			return finalText;

		}

	}

}
