package wbs.framework.component.config;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("database")
@PrototypeComponent ("wbsConfigDatabase")
public
class WbsConfigDatabase {

	@DataAttribute (
		required = true)
	String hostname;

	@DataAttribute (
		required = false)
	Long port;

	@DataAttribute (
		required = true)
	String username;

	@DataAttribute (
		required = true)
	String password;

	@DataAttribute (
		required = true)
	String databaseName;

	@DataAttribute (
		required = false)
	Boolean formatSql = true;

	@DataAttribute (
		required = false)
	Boolean showSql = false;

}
