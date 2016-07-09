package wbs.platform.object.create;

import static wbs.framework.utils.etc.Misc.optionalCast;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormField.FormType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.platform.scaffold.model.RootObjectHelper;

@Accessors (fluent = true)
@PrototypeComponent ("objectCreatePart")
public
class ObjectCreatePart<
	ObjectType extends Record<ObjectType>,
	ParentType extends Record<ParentType>
>
	extends AbstractPagePart {

	// dependencies

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	UserPrivChecker privChecker;

	@Inject
	RootObjectHelper rootHelper;

	// properties

	@Getter @Setter
	ConsoleHelper<ObjectType> consoleHelper;

	@Getter @Setter
	FormFieldSet formFieldSet;

	@Getter @Setter
	String parentPrivCode;

	@Getter @Setter
	String localFile;

	@Getter @Setter
	FieldsProvider<ObjectType,ParentType> formFieldsProvider;

	// state

	Optional<UpdateResultSet> updateResultSet;

	ConsoleHelper<ParentType> parentHelper;
	List<ParentType> parents;
	ParentType parent;

	ObjectType object;

	Map<String,Object> hints =
		new LinkedHashMap<> ();

	// implementation

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		Set<ScriptRef> scriptRefs =
			new LinkedHashSet<ScriptRef> ();

		scriptRefs.addAll (
			formFieldSet.scriptRefs ());

		return scriptRefs;

	}

	@Override
	public
	void prepare () {

		prepareParents ();

		// if a field provider was provided

		if (formFieldsProvider != null) {
			prepareFieldSet ();
		}

		// get update results

		updateResultSet =
			optionalCast (
				UpdateResultSet.class,
				requestContext.request (
					"objectCreateUpdateResultSet"));

		// create dummy instance

		object =
			consoleHelper.createInstance ();

		// set parent

		if (
			parent != null
			&& consoleHelper.canGetParent ()
		) {

			consoleHelper.setParent (
				object,
				parent);

		}

	}

	void prepareParents () {

		@SuppressWarnings ("unchecked")
		ConsoleHelper<ParentType> parentHelperTemp =
			(ConsoleHelper<ParentType>)
			objectManager.findConsoleHelper (
				consoleHelper.parentClass ());

		parentHelper =
			parentHelperTemp;

		if (parentHelper.isRoot ()) {

			@SuppressWarnings ("unchecked")
			ParentType parentTemp =
				(ParentType)
				rootHelper.findRequired (
					0);

			parent =
				parentTemp;

			return;

		}

		Integer parentId =
			requestContext.stuffInt (
				parentHelper.idKey ());

		if (parentId != null) {

			// use specific parent

			parent =
				parentHelper.findRequired (
					parentId);

			return;

		}

		ConsoleHelper<?> grandParentHelper =
			objectManager.findConsoleHelper (
				parentHelper.parentClass ());

		Integer grandParentId =
			requestContext.stuffInt (
				grandParentHelper.objectName () + "Id");

		if (grandParentId != null) {

			// show parents based on grand parent

			parents =
				parentHelper.findByParent (
					new GlobalId (
						grandParentHelper.objectTypeId (),
						grandParentId));

			// set grandparent hints

			Record<?> grandparent =
				grandParentHelper.findRequired (
					grandParentId);

			hints.put (
				"grandparent",
				grandparent);

			hints.put (
				stringFormat (
					"%s.parent",
					consoleHelper.parentFieldName ()),
				grandparent);

			hints.put (
				stringFormat (
					"parent.%s",
					parentHelper.parentFieldName ()),
				grandparent);

			hints.put (
				stringFormat (
					"%s.%s",
					consoleHelper.parentFieldName (),
					parentHelper.parentFieldName ()),
				grandparent);

			return;

		}

		// show all parents

		parents =
			parentHelper.findAll ();

	}

	void prepareFieldSet () {

		formFieldSet =
			parent != null
				? formFieldsProvider.getFieldsForParent (
					parent)
				: formFieldsProvider.getStaticFields ();

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<p>Please enter the details for the new %h</p>\n",
			consoleHelper.shortName ());

		formFieldLogic.outputFormTable (
			requestContext,
			formatWriter,
			formFieldSet,
			updateResultSet,
			object,
			hints,
			"post",
			requestContext.resolveLocalUrl (
				"/" + localFile),
			stringFormat (
				"create %h",
				consoleHelper.shortName ()),
			FormType.create,
			"create");

	}

}
