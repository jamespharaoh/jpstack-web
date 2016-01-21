package wbs.console.forms;

import org.joda.time.DateTimeZone;

public
interface FormFieldPreferencesProvider {

	Integer hourOffset ();

	DateTimeZone timeZone ();

}
