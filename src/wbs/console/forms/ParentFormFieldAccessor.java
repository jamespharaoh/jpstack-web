package wbs.console.forms;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

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

	@Override
	public
	Optional <Parent> read (
			@NonNull Container container) {

		return genericCastUnchecked (
			objectManager.getParentRequired (
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
