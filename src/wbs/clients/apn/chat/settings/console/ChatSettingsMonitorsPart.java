package wbs.clients.apn.chat.settings.console;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;

import com.google.common.collect.ImmutableMap;

import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserDao;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;

@Log4j
@PrototypeComponent ("chatSettingsMonitorsPart")
public
class ChatSettingsMonitorsPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserDao chatUserDao;

	// state

	private
	int
		gayMale,
		gayFemale,
		biMale,
		biFemale,
		straightMale,
		straightFemale;

	// implementation

	@Override
	public
	void prepare () {

		ChatRec chat =
			chatHelper.findRequired (
				requestContext.stuffInt (
					"chatId"));

		List<Integer> onlineMonitorIds =
			chatUserHelper.searchIds (
				ImmutableMap.<String,Object>builder ()
					.put ("chatId", chat.getId ())
					.put ("type", ChatUserType.monitor)
					.put ("online", true)
					.build ());

		log.debug ("Got " + onlineMonitorIds.size ());

		for (
			Integer monitorId
				: onlineMonitorIds
		) {

			ChatUserRec monitor =
				chatUserHelper.findRequired (
					monitorId);

			log.debug (
				stringFormat (
					"Orient %s, gender %s",
					monitor.getOrient (),
					monitor.getGender ()));

			switch (monitor.getOrient ()) {

			case gay:

				switch (monitor.getGender ()) {

				case male:
					gayMale ++;
					continue;

				case female:
					gayFemale++;
					continue;

				}

				throw new RuntimeException ();

			case bi:

				switch (monitor.getGender ()) {

				case male:
					biMale ++;
					continue;

				case female:
					biFemale ++;
					continue;

				}

				throw new RuntimeException ();

			case straight:

				switch (monitor.getGender ()) {

				case male:
					straightMale ++;
					continue;

				case female:
					straightFemale ++;
					continue;

				}

				throw new RuntimeException ();

			}

			throw new RuntimeException ();

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chat.settings.monitors"),
			">\n");

		printFormat (
			"<table",
			" class=\"list\"",
			">\n");

		printFormat (
			"<tr>\n",
			"<th>Orient</th>\n",
			"<th>Male</th>\n",
			"<th>Female</th>\n",
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<td>Gay</td>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"gayMale\"",
			" size=\"6\"",
			" value=\"%h\"",
			ifNull (
				requestContext.getForm ("gayMale"),
				gayMale),
			"></td>",

			"<td><input",
			" type=\"text\"",
			" name=\"gayFemale\"",
			" size=\"6\"",
			" value=\"%h\"",
			ifNull (
				requestContext.getForm ("gayFemale"),
				gayFemale),
			"></td>",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<td>Bi</td>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"biMale\"",
			" size=\"6\"",
			" value=\"%h\"",
			ifNull (
				requestContext.getForm ("biMale"),
				biMale),
			"></td>",

			"<td><input",
			" type=\"text\"",
			" name=\"biFemale\"",
			" size=\"6\"",
			" value=\"%h\"",
			ifNull (
				requestContext.getForm ("biFemale"),
				biFemale),
			"></td>",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<td>Straight</td>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"straightMale\"",
			" size=\"6\"",
			" value=\"%h\"",
			ifNull (
				requestContext.getForm ("straightMale"),
				straightMale),
			"></td>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"straightFemale\"",
			" size=\"6\"",
			" value=\"%h\"",
			ifNull (
				requestContext.getForm ("straightFemale"),
				straightFemale),
			"></td>",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"save changes\"",
			"></p>\n");

		printFormat (
			"</form>\n");

	}

}
