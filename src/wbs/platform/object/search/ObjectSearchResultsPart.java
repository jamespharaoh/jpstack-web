package wbs.platform.object.search;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.listSlice;
import static wbs.utils.etc.Misc.getMethodRequired;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.methodInvoke;
import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalIf;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.etc.TypeUtils.classEqualSafe;
import static wbs.utils.etc.TypeUtils.isNotInstanceOf;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.localDateNotEqual;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlDataAttributeFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlStyleAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPost;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowSeparatorWrite;
import static wbs.utils.web.HtmlUtils.htmlLinkWrite;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;
import org.joda.time.LocalDate;

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
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.IdObject;
import wbs.framework.entity.record.Record;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.utils.etc.PropertyUtils;

@Accessors (fluent = true)
@PrototypeComponent ("objectSearchResultsPart")
public
class ObjectSearchResultsPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// properties

	@Getter @Setter
	ConsoleHelper <?> consoleHelper;

	@Getter @Setter
	FormFieldSet formFieldSet;

	@Getter @Setter
	FormFieldSet rowsFormFieldSet;

	@Getter @Setter
	Class <?> resultsClass;

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
			classEqualSafe (
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
			stringEqualSafe (
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

		htmlFormOpenPost ();

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"new-search\"",
			" value=\"new search\"",
			">");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"repeat-search\"",
			" value=\"repeat search\"",
			">");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"download-csv\"",
			" value=\"download csv\"",
			">");

		htmlParagraphClose ();

		htmlFormClose ();

	}

	void goTotalObjects () {

		htmlParagraphWriteFormat (
			"Search returned %h items",
			totalObjects);

	}

	void goPageNumbers () {

		if (pageCount == 1)
			return;

		htmlParagraphOpen (
			htmlClassAttribute (
				"links"));

		formatWriter.writeLineFormat (
			"Select page");

		htmlLinkWrite (
			"?page=all",
			"All",
			presentInstances (
				optionalIf (
					singlePage,
					() -> htmlClassAttribute (
						"selected"))));

		for (
			long page = 0;
			page < pageCount;
			page ++
		) {

			htmlLinkWrite (
				stringFormat (
					"?page=%h",
					page),
				integerToDecimalString (
					page + 1),
				presentInstances (
					optionalIf (
						! singlePage && page == pageNumber,
						() -> htmlClassAttribute (
							"selected"))));

		}

		htmlParagraphClose ();

	}

	void goSearchResults () {

		htmlTableOpenList ();

		htmlTableRowOpen ();

		formFieldLogic.outputTableHeadings (
			formatWriter,
			formFieldSet);

		htmlTableRowClose ();

		LocalDate currentDate =
			new LocalDate (0);

		for (
			IdObject object
				: objects
		) {

			if (object == null) {

				htmlTableRowOpen ();

				htmlTableCellWrite (
					"(deleted)",
					htmlColumnSpanAttribute (
						collectionSize (
							formFieldSet.formItems ())));

				htmlTableRowClose ();

				continue;

			}

			if (

				object instanceof Record

				&& ! consoleHelper.canView (
					(Record <?>)
					object)

			)  {

				htmlTableRowOpen ();

				htmlTableCellWrite (
					"(restricted)",
					htmlColumnSpanAttribute (
						collectionSize (
							formFieldSet.formItems ())));

				htmlTableRowClose ();

				continue;

			}

			if (

				classEqualSafe (
					consoleHelper.objectClass (),
					resultsClass)

				&& consoleHelper.event ()

			) {

				Instant rowTimestamp =
					(Instant)
					PropertyUtils.getProperty (
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

					htmlTableRowSeparatorWrite ();

					htmlTableRowOpen (
						htmlStyleAttribute (
							htmlStyleRuleEntry (
								"font-weight",
								"bold")));

					htmlTableCellWrite (
						userConsoleLogic.dateStringLong (
							rowTimestamp),
						htmlColumnSpanAttribute (
							collectionSize (
								formFieldSet.formItems ())));

					htmlTableRowClose ();

				}

			}

			if (
				isNotNull (
					rowsFormFieldSet)
			) {

				htmlTableRowSeparatorWrite ();

			}

			if (object instanceof Record) {

				htmlTableRowOpen (

					htmlClassAttribute (
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

					htmlDataAttribute (
						"rows-class",
						stringFormat (
							"search-result-%s",
							object.getId ())),

					htmlDataAttribute (
						"target-href",
						objectUrl (
							(Record<?>)
							object))

				);

			} else {

				htmlTableRowOpen ();

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

				htmlTableRowClose ();

				htmlTableRowOpen (
					presentInstances (

					optionalOf (
						htmlClassAttribute (
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

					))),

					optionalOf (
						htmlDataAttributeFormat (
							"rows-class",
							"search-result-%s",
							object.getId ())),

					optionalIf (
						object instanceof Record,
						() -> htmlDataAttribute (
							"target-href",
							objectUrl (
								(Record <?>)
								object)))

				));

				formFieldLogic.outputTableRowsList (
					formatWriter,
					rowsFormFieldSet,
					object,
					false,
					formFieldSet.columns ());

			}

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

	private
	String objectUrl (
			@NonNull Record<?> object) {

		if (
			optionalIsPresent (
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
