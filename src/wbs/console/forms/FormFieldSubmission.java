package wbs.console.forms;

import org.apache.commons.fileupload.FileItem;

public
interface FormFieldSubmission {

	Boolean multipart ();

	boolean hasFileItem (
			String name);

	FileItem fileItem (
			String name);

	boolean hasParameter (
			String name);

	String parameter (
			String name);

}
