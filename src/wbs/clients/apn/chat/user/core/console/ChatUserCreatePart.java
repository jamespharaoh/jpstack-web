package wbs.clients.apn.chat.user.core.console;

import static wbs.framework.utils.etc.Misc.ifNull;

import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("chatUserCreatePart")
public
class ChatUserCreatePart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chatUser.create"),
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",

			"<th>Name</th>\n",
			"<td><input",
			" type=\"text\"",
			" name=\"name\"",
			" size=\"48\"",
			" maxlength=\"16\"",
			" value=\"%h\"",
			ifNull (requestContext.getForm ("name"), ""),
			"></td>\n",

			"</tr>");

		printFormat (
			"<tr>\n",
			"<th>Gender</th>\n",

			"<td><select name=\"gender\">\n",
			"<option>\n",

			"<option value=\"m\"",
			ifNull (requestContext.getForm ("gender"), "").equals ("m")
				? " selected"
				: "",
			">male</option>\n",

			"<option value=\"f\"",
			ifNull (requestContext.getForm ("gender"), "").equals ("f")
				? " selected"
				: "",
			">female</opton>\n",

			"</select></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Orient</th>\n",

			"<td><select name=\"orient\">\n",
			"<option>\n",

			"<option value=\"g\"",
			ifNull (requestContext.getForm ("orient"), "").equals ("g")
				? " selected"
				: "",
			">gay</opton>\n",

			"<option value=\"s\"",
			ifNull (requestContext.getForm ("orient"), "").equals ("s")
				? " selected"
				: "",
			">straight</opton>\n",

			"<option value=\"b\"",
			ifNull (requestContext.getForm ("orient"), "").equals ("b")
				? " selected"
				: "",
			">bi</opton>\n",

			"</select></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Postcode</th>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"postcode\"",
			" size=\"48\"",
			" maxlength=\"4\"",
			" value=\"%h\"",
			ifNull (requestContext.getForm ("postcode"), ""),
			"\"></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",

			"<th>Info</th>\n",

			"<td><textarea",
			" name=\"info\"",
			" cols=\"48\"",
			" rows=\"3\"",
			" maxlength=\"160\">%h</textarea></td>\n",
			ifNull (requestContext.getForm ("info"), ""),

			"</tr>\n");

		printFormat (
			"<tr>\n",

			"<th>DOB</th>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"dob\"",
			" size=\"12\"",
			" value=\"%h\"></td>\n",
			ifNull (
				requestContext.getForm ("dob"),
				""),

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"create monitor\"",
			"></p>");

		printFormat (
			"</form>\n");

	}

}
