package wbs.console.forms;

import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.requiredSuccess;
import static wbs.utils.etc.Misc.successResult;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.FormField.FormType;
import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectHelper;
import wbs.utils.etc.OptionalUtils;
import wbs.utils.string.FormatWriter;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("objectFormFieldRenderer")
public
class ObjectFormFieldRenderer <Container, Interface extends Record <Interface>>
	implements FormFieldRenderer <Container, Interface> {

	// singleton dependencies

	@SingletonDependency
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
	EntityFinder <Interface> entityFinder;

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

		Collection <? extends Record <?>> allOptions =
			entityFinder.findAllEntities ();

		// filter visible options

		List <Record <?>> filteredOptions =
			allOptions.stream ()

			.filter (
				root.isPresent ()
					? item -> objectManager.isParent (item, root.get ())
					: item -> true)

			.filter (
				item ->

				objectManager.canView (
					item)

				|| (

					optionalIsPresent (
						interfaceValue)

					&& referenceEqualWithClass (
						entityFinder.entityClass (),
						item,
						interfaceValue.get ())

				)

			)

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

			|| OptionalUtils.optionalIsNotPresent (
				currentValue)

			|| enumInSafe (
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
				objectManager.objectHelperForObjectRequired (
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
			enumInSafe (
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
			enumInSafe (
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
			stringEqualSafe (
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
			OptionalUtils.optionalIsPresent (
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
			@NonNull FormatWriter formatWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Interface> interfaceValue,
			@NonNull Boolean link,
			@NonNull Long colspan) {

		// work out root

		Optional <Record <?>> rootOptional;

		if (

			optionalIsPresent (
				interfaceValue)

			&& isNotNull (
				rootFieldName)

		) {

			rootOptional =
				optionalOf (
					(Record <?>)
					objectManager.dereference (
						container,
						rootFieldName));

		} else {

			rootOptional =
				optionalAbsent ();

		}

		// render table cell

		objectManager.writeTdForObject (
			formatWriter,
			interfaceValue.orNull (),
			rootOptional,
			mini,
			link,
			colspan);

	}

}
