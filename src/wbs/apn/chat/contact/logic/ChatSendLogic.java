package wbs.apn.chat.contact.logic;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserRec;

import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.route.router.model.RouterRec;

import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatSendLogic {

	MessageRec sendMessageRbFree (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Optional <Long> threadId,
			ServiceRec service,
			String message);

	Optional <MessageRec> sendSystem (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Optional <Long> threadId,
			String templateCode,
			RouterRec router,
			String numFrom,
			Set <String> tags,
			Optional <String> deliveryTypeCode,
			String serviceCode,
			TemplateMissing templateMissing,
			Map <String, String> params);

	Optional <MessageRec> sendSystemRbFree (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Optional <Long> threadId,
			String templateCode,
			TemplateMissing templateMissing,
			Map<String,String> params);

	MessageRec sendMessageMmsFree (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Optional<Long> threadId,
			String message,
			CommandRec command,
			ServiceRec service);

	Optional <MessageRec> sendSystemMmsFree (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Optional <Long> threadId,
			String templateCode,
			CommandRec command,
			TemplateMissing templateMissing);

	MessageRec sendMessageMagic (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Optional <Long> threadId,
			TextRec message,
			CommandRec magicCommand,
			ServiceRec service,
			Long magicRef);

	Long sendMessageMagic (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Optional <Long> threadId,
			Collection <TextRec> parts,
			CommandRec magicCommand,
			ServiceRec service,
			Long magicRef,
			Optional <UserRec> user);

	Optional <MessageRec> sendSystemMagic (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Optional <Long> threadId,
			String templateCode,
			CommandRec magicCommand,
			Long magicRef,
			TemplateMissing templateMissing,
			Map <String, String> params);

	Map <String, String> addDefaultParams (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Map <String, String> params);

	String renderTemplate (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			String templateTypeCode,
			String templateCode,
			Map <String, String> suppliedParams);

	public static
	enum TemplateMissing {
		error,
		ignore;
	}

}
