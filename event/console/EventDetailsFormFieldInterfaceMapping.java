package wbs.platform.event.console;

import static wbs.framework.utils.etc.Misc.successResult;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.FormFieldInterfaceMapping;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.event.model.EventRec;

import fj.data.Either;

@PrototypeComponent ("eventDetailsFormFieldInterfaceMapping")
public
class EventDetailsFormFieldInterfaceMapping
	implements FormFieldInterfaceMapping <EventRec, EventRec, String> {

	// dependencies

	@SingletonDependency
	EventConsoleLogic eventConsoleLogic;

	// implementation

	@Override
	public
	Either <Optional <EventRec>, String> interfaceToGeneric (
			@NonNull EventRec container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull EventRec container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <EventRec> genericValue) {

		return successResult (
			Optional.of (
				eventConsoleLogic.eventText (
					genericValue.get ())));

	}

}
