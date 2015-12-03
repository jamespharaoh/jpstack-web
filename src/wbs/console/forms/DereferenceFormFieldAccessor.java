package wbs.console.forms;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.application.annotations.PrototypeComponent;

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
			@NonNull Container principalContainer,
			@NonNull Optional<Native> nativeValue) {

		throw new RuntimeException ();

	}

}
