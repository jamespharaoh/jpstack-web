package wbs.integrations.fonix.logic;

import lombok.NonNull;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import wbs.framework.component.annotations.SingletonComponent;

@SingletonComponent ("fonixLogicImplementation")
public
class FonixLogicImplementation
	implements FonixLogic {

	@Override
	public
	Instant stringToInstant (
			@NonNull String string) {

		DateTime dateTime =
			fonixDateTimeFormat.parseDateTime (
				string);

		return dateTime.toInstant ();

	}

	public final static
	DateTimeFormatter fonixDateTimeFormat =
		DateTimeFormat.forPattern (
			"YYYYMMddHHmmss");

}
