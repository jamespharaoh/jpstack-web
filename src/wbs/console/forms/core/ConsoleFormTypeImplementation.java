package wbs.console.forms.core;

import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;
import static wbs.utils.etc.TypeUtils.classInstantiate;
import static wbs.utils.etc.TypeUtils.genericCastUncheckedNullSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormFieldSubmission;
import wbs.console.forms.types.FormType;
import wbs.console.forms.types.FormUpdateResultSet;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

@PrototypeComponent ("consoleFormType")
@Accessors (fluent = true)
public
class ConsoleFormTypeImplementation <Container>
	implements ConsoleFormType <Container> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	// uninitialized dependencies

	@UninitializedDependency
	Provider <ConsoleFormImplementation <Container>> formContextProvider;

	// properties

	@Getter @Setter
	Class <Container> containerClass;

	@Getter @Setter
	String formName;

	@Getter @Setter
	FormType formType;

	@Getter @Setter
	FormFieldSet <Container> columnFields;

	@Getter @Setter
	FormFieldSet <Container> rowFields;

	// implementation

	@Override
	public
	ConsoleForm <Container> buildAction (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Object> hints,
			@NonNull List <Container> values) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"build");

		) {

			FormUpdateResultSet updateResults =
				new FormUpdateResultSet ();

			requestContext.request (
				stringFormat (
					"form--%s--results",
					formName),
				updateResults);

			return formContextProvider.get ()

				.requestContext (
					requestContext)

				.privChecker (
					privChecker)

				.containerClass (
					containerClass)

				.columnFields (
					columnFields)

				.rowFields (
					rowFields)

				.formName (
					formName)

				.formType (
					formType)

				.hints (
					hints)

				.values (
					values)

				.submission (
					FormFieldSubmission.fromRequestContext (
						requestContext))

				.updateResultSet (
					updateResults)

			;

		}

	}

	@Override
	public
	ConsoleForm <Container> buildAction (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Object> hints) {

		return buildAction (
			parentTransaction,
			hints,
			ImmutableList.of (
				classInstantiate (
					containerClass)));

	}

	@Override
	public
	ConsoleForm <Container> buildResponse (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Object> hints,
			@NonNull List <Container> objects) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"build");

		) {

			FormUpdateResultSet updateResults =
				genericCastUncheckedNullSafe (
					optionalOrElseRequired (
						requestContext.request (
							stringFormat (
								"form--%s--results",
								formName)),
						() -> new FormUpdateResultSet ()));

			return formContextProvider.get ()

				.requestContext (
					requestContext)

				.formatWriter (
					requestContext.formatWriter ())

				.privChecker (
					privChecker)

				.containerClass (
					containerClass)

				.columnFields (
					columnFields)

				.rowFields (
					rowFields)

				.formName (
					formName)

				.formType (
					formType)

				.hints (
					hints)

				.values (
					objects)

				.submission (
					FormFieldSubmission.fromRequestContext (
						requestContext))

				.updateResultSet (
					updateResults)

			;

		}

	}

	@Override
	public
	ConsoleForm <Container> buildResponse (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Object> hints) {

		return buildResponse (
			parentTransaction,
			hints,
			ImmutableList.of (
				classInstantiate (
					containerClass)));

	}

}
