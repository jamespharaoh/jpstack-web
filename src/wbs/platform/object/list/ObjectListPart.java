package wbs.platform.object.list;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Interval;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.html.ObsoleteDateField;
import wbs.console.html.ObsoleteDateLinks;
import wbs.console.module.ConsoleManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.PrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.Html;
import wbs.platform.object.criteria.CriteriaSpec;
import wbs.services.ticket.core.console.FieldsProvider;

import com.google.common.base.Optional;

@Accessors (fluent = true)
@PrototypeComponent ("objectListPart")
public
class ObjectListPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ConsoleManager consoleManager;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	PrivChecker privChecker;

	// properties

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

	@Getter @Setter
	String typeCode;

	@Getter @Setter
	String localName;

	@Getter @Setter
	Map<String,ObjectListBrowserSpec> listBrowserSpecs;

	@Getter @Setter
	Map<String,ObjectListTabSpec> listTabSpecs;

	@Getter @Setter
	FieldsProvider formFieldsProvider;

	@Getter @Setter
	String targetContextTypeName;

	// state

	FormFieldSet formFieldSet;

	ObsoleteDateField dateField;

	Optional<ObjectListBrowserSpec> currentListBrowserSpec;

	ObjectListTabSpec currentListTabSpec;
	Record<?> currentObject;

	List<? extends Record<?>> allObjects;
	List<Record<?>> selectedObjects;

	ConsoleContext targetContext;
	Record<?> parent;

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

		formFieldSet = formFieldsProvider.getFields(
			parent);

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
					requestContext.parameter ("date"));

			if (dateField.date == null) {

				requestContext.addError (
					"Invalid date");

				return;

			}

		}

	}

	void prepareTabSpec () {

		String currentTabName =
			requestContext.parameter (
				"tab",
				listTabSpecs.values ().iterator ().next ().name ());

		currentListTabSpec =
			listTabSpecs.get (
				currentTabName);

	}

	void prepareCurrentObject () {

		Integer objectId =
			(Integer)
			requestContext.stuff (
				consoleHelper.objectName () + "Id");

		if (objectId != null) {

			currentObject =
				consoleHelper.find (
					objectId);

		}

	}

	void prepareAllObjects () {

		// locate via parent

		if (consoleHelper.parentTypeIsFixed ()) {

			ConsoleHelper<?> parentHelper =
				objectManager.getConsoleObjectHelper (
					consoleHelper.parentClass ());

			Integer parentId =
				(Integer)
				requestContext.stuff (
					parentHelper.idKey ());

			if (parentId != null) {

				parent =
					parentHelper.find (
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
					objectManager.getConsoleObjectHelper (
						parentHelper.parentClass ());

				Integer grandParentId =
					(Integer)
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
			ConsoleHelper<?> parentHelper,
			Integer parentId) {

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
			ConsoleHelper<?> parentHelper,
			ConsoleHelper<?> grandParentHelper,
			Integer grandParentId) {

		if (currentListBrowserSpec.isPresent ()) {

			ObjectListBrowserSpec browserSpec =
				currentListBrowserSpec.get ();

			Record<?> grandParentObject =
				grandParentHelper.find (
					grandParentId);

			if (grandParentObject == null) {

				throw new NullPointerException (
					stringFormat (
						"Can't find grand parent object %s with id %s",
						grandParentHelper.objectName (),
						grandParentId));

			}

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
				List<Record<?>> allObjectsTemp =
					(List<Record<?>>)
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

			List<? extends Record<?>> parentObjects =
				parentHelper.findByParent (
					grandParentGlobalId);

			List<Record<?>> allObjectsTemp =
				new ArrayList<Record<?>> ();

			for (
				Record<?> parentObject
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
			new ArrayList<Record<?>> ();

	OUTER:

		for (
			Record<?> object
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
		List<Comparable<Comparable<?>>> temp =
			(List<Comparable<Comparable<?>>>)
			(List<?>)
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
			consoleManager.relatedContext (
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

		printFormat (
			"<form",
			" method=\"get\"",
			" action=\"%h\"",
			localUrl,
			">\n");

		printFormat (
			"<p",
			" class=\"links\"",
			">%h\n",
			ifNull (
				browserSpec.label (),
				capitalise (
					camelToSpaces (
						browserSpec.fieldName ()))));

		printFormat (
			"<input",
			" type=\"text\"",
			" name=\"date\"",
			" value=\"%h\"",
			dateField.text,
			">\n");

		printFormat (
			"<input",
			" type=\"submit\"",
			" value=\"ok\"",
			">\n");

		printFormat (
			"%s</p>\n",
			ObsoleteDateLinks.dailyBrowserLinks (
				localUrl,
				requestContext.getFormData (),
				dateField.date));

		printFormat (
			"</form>\n");

	}

	void goTabs () {

		if (
			listTabSpecs == null
			|| listTabSpecs.size () <= 1
		) {
			return;
		}

		printFormat (
			"<p class=\"links\">\n");

		for (
			ObjectListTabSpec listTabSpec
				: listTabSpecs.values ()
		) {

			printFormat (
				"<a",

				" href=\"%h\"",
				requestContext.resolveLocalUrl (
					stringFormat (
						"/%u",
						localName,
						"?tab=%u",
						listTabSpec.name ())),

				listTabSpec == currentListTabSpec
					? " class=\"selected\""
					: "",

				">%h</a>\n",
				listTabSpec.label ());

		}

		printFormat (
			"</p>\n");

	}

	void goList () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n");

		formFieldLogic.outputTableHeadings (
			formatWriter,
			formFieldSet);

		printFormat (
			"</tr>\n");

		// render rows

		for (
			Record<?> object
				: selectedObjects
		) {

			String targetUrl =
				requestContext.resolveContextUrl (
					stringFormat (
						"%s",
						targetContext.pathPrefix (),
						"/%s",
						consoleHelper.getPathId (
							object)));

			boolean isCurrentObject =
				object == currentObject;

			printFormat (
				"%s",
				Html.magicTr (
					targetUrl,
					isCurrentObject));

			formFieldLogic.outputTableCellsList (
				formatWriter,
				formFieldSet,
				object,
				false);

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
