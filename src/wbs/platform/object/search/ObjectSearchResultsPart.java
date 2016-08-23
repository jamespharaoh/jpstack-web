package wbs.platform.object.search;

import static wbs.framework.utils.etc.CollectionUtils.listSlice;
import static wbs.framework.utils.etc.Misc.getMethodRequired;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.methodInvoke;
import static wbs.framework.utils.etc.Misc.requiredValue;
import static wbs.framework.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.framework.utils.etc.OptionalUtils.isPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalIf;
import static wbs.framework.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.framework.utils.etc.OptionalUtils.presentInstances;
import static wbs.framework.utils.etc.StringUtils.joinWithSpace;
import static wbs.framework.utils.etc.StringUtils.stringEqual;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.TimeUtils.localDateNotEqual;
import static wbs.framework.utils.etc.TypeUtils.classEqual;
import static wbs.framework.utils.etc.TypeUtils.isNotInstanceOf;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.joda.time.Instant;
import org.joda.time.LocalDate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleHooks;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.module.ConsoleManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.IdObject;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;
import wbs.platform.user.console.UserConsoleLogic;

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
	UserConsoleLogic userConsoleLogic;

	// properties

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

	@Getter @Setter
	FormFieldSet formFieldSet;

	@Getter @Setter
	FormFieldSet rowsFormFieldSet;

	@Getter @Setter
	Class<?> resultsClass;

	@Getter @Setter
	String resultsDaoMethodName;

	@Getter @Setter
	String sessionKey;

	@Getter @Setter
	Long itemsPerPage;

	@Getter @Setter
	String targetContextTypeName;

	// state

	IdObject currentObject;
	List <IdObject> objects;
	Integer totalObjects;

	Boolean singlePage;
	Long pageNumber;
	Long pageCount;

	Optional <ConsoleContext> targetContext;

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

		if (
			classEqual (
				consoleHelper.objectClass (),
				resultsClass)
		) {

			Long currentObjectId =
				(Long)
				requestContext.stuff (
					consoleHelper.objectName () + "Id");

			if (currentObjectId != null) {

				currentObject =
					consoleHelper.findRequired (
						currentObjectId);

			}

		}

		// set search object

		Object searchObject =
			requiredValue (
				requestContext.session (
					sessionKey + "Fields"));

		// get search results for page

		@SuppressWarnings ("unchecked")
		List<Long> allObjectIds =
			(List<Long>)
			requiredValue (
				requestContext.session (
					sessionKey + "Results"));

		totalObjects =
			allObjectIds.size ();

		if (
			stringEqual (
				requestContext.parameterOrDefault (
					"page",
					"0"),
				"all")
		) {

			singlePage = true;

		} else {

			singlePage = false;

			pageNumber =
				parseIntegerRequired (
					requestContext.parameterOrDefault (
						"page",
						"0"));

		}

		List <Long> pageObjectIds =
			singlePage

			? allObjectIds

			: listSlice (
				allObjectIds,
				pageNumber * itemsPerPage,
				Math.min (
					(pageNumber + 1) * itemsPerPage,
					allObjectIds.size ()));

		pageCount =
			(allObjectIds.size () - 1) / itemsPerPage + 1;

		// load objects

		if (
			isNotNull (
				resultsDaoMethodName)
		) {

			Method method =
				getMethodRequired (
					consoleHelper.getClass (),
					resultsDaoMethodName,
					ImmutableList.<Class<?>>of (
						searchObject.getClass (),
						List.class));

			@SuppressWarnings ("unchecked")
			List<IdObject> objectsTemp =
				(List<IdObject>)
				methodInvoke (
					method,
					consoleHelper,
					ImmutableList.<Object>of (
						searchObject,
						pageObjectIds));

			objects =
				objectsTemp;

		} else {

			objects =
				new ArrayList<IdObject> ();

			for (
				Long objectId
					: pageObjectIds
			) {

				objects.add (
					optionalOrNull (
						consoleHelper.find (
							objectId)));

			}

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

		printFormat (
			"<a",
			" class=\"%h\"",
			singlePage
				? "selected"
				: "",
			" href=\"?page=all\"",
			">All</a>\n");

		for (
			int page = 0;
			page < pageCount;
			page ++
		) {

			printFormat (
				"<a",
				" class=\"%h\"",
				! singlePage && page == pageNumber
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
			"<table",
			" class=\"list\"",
			">\n");

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
			IdObject object
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

			if (

				object instanceof Record

				&& ! consoleHelper.canView (
					(Record<?>)
					object)

			)  {

				printFormat (
					"<tr>\n",
					"<td",
					" colspan=\"%h\"",
					formFieldSet.formFields ().size (),
					">(resricted)</td>\n",
					"</tr>\n");

				continue;

			}

			if (

				classEqual (
					consoleHelper.objectClass (),
					resultsClass)

				&& consoleHelper.event ()

			) {

				Instant rowTimestamp =
					(Instant)
					BeanLogic.getProperty (
						object,
						consoleHelper.timestampField ().name ());

				LocalDate rowDate =
					rowTimestamp.toDateTime ().toLocalDate ();

				if (
					localDateNotEqual (
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
						userConsoleLogic.dateStringLong (
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

			if (object instanceof Record) {

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
							() -> "selected"),

						getListClass (
							object)

					)),

					" data-rows-class=\"%h\"",
					stringFormat (
						"search-result-%s",
						object.getId ()),

					" data-target-href=\"%h\"",
					objectUrl (
						(Record<?>)
						object),

					">\n");

			} else {

				printFormat (
					"<tr>\n");

			}

			formFieldLogic.outputTableCellsList (
				formatWriter,
				formFieldSet,
				object,
				ImmutableMap.of (),
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
							() -> "selected"),

						getListClass (
							object))),

					" data-rows-class=\"%h\"",
					stringFormat (
						"search-result-%s",
						object.getId ()));

				if (object instanceof Record) {

					printFormat (
						" data-target-href=\"%h\"",
						objectUrl (
							(Record<?>)
							object));

				}

				printFormat (
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

	private
	String objectUrl (
			@NonNull Record<?> object) {

		if (
			isPresent (
				targetContext)
		) {

			return requestContext.resolveContextUrl (
				stringFormat (
					"%s",
					targetContext.get ().pathPrefix (),
					"/%s",
					consoleHelper.getPathId (
						object)));

		} else {

			return requestContext.resolveLocalUrl (
				consoleHelper.getDefaultLocalPath (
					object));

		}

	}

	private <ObjectType extends Record<ObjectType>>
	Optional<String> getListClass (
			@NonNull IdObject idObject) {

		if (
			isNotInstanceOf (
				Record.class,
				idObject)
		) {
			return Optional.absent ();
		}

		@SuppressWarnings ("unchecked")
		ConsoleHooks<ObjectType> consoleHooks =
			(ConsoleHooks<ObjectType>)
			consoleHelper.consoleHooks ();

		@SuppressWarnings ("unchecked")
		ObjectType object =
			(ObjectType)
			idObject;

		return consoleHooks.getListClass (
			object);

	}

}
