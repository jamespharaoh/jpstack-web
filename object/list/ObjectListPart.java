package wbs.platform.object.list;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalIf;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenGetAction;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;
import static wbs.utils.web.HtmlUtils.htmlLinkWrite;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
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
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ObsoleteDateField;
import wbs.console.html.ObsoleteDateLinks;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.module.ConsoleManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
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
	ConsoleObjectManager objectManager;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

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

	FormFieldSet formFieldSet;

	ObsoleteDateField dateField;

	Optional <ObjectListBrowserSpec> currentListBrowserSpec;

	ObjectListTabSpec currentListTabSpec;
	Record <?> currentObject;

	List <? extends Record <?>> allObjects;
	List <Record <?>> selectedObjects;

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
	void prepare () {

		prepareBrowserSpec ();
		prepareTabSpec ();
		prepareCurrentObject ();
		prepareAllObjects ();
		prepareSelectedObjects ();
		prepareTargetContext ();
		prepareFieldSet ();

	}

	void prepareFieldSet () {

		formFieldSet =
			parent != null
				? formFieldsProvider.getFieldsForParent (
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
					requestContext.parameterOrNull ("date"));

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

			@SuppressWarnings ("unchecked")
			ConsoleHelper<ParentType> parentHelper =
				(ConsoleHelper<ParentType>)
				objectManager.findConsoleHelper (
					consoleHelper.parentClass ());

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
					objectManager.findConsoleHelper (
						parentHelper.parentClass ());

				Long grandParentId =
					(Long)
					requestContext.stuff (
						grandParentHelper.idKey ());

				if (grandParentId != null) {

					prepareAllObjectsViaGrandParent (
						parentHelper,
						grandParentHelper,
						grandParentId);

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
							grandParentId)));

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

				@SuppressWarnings ("unchecked")
				List <Record <?>> allObjectsTemp =
					(List <Record <?>>)
					daoMethod.invoke (
						consoleHelper,
						grandParentObject,
						dateField.date.toInterval ());

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

			List <Record <?>> allObjectsTemp =
				new ArrayList <Record <?>> ();

			for (
				Record <?> parentObject
					: parentObjects
			) {

				GlobalId parentGlobalId =
					new GlobalId (
						parentHelper.objectTypeId (),
						parentObject.getId ());

				if (typeCode != null) {

					allObjectsTemp.addAll (
						consoleHelper.findByParentAndType (
							parentGlobalId,
							typeCode));

				} else {

					allObjectsTemp.addAll (
						consoleHelper.findByParent (
							parentGlobalId));

				}

			}

			allObjects =
				allObjectsTemp;

		}

	}

	void prepareSelectedObjects () {

		// select which objects we want to display

		selectedObjects =
			new ArrayList <Record <?>> ();

	OUTER:

		for (
			Record <?> object
				: allObjects
		) {

			if (! consoleHelper.canView (
					object)) {

				continue;

			}

			for (
				CriteriaSpec criteriaSpec
					: currentListTabSpec.criterias ()
			) {

				if (
					! criteriaSpec.evaluate (
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

	void prepareTargetContext () {

		ConsoleContextType targetContextType =
			consoleManager.contextType (
				targetContextTypeName,
				true);

		targetContext =
			consoleManager.relatedContextRequired (
				requestContext.consoleContext (),
				targetContextType);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		goBrowser ();
		goTabs ();
		goList ();

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

	void goList () {

		htmlTableOpenList ();

		htmlTableRowOpen ();

		formFieldLogic.outputTableHeadings (
			formatWriter,
			formFieldSet);

		htmlTableRowClose ();

		// render rows

		for (
			Record <?> object
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
								object))))

			);

			formFieldLogic.outputTableCellsList (
				formatWriter,
				formFieldSet,
				object,
				ImmutableMap.of (),
				false);

			htmlTableRowClose (
				formatWriter);

		}

		htmlTableClose (
			formatWriter);

	}

}
