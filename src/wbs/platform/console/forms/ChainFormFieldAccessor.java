package wbs.platform.console.forms;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("chainFormFieldAccessor")
@SuppressWarnings ({ "unchecked", "rawtypes" })
public
class ChainFormFieldAccessor
	implements FormFieldAccessor {

	@Getter @Setter
	List<FormFieldAccessor> accessors;

	@Override
	public
	Object read (
			Object container) {

		Object currentObject =
			container;

		for (FormFieldAccessor accessor
				: accessors) {

			currentObject =
				accessor.read (
					currentObject);

		}

		return currentObject;

	}

	@Override
	public
	void write (
			Object container,
			Object nativeValue) {

		List<FormFieldAccessor> accessorsAllButLast =
			accessors.subList (
				0,
				accessors.size () - 1);

		Object currentObject =
			container;

		for (FormFieldAccessor accessor
				: accessorsAllButLast) {

			currentObject =
				accessor.read (
					currentObject);

		}

		FormFieldAccessor lastAccessor =
			accessors.get (
				accessors.size () - 1);

		lastAccessor.write (
			currentObject,
			nativeValue);

	}

}
