package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.requiredSuccess;
import static wbs.framework.utils.etc.Misc.successResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.forms.FormField.FormType;
import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.object.ObjectHelper;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("objectFormFieldRenderer")
public
class ObjectFormFieldRenderer<Container,Interface extends Record<Interface>>
	implements FormFieldRenderer<Container,Interface> {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String label;

	@Getter @Setter
	Boolean nullable;

	@Getter @Setter
	String rootFieldName;

	@Getter @Setter
	EntityFinder<Interface> entityFinder;

	@Getter @Setter
	Boolean mini;

	// implementation

	@Override
	public
	void renderFormTemporarilyHidden (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Interface> interfaceValue,
			@NonNull FormType formType) {

		doNothing ();

	}

	@Override
	public
	void renderFormInput (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Interface> interfaceValue,
			@NonNull FormType formType) {

		// lookup root

		Optional<Record<?>> root;

		if (rootFieldName != null) {

			root =
				Optional.<Record<?>>of (
					(Record<?>)
					objectManager.dereference (
						container,
						rootFieldName,
						hints));

		} else {

			root =
				Optional.<Record<?>>absent ();

		}

		// get current option

		Optional<Interface> currentValue =
			formValuePresent (
					submission)
				? requiredSuccess (
					formToInterface (
						submission))
				: interfaceValue;

		// get a list of options

		Collection<? extends Record<?>> allOptions =
			entityFinder.findEntities ();

		// filter visible options

		List<Record<?>> filteredOptions =
			allOptions.stream ()

			.filter (
				root.isPresent ()
					? item -> objectManager.isParent (item, root.get ())
					: item -> true)

			.filter (
				item ->
					objectManager.canView (item)
					|| equal (item, interfaceValue.orNull ()))

			.collect (
				Collectors.toList ());

		// sort options by path

		Map<String,Record<?>> sortedOptions =
			new TreeMap<String,Record<?>> ();

		for (
			Record<?> option
				: filteredOptions
		) {

			sortedOptions.put (
				objectManager.objectPathMiniPreload (
					option,
					root),
				option);

		}

		out.writeFormat (
			"<select",
			" id=\"%h\"",
			name,
			" name=\"%h\"",
			name,
			">\n");

		// none option

		if (

			nullable ()

			|| isNotPresent (
				currentValue)

			|| in (
				formType,
				FormType.create,
				FormType.perform,
				FormType.search)

		) {

			out.writeFormat (
				"<option",
				" value=\"none\"",
				currentValue.isPresent ()
					? ""
					: " selected",
				">&mdash;</option>\n");

		}

		// value options

		for (
			Map.Entry<String,Record<?>> optionEntry
				: sortedOptions.entrySet ()
		) {

			String optionLabel =
				optionEntry.getKey ();

			Record<?> optionValue =
				optionEntry.getValue ();

			ObjectHelper<?> objectHelper =
				objectManager.objectHelperForObject (
					optionValue);

			boolean selected =
				optionValue == currentValue.orNull ();

			if (

				! selected

				&& objectHelper.getDeleted (
					optionValue,
					true)

			) {
				continue;
			}

			out.writeFormat (
				"<option",
				" value=\"%h\"",
				optionValue.getId (),
				selected
					? " selected"
					: "",
				">%h</option>\n",
				optionLabel);

		}

		out.writeFormat (
			"</select>");

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull String indent,
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue,
			@NonNull FormType formType) {

		if (
			in (
				formType,
				FormType.create,
				FormType.perform,
				FormType.search)
		) {

			javascriptWriter.writeFormat (
				"%s$(\"#%j\").val (\"none\");\n",
				indent,
				name);

		} else if (
			in (
				formType,
				FormType.update)
		) {

			javascriptWriter.writeFormat (
				"%s$(\"#%j\").val (\"%h\");\n",
				indent,
				name,
				interfaceValue.isPresent ()
					? interfaceValue.get ().getId ()
					: "none");

		} else {

			throw new RuntimeException ();

		}

	}

	@Override
	public
	boolean formValuePresent (
			@NonNull FormFieldSubmission submission) {

		return (

			submission.hasParameter (
				name ())

		);

	}

	@Override
	public
	Either<Optional<Interface>,String> formToInterface (
			@NonNull FormFieldSubmission submission) {

		String param =
			submission.parameter (
				name ());

		if (
			equal (
				param,
				"none")
		) {

			return successResult (
				Optional.<Interface>absent ());

		} else {

			Integer objectId =
				Integer.parseInt (
					param);

			Interface interfaceValue =
				entityFinder.findEntity (
					objectId);

			return successResult (
				Optional.of (
					interfaceValue));

		}

	}

	@Override
	public
	String interfaceToHtmlSimple (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Interface> interfaceValue,
			boolean link) {

		// work out root

		Optional<Record<?>> root;

		if (rootFieldName != null) {

			root =
				Optional.of (
					(Record<?>)
					objectManager.dereference (
						container,
						rootFieldName));

		} else {

			root =
				Optional.absent ();

		}

		// render object path

		if (
			isPresent (
				interfaceValue)
		) {

			return objectManager.objectPath (
				interfaceValue.get (),
				root,
				true,
				link);

		} else {

			return "&mdash;";

		}

	}

	@Override
	public
	String interfaceToHtmlComplex (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Interface> interfaceValue) {

		return interfaceToHtmlSimple (
			container,
			hints,
			interfaceValue,
			true);

	}

}
