package wbs.platform.console.helper;

import wbs.framework.record.Record;
import wbs.platform.console.context.ConsoleContextStuff;

public
interface ConsoleHelperProvider<ConcreteType extends Record<ConcreteType>> {

	String objectName ();

	String getPathId (
			Integer objectId);

	String getDefaultContextPath (
			Record<?> object);

	String localPath (
			Record<?> object);

	boolean canView (
			Record<?> object);

	Record<?> lookupObject (
			ConsoleContextStuff contextStuff);

	Class<ConcreteType> objectClass ();

	String idKey ();

	void postProcess (
			ConsoleContextStuff contextStuff);

}
