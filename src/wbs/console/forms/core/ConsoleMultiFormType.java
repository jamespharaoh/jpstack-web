package wbs.console.forms.core;

import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.classInstantiate;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Provider;

import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

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

@PrototypeComponent ("consoleMultiFormType")
@Accessors (fluent = true)
public
class ConsoleMultiFormType <Container> {

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
	Map <String, FormFieldSet <Container>> fieldSets;

	// implementation

	public
	ConsoleMultiForm <Container> build (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Object> hints) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"build");

		) {

			Container value =
				containerClass.cast (
					optionalOrElseRequired (
						requestContext.request (
							stringFormat (
								"form-context--%s--value",
								formName)),
						() -> classInstantiate (
							containerClass)));

			FormUpdateResultSet updateResults =
				genericCastUnchecked (
					optionalOrNull (
						requestContext.request (
							stringFormat (
								"form-context--%s--results",
								formName))));

			ConsoleMultiForm <Container> form =
				new ConsoleMultiForm <Container> ()

				.value (
					value)

				.formContexts (
					fieldSets.entrySet ().stream ()

					.map (
						fieldSetEntry -> {

						String name =
							fieldSetEntry.getKey ();

						FormFieldSet <Container> fields =
							fieldSetEntry.getValue ();

						return Pair.of (
							name,
							formContextProvider.get ()

							.requestContext (
								requestContext)

							.privChecker (
								privChecker)

							.formName (
								formName)

							.containerClass (
								containerClass)

							.columnFields (
								fields)

							.formType (
								formType)

							.values (
								ImmutableList.of (
									value))

							.hints (
								hints)

							.submission (
								FormFieldSubmission.fromRequestContext (
									requestContext))

							.updateResultSet (
								updateResults)

						);

					})

					.collect (
						Collectors.toMap (
							Pair::getKey,
							Pair::getValue))

				)

			;

			return form;

		}

	}

}
