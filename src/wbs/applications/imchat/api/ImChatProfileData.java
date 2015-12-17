package wbs.applications.imchat.api;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class ImChatProfileData {

	@DataAttribute
	String code;

	@DataAttribute
	String state;

	@DataAttribute
	String name;

	@DataAttribute
	String description;

	@DataAttribute
	String descriptionShort;

	// thumbnail image

	@DataAttribute
	String thumbnailImageLink;

	@DataAttribute
	Integer thumbnailImageWidth;

	@DataAttribute
	Integer thumbnailImageHeight;

	// miniature image

	@DataAttribute
	String miniatureImageLink;

	@DataAttribute
	Integer miniatureImageWidth;

	@DataAttribute
	Integer miniatureImageHeight;

}
