package wbs.console.forms.types;

import com.google.common.collect.Maps;

import lombok.NonNull;

import org.apache.commons.fileupload.FileItem;

import wbs.console.forms.core.FormFieldSubmissionImplementation;
import wbs.console.request.ConsoleRequestContext;

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

	public static
	FormFieldSubmission fromRequestContext (
			@NonNull ConsoleRequestContext requestContext) {

		return new FormFieldSubmissionImplementation ()

			.multipart (
				requestContext.isMultipart ())

			.parameters (
				requestContext.formData ())

			.fileItems (
				requestContext.isMultipart ()
					? Maps.uniqueIndex (
						requestContext.fileItems (),
						FileItem::getFieldName)
					: null);

	}

}
