package wbs.platform.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.BeanLogic;

@Accessors (fluent = true)
@PrototypeComponent ("simpleFormFieldAccessor")
public
class SimpleFormFieldAccessor<Container,Native>
	implements FormFieldAccessor<Container,Native> {

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	Boolean dynamic;
	
	@Getter @Setter
	Class<? extends Native> nativeClass;

	// implementation

	@Override
	public
	Native read (
		Container container) {

		// get native object
		Object nativeObject;
		if (!dynamic) {
			 nativeObject =
				BeanLogic.getProperty (
					container,
					name);
		}
		else {
			// TODO
			nativeObject = 
					null;
		}

		// special case for null

		if (nativeObject == null)
			return null;

		// sanity check native type

		if (
			! nativeClass.isInstance (
				nativeObject)
		) {

			throw new RuntimeException (
				stringFormat (
					"Field %s is %s, not %s",
					name,
					nativeObject.getClass ().getSimpleName (),
					nativeClass.getSimpleName ()));

		}

		// cast and return

		return nativeClass.cast (
			nativeObject);

	}

	@Override
	public
	void write (
			Container container,
			Native nativeValue) {

		// sanity check native type

		if (

			nativeValue != null

			&& ! nativeClass.isInstance (
				nativeValue)

		) {

			throw new RuntimeException (
				stringFormat (
					"Field %s is %s, not %s",
					name,
					nativeClass.getSimpleName (),
					nativeValue.getClass ().getSimpleName ()));

		}

		// set property

		BeanLogic.setProperty (
			container,
			name,
			nativeValue);

	}

}
