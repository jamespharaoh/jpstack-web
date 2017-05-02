package wbs.console.forms;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringInSafe;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.manager.ConsoleObjectManager;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.etc.PropertyUtils;

@Accessors (fluent = true)
@PrototypeComponent ("dereferenceFormFieldacessor")
public
class DereferenceFormFieldAccessor <Container, Native>
	implements FormFieldAccessor <Container, Native> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// properties

	@Getter @Setter
	String path;

	@Getter @Setter
	Class<?> nativeClass;

	// implementation

	@Override
	public
	Optional <Native> read (
			@NonNull Transaction parentTransaction,
			@NonNull Container container) {

		return genericCastUnchecked (
			objectManager.dereference (
				parentTransaction,
				container,
				path));

	}

	@Override
	public
	void write (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Native> nativeValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"write");

		) {

			if (

				path.indexOf ('.') >= 0

				|| stringInSafe (
					path,
					"this",
					"parent",
					"grandparent",
					"greatgrandparent",
					"root")

			) {

				throw new RuntimeException ();

			}

			// sanity check native type

			if (

				nativeValue.isPresent ()

				&& isNotNull (
					nativeClass)

				&& ! nativeClass.isInstance (
					nativeValue.get ())

			) {

				throw new RuntimeException (
					stringFormat (
						"Field %s.%s ",
						container.getClass ().getSimpleName (),
						path,
						"is %s, ",
						nativeClass.getSimpleName (),
						"attempted to write %s",
						nativeValue.get ().getClass ().getSimpleName ()));

			}

			// set property

			PropertyUtils.propertySetAuto (
				container,
				path,
				nativeValue.orNull ());

		}

	}

}
