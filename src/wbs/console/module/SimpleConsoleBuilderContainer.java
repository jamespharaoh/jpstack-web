package wbs.console.module;

import wbs.framework.logging.TaskLogger;

public
interface SimpleConsoleBuilderContainer  {

	TaskLogger taskLogger ();

	String newBeanNamePrefix ();

	String existingBeanNamePrefix ();

}
