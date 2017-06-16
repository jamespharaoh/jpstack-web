package wbs.apn.chat.core.console;

import static wbs.utils.collection.IterableUtils.iterableFilterMapToSet;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.etc.LogicUtils.predicatesCombineAll;
import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlInputUtils.htmlSelect;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.IdObject;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserEditReason;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.info.model.ChatUserInfoStatus;

@SingletonComponent ("chatConsoleLogic")
public
class ChatConsoleLogicImplementation
	implements ChatConsoleLogic {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

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

	@Override
	public
	Set <Long> getSupervisorSearchChatIds (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Set <String>> conditions) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getSupervisorChatIds");

		) {

			List <Predicate <ChatRec>> predicates =
				new ArrayList<> ();

			// handle slice code condition

			Optional <Set <String>> sliceCodes =
				mapItemForKey (
					conditions,
					"slice-code");

			if (
				optionalIsPresent (
					sliceCodes)
			) {

				predicates.add (
					chat ->
						contains (
							optionalGetRequired (
								sliceCodes),
							chat.getSlice ().getCode ()));

			}

			// handle chat slice code condition

			Optional <Set <String>> chatSliceCodes =
				mapItemForKey (
					conditions,
					"clat-slice-code");

			if (
				optionalIsPresent (
					chatSliceCodes)
			) {

				predicates.add (
					chat ->
						contains (
							optionalGetRequired (
								chatSliceCodes),
							chat.getSlice ().getCode ()));

			}

			// handle chat code condition

			Optional <Set <String>> chatCodes =
				mapItemForKey (
					conditions,
					"chat-code");

			if (
				optionalIsPresent (
					chatCodes)
			) {

				predicates.add (
					chat ->
						contains (
							optionalGetRequired (
								chatCodes),
							joinWithFullStop (
								chat.getSlice ().getCode (),
								chat.getCode ())));

			}

			// return

			return iterableFilterMapToSet (
				chatHelper.findAll (
					transaction),
				predicatesCombineAll (
					predicates),
				IdObject::getId);

		}

	}

	@Override
	public
	Set <Long> getSupervisorFilterChatIds (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getSupervisorFilterChatIds");

		) {

			return iterableFilterMapToSet (
				chatHelper.findAll (
					transaction),
				chat ->
					privChecker.canRecursive (
						transaction,
						chat,
						"supervisor"),
				ChatRec::getId);

		}

	}

}
