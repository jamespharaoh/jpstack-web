package wbs.platform.object.browse;

import static wbs.utils.collection.CollectionUtils.collectionStream;
import static wbs.utils.etc.OptionalUtils.optionalIf;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.module.ConsoleManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;

@Accessors (fluent = true)
@PrototypeComponent ("objectBrowsePart")
public
class ObjectBrowsePart <ObjectType extends Record <ObjectType>>
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
	FormFieldSet <ObjectType> formFieldSet;

	@Getter @Setter
	String targetContextTypeName;

	// state

	ObjectType currentObject;
	List <ObjectType> allObjects;

	ConsoleContext targetContext;

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

		prepareCurrentObject ();
		prepareAllObjects ();
		prepareTargetContext ();

	}

	void prepareCurrentObject () {

		Long objectId =
			requestContext.stuffInteger (
				consoleHelper.objectName () + "Id");

		if (objectId != null) {

			currentObject =
				consoleHelper.findRequired (
					objectId);

		}

	}

	void prepareAllObjects () {

		// locate via parent

		ConsoleHelper<?> parentHelper =
			objectManager.findConsoleHelper (
				consoleHelper.parentClass ());

		Long parentId =
			requestContext.stuffInteger (
				parentHelper.idKey ());

		if (parentId != null) {

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
			objectManager.findConsoleHelper (
				parentHelper.parentClass ());

		Long grandParentId =
			requestContext.stuffInteger (
				grandParentHelper.idKey ());

		if (grandParentId != null) {

			GlobalId grandParentGlobalId =
				GlobalId.of (
					grandParentHelper.objectTypeId (),
					grandParentId);

			List <? extends Record<?>> parentObjects =
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

		// table open

		htmlTableOpenList ();

		// table header

		htmlTableRowOpen ();

		formFieldLogic.outputTableHeadings (
			formatWriter,
			formFieldSet);

		htmlTableRowClose ();

		// table content

		for (
			ObjectType object
				: allObjects
		) {

			htmlTableRowOpen (

				htmlClassAttribute (
					presentInstances (

					optionalOf (
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

			htmlTableRowClose ();

		}

		// table close

		htmlTableClose ();

	}

}
