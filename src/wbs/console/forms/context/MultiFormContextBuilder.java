package wbs.console.forms.context;

import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.classInstantiate;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.camelToHyphen;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.uncapitalise;

import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Provider;

import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

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

@PrototypeComponent ("multiFormContextBuilder")
@Accessors (fluent = true)
public
class MultiFormContextBuilder <Container> {

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
	Map <String, FormFieldSet <Container>> fieldSets;

	// implementation

	public
	MultiFormContexts <Container> build (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Object> hints) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"build");

		) {

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

			MultiFormContexts <Container> contexts =
				new MultiFormContexts <Container> ()

				.object (
					object)

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

							.columnFields (
								fields)

							.formName (
								formName)

							.formType (
								formType)

							.objects (
								ImmutableList.of (
									object))

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

			return contexts;

		}

	}

}
