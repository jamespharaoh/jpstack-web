package wbs.platform.exception.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.spacify;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.substring;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.framework.utils.etc.StringFormatter;
import wbs.platform.console.context.ConsoleContextScriptRef;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.misc.PageBuilder;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.exception.model.ExceptionLogObjectHelper;
import wbs.platform.exception.model.ExceptionLogRec;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

@PrototypeComponent ("exceptionLogAlertsPart")
public
class ExceptionLogAlertsPart
	extends AbstractPagePart {

	public final static
	int itemsPerPage = 100;

	public final static
	int maxPages = 10;

	@Inject
	ExceptionLogObjectHelper exceptionLogHelper;

	@Inject
	TimeFormatter timeFormatter;

	List<ExceptionLogRec> exceptions;

	PageBuilder pageBuilders [] =
		new PageBuilder [] {
			new PageBuilder ()
		};

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/jquery-1.7.1.js"))

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/page-builder.js"))

			.build ();

	}

	@Override
	public
	void prepare () {

		exceptions =
			exceptionLogHelper.search (
				ImmutableMap.<String,Object>builder ()

					.put (
						"alert",
						true)

					.put (
						"limit",
						itemsPerPage * maxPages)

					.put (
						"orderBy",
						"timestampDesc")

					.build ());

		goPageData ();

	}

	@Override
	public
	void goHeadStuff () {

		printFormat (
			"<style type=\"text/css\">\n",
			"table.list td.type-console { background-color: #e0ffe0; }\n",
			"table.list td.type-daemon { background-color: #ffe0e0; }\n",
			"table.list td.type-webapi { background-color: #e0e0ff; }\n",
			"table.list td.fatal-yes { background-color: #ff0000; color: white; font-weight: bold; }\n",
			"</style>\n");

	}

	@Override
	public
	void goBodyStuff () {

		goPageNumbers ();

		printFormat (
			"<div id=\"pageHolder\">Please wait...</div>\n");

		goPageNumbers ();

		goPageBuilder ();

	}

	void goPageNumbers () {

		if (pageBuilders [0].pages () <= 1)
			return;

		printFormat (
			"<p",
			" class=\"links\"",
			">\n");

		for (
			int page = 0;
			page < pageBuilders [0].pages ();
			page ++
		) {

			printFormat (
				"<a",
				" class=\"little-page-link-%s\"",
				page,
				" href=\"#\"",
				" onclick=\"pageBuilder.showLittlePage (%s);\"",
				page,
				">%s</a>\n",
				page + 1);

		}

		printFormat (
			"</p>\n");

	}

	void goPageData () {

		PageBuilder pageBuilder =
			pageBuilders [0];

		// head

		StringFormatter.printWriterFormat (
			pageBuilder.headWriter (),

			"<table class=\"list\">\n",

			"<tr>\n",
			"<th>Time</th>\n",
			"<th>Type</th>\n",
			"<th>Source</th>\n",
			"<th>User</th>\n",
			"<th>Fatal</th>\n",
			"</tr>\n");

		// body

		int index = 0;

		for (ExceptionLogRec exception
				: exceptions) {

			StringFormatter.printWriterFormat (
				pageBuilder.writer (),

				"<tr class=\"sep\">\n",

				"%s\n",
				Html.magicTr (
					requestContext.resolveContextUrl (
						stringFormat (
							"/exceptionLog",
							"/%u",
							exception.getId (),
							"/exceptionLog.details")),
					false),

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					dateToInstant (exception.getTimestamp ())),

				"<td class=\"type-%h\">%h</td>\n",
				exception.getType ().getCode (),
				exception.getType ().getCode (),

				"<td>%h</td>\n",
				spacify (exception.getSource ()),

				"<td>%h</td>\n",
				exception.getUser () != null
					? exception.getUser ().getUsername ()
					: "-",

				"<td class=\"fatal-%h\">%h</td>\n",
				exception.getFatal () ? "yes" : "no",
				exception.getFatal () ? "yes" : "no",

				"</tr>\n",

				"%s\n",
				Html.magicTr (
					requestContext.resolveContextUrl (
						stringFormat (
							"/exceptionLog",
							"/%u",
							exception.getId (),
							"/exceptionLog.details")),
					false),

				"<td colspan=\"5\">%h</td>\n",
				spacify (substring (exception.getSummary (), 0, 512)),

				"</td>\n",

				"</tr>\n");

			if (index ++ == itemsPerPage) {

				pageBuilder.endPage ();

				index = 0;

			}

		}

		pageBuilder.endPage ();

			StringFormatter.printWriterFormat (
			pageBuilder.footWriter (),

			"</table>\n");

	}

	void goPageBuilder () {

		printFormat (
			"<script type=\"text/javascript\">\n");

		printFormat (
			"var pages = [ [\n");

		pageBuilders [0].goPages (out);

		printFormat (
			"] ];\n");

		printFormat (
			"var pageBuilder =\n",
			"\tnew PageBuilder (pages);\n");

		printFormat (
			"$(function () {\n",
			"pageBuilder.init ();\n",
			"});\n");

		printFormat (
			"</script>\n");

	}

}
