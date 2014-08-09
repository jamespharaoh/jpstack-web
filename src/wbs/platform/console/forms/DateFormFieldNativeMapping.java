package wbs.platform.console.forms;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.instantToDate;

import java.util.Date;

import javax.inject.Inject;

import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.text.model.TextObjectHelper;

@Accessors (fluent = true)
@PrototypeComponent ("dateFormFieldNativeMapping")
public
class DateFormFieldNativeMapping
	implements FormFieldNativeMapping<Instant,Date> {

	// dependencies

	@Inject
	TextObjectHelper textHelper;

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
