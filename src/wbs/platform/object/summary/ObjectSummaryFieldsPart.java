package wbs.platform.object.summary;

import static wbs.framework.utils.etc.Misc.isNotNull;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.platform.scaffold.model.RootObjectHelper;

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
				parentHelper.findRequired (
					0l);

			return;

		}

		Long parentId =
			requestContext.stuffInteger (
				parentHelper.idKey ());

		if (
			isNotNull (
				parentId)
		) {

			// use specific parent

			parent =
				parentHelper.findRequired (
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
			object,
			ImmutableMap.of ());

		printFormat (
			"</table>\n");

	}

}
