package wbs.apn.chat.core.console;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlInputUtils.htmlSelect;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.apn.chat.user.core.model.ChatUserEditReason;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.utils.string.FormatWriter;

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
	String textForChatUserInfoStatus (
			@NonNull ChatUserInfoStatus status) {

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
	void writeTdForChatUserTypeShort (
			@NonNull FormatWriter formatWriter,
			@NonNull ChatUserRec chatUser) {

		switch (chatUser.getType ()) {

		case user:

			htmlTableCellWrite (
				formatWriter,
				"U",
				htmlClassAttribute (
					"chat-user-type-user"));

			break;

		case monitor:

			htmlTableCellWrite (
				formatWriter,
				"M",
				htmlClassAttribute (
					"chat-user-type-monitor"));

			break;

		default:

			shouldNeverHappen ();

		}

	}

	@Override
	public
	void writeTdForChatUserGenderShort (
			@NonNull FormatWriter formatWriter,
			@NonNull ChatUserRec chatUser) {

		if (
			isNull (
				chatUser.getGender ())
		) {

			htmlTableCellWrite (
				formatWriter,
				"—");

		} else {

			switch (chatUser.getGender ()) {

			case male:

				htmlTableCellWrite (
					formatWriter,
					"M",
					htmlClassAttribute (
						"gender-male"));

				break;

			case female:

				htmlTableCellWrite (
					formatWriter,
					"F",
					htmlClassAttribute (
						"gender-female"));

				break;

			default:

				shouldNeverHappen ();

			}

		}

	}

	@Override
	public
	void writeTdForChatUserOrientShort (
			@NonNull FormatWriter formatWriter,
			@NonNull ChatUserRec chatUser) {

		if (
			isNull (
				chatUser.getOrient ())
		) {

			htmlTableCellWrite (
				formatWriter,
				"—");

		} else switch (chatUser.getOrient ()) {

		case gay:

			htmlTableCellWrite (
				formatWriter,
				"G",
				htmlClassAttribute (
					"orient-gay"));

			break;

		case bi:

			htmlTableCellWrite (
				formatWriter,
				"B",
				htmlClassAttribute (
					"orient-bi"));

			break;

		case straight:

			htmlTableCellWrite (
				formatWriter,
				"S",
				htmlClassAttribute (
					"orient-straight"));

			break;

		default:

			shouldNeverHappen ();

		}

	}

	@Override
	public
	void writeTdsForChatUserTypeGenderOrientShort (
			@NonNull FormatWriter formatWriter,
			@NonNull ChatUserRec chatUser) {

		writeTdForChatUserTypeShort (
			formatWriter,
			chatUser);

		writeTdForChatUserGenderShort (
			formatWriter,
			chatUser);

		writeTdForChatUserOrientShort (
			formatWriter,
			chatUser);

	}

	@Override
	public
	void writeSelectForChatUserEditReason (
			@NonNull FormatWriter formatWriter,
			@NonNull String name,
			@NonNull String value) {

		Map <String, String> options =
			new LinkedHashMap<> ();

		options.put (
			"",
			"");

		for (
			Map.Entry <ChatUserEditReason, String> ent
				: chatUserEditReasonToText.entrySet ()
		) {

			options.put (
				ent.getKey ().toString (),
				ent.getValue ().toString ());

		}

		htmlSelect (
			formatWriter,
			name,
			options,
			value);

	}

}
