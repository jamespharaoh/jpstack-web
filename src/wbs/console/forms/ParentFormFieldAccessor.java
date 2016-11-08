package wbs.console.forms;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectManager;

@Accessors (fluent = true)
@PrototypeComponent ("parentFormFieldAccessor")
public
class ParentFormFieldAccessor <
	Container extends Record <Container>,
	Parent extends Record <Parent>
>
	implements FormFieldAccessor <Container, Parent> {

	// singleton dependencies

	@SingletonDependency
	ObjectManager objectManager;

	// implementation

	@SuppressWarnings ("unchecked")
	@Override
	public
	Optional <Parent> read (
			Container container) {

		return Optional.of (
			(Parent)
			objectManager.getParentOrNull (
				container));

	}

	@Override
	public
	void write (
			@NonNull Container container,
			@NonNull Optional <Parent> nativeValue) {

		throw new UnsupportedOperationException ();

	}

}
