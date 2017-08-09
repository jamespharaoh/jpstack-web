package wbs.console.forms.core;

import static wbs.utils.collection.IterableUtils.iterableFlatMap;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.TypeUtils.isInstanceOf;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormField;
import wbs.console.forms.types.FormItem;
import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataChildrenIndex;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@DataClass ("form-field-set")
public
class FormFieldSetImplementation <Container>
	implements FormFieldSet <Container> {

	// properties

	@DataAttribute
	@Getter @Setter
	String name;

	@DataAttribute
	@Getter @Setter
	Boolean fileUpload;

	@DataAttribute
	@Getter @Setter
	Class <Container> containerClass;

	@DataChildren
	@Getter
	List <FormItem <Container>> formItems =
		new ArrayList<> ();

	@Getter
	List <FormField <Container, ?, ?, ?>> formFields =
		new ArrayList<> ();

	@Getter
	Long columns = 0l;

	// state

	@DataChildrenIndex
	@Getter @Setter
	Map <String, FormField <Container, ?, ?, ?>> formFieldsByName =
		new HashMap<> ();

	// utility methods

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.copyOf (
			iterableFlatMap (
				formItems,
				FormItem::scriptRefs));

	}

	@Override
	public
	Set <HtmlLink> styles () {

		return ImmutableSet.copyOf (
			iterableFlatMap (
				formItems,
				FormItem::styles));

	}

	public
	FormFieldSet <Container> addFormItem (
			@NonNull FormItem <Container> formItem) {

		if (
			isInstanceOf (
				FormField.class,
				formItem)
		) {

			@SuppressWarnings ("unchecked")
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

	@Override
	public
	Optional <FormField <Container, ?, ?, ?>> formField (
			@NonNull String name) {

		return mapItemForKey (
			formFieldsByName,
			name);

	}

}
