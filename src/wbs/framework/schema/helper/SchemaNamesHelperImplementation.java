package wbs.framework.schema.helper;

import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.StringUtils.camelToUnderscore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("schemaNamesHelper")
public
class SchemaNamesHelperImplementation
	implements SchemaNamesHelper {

	@Override
	public
	String tableName (
			Class<?> entityClass) {

		return camelToUnderscore (
			objectName (
				entityClass));

	}

	@Override
	public
	String columnName (
			String fieldName) {

		return camelToUnderscore (
			fieldName);

	}

	@Override
	public
	String idColumnName (
			String fieldName) {

		return stringFormat (
			"%s_id",
			camelToUnderscore (
				fieldName));

	}

	@Override
	public
	String idColumnName (
			Class<?> objectClass) {

		return stringFormat (
			"%s_id",
			camelToUnderscore (
				objectName (objectClass)));

	}

	@Override
	public
	String idSequenceName (
			Class<?> objectClass) {

		return stringFormat (
			"%s_id_seq",
			camelToUnderscore (
				objectName (objectClass)));

	}

	@Override
	public
	String objectName (
			Class<?> objectClass) {

		String className =
			objectClass.getSimpleName ();

		Matcher matcher =
			entityNamePattern.matcher (className);

		if (! matcher.matches ())
			throw new IllegalArgumentException (className);

		return matcher.group (1);

	}

	public final static
	Pattern entityNamePattern =
		Pattern.compile (
			"(.+)(Rec|View)");

}
