package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.instantToDate;

import java.util.Date;

import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("dateFormFieldNativeMapping")
public
class DateFormFieldNativeMapping
	implements FormFieldNativeMapping<Instant,Date> {

	// implementation

	@Override
	public
	Date genericToNative (
			Instant genericValue) {

		if (genericValue == null)
			return null;

		return instantToDate (
			genericValue);

	}

	@Override
	public
	Instant nativeToGeneric (
			Date nativeValue) {

		if (nativeValue == null)
			return null;

		return dateToInstant (
			nativeValue);

	}

}
