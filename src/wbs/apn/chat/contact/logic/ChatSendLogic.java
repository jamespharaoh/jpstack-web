package wbs.apn.chat.contact.logic;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import wbs.apn.chat.user.core.model.ChatUserRec;
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

	MessageRec sendSystem (
			ChatUserRec chatUser,
			Optional<Integer> threadId,
			String templateCode,
			RouterRec router,
			String numFrom,
			Set<String> tags,
			Optional<String> deliveryTypeCode,
			String serviceCode,
			Map<String,String> params);

	MessageRec sendSystemRbFree (
			ChatUserRec chatUser,
			Optional<Integer> threadId,
			String templateCode,
			Map<String,String> params);

	MessageRec sendMessageMmsFree (
			ChatUserRec chatUser,
			Optional<Integer> threadId,
			String message,
			CommandRec command,
			ServiceRec service);

	MessageRec sendSystemMmsFree (
			ChatUserRec chatUser,
			Optional<Integer> threadId,
			String templateCode,
			CommandRec command);

	MessageRec sendMessageMagic (
			ChatUserRec chatUser,
			Optional<Integer> threadId,
			TextRec message,
			CommandRec magicCommand,
			ServiceRec service,
			Integer magicRef);

	MessageRec sendSystemMagic (
			ChatUserRec chatUser,
			Optional<Integer> threadId,
			String templateCode,
			CommandRec magicCommand,
			Integer magicRef,
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

}
