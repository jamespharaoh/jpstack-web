package wbs.console.formaction;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.core.ConsoleFormType;
import wbs.console.part.PagePart;
import wbs.console.part.PagePartFactory;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

@Accessors (fluent = true)
public
class ConsoleFormActionPartFactory <FormState, History>
	implements PagePartFactory {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ConsoleFormActionPart <FormState, History>>
		consoleFormActionPartProvider;

	// properties

	@Getter @Setter
	ComponentProvider <ConsoleFormActionHelper <FormState, History>>
		helperProvider;

	@Getter @Setter
	ConsoleFormType <FormState> actionFormType;

	@Getter @Setter
	String name;

	@Getter @Setter
	String heading;

	@Getter @Setter
	String helpText;

	@Getter @Setter
	String submitLabel;

	@Getter @Setter
	String localFile;

	@Getter @Setter
	String historyHeading;

	@Getter @Setter
	ConsoleFormType <History> historyFormType;

	// implementation

	@Override
	public
	PagePart buildPagePart (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"buildPagePart");

		) {

			return consoleFormActionPartProvider.provide (
				transaction)

				.name (
					"action")

				.heading (
					heading)

				.helper (
					helperProvider.provide (
						transaction))

				.actionFormType (
					actionFormType)

				.helpText (
					helpText)

				.submitLabel (
					submitLabel)

				.localFile (
					localFile)

				.historyHeading (
					historyHeading)

				.historyFormType (
					historyFormType)

			;

		}

	}

/*
	void buildPagePartFactory () {

		pagePartFactory =
			parentTaskLogger ->
				consoleFormActionPartProvider.get ()

			.name (
				"action")

			.heading (
				capitalise (
					joinWithSpace (
						hyphenToSpaces (
							consoleModule.name ()),
						spec.name ())))

			.helper (
				formActionHelperProvider.get ())

			.actionFormContextBuilder (
				actionFormType)

			.helpText (
				spec.helpText ())

			.submitLabel (
				spec.submitLabel ())

			.localFile (
				"/" + localFile)

			.historyHeading (
				spec.historyHeading ())

			.historyFormContextBuilder (
				historyFormType)

		;
*/

}
