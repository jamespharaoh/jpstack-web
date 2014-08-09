package wbs.platform.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

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
	Class<? extends Native> nativeClass;

	// implementation

	Pair<Object,String> resolveEffectiveContainerAndName (
			Container container) {

		Object effectiveContainer =
			container;

		String[] nameParts =
			name.split ("\\.");

		for (
			int index = 0;
			index < nameParts.length - 1;
			index ++
		) {

			String namePart =
				nameParts [index];

			effectiveContainer =
				BeanLogic.getProperty (
					effectiveContainer,
					namePart);

		}

		return Pair.of (
			effectiveContainer,
			nameParts [nameParts.length - 1]);

	}

	@Override
	public
	Native read (
			Container container) {

		// resolve effective container

		Pair<Object,String> effectiveContainerAndName =
			resolveEffectiveContainerAndName (
				container);

		Object effectiveContainer =
			effectiveContainerAndName.getLeft ();

		String effectiveName =
			effectiveContainerAndName.getRight ();

		// special case for null container

		if (effectiveContainer == null)
			return null;

		// get native object

		Object nativeObject =
			BeanLogic.getProperty (
				effectiveContainer,
				effectiveName);

		// special case for null

		if (nativeObject == null)
			return null;

		// sanity check native type

		if (! nativeClass.isInstance (
				nativeObject)) {

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

		// resolve effective container

		Pair<Object,String> effectiveContainerAndName =
			resolveEffectiveContainerAndName (
				container);

		Object effectiveContainer =
			effectiveContainerAndName.getLeft ();

		String effectiveName =
			effectiveContainerAndName.getRight ();

		// set property

		BeanLogic.setProperty (
			effectiveContainer,
			effectiveName,
			nativeValue);

	}

}
