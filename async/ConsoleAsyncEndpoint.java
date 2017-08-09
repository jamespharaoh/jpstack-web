package wbs.console.async;

import com.google.common.base.Optional;

import wbs.framework.logging.TaskLogger;

public
interface ConsoleAsyncEndpoint <Request> {

	String endpointPath ();

	Class <Request> requestClass ();

	Optional <?> message (
			TaskLogger parentTaskLogger,
			ConsoleAsyncConnectionHandle connectionHandle,
			Long userId,
			Request request);

}
