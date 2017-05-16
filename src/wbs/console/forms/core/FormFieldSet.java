package wbs.console.forms.core;

import static wbs.utils.etc.OptionalUtils.optionalOrThrow;
import static wbs.utils.etc.TypeUtils.classEqualSafe;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.types.FormField;
import wbs.console.forms.types.FormItem;
import wbs.console.html.ScriptRef;

public
interface FormFieldSet <Container> {

	String name ();

	Iterable <FormItem <Container>> formItems ();

	Iterable <FormField <Container, ?, ?, ?>> formFields ();

	Optional <FormField <Container, ?, ?, ?>> formField (
			String name);

	default
	FormField <Container, ?, ?, ?> formFieldRequired (
			@NonNull String name) {

		return optionalOrThrow (
			formField (
				name),
			() -> new NoSuchElementException (
				stringFormat (
					"No such form field: %s",
					name)));

	}

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
