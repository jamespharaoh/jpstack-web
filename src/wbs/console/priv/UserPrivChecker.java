package wbs.console.priv;

import java.util.Collection;
import java.util.Map;

import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;

public
interface UserPrivChecker {

	boolean canRecursive (
		int privId);

	boolean canRecursive (
		GlobalId parentObjectId,
		String... privCodes);

	boolean canRecursive (
		Class<? extends Record<?>> parentObjectClass,
		int parentObjectId,
		String... privCodes);

	boolean canRecursive (
		Record<?> object,
		String... privCodes);

	boolean canSimple (
		GlobalId parentObjectId,
		String... privCodes);

	boolean canSimple (
		Record<?> parentObject,
		String... privCodes);

	boolean canRecursive (
		Map<Object,Collection<String>> map);

	boolean canGrant (
		int privId);

	void refresh ();

}
