package wbs.platform.daemon;

import java.util.List;

import wbs.framework.logging.TaskLogger;

public
interface ObjectDaemon <IdType> {

	String backgroundProcessName ();

	List <IdType> findObjectIds (
			TaskLogger parentTaskLogger);

	void processObject (
			TaskLogger parentTaskLogger,
			IdType id);

}
