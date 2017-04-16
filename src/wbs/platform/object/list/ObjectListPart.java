package wbs.platform.object.list;

import static wbs.utils.collection.CollectionUtils.collectionStream;
import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIf;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenGetAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlUtils.htmlLinkWrite;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Interval;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ObsoleteDateField;
import wbs.console.html.ObsoleteDateLinks;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.module.ConsoleManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.object.criteria.CriteriaSpec;

@Accessors (fluent = true)
@PrototypeComponent ("objectListPart")
public
class ObjectListPart <
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
>
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	// properties

	@Getter @Setter
	ConsoleHelper <ObjectType> consoleHelper;

	@Getter @Setter
	String typeCode;

	@Getter @Setter
	String localName;

	@Getter @Setter
	Map <String, ObjectListBrowserSpec> listBrowserSpecs;

	@Getter @Setter
	Map <String, ObjectListTabSpec> listTabSpecs;

	@Getter @Setter
	FieldsProvider <ObjectType, ParentType> formFieldsProvider;

	@Getter @Setter
	String targetContextTypeName;

	// state

	FormFieldSet <ObjectType> fields;

	ObsoleteDateField dateField;

	Optional <ObjectListBrowserSpec> currentListBrowserSpec;

	ObjectListTabSpec currentListTabSpec;
	ObjectType currentObject;

	List <ObjectType> allObjects;
	List <ObjectType> selectedObjects;

	ConsoleContext targetContext;
	ParentType parent;

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

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
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"prepare");

		prepareBrowserSpec ();
		prepareTabSpec ();
		prepareCurrentObject ();
		prepareAllObjects ();

		prepareSelectedObjects (
			taskLogger);

		prepareTargetContext (
			taskLogger);

		prepareFieldSet (
			taskLogger);

	}

	void prepareFieldSet (
			@NonNull TaskLogger parentTaskLogger) {

		fields =
			parent != null
				? formFieldsProvider.getFieldsForParent (
					parentTaskLogger,
					parent)
				: formFieldsProvider.getStaticFields ();

	}

	void prepareBrowserSpec () {

		if (listBrowserSpecs ().isEmpty ()) {

			currentListBrowserSpec =
				Optional.absent ();

		} else if (listBrowserSpecs ().size () > 1) {

			throw new RuntimeException ("TODO");

		} else {

			currentListBrowserSpec =
				Optional.of (
					listBrowserSpecs.values ().iterator ().next ());

			dateField =
				ObsoleteDateField.parse (
					requestContext.parameterOrNull (
						"date"));

			if (dateField.date == null) {

				requestContext.addError (
					"Invalid date");

				return;

			}

		}

	}

	void prepareTabSpec () {

		String currentTabName =
			requestContext.parameterOrDefault (
				"tab",
				listTabSpecs.values ().iterator ().next ().name ());

		currentListTabSpec =
			listTabSpecs.get (
				currentTabName);

	}

	void prepareCurrentObject () {

		Long objectId =
			(Long)
			requestContext.stuff (
				consoleHelper.objectName () + "Id");

		if (
			isNotNull (
				objectId)
		) {

			currentObject =
				consoleHelper.findRequired (
					objectId);

		}

	}

	void prepareAllObjects () {

		// locate via parent

		if (consoleHelper.parentTypeIsFixed ()) {

			ConsoleHelper <ParentType> parentHelper =
				genericCastUnchecked (
					objectManager.findConsoleHelperRequired (
						consoleHelper.parentClass ()));

			Long parentId =
				(Long)
				requestContext.stuff (
					parentHelper.idKey ());

			if (
				isNotNull (
					parentId)
			) {

				parent =
					parentHelper.findRequired (
						parentId);

				prepareAllObjectsViaParent (
					parentHelper,
					parentId);

				return;

			}

			// locate via grand parent

			if (
				! parentHelper.isRoot ()
				&& parentHelper.parentTypeIsFixed ()
			) {

				ConsoleHelper<?> grandParentHelper =
					objectManager.findConsoleHelperRequired (
						parentHelper.parentClass ());

				Optional <Long> grandParentIdOptional =
					requestContext.stuffInteger (
						grandParentHelper.idKey ());

				if (
					optionalIsPresent (
						grandParentIdOptional)
				) {

					prepareAllObjectsViaGrandParent (
						parentHelper,
						grandParentHelper,
						optionalGetRequired (
							grandParentIdOptional));

					return;

				}

			}

		}

		// return all

		if (typeCode != null) {

			throw new RuntimeException ();

		} else if (currentListBrowserSpec.isPresent ()) {

			throw new RuntimeException ("TODO");

		} else {

			allObjects =
				consoleHelper.findAll ();

		}

	}

	void prepareAllObjectsViaParent (
			@NonNull ConsoleHelper<?> parentHelper,
			@NonNull Long parentId) {

		if (currentListBrowserSpec.isPresent ()) {

			throw new RuntimeException ("TODO");

		} else {

			GlobalId parentGlobalId =
				new GlobalId (
					parentHelper.objectTypeId (),
					parentId);

			if (typeCode != null) {

				allObjects =
					consoleHelper.findByParentAndType (
						parentGlobalId,
						typeCode);

			} else {

				allObjects =
					consoleHelper.findByParent (
						parentGlobalId);

			}

		}

	}

	void prepareAllObjectsViaGrandParent (
			ConsoleHelper <?> parentHelper,
			ConsoleHelper <?> grandParentHelper,
			Long grandParentId) {

		if (currentListBrowserSpec.isPresent ()) {

			ObjectListBrowserSpec browserSpec =
				currentListBrowserSpec.get ();

			Record <?> grandParentObject =
				grandParentHelper.findOrThrow (
					grandParentId,
					() -> new NullPointerException (
						stringFormat (
							"Can't find grand parent object %s with id %s",
							grandParentHelper.objectName (),
							integerToDecimalString (
								grandParentId))));

			String daoMethodName =
				stringFormat (
					"findBy",
					capitalise (
						browserSpec.fieldName ()));

			Method daoMethod;

			try {

				daoMethod =
					consoleHelper.getClass ().getMethod (
						daoMethodName,
						grandParentHelper.objectClass (),
						Interval.class);

			} catch (NoSuchMethodException exception) {

				throw new RuntimeException (
					stringFormat (
						"DAO method not found: %s.%s (%s, Interval)",
						stringFormat (
							"%sHelper",
							consoleHelper.objectName ()),
						daoMethodName,
						grandParentHelper.objectClass ().getSimpleName ()));

			}

			try {

				List <ObjectType> allObjectsTemp =
					genericCastUnchecked (
						daoMethod.invoke (
							consoleHelper,
							grandParentObject,
							dateField.date.toInterval ()));

				allObjects =
					allObjectsTemp;

			} catch (InvocationTargetException exception) {

				Throwable targetException =
					exception.getTargetException ();

				if (targetException instanceof RuntimeException) {

					throw (RuntimeException) targetException;

				} else {

					throw new RuntimeException (
						targetException);

				}

			} catch (IllegalAccessException exception) {

				throw new RuntimeException (
					exception);

			}

		} else {

			GlobalId grandParentGlobalId =
				new GlobalId (
					grandParentHelper.objectTypeId (),
					grandParentId);

			List <? extends Record <?>> parentObjects =
				parentHelper.findByParent (
					grandParentGlobalId);

			allObjects =
				parentObjects.stream ()

				.flatMap (
					parentObject -> {

					GlobalId parentGlobalId =
						new GlobalId (
							parentHelper.objectTypeId (),
							parentObject.getId ());

					if (typeCode != null) {

						return collectionStream (
							consoleHelper.findByParentAndType (
								parentGlobalId,
								typeCode));

					} else {

						return collectionStream (
							consoleHelper.findByParent (
								parentGlobalId));

					}

				})

				.collect (
					Collectors.toList ());

		}

	}

	private
	void prepareSelectedObjects (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"prepareSelectedObjects");

		// select which objects we want to display

		selectedObjects =
			new ArrayList <ObjectType> ();

	OUTER:

		for (
			ObjectType object
				: allObjects
		) {

			if (
				! consoleHelper.canView (
					taskLogger,
					object)
			) {

				continue;

			}

			for (
				CriteriaSpec criteriaSpec
					: currentListTabSpec.criterias ()
			) {

				if (
					! criteriaSpec.evaluate (
						taskLogger,
						consoleHelper,
						object)
				) {

					continue OUTER;

				}

			}

			selectedObjects.add (
				object);

		}

		// TODO i hate generics

		@SuppressWarnings ("unchecked")
		List <Comparable <Comparable <?>>> temp =
			(List <Comparable <Comparable <?>>>)
			(List <?>)
			selectedObjects;

		Collections.sort (
			temp);

	}

	void prepareTargetContext (
			@NonNull TaskLogger taskLogger) {

		ConsoleContextType targetContextType =
			consoleManager.contextType (
				targetContextTypeName,
				true);

		targetContext =
			consoleManager.relatedContextRequired (
				taskLogger,
				requestContext.consoleContextRequired (),
				targetContextType);

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

		goBrowser ();
		goTabs ();

		goList (
			taskLogger);

	}

	void goBrowser () {

		if (! currentListBrowserSpec.isPresent ()) {
			return;
		}

		ObjectListBrowserSpec browserSpec =
			currentListBrowserSpec.get ();

		String localUrl =
			requestContext.resolveLocalUrl (
				"/" + localName);

		htmlFormOpenGetAction (
			localUrl);

		htmlParagraphOpen (
			htmlClassAttribute (
				"links"));

		formatWriter.writeLineFormat (
			"%h",
			ifNull (
				browserSpec.label (),
				capitalise (
					camelToSpaces (
						browserSpec.fieldName ()))));

		formatWriter.writeLineFormat (
			"<input",
			" type=\"text\"",
			" name=\"date\"",
			" value=\"%h\"",
			dateField.text,
			">");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"ok\"",
			">");

		ObsoleteDateLinks.dailyBrowserLinks (
			formatWriter,
			localUrl,
			requestContext.formData (),
			dateField.date);

		htmlParagraphClose ();

		htmlFormClose ();

	}

	void goTabs () {

		if (
			listTabSpecs == null
			|| listTabSpecs.size () <= 1
		) {
			return;
		}

		htmlParagraphOpen (
			htmlClassAttribute (
				"links"));

		for (
			ObjectListTabSpec listTabSpec
				: listTabSpecs.values ()
		) {

			htmlLinkWrite (
				formatWriter,
				requestContext.resolveLocalUrl (
					stringFormat (
						"/%u",
						localName,
						"?tab=%u",
						listTabSpec.name ())),
				listTabSpec.label (),
				presentInstances (
					optionalIf (
						listTabSpec == currentListTabSpec,
						() -> htmlClassAttribute (
							"selected"))));

		}

		htmlParagraphClose ();

	}

	void goList (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goList");

		htmlTableOpenList ();

		htmlTableRowOpen ();

		formFieldLogic.outputTableHeadings (
			formatWriter,
			fields);

		htmlTableRowClose ();

		// render rows

		for (
			ObjectType object
				: selectedObjects
		) {

			htmlTableRowOpen (
				formatWriter,

				htmlClassAttribute (
					presentInstances (

					Optional.of (
						"magic-table-row"),

					optionalIf (
						object == currentObject,
						() -> "selected")

				)),

				htmlDataAttribute (
					"target-href",
					requestContext.resolveContextUrl (
						stringFormat (
							"%s",
							targetContext.pathPrefix (),
							"/%s",
							consoleHelper.getPathId (
								taskLogger,
								object))))

			);

			formFieldLogic.outputTableCellsList (
				taskLogger,
				formatWriter,
				fields,
				object,
				emptyMap (),
				false);

			htmlTableRowClose (
				formatWriter);

		}

		htmlTableClose (
			formatWriter);

	}

}
