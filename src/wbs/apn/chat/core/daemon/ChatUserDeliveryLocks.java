package wbs.apn.chat.core.daemon;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.misc.SymbolicLock;

@SingletonComponent ("chatUserDeliveryLocks")
public
class ChatUserDeliveryLocks
	implements ComponentFactory <SymbolicLock <Integer>> {

	// singleton dependences

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	SymbolicLock <Integer> makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return new SymbolicLock<> ();

		}

	}

}
