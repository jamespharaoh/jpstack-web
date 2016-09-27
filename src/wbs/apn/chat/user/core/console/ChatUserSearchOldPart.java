package wbs.apn.chat.user.core.console;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.utils.string.StringUtils.objectToStringNullSafe;
import static wbs.utils.web.HtmlInputUtils.htmlSelect;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

import com.google.common.collect.ImmutableMap;

import wbs.console.helper.EnumConsoleHelper;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@Deprecated
@PrototypeComponent ("chatUserSearchOldPart")
public
class ChatUserSearchOldPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@Named
	EnumConsoleHelper <?> chatUserCreditModeConsoleHelper;

	@SingletonDependency
	@Named
	EnumConsoleHelper <?> chatUserDateModeConsoleHelper;

	@SingletonDependency
	@Named ("chatUserSearchItemsPerSubPage")
	Integer itemsPerSubPage;

	@SingletonDependency
	@Named ("chatUserSearchSubPagesPerPage")
	Integer subPagesPerPage;

	// implementation

	@Override
	public
	void renderHtmlHeadContent () {

		printFormat (
			"<script type=\"text/javascript\">\n",

			"var inputDefaults = {\n",
			"  type: '',\n",
			"  code: '',\n",
			"  number: '',\n",
			"  name: '',\n",
			"  info: '',\n",
			"  picture: '',\n",
			"  creditMode: '',\n",
			"  online: '',\n",
			"  output: 'normal',\n",
			"  order: 'code'\n",
			"}\n",

			"function clearForm () {\n",
			"  for (var inputId in inputDefaults)\n",
			"    document.getElementById (inputId).value =\n",
			"      inputDefaults [inputId];\n",
			"}\n",

			"</script>\n");

	}

	@Override
	public
	void renderHtmlBodyContent () {

		@SuppressWarnings ("unchecked")
		Map <String, String> params =
			ifNull (

			(Map <String, String>)
			requestContext.session (
				"chatUserSearchParams"),

			new HashMap<> ()

		);

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chatUser.search.old"),
			">\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"search\"",
			">\n");

		printFormat (
			"<input",
			" type=\"button\"",
			" value=\"clear form\"",
			" onclick=\"clearForm ();\"",
			"></p>\n");

		printFormat (
			"<table class=\"details\">\n");

		htmlTableDetailsRowWriteHtml (
			"Type",
			() -> htmlSelect (
				"type",
				searchTypeOptions,
				ifNull (
					params.get (
						"type"),
					"")));

		printFormat (
			"<tr>\n",
			"<th>User number</th>\n",

			"<td><input",
			" type=\"text\"",
			" id=\"code\"",
			" name=\"code\"",
			" size=\"16\"",
			" maxlength=\"6\"",
			" value=\"%h\"",
			ifNull (
				params.get (
					"code"),
				""),
			"></td>\n",

			"</tr>\n");

		htmlTableDetailsRowWriteHtml (
			"Gender",
			() -> htmlSelect (
				"gender",
				searchGenderOptions,
				ifNull (
					params.get (
						"gender"),
					"")));

		htmlTableDetailsRowWriteHtml (
			"Orient",
			() -> htmlSelect (
				"orient",
				searchOrientOptions,
				objectToStringNullSafe (
					ifNull (
						params.get (
							"orient"),
						""))));

		printFormat (
			"<tr>\n",
			"<th>Mobile number</th>\n",

			"<td><input",
			" type=\"text\"",
			" id=\"number\"",
			" name=\"number\"",
			" size=\"32\"",
			" value=\"%h\"",
			ifNull (
				params.get (
					"number"),
				""),
			">\n",

			"<input",
			" id=\"includeDeleted\"",
			" type=\"checkbox\"",
			" name=\"includeDeleted\"",
			params.containsKey ("includeDeleted")
				? " checked"
				: "",
			">\n",

			"<label",
			" for=\"includeDeleted\"",
			">include deleted</label></td>",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Name</th>\n",

			"<td><input",
			" type=\"text\"",
			" id=\"name\"",
			" name=\"name\"",
			" size=\"32\"",
			" value=\"%h\"",
			ifNull (
				params.get (
					"name"),
				""),
			"></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Info</th>\n",

			"<td><input",
			" type=\"text\"",
			" id=\"info\"",
			" name=\"info\"",
			" size=\"32\"",
			" value=\"%h\"",
			ifNull (
				params.get (
					"info"),
				""),
			"></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Location</th>\n",

			"<td><input",
			" type=\"text\"",
			" id=\"info\"",
			" name=\"location\"",
			" size=\"32\"",
			" value=\"%h\"",
			ifNull (
				params.get (
					"location"),
				""),
			"></td>\n",

			"</tr>\n");

		htmlTableDetailsRowWriteHtml (
			"Picture",
			() -> htmlSelect (
				"picture",
				searchTrueFalseOptions,
				ifNull (
					params.get (
						"picture"),
					"")));

		htmlTableDetailsRowWriteHtml (
			"Video",
			() -> htmlSelect (
				"video",
				searchTrueFalseOptions,
				ifNull (
					params.get (
						"video"),
					"")));

		htmlTableDetailsRowWriteHtml (
			"Adult verified",
			() -> htmlSelect (
				"adultVerified",
				searchTrueFalseOptions,
				ifNull (
					params.get (
						"adultVerified"),
					"")));

		htmlTableDetailsRowWriteHtml (
			"Credit mode",
			() -> chatUserCreditModeConsoleHelper.writeSelect (
				"creditMode",
				ifNull (
					params.get (
						"creditMode"),
					"")));

		printFormat (
			"<tr>\n",
			"<th>Credit failed</th>\n",

			"<td><input",
			" type=\"text\"",
			" id=\"creditFailedGte\"",
			" name=\"creditFailedGte\"",
			" size=\"8\"",
			" value=\"%h\"",
			emptyStringIfNull (
				params.get ("creditFailedGte")),
			">\n",

			"to\n",

			"<input",
			" type=\"text\"",
			" id=\"creditFailedLte\"",
			" name=\"creditFailedLte\"",
			" size=\"8\"",
			" value=\"%h\"",
			emptyStringIfNull (
				params.get ("creditFailedLte")),
			"></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Credit no reports</th>\n",

			"<td><input",
			" type=\"text\"",
			" id=\"creditNoReportGte\"",
			" name=\"creditNoReportGte\"",
			" size=\"8\"",
			" value=\"%h\"",
			emptyStringIfNull (
				params.get ("creditNoReportGte")),
			">\n",

			"to\n",

			"<input",
			" type=\"text\"",
			" id=\"creditNoReportLte\"",
			" name=\"creditNoReportLte\"",
			" size=\"8\"",
			" value=\"%h\"",
			emptyStringIfNull (
				params.get ("creditNoReportLte")),
			"></td>\n",

			"</tr>\n");

		printFormat (
			"<tr> <th>Total spent</th> <td>",
			"<input type=\"text\" id=\"valueSinceEverGte\" name=\"valueSinceEverGte\" size=\"8\" value=\"%h\"> to ",
			emptyStringIfNull (
				params.get ("valueSinceEverGte")),
			"<input type=\"text\" id=\"valueSinceEverLte\" name=\"valueSinceEverLte\" size=\"8\" value=\"%h\"></td> </tr>\n",
			emptyStringIfNull (
				params.get ("valueSinceEverLte")));

		printFormat (
			"<tr> <th>First join</th> <td>",
			"<input type=\"text\" id=\"firstJoinGte\" name=\"firstJoinGte\" size=\"16\" value=\"%h\"> to ",
			emptyStringIfNull (
				params.get ("firstJoinGte")),
			"<input type=\"text\" id=\"firstJoinLte\" name=\"firstJoinLte\" size=\"16\" value=\"%h\"></td> </tr>\n",
			emptyStringIfNull (
				params.get ("firstJoinLte")));

		htmlTableDetailsRowWriteHtml (
			"Dating mode",
			() -> chatUserDateModeConsoleHelper.writeSelect (
				"dateMode",
				emptyStringIfNull (
					params.get (
						"dateMode"))));

		htmlTableDetailsRowWriteHtml (
			"Online",
			() -> htmlSelect (
				"online",
				searchOnlineOptions,
				emptyStringIfNull (
					params.get (
						"online"))));

		htmlTableDetailsRowWriteHtml (
			"Output",
			() -> htmlSelect (
				"output",
				searchOutputOptions,
				emptyStringIfNull (
					params.get (
						"output"))));

		htmlTableDetailsRowWriteHtml (
			"Order",
			() -> htmlSelect (
				"order",
				searchOrderOptions,
				emptyStringIfNull (
					params.get (
						"order"))));

		printFormat (
			"<tr>\n",
			"<th>Max results</th>\n",

			"<td><input",
			" type=\"text\"",
			" id=\"limit\"",
			" name=\"limit\"",
			" size=\"8\"",
			" value=\"%h\"",
			ifNull (
				params.get ("limit"),
				Integer.toString (
					itemsPerSubPage
					* subPagesPerPage)),
			"></td>\n",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"search\"",
			">\n",

			"<input",
			" type=\"button\"",
			" value=\"clear form\"",
			" onclick=\"clearForm ();\"",
			"></p>\n");

		printFormat (
			"</form>\n");

	}

	static Map<String,String> searchTypeOptions =
		ImmutableMap.<String,String>builder ()
			.put ("", "")
			.put ("user", "User")
			.put ("monitor", "Monitor")
			.build ();

	static Map<String,String> searchGenderOptions =
		ImmutableMap.<String,String>builder ()
			.put ("", "")
			.put ("male", "Male")
			.put ("female", "Female")
			.build ();

	static Map<String,String> searchOrientOptions =
		ImmutableMap.<String,String>builder ()
			.put ("", "")
			.put ("gay", "Gay")
			.put ("bi", "Bi")
			.put ("straight", "Straight")
			.build ();

	static Map<String,String> searchTrueFalseOptions =
		ImmutableMap.<String,String>builder ()
			.put ("", "")
			.put ("false", "No")
			.put ("true", "Yes")
			.build ();

	static Map<String,String> searchOutputOptions =
		ImmutableMap.<String,String>builder ()
			.put ("normal", "Normal")
			.put ("imageZip", "ZIP file with photos")
			.build ();

	static Map<String,String> searchOnlineOptions =
		ImmutableMap.<String,String>builder ()
			.put ("", "")
			.put ("0", "Now")
			.put ("3600", "Last hour")
			.put ("26400", "Last 4 hours")
			.put ("43200", "Last 12 hours")
			.put ("86400", "Last 24 hours")
			.put ("259200", "Last 3 days")
			.put ("604800", "Last 7 days")
			.put ("2692000", "Last 30 days")
			.put ("7776000", "Last 90 days")
			.build ();

	static Map<String,String> searchOrderOptions =
		ImmutableMap.<String,String>builder ()
			.put ("code", "User number")
			.put ("creditFailedDesc", "Credit failed")
			.build ();

}
