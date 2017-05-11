package wbs.console.forms.core;

import static wbs.utils.etc.TypeUtils.classEqualSafe;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Set;

import lombok.NonNull;

import wbs.console.forms.types.FormField;
import wbs.console.forms.types.FormItem;
import wbs.console.html.ScriptRef;

public
interface FormFieldSet <Container> {

	Iterable <FormItem <Container>> formItems ();

	Iterable <FormField <Container, ?, ?, ?>> formFields ();

	FormField <Container, ?, ?, ?> formField (
			String name);

	Boolean fileUpload ();

	Long columns ();

	Set <ScriptRef> scriptRefs ();

	Class <Container> containerClass ();

	default <ContainerAgain>
	FormFieldSet <ContainerAgain> cast (
			@NonNull Class <?> containerClass) {

		if (
			classEqualSafe (
				containerClass,
				containerClass ())
		) {

			return genericCastUnchecked (
				this);

		} else {

			throw new ClassCastException (
				stringFormat (
					"Tried to cast FormFieldSet <%s> ",
					containerClass ().getSimpleName (),
					"to FormFieldSet <%s>",
					containerClass.getSimpleName ()));

		}

	}

}
