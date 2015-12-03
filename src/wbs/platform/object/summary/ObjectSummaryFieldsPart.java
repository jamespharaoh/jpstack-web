package wbs.platform.object.summary;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.services.ticket.core.console.FieldsProvider;

@Accessors (fluent = true)
@PrototypeComponent ("objectSummaryFieldsPart")
public
class ObjectSummaryFieldsPart<
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
	ConsoleHelper<ObjectType> consoleHelper;

	@Getter @Setter
	FormFieldSet formFieldSet;

	@Getter @Setter
	FieldsProvider<ObjectType,ParentType> formFieldsProvider;

	// state

	ObjectType object;
	ParentType parent;

	// implementation

	@Override
	public
	void prepare () {

		object =
			consoleHelper.lookupObject (
				requestContext.contextStuff ());

		if (formFieldsProvider != null) {
			prepareParent();
			prepareFieldSet();
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
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputTableRows (
			formatWriter,
			formFieldSet,
			object);

		printFormat (
			"</table>\n");

	}

}
