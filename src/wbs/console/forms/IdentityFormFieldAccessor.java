package wbs.console.forms;

import static wbs.framework.utils.etc.LogicUtils.referenceNotEqualWithClass;

import javax.inject.Inject;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("identityFormFieldAccessor")
public
class IdentityFormFieldAccessor <Container>
	implements FormFieldAccessor <Container, Container> {

	// dependencies

	@Inject
	ConsoleObjectManager consoleObjectManager;

	// properties

	@Getter @Setter
	Class <Container> containerClass;

	// implementation

	@Override
	public
	Optional <Container> read (
			@NonNull Container container) {

		return Optional.of (
			container);

	}

	@Override
	public
	void write (
			@NonNull Container container,
			@NonNull Optional <Container> nativeValue) {

		if (
			referenceNotEqualWithClass (
				containerClass,
				container,
				nativeValue.get ())
		) {

			throw new RuntimeException ();

		}

	}

}
