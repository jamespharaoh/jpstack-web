package wbs.console.priv;

import java.util.Collection;
import java.util.Map;

import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;

public
interface PrivChecker {

	boolean can (
		int privId);

	boolean can (
		GlobalId parentObjectId,
		String... privCodes);

	boolean can (
		Class<? extends Record<?>> parentObjectClass,
		int parentObjectId,
		String... privCodes);

	boolean can (
		Record<?> object,
		String... privCodes);

	boolean can (
		Map<Object,Collection<String>> map);

	boolean canGrant (
		int privId);

	void refresh ();

}
