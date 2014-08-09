package wbs.platform.exception.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;

import javax.inject.Inject;

import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.exception.model.ExceptionLogRec;

@Accessors (fluent = true)
@PrototypeComponent ("exceptionLogDetailsPart")
public
class ExceptionLogDetailsPart
	extends AbstractPagePart {

	@Inject
	ExceptionLogConsoleHelper exceptionLogHelper;

	@Inject
	TimeFormatter timeFormatter;

	ExceptionLogRec exceptionLog;

	@Override
	public
	void prepare () {

		exceptionLog =
			exceptionLogHelper.find (
				requestContext.stuffInt ("exceptionLogId"));

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Id</th>\n",

			"<td>%h</td>\n",
			exceptionLog.getId (),

			"</tr>");

		printFormat (
			"<tr>\n",
			"<th>Type</th>\n",

			"<td>%h</td>\n",
			exceptionLog.getType ().getDescription (),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Timestamp</th>\n",

			"<td>%h</td>\n",
			timeFormatter.instantToTimestampString (
				dateToInstant (exceptionLog.getTimestamp ())),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Source</th>\n",

			"<td>%h</td>\n",
			exceptionLog.getSource (),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Summary</th>\n",

			"<td>%h</td>\n",
			exceptionLog.getSummary (),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Dump</th>\n",

			"<td style=\"font: monospace\">%s</td>\n",
			Html.newlineToBr (Html.encode (exceptionLog.getDump ())),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Alert</th>\n",

			"<td>%h</td>\n",
			exceptionLog.getAlert ()
				? "yes"
				: "no",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Fatal</th>\n",

			"<td>%h</td>\n",
			exceptionLog.getFatal ()
				? "yes"
				: "no",

			"</tr>\n");

		printFormat (
			"</table>\n");

	}

}
