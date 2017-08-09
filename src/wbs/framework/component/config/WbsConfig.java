package wbs.framework.component.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.logging.LogContext;

@Accessors (fluent = true)
@Data
@DataClass ("wbs-config")
@PrototypeComponent ("wbsConfig")
public
class WbsConfig {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// general information

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		required = true)
	String consoleTitle;

	@DataAttribute (
		required = true)
	String apiUrl;

	@DataAttribute (
		required = true)
	String homeDirectory;

	@DataAttribute (
		required = true)
	String httpUserAgent;

	@DataAttribute (
		required = true)
	String defaultSlice;

	@DataAttribute
	String defaultTimezone;

	// email settings

	@DataChild (
		required = true)
	WbsConfigDatabase database;

	@DataChild (
		required = true)
	WbsConfigEmail email;

	@DataChild
	WbsConfigProcessApi processApi;

	@DataChild
	WbsConfigConsoleServer consoleServer;

	// security

	@DataAttribute (
		required = true)
	String cryptorSeed;

	// runtime controls

	@DataChildren (
		childrenElement = "runtime-settings",
		childElement = "runtime-setting",
		keyAttribute = "name",
		valueAttribute = "value")
	Map <String, String> runtimeSettings;

	// test and development

	@DataChildren (
		childrenElement = "test-users",
		childElement = "test-user",
		valueAttribute = "name")
	List <String> testUsers =
		new ArrayList<> ();

	// unknown elements

	@DataChildren (
		direct = true)
	List <Object> otherElements =
		new ArrayList<> ();

}
