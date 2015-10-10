package wbs.console.forms;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode
public
class FileUpload {

	String name;
	byte[] data;
	String contentType;

}
