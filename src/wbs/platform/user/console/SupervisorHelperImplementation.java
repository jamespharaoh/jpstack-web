package wbs.platform.user.console;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.string.StringUtils.stringSplitComma;

import java.util.Collections;
import java.util.List;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.supervisor.SupervisorHelper;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.scaffold.model.SliceRec;

@SingletonComponent ("supervisorHelper")
public
class SupervisorHelperImplementation
	implements SupervisorHelper {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// implementation

	@Override
	public
	List <String> getSupervisorConfigNames () {

		SliceRec slice =
			userConsoleLogic.sliceRequired ();

		List <String> supervisorConfigNames =
			ifThenElse (
				isNotNull (
					slice.getSupervisorConfigNames ()),
				() -> stringSplitComma (
					slice.getSupervisorConfigNames ()),
				() -> Collections.emptyList ());

		return supervisorConfigNames;

	}

}
