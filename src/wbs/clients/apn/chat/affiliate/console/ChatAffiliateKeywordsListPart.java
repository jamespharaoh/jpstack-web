package wbs.clients.apn.chat.affiliate.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wbs.clients.apn.chat.affiliate.model.ChatAffiliateObjectHelper;
import wbs.clients.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeKeywordObjectHelper;
import wbs.clients.apn.chat.scheme.model.ChatSchemeKeywordRec;
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

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Scheme</th>\n",
			"<th>Keyword</th>\n",
			"<th>Join type</th>\n",
			"<th>Gender</th>\n",
			"<th>Orient</th>\n",
			"</tr>\n");

		for (ChatSchemeKeywordRec chatSchemeKeyword
				: chatSchemeKeywords) {

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				chatSchemeKeyword.getChatScheme ().getCode (),

				"<td>%h</td>\n",
				chatSchemeKeyword.getKeyword (),

				"<td>%h</td>\n",
				chatSchemeKeyword.getJoinType (),

				"<td>%h</td>\n",
				chatSchemeKeyword.getJoinGender (),

				"<td>%h</td>\n",
				chatSchemeKeyword.getJoinOrient (),

				"</tr>\n");
		}

		printFormat (
			"</table>\n");

	}

}
