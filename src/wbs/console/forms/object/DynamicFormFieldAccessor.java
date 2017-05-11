package wbs.console.forms.object;

import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.objectClassNameSimple;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormFieldAccessor;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

@Accessors (fluent = true)
@PrototypeComponent ("dynamicFormFieldAccessor")
public
class DynamicFormFieldAccessor <
	Container extends Record <Container>,
	Native
>
	implements FormFieldAccessor <Container, Native> {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager consoleObjectManager;

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	Class <? extends Native> nativeClass;

	// implementation

	@Override
	public
	Optional <Native> read (
			@NonNull Transaction parentTransaction,
			@NonNull Container container) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"read");

		) {

			// get native object

			ConsoleHelper <?> consoleHelper =
				consoleObjectManager.findConsoleHelperRequired (
					container);

			Object nativeObject =
				consoleHelper.getDynamic (
					transaction,
					genericCastUnchecked (
						container),
					name);

			// special case for null

			if (nativeObject == null) {
				return Optional.<Native>absent ();
			}

			// sanity check native type

			if (
				! nativeClass.isInstance (
					nativeObject)
			) {

				throw new RuntimeException (
					stringFormat (
						"Field %s is %s, not %s",
						name,
						nativeObject.getClass ().getSimpleName (),
						nativeClass.getSimpleName ()));

			}

			// cast and return

			return optionalOf (
				nativeClass.cast (
					nativeObject));

		}

	}

	@Override
	public
	void write (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Native> nativeValueOptional) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"write");

		) {

			// sanity check native type

			if (

				nativeValueOptional.isPresent ()

				&& ! nativeClass.isInstance (
					nativeValueOptional.get ())

			) {

				throw new RuntimeException (
					stringFormat (
						"Field %s is %s, not %s",
						name,
						nativeClass.getSimpleName (),
						objectClassNameSimple (
							nativeValueOptional.get ())));

			}

			// set property

			ConsoleHelper <?> consoleHelper =
				consoleObjectManager.findConsoleHelperRequired (
					container);

			consoleHelper.setDynamic (
				transaction,
				genericCastUnchecked (
					container),
				name,
				nativeValueOptional);

		}

	}

}
