package wbs.apn.chat.tv.core.console;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("chatTvSchemeListPart")
public
class ChatTvSchemeListPart
	extends AbstractPagePart {

	/*
	@Inject
	ChatTvDao chatTvDao;

	@Inject
	ConsoleManager consoleManager;

	List<ChatTvSchemeRec> tvSchemes;

	@Override
	public
	void prepare () {

		tvSchemes =
			chatTvDao.findChatTvSchemesByChatId (
				requestContext.stuffInt ("chatId"));
	}

	@Override
	public
	void goBodyStuff () {

		pf ("<table class=\"list\">\n");

		pf ("<tr>\n",
			"<th>Code</th>\n",
			"<th>Description</th>\n",
			"</tr>\n");

		for (ChatTvSchemeRec tvScheme
				: tvSchemes) {

			ChatSchemeRec scheme =
				tvScheme.getChatScheme ();

			pf ("%s\n",
				Html.magicTr (
					requestContext.contextUrl (
						sf ("%s/%s/chatTvScheme_details",
							consoleManager
								.context ("chatTvScheme")
								.pathPrefix (),
							scheme.getId ())),
					false));

			pf ("<td>%h</td>\n",
				scheme.getCode ());

			pf ("<td>%h</td>\n",
				scheme.getDescription ());

			pf ("</tr>\n");
		}

		pf ("</table>");
	}
	*/

}