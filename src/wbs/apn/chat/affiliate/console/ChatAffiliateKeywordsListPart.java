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

import wbs.apn.chat.affiliate.model.ChatAffiliateObjectHelper;
import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.scheme.model.ChatSchemeKeywordObjectHelper;
import wbs.apn.chat.scheme.model.ChatSchemeKeywordRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("chatAffiliateKeywordsListPart")
public
class ChatAffiliateKeywordsListPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatAffiliateObjectHelper chatAffiliateHelper;

	@SingletonDependency
	ChatSchemeKeywordObjectHelper chatSchemeKeywordHelper;

	// state

	List <ChatSchemeKeywordRec> chatSchemeKeywords;

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		ChatAffiliateRec chatAffiliate =
			chatAffiliateHelper.findRequired (
				requestContext.stuffInteger (
					"chatAffiliateId"));

		chatSchemeKeywords =
			new ArrayList <> (
				chatAffiliate.getChatSchemeKeywords ());

		Collections.sort (
			chatSchemeKeywords);

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

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
