package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.misc.IntervalFormatter;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("secondsFormFieldInterfaceMapping")
public
class SecondsFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,Integer,String> {

	// dependencies

	@Inject
	IntervalFormatter intervalFormatter;

	// properties

	@Getter @Setter
	String label;

	// implementation

	@Override
	public
	Integer interfaceToGeneric (
			Container container,
			String interfaceValue,
			List<String> errors) {

		if (interfaceValue == null)
			return null;

		if (interfaceValue.isEmpty ())
			return null;

		Integer genericValue =
			intervalFormatter.processIntervalStringSeconds (
				interfaceValue);

		if (genericValue == null) {

			errors.add (
				stringFormat (
					"Please enter a valid interval for '%s'",
					label));

			return null;

		}

		return genericValue;

	}

	@Override
	public
	String genericToInterface (
			Container container,
			Integer genericValue) {

		if (genericValue == null)
			return null;

		return intervalFormatter.createIntervalStringSeconds (
			genericValue);

	}

}
