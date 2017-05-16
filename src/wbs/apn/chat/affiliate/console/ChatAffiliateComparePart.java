package wbs.apn.chat.affiliate.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenGetAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.time.DurationFormatter;
import wbs.utils.time.TextualInterval;

import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserSearch;

@PrototypeComponent ("chatAffiliateComparePart")
public
class ChatAffiliateComparePart
	extends AbstractPagePart {

	// dependencies

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	DurationFormatter durationFormatter;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	// state

	String timePeriodString;
	List <ChatAffiliateWithNewUserCount> chatAffiliateWithNewUserCounts;

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			// check units

			timePeriodString =
				requestContext.parameterOrDefault (
					"timePeriod",
					"7 days");

			Optional <Duration> timePeriodDurationOptional =
				durationFormatter.stringToDuration (
					timePeriodString);

			if (
				optionalIsNotPresent (
					timePeriodDurationOptional)
			) {

				requestContext.addError (
					"Invalid time period");

				return;

			}

			Duration timePeriodDuration =
				optionalGetRequired (
					timePeriodDurationOptional);

			// get objects

			ChatRec chat =
				chatHelper.findFromContextRequired (
					transaction);

			// work out first join time

			DateTimeZone timeZone =
				DateTimeZone.forID (
					chat.getTimezone ());

			Instant firstJoinAfter =
				DateTime.now (
					timeZone)

				.minus (
					timePeriodDuration)

				.toInstant ();

			// get all relevant users

			List <Long> newUserIds =
				chatUserHelper.searchIds (
					transaction,
					new ChatUserSearch ()

				.chatId (
					chat.getId ())

				.firstJoin (
					TextualInterval.after (
						DateTimeZone.UTC,
						firstJoinAfter))

			);

			// count them grouping by affiliate

			Map <Long, ChatAffiliateWithNewUserCount> map =
				new HashMap<> ();

			for (
				Long chatUserId
					: newUserIds
			) {

				ChatUserRec chatUser =
					chatUserHelper.findRequired (
						transaction,
						chatUserId);

				Long chatAffiliateId =
					chatUser.getChatAffiliate () != null
						? chatUser.getChatAffiliate ().getId ()
						: null;

				ChatAffiliateWithNewUserCount chatAffiliateWithNewUserCount =
					map.get (
						chatAffiliateId);

				if (chatAffiliateWithNewUserCount == null) {

					map.put (
						chatAffiliateId,
						chatAffiliateWithNewUserCount =
							new ChatAffiliateWithNewUserCount ()

								.chatAffiliate (
									chatUser.getChatAffiliate ())

					);

				}

				chatAffiliateWithNewUserCount.newUsers ++;

			}

			// now select and sort the ones we are allowed to see

			chatAffiliateWithNewUserCounts =
				new ArrayList<> ();

			if (
				privChecker.canRecursive (
					transaction,
					chat,
					"stats")
			) {

				chatAffiliateWithNewUserCounts.addAll (
					map.values ());

				for (
					ChatAffiliateWithNewUserCount chatAffiliateWithNewUserCount
						: map.values ()
				) {

					ChatAffiliateRec chatAffiliate =
						chatAffiliateWithNewUserCount.chatAffiliate;

					if (
						! privChecker.canRecursive (
							transaction,
							chatAffiliate,
							"stats")
					) {
						continue;
					}

					chatAffiliateWithNewUserCounts.add (
						chatAffiliateWithNewUserCount);

				}

			}

			Collections.sort (
				chatAffiliateWithNewUserCounts);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		renderForm ();
		renderHistory ();

	}

	private
	void renderForm () {

		htmlFormOpenGetAction (
			requestContext.resolveLocalUrl (
				"/chatAffiliate.compare"));

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"Time period<br>");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"text\"",
			" name=\"timePeriod\"",
			" value=\"%h\"",
			timePeriodString,
			"\">");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"ok\">");

		htmlParagraphClose ();

		htmlFormClose ();

	}

	private
	void renderHistory () {

		if (
			isNull (
				chatAffiliateWithNewUserCounts)
		) {
			return;
		}

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Scheme",
			"Affiliate<",
			"Descriptionn",
			"New users");

		for (
			ChatAffiliateWithNewUserCount chatAffiliateWithNewUserCount
				: chatAffiliateWithNewUserCounts
		) {

			ChatAffiliateRec chatAffiliate =
				chatAffiliateWithNewUserCount.chatAffiliate;

			htmlTableRowOpen ();

			htmlTableCellWrite (
				chatAffiliate != null
					? chatAffiliate.getChatScheme ().getCode ()
					: "(no affiliate)");

			htmlTableCellWrite (
				chatAffiliate != null
					? chatAffiliate.getCode ()
					: "(no affiliate)");

			htmlTableCellWrite (
				chatAffiliate != null
					? chatAffiliate.getDescription ()
					: "-");

			htmlTableCellWrite (
				integerToDecimalString (
					chatAffiliateWithNewUserCount.newUsers));

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

}
