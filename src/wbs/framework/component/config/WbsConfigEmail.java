package wbs.framework.component.config;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("email")
@PrototypeComponent ("wbsConfigEmail")
public
class WbsConfigEmail {

	@DataAttribute (
		required = true)
	String smtpHostname;

	@DataAttribute (
		required = true)
	String smtpPort;

	@DataAttribute (
		required = true)
	String smtpUsername;

	@DataAttribute (
		required = true)
	String smtpPassword;

	@DataAttribute (
		required = true)
	String defaultEnvelopeFrom;

	@DataAttribute (
		required = true)
	String defaultFromAddress;

	@DataAttribute (
		required = true)
	String defaultFromName;

	@DataAttribute (
		required = true)
	String defaultReplyToAddress;

	@DataAttribute (
		required = true)
	String developerAddress;

}
