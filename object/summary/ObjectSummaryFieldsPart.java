package wbs.platform.object.summary;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;

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
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.platform.scaffold.model.RootObjectHelper;

@Accessors (fluent = true)
@PrototypeComponent ("objectSummaryFieldsPart")
public
class ObjectSummaryFieldsPart <
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
>
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@SingletonDependency
	RootObjectHelper rootHelper;

	// properties

	@Getter @Setter
	ConsoleHelper <ObjectType> consoleHelper;

	@Getter @Setter
	FormFieldSet <ObjectType> formFieldSet;

	@Getter @Setter
	FieldsProvider <ObjectType, ParentType> formFieldsProvider;

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

		htmlTableOpenDetails ();

		formFieldLogic.outputTableRows (
			formatWriter,
			formFieldSet,
			object,
			ImmutableMap.of ());

		htmlTableClose ();

	}

}
