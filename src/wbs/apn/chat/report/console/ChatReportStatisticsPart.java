package wbs.apn.chat.report.console;

import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.NumberUtils.notMoreThanZero;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.List;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.affiliate.console.ChatAffiliateUsersSummaryConsoleHelper;
import wbs.apn.chat.affiliate.model.ChatAffiliateUsersSummaryRec;
import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.console.ChatUsersSummaryConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUsersSummaryRec;

@PrototypeComponent ("chatReportStatisticsPart")
public
class ChatReportStatisticsPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatAffiliateUsersSummaryConsoleHelper chatAffiliateUsersSummaryHelper;

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	ChatUsersSummaryConsoleHelper chatUsersSummaryHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	// state

	ChatRec chat;
	ChatUsersSummaryRec users;

	long numAffiliates;

	long numUsersGayMale;
	long numUsersGayFemale;
	long numUsersBiMale;
	long numUsersBiFemale;
	long numUsersStraightMale;
	long numUsersStraightFemale;

	long numJoinedLastDay;
	long numJoinedLastWeek;
	long numJoinedLastMonth;

	long numOnlineLastDay;
	long numOnlineLastWeek;
	long numOnlineLastMonth;

	boolean canMonitor;

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

			chat =
				chatHelper.findFromContextRequired (
					transaction);

			List <ChatAffiliateUsersSummaryRec> chatAffiliateUsersSummaries =
				chatAffiliateUsersSummaryHelper.findByParent (
					transaction,
					chat);

			for (
				ChatAffiliateUsersSummaryRec chatAffiliateUsersSummary
					: chatAffiliateUsersSummaries
			) {

				if (
					! privChecker.canRecursive (
						transaction,
						chatAffiliateUsersSummary.getChatAffiliate (),
						"chat_user_view")
				) {
					continue;
				}

				numAffiliates ++;

				numUsersGayMale +=
					chatAffiliateUsersSummary.getNumUsersGayMale ();

				numUsersGayFemale +=
					chatAffiliateUsersSummary.getNumUsersGayFemale ();

				numUsersBiMale +=
					chatAffiliateUsersSummary.getNumUsersBiMale ();

				numUsersBiFemale +=
					chatAffiliateUsersSummary.getNumUsersBiFemale ();

				numUsersStraightMale +=
					chatAffiliateUsersSummary.getNumUsersStraightMale ();

				numUsersStraightFemale +=
					chatAffiliateUsersSummary.getNumUsersStraightFemale ();

				numJoinedLastDay +=
					chatAffiliateUsersSummary.getNumJoinedLastDay ();

				numJoinedLastWeek +=
					chatAffiliateUsersSummary.getNumJoinedLastWeek ();

				numJoinedLastMonth +=
					chatAffiliateUsersSummary.getNumJoinedLastMonth ();

				numOnlineLastDay +=
					chatAffiliateUsersSummary.getNumOnlineLastDay ();

				numOnlineLastWeek +=
					chatAffiliateUsersSummary.getNumOnlineLastWeek ();

				numOnlineLastMonth +=
					chatAffiliateUsersSummary.getNumOnlineLastMonth ();

			}

			users =
				chatUsersSummaryHelper.findRequired (
					transaction,
					chat.getId ());

			canMonitor =
				privChecker.canRecursive (
					transaction,
					chat,
					"monitor");

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			if (

				equalToZero (
					numAffiliates)

				&& ! canMonitor

			) {

				htmlParagraphWriteFormat (
					"You don't have permission to view any information on ",
					"this page.");

				return;

			}

			writeNumberOfAffiliates ();
			writeTotals ();
			writeJoiners ();

		}

	}

	private
	void writeNumberOfAffiliates () {

		// write number of affiliates

		if (
			moreThanZero (
				numAffiliates)
		) {

			htmlParagraphWriteFormat (
				"Number of affiliates: %h",
				integerToDecimalString (
					numAffiliates));

		}

	}

	private
	void writeTotals () {

		// heading write

		htmlHeadingTwoWrite (
			"Users");

		// table open

		htmlTableOpenList ();

		// table header row

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Orient");

		htmlTableHeaderCellWrite (
			"Gender");

		if (
			moreThanZero (
				numAffiliates)
		) {

			htmlTableHeaderCellWrite (
				"Users");

		}

		if (canMonitor) {

			htmlTableHeaderCellWrite (
				"Monitors");

		}

		htmlTableRowClose ();

		// table content

		writeRow (
			"Gay",
			"Male",
			numUsersGayMale,
			users.getNumMonitorsGayMale ());

		writeRow (
			"Gay",
			"Female",
			numUsersGayFemale,
			users.getNumMonitorsGayFemale ());

		writeRow (
			"Bi",
			"Male",
			numUsersBiMale,
			users.getNumMonitorsBiMale ());

		writeRow (
			"Bi",
			"Female",
			numUsersBiFemale,
			users.getNumMonitorsBiFemale ());

		writeRow (
			"Straight",
			"Male",
			numUsersStraightMale,
			users.getNumMonitorsStraightMale ());

		writeRow (
			"Straight",
			"Female",
			numUsersStraightFemale,
			users.getNumMonitorsStraightFemale ());

		// table close

		htmlTableClose ();

	}

	private
	void writeRow (
			@NonNull String orient,
			@NonNull String gender,
			@NonNull Long users,
			@NonNull Long monitors) {

		htmlTableRowOpen ();

		htmlTableCellWrite (
			"Gay");

		htmlTableCellWrite (
			"Male");

		if (
			moreThanZero (
				numAffiliates)
		) {

			htmlTableCellWrite (
				integerToDecimalString (
					users));

		}

		htmlTableCellWrite (
			integerToDecimalString (
				monitors));

		htmlTableRowClose ();

	}

	private
	void writeJoiners () {

		if (
			notMoreThanZero (
				numAffiliates)
		) {
			return;
		}

		// heading

		htmlHeadingTwoWrite (
			"User activity");

		// table open

		htmlTableOpenList ();

		// table header

		htmlTableHeaderRowWrite (
			"Timescale",
			"Joined",
			"Online");

		// table content

		writeJoinersRow (
			"Last day",
			numJoinedLastDay,
			numOnlineLastDay);

		writeJoinersRow (
			"Last week",
			numJoinedLastWeek,
			numOnlineLastWeek);

		writeJoinersRow (
			"Last month",
			numJoinedLastMonth,
			numOnlineLastMonth);

		// table close

		htmlTableClose ();

	}

	private
	void writeJoinersRow (
			@NonNull String name,
			@NonNull Long numJoiners,
			@NonNull Long numOnline) {

		htmlTableRowOpen ();

		htmlTableCellWrite (
			name);

		htmlTableCellWrite (
			integerToDecimalString (
				numJoiners));

		htmlTableCellWrite (
			integerToDecimalString (
				numOnline));

		htmlTableRowClose ();

	}

}
