package wbs.platform.user.console;

import javax.inject.Inject;

import org.joda.time.DateTimeZone;

import wbs.console.forms.FormFieldPreferencesProvider;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.user.model.UserRec;

@SingletonComponent ("userFormFieldPreferencesProvider")
public
class UserFormFieldPreferencesProvider
	implements FormFieldPreferencesProvider {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserConsoleHelper userHelper;

	// implementation

	@Override
	public
	Integer hourOffset () {

		UserRec user =
			getUser ();

		return (int) (long)
			user.getSlice ().getDefaultHourOffset ();

	}

	@Override
	public
	DateTimeZone timeZone () {

		return DateTimeZone.forID (
			"Europe/London");

	}

	private
	UserRec getUser () {

		return userHelper.find (
			requestContext.userId ());

	}

}
