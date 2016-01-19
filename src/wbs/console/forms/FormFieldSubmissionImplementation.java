package wbs.console.forms;

import java.util.Map;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.apache.commons.fileupload.FileItem;

@Accessors (fluent = true)
@Data
public
class FormFieldSubmissionImplementation
	implements FormFieldSubmission {

	Boolean multipart;

	Map<String,FileItem> fileItems;

	Map<String,String> parameters;

	@Override
	public
	boolean hasFileItem (
			@NonNull String name) {

		return fileItems.containsKey (
			name);

	}

	@Override
	public
	FileItem fileItem (
			@NonNull String name) {

		return fileItems.get (
			name);

	}

	@Override
	public
	boolean hasParameter (
			@NonNull String name) {

		return parameters.containsKey (
			name);

	}

	@Override
	public
	String parameter (
			@NonNull String name) {

		return parameters.get (
			name);

	}

}
