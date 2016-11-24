package wbs.integrations.oxygen8.api;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataContent;

@Accessors (fluent = true)
@Data
@DataClass ("Message")
public
class Oxygen8RouteInMmsNewRequest {

	@DataAttribute (
		required = true)
	String type;

	@DataContent (
		name = "Source",
		required = true)
	String source;

	@DataContent (
		name = "Destination",
		required = true)
	String destination;

	@DataContent (
		name = "O8reference",
		required = true)
	String oxygenateReference;

	@DataContent (
		name = "MessageID",
		required = true)
	String messageId;

	@DataContent (
		name = "Subject",
		required = true)
	String subject;

	@DataChildren (
		childrenElement = "Attachments",
		childElement = "Attachment")
	List <Attachment> attachments;

	@Accessors (fluent = true)
	@Data
	@DataClass ("Attachment")
	public static
	class Attachment {

		@DataAttribute (
			name = "FileName",
			required = true)
		String fileName;

		@DataAttribute (
			name = "ContentType",
			required = true)
		String contentType;

		@DataAttribute (
			name = "Ordinal",
			required = true)
		Long ordinal;

		@DataAttribute (
			name = "Encoding",
			required = true)
		String encoding;

		@DataContent
		String content;

	}

}
