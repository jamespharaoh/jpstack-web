package wbs.platform.event.console;

import java.util.List;

import javax.inject.Inject;

import lombok.NonNull;

import com.google.common.base.Optional;

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
	Optional<EventRec> interfaceToGeneric (
			@NonNull EventRec container,
			@NonNull Optional<String> interfaceValue,
			@NonNull List<String> errors) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Optional<String> genericToInterface (
			@NonNull EventRec container,
			@NonNull Optional<EventRec> genericValue) {

		return Optional.of (
			eventConsoleLogic.eventText (
				genericValue.get ()));

	}

}
