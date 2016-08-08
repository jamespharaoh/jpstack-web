package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.BeanLogic;

@Accessors (fluent = true)
@PrototypeComponent ("dereferenceFormFieldacessor")
public
class DereferenceFormFieldAccessor<Container,Native>
	implements FormFieldAccessor<Container,Native> {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	// properties

	@Getter @Setter
	String path;

	@Getter @Setter
	Class<?> nativeClass;

	// implementation

	@Override
	public
	Optional<Native> read (
			@NonNull Container container) {

		@SuppressWarnings ("unchecked")
		Native nativeValue =
			(Native)
			objectManager.dereference (
				container,
				path);

		return Optional.fromNullable (
			nativeValue);

	}

	@Override
	public
	void write (
			@NonNull Container container,
			@NonNull Optional<Native> nativeValue) {

		if (

			path.indexOf ('.') >= 0

			|| in (
				nativeValue,
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

		BeanLogic.setProperty (
			container,
			path,
			nativeValue.orNull ());

	}

}
