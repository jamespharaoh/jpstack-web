package wbs.framework.schema.helper;


public
interface SchemaNamesHelper {

	String tableName (
			Class<?> entityClass);

	String columnName (
			String fieldName);

	String idColumnName (
			String fieldName);

	String idColumnName (
			Class<?> objectClass);

	String idSequenceName (
			Class<?> objectClass);

	String objectName (
			Class<?> objectClass);

}
