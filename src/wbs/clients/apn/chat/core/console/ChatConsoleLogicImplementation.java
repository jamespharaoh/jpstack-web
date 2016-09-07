package wbs.clients.apn.chat.core.console;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import wbs.clients.apn.chat.user.core.model.ChatUserEditReason;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.utils.etc.Html;

@SingletonComponent ("chatConsoleLogic")
public
class ChatConsoleLogicImplementation
	implements ChatConsoleLogic {

	// implementation

	@Override
	public
	String textForChatUser (
			ChatUserRec chatUser) {

		if (chatUser == null)
			return "-";

		String code =
			chatUser.getCode ();

		String name =
			chatUser.getName ();

		if (name == null)
			return code;

		return code + " " + name;

	}

	@Override
	public
	String tdForChatUserTypeShort (
			ChatUserRec chatUser) {

		switch (chatUser.getType ()) {

		case user:
			return "<td class=\"chat-user-type-user\">U</td>";

		case monitor:
			return "<td class=\"chat-user-type-monitor\">M</td>";

		}

		throw new IllegalArgumentException ();

	}

	@Override
	public
	String tdForChatUserGenderShort (
			ChatUserRec chatUser) {

		if (chatUser.getGender () == null)
			return "<td>-</td>";

		switch (chatUser.getGender ()) {

		case male:
			return "<td class=\"gender-male\">M</td>";

		case female:
			return "<td class=\"gender-female\">F</td>";

		}

		throw new IllegalArgumentException ();

	}

	@Override
	public
	String tdForChatUserOrientShort (
			ChatUserRec chatUser) {

		if (chatUser.getOrient () == null)
			return "<td>-</td>";

		switch (chatUser.getOrient ()) {

		case gay:
			return "<td class=\"orient-gay\">G</td>";

		case bi:
			return "<td class=\"orient-bi\">B</td>";

		case straight:
			return "<td class=\"orient-straight\">S</td>";

		}

		throw new IllegalArgumentException ();

	}

	@Override
	public
	String tdsForChatUserTypeGenderOrientShort (
			ChatUserRec chatUser) {

		return
			tdForChatUserTypeShort (chatUser) +
			tdForChatUserGenderShort (chatUser) +
			tdForChatUserOrientShort (chatUser);

	}

	@Override
	public
	String textForChatUserInfoStatus (
			ChatUserInfoStatus status) {

		if (status == null)
			return "-";

		switch (status) {

		case set:
			return "user set";

		case autoEdited:
			return "user set, auto edited";

		case moderatorPending:
			return "user set, pending approval";

		case moderatorApproved:
			return "user set, approved";

		case moderatorRejected:
			return "user set, rejected";

		case moderatorAutoEdited:
			return "user set, auto edited, approved";

		case moderatorEdited:
			return "user set, edited";

		case console:
			return "console set";

		}

		throw new IllegalArgumentException ();

	}

	public static
	Map<ChatUserEditReason,String> chatUserEditReasonToText =
		new ImmutableMap.Builder<ChatUserEditReason,String> ()

		.put (
			ChatUserEditReason.userRequest,
			"user requested")

		.put (
			ChatUserEditReason.inappropriateContent,
			"inappropriate content")

		.put (
			ChatUserEditReason.other,
			"other")

		.build ();

	@Override
	public
	String textForChatUserEditReason (
			ChatUserEditReason reason) {

		if (reason == null)
			return "-";

		String ret =
			chatUserEditReasonToText.get (reason);

		if (ret == null)
			throw new IllegalArgumentException ();

		return ret;

	}

	@Override
	public
	String selectForChatUserEditReason (
			String name,
			String value) {

		Map<String,String> options =
			new LinkedHashMap<String,String> ();

		options.put (
			"",
			"");

		for (Map.Entry<ChatUserEditReason,String> ent
				: chatUserEditReasonToText.entrySet ()) {

			options.put (
				ent.getKey ().toString (),
				ent.getValue ().toString ());

		}

		return Html.select (
			name,
			options,
			value);

	}

}
