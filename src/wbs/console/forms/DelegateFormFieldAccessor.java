package wbs.console.forms;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@Accessors (fluent = true)
@PrototypeComponent ("delegateFormFieldacessor")
public
class DelegateFormFieldAccessor <PrincipalContainer, DelegateContainer, Native>
	implements FormFieldAccessor <PrincipalContainer, Native> {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// properties

	@Getter @Setter
	String path;

	@Getter @Setter
	FormFieldAccessor <DelegateContainer, Native> delegateFormFieldAccessor;

	// implementation

	@Override
	public
	Optional <Native> read (
			PrincipalContainer principalContainer) {

		@SuppressWarnings ("unchecked")
		DelegateContainer delegateContainer =
			(DelegateContainer)
			objectManager.dereference (
				principalContainer,
				path);

		if (delegateContainer == null) {

			return Optional.<Native>absent ();

		}

		return delegateFormFieldAccessor.read (
			delegateContainer);

	}

	@Override
	public
	void write (
			@NonNull PrincipalContainer principalContainer,
			@NonNull Optional<Native> nativeValue) {

		@SuppressWarnings ("unchecked")
		DelegateContainer delegateContainer =
			(DelegateContainer)
			objectManager.dereference (
				principalContainer,
				path);

		if (delegateContainer == null) {
			throw new RuntimeException ();
		}

		delegateFormFieldAccessor.write (
			delegateContainer,
			nativeValue);

	}

}
