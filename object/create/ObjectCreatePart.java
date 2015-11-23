package wbs.platform.object.create;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.PrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.services.ticket.core.console.FieldsProvider;

@Accessors (fluent = true)
@PrototypeComponent ("objectCreatePart")
public
class ObjectCreatePart<
	ObjectType extends Record<ObjectType>,
	ParentType extends Record<ParentType>
>
	extends AbstractPagePart {

	// dependencies

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	PrivChecker privChecker;

	@Inject
	RootObjectHelper rootHelper;

	// properties

	@Getter @Setter
	ConsoleHelper<ObjectType> consoleHelper;

	@Getter @Setter
	FormFieldSet formFieldSet;

	@Getter @Setter
	String parentPrivCode;

	@Getter @Setter
	String localFile;

	@Getter @Setter
	FieldsProvider<ObjectType,ParentType> formFieldsProvider;

	// state

	ParentType parent;

	List<ParentType> parents;

	ConsoleHelper<ParentType> parentHelper;

	ObjectType object;

	// implementation

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		Set<ScriptRef> scriptRefs =
			new LinkedHashSet<ScriptRef> ();

		scriptRefs.addAll (
			formFieldSet.scriptRefs ());

		return scriptRefs;

	}

	@Override
	public
	void prepare () {

		prepareParents ();

		// if a field provider was provided

		if (formFieldsProvider != null) {
			prepareFieldSet();
		}

		// create dummy instance

		object =
			consoleHelper.createInstance ();

		// set parent

		if (
			parent != null
			&& consoleHelper.canGetParent ()
		) {

			consoleHelper.setParent (
				object,
				parent);

		}

	}

	void prepareParents () {

		@SuppressWarnings ("unchecked")
		ConsoleHelper<ParentType> parentHelperTemp =
			(ConsoleHelper<ParentType>)
			objectManager.getConsoleObjectHelper (
				consoleHelper.parentClass ());

		parentHelper =
			parentHelperTemp;

		if (parentHelper.isRoot ()) {

			@SuppressWarnings ("unchecked")
			ParentType parentTemp =
				(ParentType)
				(Object)
				rootHelper.find (
					0);

			parent =
				parentTemp;

			return;

		}

		Integer parentId =
			requestContext.stuffInt (
				parentHelper.idKey ());

		if (parentId != null) {

			// use specific parent

			parent =
				parentHelper.find (
					parentId);

			return;

		}

		ConsoleHelper<?> grandParentHelper =
			objectManager.getConsoleObjectHelper (
				parentHelper.parentClass ());

		Integer grandParentId =
			requestContext.stuffInt (
				grandParentHelper.objectName () + "Id");

		if (grandParentId != null) {

			// show parents based on grand parent

			parents =
				parentHelper.findByParent (
					new GlobalId (
						grandParentHelper.objectTypeId (),
						grandParentId));

			return;

		}

		// show all parents

		parents =
			parentHelper.findAll ();

	}

	void prepareFieldSet () {

		formFieldSet =
			parent != null
				? formFieldsProvider.getFieldsForParent (
					parent)
				: formFieldsProvider.getStaticFields ();

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<p>Please enter the details for the new %h</p>\n",
			consoleHelper.shortName ());

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/" + localFile),
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputFormRows (
			formatWriter,
			formFieldSet,
			object);

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"create %h\"",
			consoleHelper.shortName (),
			"></p>\n");

		printFormat (
			"</form>\n");

	}

}
