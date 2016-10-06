package wbs.apn.chat.affiliate.console;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wbs.apn.chat.affiliate.model.ChatAffiliateObjectHelper;
import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.scheme.model.ChatSchemeKeywordObjectHelper;
import wbs.apn.chat.scheme.model.ChatSchemeKeywordRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

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

	List<ChatSchemeKeywordRec> chatSchemeKeywords;

	@Override
	public
	void prepare () {

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
	void renderHtmlBodyContent () {

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
