package wbs.platform.user.console;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.supervisor.SupervisorHelper;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

import com.google.common.collect.ImmutableList;

@SingletonComponent ("supervisorHelper")
public
class SupervisorHelperImplementation
	implements SupervisorHelper {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserObjectHelper userHelper;

	// implementation

	@Override
	public
	List<String> getSupervisorConfigNames () {

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		SliceRec slice =
			myUser.getSlice ();

		List<String> supervisorConfigNames =
			slice.getSupervisorConfigNames () != null
				? ImmutableList.<String>copyOf (
					slice.getSupervisorConfigNames ().split (","))
				: Collections.<String>emptyList ();

		return supervisorConfigNames;

	}

}
