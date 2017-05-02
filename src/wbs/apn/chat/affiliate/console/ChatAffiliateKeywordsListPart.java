package wbs.apn.chat.affiliate.console;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.scheme.model.ChatSchemeKeywordObjectHelper;
import wbs.apn.chat.scheme.model.ChatSchemeKeywordRec;

@PrototypeComponent ("chatAffiliateKeywordsListPart")
public
class ChatAffiliateKeywordsListPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatAffiliateConsoleHelper chatAffiliateHelper;

	@SingletonDependency
	ChatSchemeKeywordObjectHelper chatSchemeKeywordHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// state

	List <ChatSchemeKeywordRec> chatSchemeKeywords;

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

			ChatAffiliateRec chatAffiliate =
				chatAffiliateHelper.findFromContextRequired (
					transaction);

			chatSchemeKeywords =
				new ArrayList<> (
					chatAffiliate.getChatSchemeKeywords ());

			Collections.sort (
				chatSchemeKeywords);

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

			// table open

			htmlTableOpenList ();

			htmlTableHeaderRowWrite (
				"Scheme",
				"Keyword",
				"Join type",
				"Gender",
				"Orient");

			// table rows

			for (
				ChatSchemeKeywordRec chatSchemeKeyword
					: chatSchemeKeywords
			) {

				htmlTableRowOpen ();

				htmlTableCellWrite (
					chatSchemeKeyword.getChatScheme ().getCode ());

				htmlTableCellWrite (
					chatSchemeKeyword.getKeyword ());

				htmlTableCellWrite (
					ifNotNullThenElseEmDash (
						chatSchemeKeyword.getJoinType (),
						() -> chatSchemeKeyword.getJoinType ().name ()));

				htmlTableCellWrite (
					ifNotNullThenElseEmDash (
						chatSchemeKeyword.getJoinGender (),
						() -> chatSchemeKeyword.getJoinGender ().name ()));

				htmlTableCellWrite (
					ifNotNullThenElseEmDash (
						chatSchemeKeyword.getJoinOrient (),
						() -> chatSchemeKeyword.getJoinOrient ().name ()));

				htmlTableRowClose ();

			}

			// table close

			htmlTableClose ();

		}

	}

}
