package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.successResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.object.ObjectHelper;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;
import wbs.framework.utils.etc.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("objectFormFieldRenderer")
public
class ObjectFormFieldRenderer<Container,Interface extends Record<Interface>>
	implements FormFieldRenderer<Container,Interface> {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

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

	// details

	@Getter
	boolean fileUpload = false;

	// implementation

	@Override
	public
	void renderTableCellList (
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue,
			boolean link,
			int colspan) {

		// work out root

		Optional<Record<?>> root;

		if (rootFieldName != null) {

			root =
				Optional.<Record<?>>fromNullable (
					(Record<?>)
					objectManager.dereference (
						container,
						rootFieldName));

		} else {

			root =
				Optional.<Record<?>>absent ();

		}

		// write table cell

		out.writeFormat (
			"%s\n",
			objectManager.tdForObject (
				interfaceValue.orNull (),
				root.orNull (),
				mini,
				link,
				colspan));

	}

	@Override
	public
	void renderTableCellProperties (
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue) {

		// work out root

		Optional<Record<?>> root;

		if (rootFieldName != null) {

			root =
				Optional.<Record<?>>fromNullable (
					(Record<?>)
					objectManager.dereference (
						container,
						rootFieldName));

		} else {

			root =
				Optional.<Record<?>>absent ();

		}

		// write table cell

		out.writeFormat (
			"%s\n",
			objectManager.tdForObject (
				interfaceValue.orNull (),
				root.orNull (),
				mini,
				true,
				1));

	}

	@Override
	public
	void renderTableRow (
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue) {

		out.writeFormat (
			"<tr>\n",
			"<th>%h</th>\n",
			label ());

		renderTableCellProperties (
			out,
			container,
			interfaceValue);

		out.writeFormat (
			"</tr>\n");

	}

	@Override
	public
	void renderFormRow (
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue,
			@NonNull Optional<String> error) {

		out.writeFormat (
			"<tr>\n",
			"<th>%h</th>\n",
			label (),
			"<td>");

		renderFormInput (
			out,
			container,
			interfaceValue);

		if (
			isPresent (
				error)
		) {

			out.writeFormat (
				"<br>\n",
				"%h",
				error.get ());

		}

		out.writeFormat (
			"</td>\n",
			"</tr>\n");

	}

	@Override
	public
	void renderFormInput (
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue) {

		// get a list of options

		Collection<? extends Record<?>> allOptions =
			entityFinder.findEntities ();

		// filter visible options

		List<Record<?>> filteredOptions =
			new ArrayList<Record<?>> ();

		for (
			Record<?> option
				: allOptions
		) {

			if (
				! objectManager.canView (
					option)
			) {
				continue;
			}

			filteredOptions.add (
				option);

		}

		// lookup root

		Optional<Record<?>> root;

		if (rootFieldName != null) {

			root =
				Optional.<Record<?>>fromNullable (
					(Record<?>)
					objectManager.dereference (
						container,
						rootFieldName));

		} else {

			root =
				Optional.<Record<?>>absent ();

		}

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

		// unchanged option

		out.writeFormat (
			"<option",
			" value=\"unchanged\"",
			">%h</option>\n",
			interfaceValue.isPresent ()
				? objectManager.objectPathMiniPreload (
					(Record<?>) interfaceValue.get (),
					root)
				: "none");

		// null option

		out.writeFormat (
			"<option",
			" value=\"null\"",
			equal (
					requestContext.getForm (name),
					"null")
				? " selected"
				: "",
			">none</option>\n");

		// value options

		for (
			Map.Entry<String,Record<?>> optionEntry
				: sortedOptions.entrySet ()
		) {

			String path =
				optionEntry.getKey ();

			Record<?> option =
				optionEntry.getValue ();

			ObjectHelper<?> objectHelper =
				objectManager.objectHelperForObject (option);

			if (
				objectHelper.getDeleted (
					option,
					true)
			) {
				continue;
			}

			out.writeFormat (
				"<option value=\"%h\"",
				option.getId (),
				equal (
						requestContext.getForm (name ()),
						option.getId ().toString ())
					? " selected"
					: "",
				">%h</option>\n",
				path);

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
			@NonNull Optional<Interface> interfaceValue) {

		javascriptWriter.writeFormat (
			"%s$(\"#%j\").val (\"null\");\n",
			indent,
			name);

	}

	@Override
	public
	boolean formValuePresent () {

		String param =
			requestContext.getForm (
				name ());

		if (param == null)
			return false;

		if (equal (
				param,
				"unchanged"))
			return false;

		return true;

	}

	@Override
	public
	Either<Optional<Interface>,String> formToInterface () {

		String param =
			requestContext.parameter (
				name ());

		if (param == null) {

			return successResult (
				Optional.<Interface>absent ());

		}

		if (
			equal (
				param,
				"null")
		) {

			return successResult (
				Optional.<Interface>absent ());

		}

		if (
			equal (
				param,
				"unchanged")
		) {
			throw new IllegalStateException ();
		}

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

	@Override
	public
	String interfaceToHtmlSimple (
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue,
			boolean link) {

		// work out root

		Record<?> root;

		if (rootFieldName != null) {

			root =
				(Record<?>)
				BeanLogic.getProperty (
					container,
					rootFieldName);

		} else {

			root = null;

		}

		// render object path

		return objectManager.tdForObject (
			interfaceValue.orNull (),
			root,
			true,
			link,
			1);

	}

	@Override
	public
	String interfaceToHtmlComplex (
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue) {

		return interfaceToHtmlSimple (
			container,
			interfaceValue,
			true);

	}

}
