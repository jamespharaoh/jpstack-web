package wbs.clients.apn.chat.contact.logic;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextRec;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.route.router.model.RouterRec;

import com.google.common.base.Optional;

public
interface ChatSendLogic {

	MessageRec sendMessageRbFree (
			ChatUserRec chatUser,
			Optional<Integer> threadId,
			ServiceRec service,
			String message);

	Optional<MessageRec> sendSystem (
			ChatUserRec chatUser,
			Optional<Integer> threadId,
			String templateCode,
			RouterRec router,
			String numFrom,
			Set<String> tags,
			Optional<String> deliveryTypeCode,
			String serviceCode,
			Boolean required,
			Map<String,String> params);

	Optional<MessageRec> sendSystemRbFree (
			ChatUserRec chatUser,
			Optional<Integer> threadId,
			String templateCode,
			Boolean required,
			Map<String,String> params);

	MessageRec sendMessageMmsFree (
			ChatUserRec chatUser,
			Optional<Integer> threadId,
			String message,
			CommandRec command,
			ServiceRec service);

	Optional<MessageRec> sendSystemMmsFree (
			ChatUserRec chatUser,
			Optional<Integer> threadId,
			String templateCode,
			CommandRec command,
			Boolean required);

	MessageRec sendMessageMagic (
			ChatUserRec chatUser,
			Optional<Integer> threadId,
			TextRec message,
			CommandRec magicCommand,
			ServiceRec service,
			Integer magicRef);

	Optional<MessageRec> sendSystemMagic (
			ChatUserRec chatUser,
			Optional<Integer> threadId,
			String templateCode,
			CommandRec magicCommand,
			Integer magicRef,
			Boolean required,
			Map<String,String> params);

	Integer sendMessageMagic (
			ChatUserRec chatUser,
			Optional<Integer> threadId,
			Collection<TextRec> parts,
			CommandRec magicCommand,
			ServiceRec service,
			Integer magicRef);

	Map<String,String> addDefaultParams (
			ChatUserRec chatUser,
			Map<String,String> params);

	String renderTemplate (
			ChatUserRec chatUser,
			String templateTypeCode,
			String templateCode,
			Map<String,String> suppliedParams);

}
