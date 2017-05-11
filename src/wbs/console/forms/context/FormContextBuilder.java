package wbs.console.forms.context;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.classInstantiate;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.camelToHyphen;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.uncapitalise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.core.FormFieldSet;
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

@PrototypeComponent ("formContextBuilder")
@Accessors (fluent = true)
public
class FormContextBuilder <Container> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	// uninitialized dependencies

	@UninitializedDependency
	Provider <FormContextImplementation <Container>> formContextProvider;

	// properties

	@Getter @Setter
	Class <Container> objectClass;

	@Getter @Setter
	String formName;

	@Getter @Setter
	FormType formType;

	@Getter @Setter
	FormFieldSet <Container> columnFields;

	@Getter @Setter
	FormFieldSet <Container> rowFields;

	// implementation

	public
	FormContext <Container> build (
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
				genericCastUnchecked (
					optionalOrNull (
						requestContext.request (
							stringFormat (
								"form-context--%s--%s--results",
								camelToHyphen (
									uncapitalise (
										classNameSimple (
											objectClass))),
								formName))));

			return formContextProvider.get ()

				.requestContext (
					requestContext)

				.privChecker (
					privChecker)

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

				.objects (
					objects)

				.submission (
					FormFieldSubmission.fromRequestContext (
						requestContext))

				.updateResultSet (
					updateResults)

			;

		}

	}

	public
	FormContext <Container> build (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <List <Container>> objects) {

		return build (
			parentTransaction,
			hints,
			optionalOrElseRequired (
				objects,
				() -> emptyList ()));

	}

	public
	FormContext <Container> build (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Object> hints,
			@NonNull Container object) {

		return build (
			parentTransaction,
			hints,
			new ArrayList <Container> (
				Collections.singleton (
					object)));

	}

	public
	FormContext <Container> build (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Object> hints) {

		Container object =
			objectClass.cast (
				optionalOrElseRequired (
					requestContext.request (
						stringFormat (
							"form-context--%s--%s--value",
							camelToHyphen (
								uncapitalise (
									classNameSimple (
										objectClass))),
							formName)),
					() -> classInstantiate (
						objectClass)));

		return build (
			parentTransaction,
			hints,
			new ArrayList <Container> (
				Collections.singleton (
					object)));

	}

	// private implementation

}
