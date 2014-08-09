package wbs.platform.rpc.core;

import static wbs.framework.utils.etc.Misc.stringFormat;
import wbs.framework.utils.cal.CalDate;

public
class RpcPrimitive
	extends RpcElem {

	private final
	Object value;

	public
	RpcPrimitive (
			String name,
			Object newValue) {

		super (
			name,
			typeOfPrimitive (
				newValue));

		value =
			newValue;

	}

	private static
	RpcType typeOfPrimitive (
			Object primitive) {

		if (primitive instanceof Integer || primitive instanceof Long)
			return RpcType.rInteger;

		if (primitive instanceof String)
			return RpcType.rString;

		if (primitive instanceof Boolean)
			return RpcType.rBoolean;

		if (primitive instanceof Double || primitive instanceof Float)
			return RpcType.rFloat;

		if (primitive instanceof byte[])
			return RpcType.rBinary;

		if (primitive instanceof CalDate)
			return RpcType.rDate;

		throw new RuntimeException (
			stringFormat (
				"Don't know what to do with %s",
				primitive.getClass ()));

	}

	@Override
	public
	Object getValue () {
		return value;
	}

	@Override
	public
	Object getNative () {
		return value;
	}

}
