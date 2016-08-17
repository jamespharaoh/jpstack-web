package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.requiredSuccess;
import static wbs.framework.utils.etc.Misc.successResult;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.base.Optional;

import fj.data.Either;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.forms.FormField.FormType;
import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.object.ObjectHelper;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.FormatWriter;
import wbs.framework.utils.etc.OptionalUtils;

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
			@NonNull FormType formType,
			@NonNull String formName) {

		htmlWriter.writeFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"%h-%h\"",
			formName,
			name (),
			" value=\"%h\"",
			interfaceValue.isPresent ()
				? interfaceValue.get ().getId ()
				: "none",
			">\n");

	}

	@Override
	public
	void renderFormInput (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Interface> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

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
					submission,
					formName)
				? requiredSuccess (
					formToInterface (
						submission,
						formName))
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
			" id=\"%h-%h\"",
			formName,
			name,
			" name=\"%h-%h\"",
			formName,
			name,
			">\n");

		// none option

		if (

			nullable ()

			|| OptionalUtils.isNotPresent (
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
			@NonNull FormType formType,
			@NonNull String formName) {

		if (
			in (
				formType,
				FormType.create,
				FormType.perform,
				FormType.search)
		) {

			javascriptWriter.writeFormat (
				"%s$(\"#%j-%j\").val (\"none\");\n",
				indent,
				formName,
				name);

		} else if (
			in (
				formType,
				FormType.update)
		) {

			javascriptWriter.writeFormat (
				"%s$(\"#%j-%j\").val (\"%h\");\n",
				indent,
				formName,
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
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		return (

			submission.hasParameter (
				stringFormat (
					"%s-%s",
					formName,
					name ()))

		);

	}

	@Override
	public
	Either<Optional<Interface>,String> formToInterface (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		String param =
			submission.parameter (
				stringFormat (
					"%s-%s",
					formName,
					name ()));

		if (
			equal (
				param,
				"none")
		) {

			return successResult (
				Optional.<Interface>absent ());

		} else {

			Long objectId =
				Long.parseLong (
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
	void renderHtmlSimple (
			@NonNull FormatWriter htmlWriter,
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
			OptionalUtils.isPresent (
				interfaceValue)
		) {

			htmlWriter.writeFormat (
				"%h",
				objectManager.objectPath (
					interfaceValue.get (),
					root,
					true,
					false));

		} else {

			htmlWriter.writeFormat (
				"&mdash;");

		}

	}

	@Override
	public
	void renderHtmlTableCell (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Interface> interfaceValue,
			boolean link,
			int colspan) {

		// work out root

		Optional<Record<?>> root;

		if (

			OptionalUtils.isPresent (
				interfaceValue)

			&& isNotNull (
				rootFieldName)

		) {

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

		// render table cell

		htmlWriter.writeFormat (
			"%s",
			objectManager.tdForObject (
				interfaceValue.orNull (),
				root.orNull (),
				mini,
				link,
				colspan));

	}

}
