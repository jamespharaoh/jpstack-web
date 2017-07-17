package wbs.console.forms.object;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
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
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

import wbs.utils.etc.PropertyUtils;

@Accessors (fluent = true)
@PrototypeComponent ("specialFormFieldAccessor")
public
class SpecialFormFieldAccessor <Container extends Record <Container>, Native>
	implements FormFieldAccessor <Container, Native> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// properties

	@Getter @Setter
	String specialName;

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

			// get field name

			ConsoleHelper <?> consoleHelper =
				objectManager.consoleHelperForObjectRequired (
					container);

			ModelField modelField =
				(ModelField)
				PropertyUtils.propertyGetSimple (
					consoleHelper,
					stringFormat (
						"%sField",
						specialName));

			String name =
				modelField.name ();

			// get native object

			Object nativeObject =
				PropertyUtils.propertyGetAuto (
					container,
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
	Optional <String> write (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional<Native> nativeValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"write");

		) {

			// get field name

			ConsoleHelper <?> consoleHelper =
				objectManager.consoleHelperForObjectRequired (
					container);

			ModelField modelField =
				(ModelField)
				PropertyUtils.propertyGetSimple (
					consoleHelper,
					stringFormat (
						"%sField",
						specialName));

			String name =
				modelField.name ();

			// sanity check native type

			if (

				nativeValue.isPresent ()

				&& ! nativeClass.isInstance (
					nativeValue.get ())

			) {

				throw new RuntimeException (
					stringFormat (
						"Field %s is %s, not %s",
						name,
						nativeClass.getSimpleName (),
						nativeValue.get ().getClass ().getSimpleName ()));

			}

			// set property

			PropertyUtils.propertySetAuto (
				container,
				name,
				nativeValue.orNull ());

			// return

			return optionalAbsent ();

		}

	}

}
