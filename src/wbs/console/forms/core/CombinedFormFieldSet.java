package wbs.console.forms.core;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.OptionalUtils.optionalFromJava;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;

import lombok.NonNull;

import wbs.console.forms.types.FormField;
import wbs.console.forms.types.FormItem;
import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;

public
class CombinedFormFieldSet <Container>
	implements FormFieldSet <Container> {

	// state

	private final
	String name;

	private final
	Class <Container> containerClass;

	private final
	List <FormFieldSet <Container>> members;

	// constructors

	public
	CombinedFormFieldSet (
			@NonNull String name,
			@NonNull Class <Container> containerClass,
			@NonNull Iterable <FormFieldSet <Container>> members) {

		this.name =
			name;

		this.containerClass =
			containerClass;

		this.members =
			ImmutableList.copyOf (
				members);

		// check for duplicates

		Set <String> fieldNames =
			new HashSet<> ();

		Set <String> duplicateFieldNames =
			new HashSet<> ();

		for (
			FormField <Container, ?, ?, ?> formField
				: formFields ()
		) {

			if (
				contains (
					fieldNames,
					formField.name ())
			) {

				duplicateFieldNames.add (
					formField.name ());

			} else {

				fieldNames.add (
					formField.name ());

			}

		}

		if (
			collectionIsNotEmpty (
				duplicateFieldNames)
		) {
			throw new IllegalArgumentException ();
		}

	}

	// implementation

	@Override
	public
	String name () {
		return name;
	}

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return members.stream ()

			.flatMap (
				member ->
					Streams.stream (
						member.formItems ()))

			.flatMap (
				formItem ->
					formItem.scriptRefs ().stream ())

			.collect (
				Collectors.toCollection (
					LinkedHashSet::new))

		;

	}

	@Override
	public
	Set <HtmlLink> styles () {

		return members.stream ()

			.flatMap (
				member ->
					Streams.stream (
						member.formItems ()))

			.flatMap (
				formItem ->
					formItem.styles ().stream ())

			.collect (
				Collectors.toCollection (
					LinkedHashSet::new))

		;

	}

	@Override
	public
	Iterable <FormItem <Container>> formItems () {

		return () ->
			members.stream ()

			.flatMap (
				member ->
					Streams.stream (
						member.formItems ()))

			.iterator ()

		;

	}

	@Override
	public
	Iterable <FormField <Container, ?, ?, ?>> formFields () {

		return () ->
			members.stream ()

			.flatMap (
				member ->
					Streams.stream (
						member.formFields ()))

			.iterator ()

		;

	}

	@Override
	public
	Optional <FormField <Container, ?, ?, ?>> formField (
			@NonNull String name) {

		return genericCastUnchecked (
			optionalFromJava (
				members.stream ()

			.map (
				member ->
					member.formField (
						name))

			.filter (
				fieldOptional ->
					optionalIsPresent (
						fieldOptional))

			.map (
				fieldOptional ->
					genericCastUnchecked (
						optionalGetRequired (
							fieldOptional)))

			.findFirst ()

		));

	}

	@Override
	public
	Boolean fileUpload () {

		return members.stream ()

			.anyMatch (
				member ->
					member.fileUpload ())

		;

	}

	@Override
	public
	Long columns () {

		return members.stream ()

			.mapToLong (
				member ->
					member.columns ())

			.sum ();

	}

	@Override
	public
	Class <Container> containerClass () {
		return containerClass;
	}

}
