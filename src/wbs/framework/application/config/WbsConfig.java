package wbs.framework.application.config;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.tools.DataFromXml;

import com.google.common.collect.ImmutableList;

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
