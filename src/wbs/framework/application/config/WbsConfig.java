package wbs.framework.application.config;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.tools.DataFromXml;

@Accessors (fluent = true)
@Data
@DataClass ("wbs-config")
@PrototypeComponent ("wbsConfig")
public
class WbsConfig {

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

	// security

	@DataAttribute (
		required = true)
	String cryptorSeed;

	// test and development

	@DataChildren (
		childrenElement = "test-users",
		childElement = "test-user",
		valueAttribute = "name")
	List <String> testUsers =
		new ArrayList<> ();

	// implementation

	public static
	WbsConfig readFilename (
			@NonNull String filename) {

		DataFromXml dataFromXml =
			new DataFromXml ()

			.registerBuilderClasses (
				ImmutableList.of (
					WbsConfig.class,
					WbsConfigDatabase.class,
					WbsConfigEmail.class));

		WbsConfig wbsConfig =
			(WbsConfig)
			dataFromXml.readFilename (
				filename);

		return wbsConfig;

	}

}
