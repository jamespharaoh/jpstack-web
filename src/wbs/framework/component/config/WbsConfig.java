package wbs.framework.component.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.logging.DefaultLogContext;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@Data
@DataClass ("wbs-config")
@PrototypeComponent ("wbsConfig")
public
class WbsConfig {

	private final static
	LogContext logContext =
		DefaultLogContext.forClass (
			WbsConfig.class);

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

	// implementation

	public static
	WbsConfig readFilename (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String filename) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"readFilename");

		DataFromXml dataFromXml =
			new DataFromXmlBuilder ()

			.registerBuilderClasses (
				ImmutableList.of (
					WbsConfig.class,
					WbsConfigDatabase.class,
					WbsConfigEmail.class,
					WbsConfigProcessApi.class))

			.build ();

		WbsConfig wbsConfig =
			(WbsConfig)
			dataFromXml.readFilename (
				taskLogger,
				filename);

		return wbsConfig;

	}

}
