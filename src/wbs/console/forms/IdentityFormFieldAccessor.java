package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.referenceNotEqual;

import javax.inject.Inject;

import lombok.experimental.Accessors;

import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("identityFormFieldAccessor")
public
class IdentityFormFieldAccessor<Container>
	implements FormFieldAccessor<Container,Container> {

	// properties

	@Inject
	ConsoleObjectManager consoleObjectManager;

	// implementation

	@Override
	public
	Container read (
			Container container) {

		return container;

	}

	@Override
	public
	void write (
			Container container,
			Container nativeValue) {

		if (
			referenceNotEqual (
				container,
				nativeValue)
		) {

			throw new RuntimeException ();

		}

	}

}
