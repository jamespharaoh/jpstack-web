package wbs.platform.event.console;

import static wbs.framework.utils.etc.Misc.successResult;

import javax.inject.Inject;

import lombok.NonNull;

import com.google.common.base.Optional;

import fj.data.Either;

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
	Either<Optional<EventRec>,String> interfaceToGeneric (
			@NonNull EventRec container,
			@NonNull Optional<String> interfaceValue) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull EventRec container,
			@NonNull Optional<EventRec> genericValue) {

		return successResult (
			Optional.of (
				eventConsoleLogic.eventText (
					genericValue.get ())));

	}

}
