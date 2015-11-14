package wbs.framework.application.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import com.google.common.collect.ImmutableList;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.tools.DataFromXml;

@Accessors (fluent = true)
@Data
@DataClass ("wbs-config")
@PrototypeComponent ("wbsConfig")
public
class WbsConfig {

	// attributes

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
	String databaseName;

	@DataAttribute (
		required = true)
	String defaultSlice;

	@DataAttribute (
		required = true)
	String smtpHostname;

	@DataAttribute (
		required = true)
	Integer smtpPort;

	@DataAttribute (
		required = true)
	String smtpUsername;

	@DataAttribute (
		required = true)
	String smtpPassword;

	@DataAttribute (
		required = true)
	String defaultEmailAddress;

	@DataChildren (
		childrenElement = "test-users",
		childElement = "test-user",
		valueAttribute = "name")
	List<String> testUsers =
		new ArrayList<String> ();

	// implementation

	public static
	WbsConfig readFilename (
			String filename) {

		DataFromXml dataFromXml =
			new DataFromXml ()

			.registerBuilderClasses (
				ImmutableList.<Class<?>> of (
					WbsConfig.class));

		WbsConfig wbsConfig =
			(WbsConfig)
			dataFromXml.readFilename (
				filename);

		return wbsConfig;

	}

}
