package wbs.framework.schema.helper;

import java.lang.reflect.Field;

public
interface SchemaNamesHelper {

	String tableName (
			Class<?> entityClass);

	String columnName (
			Field field);

	String idColumnName (
			Field field);

	String idColumnName (
			Class<?> objectClass);

	String idSequenceName (
			Class<?> objectClass);

	String objectName (
			Class<?> objectClass);

}
