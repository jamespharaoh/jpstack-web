package wbs.clients.apn.chat.contact.logic;

import static wbs.framework.utils.etc.LogicUtils.ifThenElse;
import static wbs.framework.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.framework.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.framework.utils.etc.OptionalUtils.optionalOf;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.clients.apn.chat.help.logic.ChatHelpTemplateLogic;
import wbs.clients.apn.chat.help.model.ChatHelpLogRec;
import wbs.clients.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.annotations.SingletonDependency;
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
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.route.router.model.RouterRec;

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
	Provider <MessageSender> messageSenderProvider;

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
			@NonNull ChatUserRec chatUser,
			@NonNull Optional<Long> threadId,
			@NonNull ServiceRec service,
			@NonNull String message) {

		if (chatUser.getNumber () == null) {

			throw new NullPointerException (
				stringFormat (
					"%s has no number",
					objectManager.objectPath (chatUser)));

		}

		ChatSchemeRec chatScheme =
			chatUser.getChatScheme ();

		if (chatScheme == null) {

			throw new NullPointerException (
				stringFormat (
					"%s has no chat scheme",
					objectManager.objectPath (chatUser)));

		}

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (chatUser);

		return messageSenderProvider.get ()

			.threadId (
				threadId.orNull ())

			.number (
				chatUser.getNumber ())

			.messageString (
				message)

			.numFrom (
				chatScheme.getRbNumber ())

			.routerResolve (
				chatScheme.getRbFreeRouter ())

			.service (
				service)

			.affiliate (
				affiliate)

			.send ();

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
	Optional<MessageRec> sendSystem (
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

		ChatRec chat =
			chatUser.getChat ();

		// find the template

		ChatHelpTemplateRec chatHelpTemplate =
			chatTemplateLogic.findChatHelpTemplate (
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
							chat)));

			} else {

				return Optional.absent ();

			}

		}

		// substitute the params

		Map<String,String> allParams =
			addDefaultParams (
				chatUser,
				suppliedParams);

		String originalText =
			chatHelpTemplate.getText ();

		String finalText =
			MapStringSubstituter.substitute (
				originalText,
				allParams);

		// Override default originator number if set in the database

		// TODO wtf is this and why is it here? this requires a hidden field
		// to be changed when the reverse bill number changes which is
		// certainly not a good thing

		if (chatHelpTemplate.getFromNumber () != null
				&& chatHelpTemplate.getFromNumber () != "") {

			numFrom =
				chatHelpTemplate.getFromNumber ();

		}

		ServiceRec service =
			serviceHelper.findByCodeRequired (
				chat,
				serviceCode);

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (
				chatUser);

		// send the message

		MessageRec message =
			messageSenderProvider.get ()

			.threadId (
				threadId.orNull ())

			.number (
				chatUser.getNumber ())

			.messageString (
				finalText)

			.numFrom (
				numFrom)

			.routerResolve (
				router)

			.service (
				service)

			.affiliate (
				affiliate)

			.deliveryTypeCode (
				deliveryTypeCode)

			.ref (
				chatUser.getId ())

			.tags (
				tags)

			.send ();

		// log it

		chatHelpLogLogic.createChatHelpLogOut (
			chatUser,
			Optional.<ChatHelpLogRec>absent (),
			Optional.<UserRec>absent (),
			message,
			Optional.<ChatMessageRec>absent (),
			finalText,
			Optional.<CommandRec>absent ());

		return Optional.of (
			message);

	}

	@Override
	public
	Optional<MessageRec> sendSystemRbFree (
			@NonNull ChatUserRec chatUser,
			@NonNull Optional<Long> threadId,
			@NonNull String templateCode,
			@NonNull TemplateMissing templateMissing,
			@NonNull Map<String,String> suppliedParams) {

		ChatRec chat =
			chatUser.getChat ();

		// find the template

		ChatHelpTemplateRec chatHelpTemplate =
			chatTemplateLogic.findChatHelpTemplate (
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

		Map<String,String> allParams =
			addDefaultParams (
				chatUser,
				suppliedParams);

		String finalText =
			MapStringSubstituter.substitute (
				originalText,
				allParams);

		// send the message

		MessageRec message =
			sendMessageRbFree (
				chatUser,
				threadId,
				serviceHelper.findByCodeRequired (
					chat,
					"system"),
				finalText);

		// log it

		chatHelpLogLogic.createChatHelpLogOut (
			chatUser,
			Optional.<ChatHelpLogRec>absent (),
			Optional.<UserRec>absent (),
			message,
			Optional.<ChatMessageRec>absent (),
			finalText,
			Optional.<CommandRec>absent ());

		// and return

		return Optional.of (message);

	}

	@Override
	public
	MessageRec sendMessageMmsFree (
			@NonNull ChatUserRec chatUser,
			@NonNull Optional<Long> threadId,
			@NonNull String message,
			@NonNull CommandRec command,
			@NonNull ServiceRec service) {

		ChatSchemeRec chatScheme =
			chatUser.getChatScheme ();

		// send the message

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (
				chatUser);

		MessageRec ret =
			messageSenderProvider.get ()

			.threadId (
				threadId.orNull ())

			.number (
				chatUser.getNumber ())

			.messageString (
				message)

			.numFrom (
				chatScheme.getMmsNumber ())

			.routerResolve (
				chatScheme.getMmsFreeRouter ())

			.service (
				service)

			.affiliate (
				affiliate)

			.send ();

		// set the fallback keyword on the mms thing to handle a reply with no
		// keyword

		if (chatScheme.getMmsKeywordSet () != null) {

			keywordLogic.createOrUpdateKeywordSetFallback (
				chatScheme.getMmsKeywordSet (),
				chatUser.getNumber (),
				command);

		}

		// and return

		return ret;

	}

	@Override
	public
	Optional<MessageRec> sendSystemMmsFree (
			@NonNull ChatUserRec chatUser,
			@NonNull Optional<Long> threadId,
			@NonNull String templateCode,
			@NonNull CommandRec command,
			@NonNull TemplateMissing templateMissing) {

		ChatRec chat =
			chatUser.getChat ();

		// look up the template

		ChatHelpTemplateRec chatHelpTemplate =
			chatTemplateLogic.findChatHelpTemplate (
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
				chat,
				"system");

		MessageRec message =
			sendMessageMmsFree (
				chatUser,
				threadId,
				chatHelpTemplate.getText (),
				command,
				systemService);

		// log it

		chatHelpLogLogic.createChatHelpLogOut (
			chatUser,
			Optional.<ChatHelpLogRec>absent (),
			Optional.<UserRec>absent (),
			message,
			Optional.<ChatMessageRec>absent (),
			chatHelpTemplate.getText (),
			Optional.<CommandRec>absent ());

		// and return

		return Optional.of (
			message);

	}

	@Override
	public
	MessageRec sendMessageMagic (
			@NonNull ChatUserRec chatUser,
			@NonNull Optional<Long> threadId,
			@NonNull TextRec message,
			@NonNull CommandRec magicCommand,
			@NonNull ServiceRec service,
			@NonNull Long magicRef) {

		ChatSchemeRec chatScheme =
			chatUser.getChatScheme ();

		// sanity check

		if (chatScheme.getMagicNumberSet () == null) {

			throw new NullPointerException (
				stringFormat (
					"Error sending to chat user %s: " +
					"Chat scheme %s has no magic number set",
					objectManager.objectPath (
						chatUser),
					objectManager.objectPath (
						chatScheme)));

		}

		// send the message and return

		return magicNumberLogic.sendMessage (
			chatScheme.getMagicNumberSet (),
			chatUser.getNumber (),
			magicCommand,
			magicRef,
			threadId,
			message,
			chatScheme.getMagicRouter (),
			service,
			Optional.<BatchRec>absent (),
			chatUserLogic.getAffiliate (
				chatUser),
			Optional.<UserRec>absent ());

	}

	@Override
	public
	Optional <MessageRec> sendSystemMagic (
			@NonNull ChatUserRec chatUser,
			@NonNull Optional <Long> threadId,
			@NonNull String templateCode,
			@NonNull CommandRec magicCommand,
			@NonNull Long magicRef,
			@NonNull TemplateMissing templateMissing,
			@NonNull Map <String, String> suppliedParams) {

		ChatRec chat =
			chatUser.getChat ();

		// lookup template

		ChatHelpTemplateRec chatHelpTemplate =
			chatTemplateLogic.findChatHelpTemplate (
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
						chatUser.getChat ().getId (),
						"user %s ",
						chatUser.getId (),
						"and thread %s ",
						threadId,
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
				chatUser,
				suppliedParams);

		String finalText =
			MapStringSubstituter.substitute (
				originalText,
				allParams);

		TextRec text =
			textHelper.findOrCreate (
				finalText);

		// send message

		MessageRec message =
			sendMessageMagic (
				chatUser,
				threadId,
				text,
				magicCommand,
				serviceHelper.findByCodeRequired (
					chat,
					"system"),
				magicRef);

		// log it

		chatHelpLogLogic.createChatHelpLogOut (
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
							chat,
							"magic")),
					() -> commandHelper.findRequired (
						magicRef),
					() -> magicCommand)));

		return optionalOf (
			message);

	}

	@Override
	public
	Long sendMessageMagic (
			@NonNull ChatUserRec chatUser,
			@NonNull Optional <Long> threadId,
			@NonNull Collection <TextRec> parts,
			@NonNull CommandRec magicCommand,
			@NonNull ServiceRec service,
			@NonNull Long magicRef,
			@NonNull Optional <UserRec> user) {

		ChatSchemeRec chatScheme =
			chatUser.getChatScheme ();

		return magicNumberLogic.sendMessage (
			chatScheme.getMagicNumberSet (),
			chatUser.getNumber (),
			magicCommand,
			magicRef,
			threadId,
			parts,
			chatScheme.getMagicRouter (),
			service,
			Optional.<BatchRec>absent (),
			chatUserLogic.getAffiliate (
				chatUser),
			user);

	}

	@Override
	public
	Map<String,String> addDefaultParams (
			@NonNull ChatUserRec chatUser,
			@NonNull Map<String,String> params) {

		return ImmutableMap.<String,String>builder ()

			.putAll (
				params)

			.put (
				"brandName",
				chatUserLogic.getBrandName (chatUser))

			.build ();

	}

	@Override
	public
	String renderTemplate (
			@NonNull ChatUserRec chatUser,
			@NonNull String templateTypeCode,
			@NonNull String templateCode,
			@NonNull Map<String,String> suppliedParams) {

		Map<String,String> allParams =
			addDefaultParams (
				chatUser,
				suppliedParams);

		ChatHelpTemplateRec template =
			chatTemplateLogic.findChatHelpTemplate (
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
