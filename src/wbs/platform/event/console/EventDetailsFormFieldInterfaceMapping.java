package wbs.platform.event.console;

import java.util.List;

import javax.inject.Inject;

import wbs.console.forms.FormFieldInterfaceMapping;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.event.model.EventRec;

@PrototypeComponent ("eventDetailsFormFieldInterfaceMapping")
public
class EventDetailsFormFieldInterfaceMapping
	implements FormFieldInterfaceMapping<EventRec,EventRec,String> {

	// dependencies

	@Inject
	EventConsoleLogic eventConsoleLogic;

	// implementation

	@Override
	public
	EventRec interfaceToGeneric (
			EventRec container,
			String interfaceValue,
			List<String> errors) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	String genericToInterface (
			EventRec container,
			EventRec genericValue) {

		return eventConsoleLogic.eventText (
			genericValue);

	}

}
