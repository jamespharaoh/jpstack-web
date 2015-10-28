package wbs.console.forms;

import javax.inject.Inject;

import lombok.NonNull;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;

@Accessors (fluent = true)
@PrototypeComponent ("parentFormFieldAccessor")
public
class ParentFormFieldAccessor<
	Container extends Record<Container>,
	Parent extends Record<Parent>
>
	implements FormFieldAccessor<Container,Parent> {

	// dependencies

	@Inject
	ObjectManager objectManager;

	// implementation

	@SuppressWarnings ("unchecked")
	@Override
	public
	Optional<Parent> read (
			Container container) {

		return Optional.of (
			(Parent)
			objectManager.getParent (
				container));

	}

	@Override
	public
	void write (
			@NonNull Container container,
			@NonNull Optional<Parent> nativeValue) {

		throw new RuntimeException ();

	}

}
