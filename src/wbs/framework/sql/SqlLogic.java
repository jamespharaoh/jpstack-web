package wbs.framework.sql;

public
interface SqlLogic {

	String quoteIdentifier (
			String identifier);

	String quoteString (
			String value);

}
