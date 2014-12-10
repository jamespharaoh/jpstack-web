package wbs.platform.console.forms;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.helper.ConsoleObjectManager;

@Accessors (fluent = true)
@PrototypeComponent ("delegateFormFieldacessor")
public
class DelegateFormFieldAccessor<PrincipalContainer,DelegateContainer,Native>
	implements FormFieldAccessor<PrincipalContainer,Native> {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	// properties

	@Getter @Setter
	String path;

	@Getter @Setter
	FormFieldAccessor<DelegateContainer,Native> delegateFormFieldAccessor;

	// implementation

	public
	Native read (
			PrincipalContainer principalContainer) {

		@SuppressWarnings ("unchecked")
		DelegateContainer delegateContainer =
			(DelegateContainer)
			objectManager.dereference (
				principalContainer,
				path);

		if (delegateContainer == null)
			return null;

		return delegateFormFieldAccessor.read (
			delegateContainer);

	}

	public
	void write (
			PrincipalContainer principalContainer,
			Native nativeValue) {

		@SuppressWarnings ("unchecked")
		DelegateContainer delegateContainer =
			(DelegateContainer)
			objectManager.dereference (
				principalContainer,
				path);

		if (delegateContainer == null)
			throw new RuntimeException ();

		delegateFormFieldAccessor.write (
			delegateContainer,
			nativeValue);

	}

}
