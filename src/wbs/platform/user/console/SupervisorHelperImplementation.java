package wbs.platform.user.console;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.supervisor.SupervisorHelper;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.scaffold.model.SliceRec;

@SingletonComponent ("supervisorHelper")
public
class SupervisorHelperImplementation
	implements SupervisorHelper {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// implementation

	@Override
	public
	List<String> getSupervisorConfigNames () {

		SliceRec slice =
			userConsoleLogic.sliceRequired ();

		List<String> supervisorConfigNames =
			slice.getSupervisorConfigNames () != null
				? ImmutableList.<String>copyOf (
					slice.getSupervisorConfigNames ().split (","))
				: Collections.<String>emptyList ();

		return supervisorConfigNames;

	}

}
