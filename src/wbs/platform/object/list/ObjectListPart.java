package wbs.platform.object.list;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.context.ConsoleContext;
import wbs.platform.console.context.ConsoleContextType;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.object.criteria.CriteriaSpec;
import wbs.platform.priv.console.PrivChecker;
import wbs.services.ticket.core.console.FieldsProvider;

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
	Map<String,ObjectListTabSpec> listTabSpecs;

	@Getter @Setter
	FieldsProvider formFieldsProvider;

	@Getter @Setter
	String targetContextTypeName;
	
	// state

	FormFieldSet formFieldSet;
	
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

		ConsoleHelper<?> parentHelper =
			objectManager.getConsoleObjectHelper (
				consoleHelper.parentClass ());

		Integer parentId =
			(Integer)
			requestContext.stuff (
				parentHelper.idKey ());

		if (parentId != null) {

			parent = parentHelper.find(
					parentId);
			
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

			return;

		}

		// locate via grand parent

		ConsoleHelper<?> grandParentHelper =
			objectManager.getConsoleObjectHelper (
				parentHelper.parentClass ());

		Integer grandParentId =
			(Integer)
			requestContext.stuff (
				grandParentHelper.idKey ());

		if (grandParentId != null) {

			GlobalId grandParentGlobalId =
				new GlobalId (
					grandParentHelper.objectTypeId (),
					grandParentId);

			List<? extends Record<?>> parentObjects =
				parentHelper.findByParent (
					grandParentGlobalId);

			List<Record<?>> allObjectsTemp =
				new ArrayList<Record<?>> ();

			for (Record<?> parentObject
					: parentObjects) {

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

			return;

		}

		// return all

		if (typeCode != null) {

			throw new RuntimeException ();

		} else {

			allObjects =
				consoleHelper.findAll ();

		}

	}

	void prepareSelectedObjects () {

		// select which objects we want to display

		selectedObjects =
			new ArrayList<Record<?>> ();

		OUTER:
		for (Record<?> object
				: allObjects) {

			if (! consoleHelper.canView (
					object))
				continue;

			for (CriteriaSpec criteriaSpec
					: currentListTabSpec.criterias ()) {

				if (! criteriaSpec.evaluate (
						consoleHelper,
						object))
					continue OUTER;

			}

			selectedObjects.add (object);

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
	void goBodyStuff () {

		if (listTabSpecs != null
				&& listTabSpecs.size () > 1) {

			printFormat (
				"<p class=\"links\">\n");

			for (ObjectListTabSpec listTabSpec
					: listTabSpecs.values ()) {

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

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n");

		formFieldLogic.outputTableHeadings (
			out,
			formFieldSet);

		printFormat (
			"</tr>\n");

		// render rows

		for (Record<?> object
				: selectedObjects) {

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
				out,
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
