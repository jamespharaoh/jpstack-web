package wbs.clients.apn.chat.user.image.console;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.forms.FileUpload;

@Accessors (fluent = true)
@Data
public
class ChatUserImageUploadForm {

	FileUpload upload;
	Boolean setAsPrimary;

}
