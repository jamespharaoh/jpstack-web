package wbs.platform.object.search;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.joinWithSpace;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.optionalIf;
import static wbs.framework.utils.etc.Misc.presentInstances;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;
import org.joda.time.LocalDate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.html.JqueryScriptRef;
import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.TimeFormatter;
import wbs.console.module.ConsoleManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;

@Accessors (fluent = true)
@PrototypeComponent ("objectSearchResultsPart")
public
class ObjectSearchResultsPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ConsoleManager consoleManager;

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	TimeFormatter timeFormatter;

	// properties

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

	@Getter @Setter
	FormFieldSet formFieldSet;

	@Getter @Setter
	FormFieldSet rowsFormFieldSet;

	@Getter @Setter
	String sessionKey;

	@Getter @Setter
	Integer itemsPerPage;

	@Getter @Setter
	String targetContextTypeName;

	// state

	Record<?> currentObject;
	List<Record<?>> objects;
	Integer totalObjects;

	Integer pageNumber;
	Integer pageCount;

	ConsoleContext targetContext;

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				MagicTableScriptRef.instance)

			.build ();

	}

	// implementation

	@Override
	public
	void prepare () {

		// current object

		Integer currentObjectId =
			(Integer)
			requestContext.stuff (
				consoleHelper.objectName () + "Id");

		if (currentObjectId != null) {

			currentObject =
				consoleHelper.find (
					currentObjectId);

		}

		// get search results for page

		@SuppressWarnings ("unchecked")
		List<Integer> allObjectIds =
			(List<Integer>)
			requestContext.session (
				sessionKey + "Results");

		totalObjects =
			allObjectIds.size ();

		pageNumber =
			Integer.parseInt (
				requestContext.parameter (
					"page",
					"0"));

		List<Integer> pageObjectIds =
			allObjectIds.subList (
				pageNumber * itemsPerPage,
				Math.min (
					(pageNumber + 1) * itemsPerPage,
					allObjectIds.size ()));

		pageCount =
			(allObjectIds.size () - 1) / itemsPerPage + 1;

		// load objects

		objects =
			new ArrayList<Record<?>> ();

		for (
			Integer objectId
				: pageObjectIds
		) {

			objects.add (
				consoleHelper.find (
					objectId));

		}

		// other stuff

		prepareTargetContext ();

	}

	void prepareTargetContext () {

		ConsoleContextType targetContextType =
			consoleManager.contextType (
				targetContextTypeName,
				true);

		targetContext =
			consoleManager.relatedContext (
				requestContext.consoleContext (),
				targetContextType);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		goNewSearch ();
		goTotalObjects ();
		goPageNumbers ();
		goSearchResults ();
		goPageNumbers ();

	}

	void goNewSearch () {

		printFormat (
			"<form method=\"post\">\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" name=\"new-search\"",
			" value=\"new search\"",
			">\n");

		printFormat (
			"<input",
			" type=\"submit\"",
			" name=\"repeat-search\"",
			" value=\"repeat search\"",
			">\n");

		printFormat (
			"<input",
			" type=\"submit\"",
			" name=\"download-csv\"",
			" value=\"download csv\"",
			"></p>\n");

		printFormat (
			"</form>");

	}

	void goTotalObjects () {

		printFormat (
			"<p>Search returned %h items</p>\n",
			totalObjects);

	}

	void goPageNumbers () {

		if (pageCount == 1)
			return;

		printFormat (
			"<p",
			" class=\"links\"",
			">Select page\n");

		for (
			int page = 0;
			page < pageCount;
			page ++
		) {

			printFormat (
				"<a",
				" class=\"%h\"",
				page == pageNumber
					? "selected"
					: "",
				" href=\"%h\"",
				stringFormat (
					"?page=%h",
					page),
				">%s</a>\n",
				page + 1);

		}

		printFormat (
			"</p>\n");

	}

	void goSearchResults () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n");

		formFieldLogic.outputTableHeadings (
			formatWriter,
			formFieldSet);

		printFormat (
			"</tr>\n");

		LocalDate currentDate =
			new LocalDate (0);

		for (
			Record<?> object
				: objects
		) {

			if (object == null) {

				printFormat (
					"<tr>\n",
					"<td",
					" colspan=\"%h\"",
					formFieldSet.formFields ().size (),
					">(deleted)</td>\n",
					"</tr>\n");

				continue;

			}

			if (! consoleHelper.canView (object)) {

				printFormat (
					"<tr>\n",
					"<td",
					" colspan=\"%h\"",
					formFieldSet.formFields ().size (),
					">(resricted)</td>\n",
					"</tr>\n");

				continue;

			}

			if (consoleHelper.event ()) {

				Instant rowTimestamp;

				if (
					equal (
						consoleHelper.timestampField ().valueType (),
						Date.class)
				) {

					rowTimestamp =
						dateToInstant (
							(Date)
							BeanLogic.getProperty (
								object,
								consoleHelper.timestampField ().name ()));

				} else if (
					equal (
						consoleHelper.timestampField ().valueType (),
						Instant.class)
				) {

					rowTimestamp =
						(Instant)
						BeanLogic.getProperty (
							object,
							consoleHelper.timestampField ().name ());

				} else {

					throw new RuntimeException ();

				}

				LocalDate rowDate =
					rowTimestamp.toDateTime ().toLocalDate ();

				if (
					notEqual (
						currentDate,
						rowDate)
				) {

					currentDate =
						rowDate;

					printFormat (
						"<tr class=\"sep\">\n");

					printFormat (
						"<tr style=\"font-weight: bold\">\n");

					printFormat (
						"<td colspan=\"%h\">%h</td>\n",
						formFieldSet.formFields ().size (),
						timeFormatter.instantToDateStringLong (
							timeFormatter.defaultTimezone (),
							rowTimestamp));

					printFormat (
						"</tr>\n");

				}

			}

			if (
				isNotNull (
					rowsFormFieldSet)
			) {

				printFormat (
					"<tr class=\"sep\">\n");

			}

			printFormat (
				"<tr",

				" class=\"%h\"",
				joinWithSpace (
					presentInstances (
						Optional.of (
							"magic-table-row"),
						Optional.of (
							stringFormat (
								"search-result-%s",
								object.getId ())),
						optionalIf (
							object == currentObject,
							"selected"),
						consoleHelper.getListClass (
							object))),

				" data-rows-class=\"%h\"",
				stringFormat (
					"search-result-%s",
					object.getId ()),

				" data-target-href=\"%h\"",
				requestContext.resolveContextUrl (
					stringFormat (
						"%s",
						targetContext.pathPrefix (),
						"/%s",
						consoleHelper.getPathId (
							object))),

				">\n");

			formFieldLogic.outputTableCellsList (
				formatWriter,
				formFieldSet,
				object,
				false);

			if (
				isNotNull (
					rowsFormFieldSet)
			) {

				printFormat (
					"</tr>\n");

				printFormat (
					"<tr",

					" class=\"%h\"",
					joinWithSpace (
						presentInstances (
							Optional.of (
								"magic-table-row"),
							Optional.of (
								stringFormat (
									"search-result-%s",
									object.getId ())),
							optionalIf (
								object == currentObject,
								"selected"),
							consoleHelper.getListClass (
								object))),

					" data-rows-class=\"%h\"",
					stringFormat (
						"search-result-%s",
						object.getId ()),

					" data-target-href=\"%h\"",
					requestContext.resolveContextUrl (
						stringFormat (
							"%s",
							targetContext.pathPrefix (),
							"/%s",
							consoleHelper.getPathId (
								object))),

					">\n");

				formFieldLogic.outputTableRowsList (
					formatWriter,
					rowsFormFieldSet,
					object,
					false,
					formFieldSet.columns ());

			}

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
