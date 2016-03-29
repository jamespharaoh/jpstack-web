package wbs.platform.user.console;

import static wbs.framework.utils.etc.Misc.ifNull;

import javax.inject.Inject;

import org.joda.time.DateTimeZone;

import wbs.console.forms.FormFieldPreferencesProvider;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;
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

	@Inject
	WbsConfig wbsConfig;

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

		UserRec user =
			getUser ();

		return DateTimeZone.forID (
			ifNull (
				user.getDefaultTimezone (),
				user.getSlice ().getDefaultTimezone (),
				wbsConfig.defaultTimezone (),
				DateTimeZone.getDefault ().getID ()));

	}

	private
	UserRec getUser () {

		return userHelper.find (
			requestContext.userId ());

	}

}
