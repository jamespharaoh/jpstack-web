package wbs.console.forms;

import javax.inject.Inject;

import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;

@Accessors (fluent = true)
@PrototypeComponent ("parentFormFieldAccessor")
public
class ParentFormFieldAccessor<
	Container extends Record<Container>
>
	implements FormFieldAccessor<Container,Record<?>> {

	// dependencies

	@Inject
	ObjectManager objectManager;

	// implementation

	@Override
	public
	Record<?> read (
			Container container) {

		return objectManager.getParent (
			container);

	}

	@Override
	public
	void write (
			Container container,
			Record<?> nativeValue) {

		throw new RuntimeException ();

	}

}
