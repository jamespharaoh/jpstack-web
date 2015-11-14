package wbs.platform.object.settings;

import java.util.LinkedHashSet;
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
import wbs.console.lookup.ObjectLookup;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.services.ticket.core.console.FieldsProvider;

@Accessors (fluent = true)
@PrototypeComponent ("objectSettingsPart")
public
class ObjectSettingsPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	RootObjectHelper rootHelper;

	// properties

	@Getter @Setter
	ObjectLookup<?> objectLookup;

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

	@Getter @Setter
	String editPrivKey;

	@Getter @Setter
	String localName;

	@Getter @Setter
	FormFieldSet formFieldSet;

	@Getter @Setter
	String removeLocalName;

	@Getter @Setter
	FieldsProvider formFieldsProvider;

	// state

	Record<?> object;
	Record<?> parent;
	boolean canEdit;

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

		object =
			(Record<?>)
			objectLookup.lookupObject (
				requestContext.contextStuff ());

		canEdit =
			editPrivKey != null
				&& requestContext.canContext (editPrivKey);

		if (formFieldsProvider != null) {
			prepareParent();
			prepareFieldSet();
		}


	}

	void prepareParent () {

		ConsoleHelper<?> parentHelper =
			objectManager.getConsoleObjectHelper (
				consoleHelper.parentClass ());

		if (parentHelper.isRoot ()) {

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

	}

	void prepareFieldSet() {

		formFieldSet = formFieldsProvider.getFields(
				parent);

	}

	@Override
	public
	void renderHtmlHeadContent () {

		//for (PagePart pagePart : pageParts)
		//	pagePart.goHeadStuff();

	}

	@Override
	public
	void renderHtmlBodyContent () {

		if (canEdit) {

			String enctype = "application/x-www-form-urlencoded";
			try {
				if (formFieldSet.fileUpload ()) {
					enctype = "multipart/form-data";
				}
			}
			catch (Exception e) {
				enctype = "application/x-www-form-urlencoded";
			}

			printFormat (
				"<form",
				" method=\"post\"",
				" action=\"%h\"",
				requestContext.resolveLocalUrl (localName),
				" enctype=\"%h\"",
				enctype,
				">\n");

		}

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputFormRows (
			formatWriter,
			formFieldSet,
			object);

		printFormat (
			"</table>");

		if (canEdit) {

			printFormat (
				"<p><input",
				" type=\"submit\"",
				" value=\"save changes\"",
				"></p>\n");

			printFormat (
				"</form>\n");

			if (removeLocalName != null) {

				printFormat (
					"<h2>Remove</h2>");

				printFormat (
					"<form",
					" method=\"post\"",
					" action=\"%h\"",
					requestContext.resolveLocalUrl (
						removeLocalName),
					">\n");

				printFormat (
					"<p><input",
					" type=\"submit\"",
					" value=\"remove\"",
					"></p>\n");

				printFormat (
					"</form>\n");

			}

		}

	}

}
