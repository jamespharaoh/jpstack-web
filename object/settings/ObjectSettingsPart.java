package wbs.platform.object.settings;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import wbs.console.forms.FormField.FormType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
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
class ObjectSettingsPart<
	ObjectType extends Record<ObjectType>,
	ParentType extends Record<ParentType>
>
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
	ObjectLookup<ObjectType> objectLookup;

	@Getter @Setter
	ConsoleHelper<ObjectType> consoleHelper;

	@Getter @Setter
	String editPrivKey;

	@Getter @Setter
	String localName;

	@Getter @Setter
	FormFieldSet formFieldSet;

	@Getter @Setter
	String removeLocalName;

	@Getter @Setter
	FieldsProvider<ObjectType,ParentType> formFieldsProvider;

	// state

	Optional<UpdateResultSet> updateResultSet;
	ObjectType object;
	ParentType parent;
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

		updateResultSet =
			Optional.fromNullable (
				(UpdateResultSet)
				requestContext.request (
					"objectSettingsUpdateResultSet"));

		object =
			objectLookup.lookupObject (
				requestContext.contextStuff ());

		canEdit = (

			editPrivKey != null

			&& requestContext.canContext (
				editPrivKey)

		);

		if (formFieldsProvider != null) {

			prepareParent ();
			prepareFieldSet ();

		}

	}

	void prepareParent () {

		@SuppressWarnings ("unchecked")
		ConsoleHelper<ParentType> parentHelper =
			(ConsoleHelper<ParentType>)
			objectManager.findConsoleHelper (
				consoleHelper.parentClass ());

		if (parentHelper.isRoot ()) {

			parent =
				parentHelper.find (
					0);

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

	void prepareFieldSet () {

		formFieldSet =
			formFieldsProvider.getFieldsForObject (
				object);

	}

	@Override
	public
	void renderHtmlHeadContent () {

	}

	@Override
	public
	void renderHtmlBodyContent () {

		if (canEdit) {

			String enctype =
				"application/x-www-form-urlencoded";

			try {

				if (formFieldSet.fileUpload ()) {

					enctype =
						"multipart/form-data";

				}

			} catch (Exception exception) {

				enctype =
					"application/x-www-form-urlencoded";

			}

			printFormat (
				"<form",
				" method=\"post\"",
				" action=\"%h\"",
				requestContext.resolveLocalUrl (
					localName),
				" enctype=\"%h\"",
				enctype,
				">\n");

		}

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputFormRows (
			requestContext,
			formatWriter,
			formFieldSet,
			updateResultSet,
			object,
			ImmutableMap.of (),
			FormType.update);

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
