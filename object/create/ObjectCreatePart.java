package wbs.platform.object.create;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.scaffold.model.RootObjectHelper;

@Accessors (fluent = true)
@PrototypeComponent ("objectCreatePart")
public
class ObjectCreatePart
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
	ConsoleHelper<?> consoleHelper;

	@Getter @Setter
	FormFieldSet formFieldSet;

	@Getter @Setter
	String parentPrivCode;

	@Getter @Setter
	String localFile;

	// state

	Record<?> parent;

	List<? extends Record<?>> parents;

	ConsoleHelper<?> parentHelper;

	Record<?> object;

	// implementation

	@Override
	public
	void prepare () {

		prepareParents ();

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

		parentHelper =
			objectManager.getConsoleObjectHelper (
				consoleHelper.parentClass ());

		if (parentHelper.root ()) {

			parent =
				rootHelper.find (0);

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


	@Override
	public
	void goBodyStuff () {

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
			out,
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
