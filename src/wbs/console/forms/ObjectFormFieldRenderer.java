package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.object.ObjectHelper;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;

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

	// details

	@Getter
	boolean fileUpload = false;

	// implementation

	@Override
	public
	void renderTableCellList (
			PrintWriter out,
			Container container,
			Interface interfaceValue,
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

		// write table cell

		out.write (
			stringFormat (
				"%s\n",
				objectManager.tdForObject (
					interfaceValue,
					root,
					true,
					link,
					1)));

	}

	@Override
	public
	void renderTableCellProperties (
			PrintWriter out,
			Container container,
			Interface interfaceValue) {

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

		// write table cell

		out.write (
			stringFormat (
				"%s\n",
				objectManager.tdForObject (
					interfaceValue,
					root,
					true,
					true,
					1)));

	}

	@Override
	public
	void renderTableRow (
			PrintWriter out,
			Container container,
			Interface interfaceValue) {

		out.print (
			stringFormat (
				"<tr>\n",
				"<th>%h</th>\n",
				label ()));

		renderTableCellProperties (
			out,
			container,
			interfaceValue);

		out.print (
			stringFormat (
				"</tr>\n"));

	}

	@Override
	public
	void renderFormRow (
			PrintWriter out,
			Container container,
			Interface interfaceValue) {

		out.print (
			stringFormat (
				"<tr>\n",
				"<th>%h</th>\n",
				label (),
				"<td>"));

		renderFormInput (
			out,
			container,
			interfaceValue);

		out.print (
			stringFormat (
				"</td>\n",
				"</tr>\n"));

	}

	@Override
	public
	void renderFormInput (
			PrintWriter out,
			Container container,
			Interface interfaceValue) {

		// get a list of options

		Collection<? extends Record<?>> options =
			entityFinder.findEntities ();

		// lookup root

		Record<?> root = null;

		if (rootFieldName != null) {

			root =
				(Record<?>)
				BeanLogic.getProperty (
					container,
					rootFieldName);

		}

		// sort options by path

		Map<String,Record<?>> sortedOptions =
			new TreeMap<String,Record<?>> ();

		for (Record<?> option
				: options) {

			sortedOptions.put (
				objectManager.objectPath (
					option,
					root,
					true,
					true),
				option);

		}

		out.write (
			stringFormat (
				"<select name=\"%h\">\n",
				name));

		// unchanged option

		out.write (
			stringFormat (
				"<option value=\"unchanged\">%h</option>\n",
				interfaceValue == null
					? "none"
					: objectManager.objectPath (
						(Record<?>) interfaceValue,
						root,
						true,
						true)));

		// null option

		out.write (
			stringFormat (
				"<option value=\"null\"",
				equal (
						requestContext.getForm (name),
						"null")
					? " selected"
					: "",
				">none</option>\n"));

		// value options

		for (Map.Entry<String,Record<?>> ent
				: sortedOptions.entrySet ()) {

			String path =
				ent.getKey ();

			Record<?> option =
				ent.getValue ();

			ObjectHelper<?> objectHelper =
				objectManager.objectHelperForObject (option);

			if (objectHelper.getDeleted (option, true))
				continue;

			out.write (
				stringFormat (
					"<option value=\"%h\"",
					option.getId (),
					equal (
							requestContext.getForm (name ()),
							option.getId ().toString ())
						? " selected"
						: "",
					">%h</option>\n",
					path));

		}

		out.write (
			stringFormat (
				"</select>"));

	}

	@Override
	public
	boolean formValuePresent () {

		String param =
			requestContext.getForm (name ());

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
	Interface formToInterface (
			List<String> errors) {

		String param =
			requestContext.parameter (
				name ());

		if (param == null)
			return null;

		if (
			equal (
				param,
				"null")
		) {
			return null;
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

		return interfaceValue;

	}

	@Override
	public
	String interfaceToHtmlSimple (
			Container container,
			Interface interfaceValue,
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
			interfaceValue,
			root,
			true,
			link,
			1);

	}

	@Override
	public
	String interfaceToHtmlComplex (
			Container container,
			Interface interfaceValue) {

		return interfaceToHtmlSimple (
			container,
			interfaceValue,
			true);

	}

}
