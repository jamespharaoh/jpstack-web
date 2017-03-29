package wbs.console.forms;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.manager.ConsoleObjectManager;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("identityFormFieldAccessor")
public
class IdentityFormFieldAccessor <Container>
	implements FormFieldAccessor <Container, Container> {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager consoleObjectManager;

	// properties

	@Getter @Setter
	Class <Container> containerClass;

	// implementation

	@Override
	public
	Optional <Container> read (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container) {

		return Optional.of (
			container);

	}

	@Override
	public
	void write (
			@NonNull TaskLogger parentTaskLogger,
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
