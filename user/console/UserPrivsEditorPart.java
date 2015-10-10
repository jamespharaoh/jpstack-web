package wbs.platform.user.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.PrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.platform.group.model.GroupRec;
import wbs.platform.priv.console.PrivConsoleHelper;
import wbs.platform.priv.model.PrivRec;
import wbs.platform.scaffold.model.RootRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserPrivRec;
import wbs.platform.user.model.UserRec;

import com.google.common.collect.ImmutableSet;

@PrototypeComponent ("userPrivsEditorPart")
public
class UserPrivsEditorPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ObjectManager objectManager;

	@Inject
	PrivChecker privChecker;

	@Inject
	PrivConsoleHelper privHelper;

	@Inject
	UserObjectHelper userHelper;

	// state

	PrivsEditorNode rootNode =
		new PrivsEditorNode ();

	Set<Integer> canPrivIds =
		new HashSet<Integer> ();

	Set<Integer> canGrantPrivIds =
		new HashSet<Integer> ();

	Set<Integer> groupPrivIds =
		new HashSet<Integer> ();

	Map<GlobalId,PrivsEditorNode> nodesByDataObjectId =
		new HashMap<GlobalId,PrivsEditorNode> ();

	// implementation

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>of (

			ConsoleApplicationScriptRef.javascript (
				"/js/tree.js")

		);

	}

	@Override
	public
	void prepare () {

		// build the privs tree

		List<PrivRec> privs =
			privHelper.findAll ();

		for (PrivRec priv
				: privs) {

			if (! privChecker.canGrant (
					priv.getId ()))
				continue;

			Record<?> parentObject =
				objectManager.getParent (priv);

			PrivsEditorNode parentNode =
				findNode (parentObject);

			parentNode.privs.put (
				priv.getCode (),
				priv);

		}

		// and fill the current priv data sets

		UserRec user =
			userHelper.find (
				requestContext.stuffInt ("userId"));

		for (UserPrivRec userPriv
				: user.getUserPrivs ()) {

			if (userPriv.getCan ())
				canPrivIds.add (
					userPriv.getPriv ().getId ());

			if (userPriv.getCanGrant ())
				canGrantPrivIds.add (
					userPriv.getPriv ().getId ());

		}

		for (GroupRec group
				: user.getGroups ()) {

			for (PrivRec priv
					: group.getPrivs ()) {

				groupPrivIds.add (
					priv.getId ());

			}
		}

		// now check which tree nodes to expand initially

		doExpansion (
			rootNode);

	}

	PrivsEditorNode findNode (
			Record<?> object) {

		// end the recursion if we reach the root node

		if ((Object) object instanceof RootRec)
			return rootNode;

		// check if the node already exists

		GlobalId dataObjectId =
			objectManager.getGlobalId (object);

		PrivsEditorNode node =
			nodesByDataObjectId.get (dataObjectId);

		if (node != null)
			return node;

		// find the parent node by recursion

		Record<?> parent =
			objectManager.getParent (object);

		PrivsEditorNode parentNode =
			findNode (parent);

		// lookup or create the map containing this node's children of this
		// type

		Map<String,PrivsEditorNode> typeNodesByCode =
			parentNode.nodesByCodeByTypeCode.get (
				objectManager.getObjectTypeCode (object));

		if (typeNodesByCode == null) {

			typeNodesByCode =
				new TreeMap<String,PrivsEditorNode> ();

			parentNode.nodesByCodeByTypeCode.put (
				objectManager.getObjectTypeCode (object),
				typeNodesByCode);
		}

		// lookup or create the node for this object

		node =
			typeNodesByCode.get (
				objectManager.getCode (object));

		if (node == null) {

			node =
				new PrivsEditorNode ();

			typeNodesByCode.put (
				objectManager.getCode (object),
				node);

			nodesByDataObjectId.put (
				dataObjectId,
				node);

		}

		// and return

		return node;

	}

	@Override
	public
	void renderHtmlHeadContent () {

		super.renderHtmlHeadContent ();

		printFormat (
			"<script language=\"JavaScript\">\n",

			"var newPrivData = {}\n",

			"var treeOpts = {\n",
			"  blankImage: '%j',\n",
			requestContext.resolveApplicationUrl (
				"/images/tree-blank.gif"),
			"  plusImage: '%j',\n",
			requestContext.resolveApplicationUrl (
				"/images/tree-plus.gif"),
			"  minusImage: '%j',\n",
			requestContext.resolveApplicationUrl (
				"/images/tree-minus.gif"),
			"  imageWidth: 17,\n",
			"  imageHeight: 17\n",
			"}\n",

			"var ob = new SimpleTreeItemGenerator ('%j');\n",
			requestContext.resolveApplicationUrl (
				"/images/tree-object.gif"),

			"ob.generateTail = function (itemData) {\n",
			"  return ' <span class=\"node\">' + itemData[2] + '</span>';\n",
			"}\n",

			"var pr = new SimpleTreeItemGenerator ('%j');\n",
			requestContext.resolveApplicationUrl (
				"/images/tree-priv.gif"),

			"var nextPrivDocId = 0;\n",

			"pr.generateTail = function (itemData) {\n",

			"  var privDocId = nextPrivDocId ++;\n",

			"  s = ' <span class=\"priv\">' + itemData[2] + '</span>'\n",
			"    + ' |'\n",
			"    + ' <a href=\"javascript:togglePriv (' + privDocId + ', &quot;can&quot;, ' + itemData[6] + ')\"'\n",
			"    + ' id=\"' + privDocId + '-can\"'\n",
			"    + ' class=\"' + (itemData[3]? 'can-on' : 'can-off') + '\">'\n",
			"    + 'can</a>'\n",
			"    + ' |'\n",
			"    + ' <a href=\"javascript:togglePriv (' + privDocId + ', &quot;cangrant&quot;, ' + itemData[6] + ')\"'\n",
			"    + ' id=\"' + privDocId + '-cangrant\"'\n",
			"    + ' class=\"' + (itemData[4]? 'cangrant-on' : 'cangrant-off') + '\">'\n",
			"    + 'can grant</a>';\n",

			"  if (itemData[5])\n",
			"    s += ' | <span class=\"group-can\">(group can)</span>';\n",

			"  return s;\n",

			"}\n",

			"function togglePriv (id, type, privId) {\n",
			"  var a = document.getElementById (id + '-' + type);\n",
			"  if (a.className == type + '-off') {\n",
			"    a.className = type + '-on';\n",
			"    newPrivData[privId + '-' + type] = 1;\n",
			"  } else {\n",
			"    a.className = type + '-off';\n",
			"    newPrivData[privId + '-' + type] = 0;\n",
			"  }\n",
			"}\n",

			"function getUpdateString () {\n",
			"  var s = '';\n",
			"  for (var key in newPrivData) {\n",
			"    if (s.length > 0) s += ',';\n",
			"      s += key + '=' + newPrivData[key];\n",
			"  }\n",
			"  return s;\n",
			"}\n",

			"var treeData = [\n");

		goNode (rootNode);

		printFormat (
			"]\n",

			"</script>\n");

		printFormat (
			"<style type=\"text/css\">\n",
			".node { font-weight: bold; color: blue; }\n",
			".can-off { text-decoration: none; color: black; font-weight: normal; }\n",
			".can-on { text-decoration: none; color: red; font-weight: bold; }\n",
			".cangrant-off { text-decoration: none; color: black; font-weight: normal; }\n",
			".cangrant-on { text-decoration: none; color: red; font-weight: bold; }\n",
			".group-can { text-decoration: none; color: red; font-weight: normal; }\n",
			"</style>\n");

	}

	void goNode (
			PrivsEditorNode node) {

		for (PrivRec priv
				: node.privs.values ()) {

			printFormat (
				"[pr, 0, '%h', %s, %s, %s, %s],\n",
				priv.getCode (),
				canPrivIds.contains (priv.getId ())
					? "1"
					: "0",
				canGrantPrivIds.contains (priv.getId ())
					? "1"
					: "0",
				groupPrivIds.contains (priv.getId ())
					? "1"
					: "0",
				priv.getId ());

		}

		for (Map.Entry<String,Map<String,PrivsEditorNode>> nodesByCodeEntry
				: node.nodesByCodeByTypeCode.entrySet ()) {

			String typeCode =
				nodesByCodeEntry.getKey ();

			Map<String,PrivsEditorNode> typeNodesByCode =
				nodesByCodeEntry.getValue ();

			boolean expanded = false;

			for (PrivsEditorNode childNode
					: typeNodesByCode.values ())

				if (childNode.expanded) {

					expanded = true;

					break;

				}

			printFormat (
				"[ob, %s, '%h', [\n",
				expanded
					? "2"
					: "1",
				typeCode);

			for (Map.Entry<String,PrivsEditorNode> typeNodeEntry
					: typeNodesByCode.entrySet ()) {

				String code =
					typeNodeEntry.getKey ();

				PrivsEditorNode childNode =
					typeNodeEntry.getValue ();

				printFormat (
					"[ob, %s, '%h', [\n",
					childNode.expanded
						? "2"
						: "1",
					code);

				goNode (childNode);

				printFormat (
					"]],\n");

			}

			printFormat (
				"]],\n");

		}

	}

	public
	boolean doExpansion (
			PrivsEditorNode node) {

		boolean expanded = false;

		// see if we have any active privs

		for (PrivRec priv
				: node.privs.values ()) {

			if (
				canPrivIds.contains (priv.getId ())
				|| canGrantPrivIds.contains (priv.getId ())
			) {

				expanded = true;

				break;

			}

		}

		// then call doExpansion for all child nodes

		for (Map<String,PrivsEditorNode> typeNodesByCode
				: node.nodesByCodeByTypeCode.values ()) {

			for (PrivsEditorNode childNode
					: typeNodesByCode.values ()) {

				if (doExpansion (childNode))
					expanded = true;

			}

		}

		// update this node's expanded status

		node.expanded = expanded;

		// and return

		return expanded;

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form",
			" method=\"post\"",

			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/user.privs.editor"),

			" onsubmit=\"%h\"",
			stringFormat (
				"javascript:%s = %s",
				"document.getElementById ('privdata').value",
				"getUpdateString ()"),

			">\n");

		printFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"privdata\"",
			" id=\"privdata\"",
			" value=\"\"",
			">\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"save changes\"",
			"></p>\n");

		printFormat (
			"<script language=\"JavaScript\">\n",
			"document.write (buildTree (treeData, treeOpts));\n",
			"</script>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"save changes\"",
			"></p>\n");

		printFormat (
			"</form>\n");

	}

	// data structures

	static
	class PrivsEditorNode {

		Map<String,Map<String,PrivsEditorNode>>
			nodesByCodeByTypeCode =
				new TreeMap<String,Map<String,PrivsEditorNode>> ();

		Map<String,PrivRec> privs =
			new TreeMap<String,PrivRec> ();

		boolean expanded = false;

	}

}
