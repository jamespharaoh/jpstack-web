package wbs.console.forms;

import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.TypeUtils.isInstanceOf;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.html.ScriptRef;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataChildrenIndex;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@SuppressWarnings ({ "rawtypes", "unchecked" })
@DataClass ("form-field-set")
public
class FormFieldSet <Container> {

	// properties

	@DataAttribute
	@Getter @Setter
	String name;

	@DataAttribute
	@Getter @Setter
	Boolean fileUpload;

	@DataChildren
	@Getter
	List <FormItem> formItems =
		new ArrayList<> ();

	@Getter
	List <FormField> formFields =
		new ArrayList<> ();

	@Getter
	Long columns = 0l;

	// state

	@DataChildrenIndex
	@Getter @Setter
	Map <String, FormField> formFieldsByName =
		new HashMap<> ();

	// utility methods

	public
	Set <ScriptRef> scriptRefs () {

		// TODO build this as we go

		Set <ScriptRef> scriptRefs =
			new LinkedHashSet<> ();

		for (
			FormItem formItem
				: formItems
		) {

			scriptRefs.addAll (
				formItem.scriptRefs ());

		}

		return scriptRefs;

	}

	public
	FormFieldSet addFormItem (
			@NonNull FormItem <Container> formItem) {

		if (
			isInstanceOf (
				FormField.class,
				formItem)
		) {

			FormField <Container, ?, ?, ?> formField =
				(FormField <Container, ?, ?, ?>)
				formItem;

			if (
				contains (
					formFieldsByName,
					formField.name ())
			) {

				throw new RuntimeException (
					stringFormat (
						"Duplicated field name: %s.%s",
						name (),
						formField.name ()));

			}

			formItems.add (
				formItem);

			formFields.add (
				formField);

			formFieldsByName.put (
				formField.name (),
				formField);

			columns ++;

		} else if (
			isInstanceOf (
				FormFieldGroup.class,
				formItem)
		) {

			formItems.add (
				formItem);

			formItem.children ().forEach (
				this::addFormItem);

		} else {

			formItems.add (
				formItem);

		}

		return this;

	}

	public
	FormField formField (
			@NonNull String name) {

		return formFieldsByName.get (
			name);

	}

	public static <Container>
	FormFieldSet <Container> unsafeCast (
			@NonNull FormFieldSet <?> fields) {

		FormFieldSet <Container> fieldsTemp =
			(FormFieldSet <Container>)
			fields;

		return fieldsTemp;

	}

}
