package wbs.clients.apn.chat.affiliate.console;

import static wbs.framework.utils.etc.Misc.emptyStringIfNull;
import static wbs.framework.utils.etc.Misc.equal;

import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import wbs.clients.apn.chat.core.console.ChatKeywordJoinTypeConsoleHelper;
import wbs.clients.apn.chat.core.console.GenderConsoleHelper;
import wbs.clients.apn.chat.core.console.OrientConsoleHelper;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.PrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("chatAffiliateCreateOldPart")
public
class ChatAffiliateCreateOldPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatKeywordJoinTypeConsoleHelper chatKeywordJoinTypeConsoleHelper;

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	GenderConsoleHelper genderConsoleHelper;

	@Inject
	OrientConsoleHelper orientConsoleHelper;

	@Inject
	PrivChecker privChecker;

	// state

	Map<String,Integer> chatSchemes;

	// implementation

	@Override
	public
	void prepare () {

		ChatRec chat =
			chatHelper.find (
				requestContext.stuffInt ("chatId"));

		chatSchemes =
			new TreeMap<String,Integer> ();

		for (
			ChatSchemeRec chatScheme
				: chat.getChatSchemes ()
		) {

			if (! privChecker.canRecursive (chatScheme, "affiliate_create"))
				continue;

			chatSchemes.put (
				objectManager.objectPathMini (
					chatScheme,
					chat),
				chatScheme.getId ());

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		if (chatSchemes.size () == 0) {

			printFormat (
				"<p>There are no schemes in which you have permission to ",
				"create new affiliates.</p>\n");

			return;

		}

		printFormat (
			"<p>Please select the scheme in which to create the affiliate, ",
			"and choose a unique name to identify it.</p>\n");

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chatAffiliate.create.old"),
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",

			"<th>Scheme</th>\n",

			"<td><select name=\"chatScheme\">\n",
			"<option>\n");

		for (Map.Entry<String,Integer> schemeEntry
				: chatSchemes.entrySet ()) {

			printFormat (
				"<option",

				" value=\"%h\"",
				schemeEntry.getValue (),

				equal (
						schemeEntry.getValue ().toString (),
						requestContext.getForm ("chatScheme"))
					? " selected"
					: "",

				">%h</option>\n",
				schemeEntry.getKey ());

		}

		printFormat (
			"</select>",
			"</td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",

			"<th>Name</th>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"name\"",
			" size=\"32\"",
			" value=\"%h\"",
			emptyStringIfNull (
				requestContext.getForm ("name")),
			"></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",

			"<th>Description</th>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"description\"",
			" size=\"32\"",
			" value=\"%h\"",
			emptyStringIfNull (
				requestContext.getForm ("description")),
			"></td>\n",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<h2>Keywords</h2>\n");

		printFormat (
			"<p>You can optionally create some join keywords for this ",
			"affiliate at this point. If not, please remember to create ",
			"some later.</p>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Keyword</th>\n",
			"<th>Join type</th>\n",
			"<th>Gender</th>\n",
			"<th>Orient</th>\n",
			"</tr>\n");

		for (
			int index = 0;
			index < 3;
			index ++
		) {

			printFormat (
				"<td><input",
				" type=\"text\"",
				" name=\"keyword%h\"",
				index,
				" value=\"%h\"",
				emptyStringIfNull (
					requestContext.getForm ("keyword" + index)),
				"></td>\n");

			printFormat (
				"<td>%s</td>\n",
				chatKeywordJoinTypeConsoleHelper.select (
					"joinType" + index,
					requestContext.getForm ("joinType" + index)));

			printFormat (
				"<td>%s</td>\n",
				genderConsoleHelper.select (
					"gender" + index,
					requestContext.getForm ("gender" + index)));

			printFormat (
				"<td>%s</td>\n",
				orientConsoleHelper.select (
					"orient" + index,
					requestContext.getForm ("orient" + index)));

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"create affiliate\"",
			"></p>\n");

		printFormat (
			"</form>\n");

	}

}
