package wbs.platform.console.helper;

import wbs.framework.record.Record;

public
interface ConsoleObjectManagerMethods {

	ConsoleHelper<?> getConsoleObjectHelper (
			Record<?> dataObject);

	ConsoleHelper<?> getConsoleObjectHelper (
			Class<?> objectClass);

	String tdForObject (
			Record<?> object,
			Record<?> assumedRoot,
			boolean mini,
			boolean link);

	String tdForObjectMiniLink (
			Record<?> object);

	String htmlForObject (
			Record<?> object,
			Record<?> assumedRoot,
			boolean mini);

	boolean canView (
			Record<?> object);

	String contextName (
			Record<?> object);

	String contextLink (
			Record<?> object);

	String localLink (
			Record<?> object);

	String objectToSimpleHtml (
			Object object,
			Record<?> assumedRoot,
			boolean mini);

}