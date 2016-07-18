package wbs.platform.group.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.platform.group.model.GroupRec;
import wbs.platform.priv.console.PrivConsoleHelper;
import wbs.platform.priv.model.PrivRec;
import wbs.platform.scaffold.model.RootRec;

@PrototypeComponent ("groupPrivsPart")
public
class GroupPrivsPart
	extends AbstractPagePart {

	@Inject
	GroupConsoleHelper groupHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	UserPrivChecker privChecker;

	@Inject
	PrivConsoleHelper privHelper;

	static
	Set<ScriptRef> privsScriptRefs =
		ImmutableSet.<ScriptRef>of (

		ConsoleApplicationScriptRef.javascript (
			"/js/tree.js")

	);

	PrivsNode rootNode =
		new PrivsNode ();

	Set<Integer> canPrivIds =
		new HashSet<Integer> ();

	Map<GlobalId,PrivsNode> nodesByDataObjectId =
		new HashMap<GlobalId,PrivsNode> ();

	@Override
	public
	Set<ScriptRef> scriptRefs () {
		return privsScriptRefs;
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

			PrivsNode parentNode =
				findNode (parentObject);

			parentNode.privs.put (
				priv.getCode (),
				priv);

		}

		// and fill the current priv data sets

		GroupRec group =
			groupHelper.findRequired (
				requestContext.stuffInt (
					"groupId"));

		for (
			PrivRec priv
				: group.getPrivs ()
		) {

			canPrivIds.add (
				priv.getId ());

		}

		// now check which tree nodes to expand initially

		doExpansion (rootNode);

	}

	private
	PrivsNode findNode (
			Record<?> object) {

		// end the recursion if we reach the root node

		if ((Object) object instanceof RootRec)
			return rootNode;

		// check if the node already exists

		GlobalId dataObjectId =
			objectManager.getGlobalId (
				object);

		PrivsNode node =
			nodesByDataObjectId.get (
				dataObjectId);

		if (node != null)
			return node;

		// find the parent node by recursion

		Record<?> parent =
			objectManager.getParent (
				object);

		PrivsNode parentNode =
			findNode(parent);

		// lookup or create the map containing this node's children of this
		// type

		Map<String,PrivsNode> typeNodesByCode =
			parentNode.nodesByCodeByTypeCode.get (
				objectManager.getObjectTypeCode (object));

		if (typeNodesByCode == null) {

			typeNodesByCode =
				new TreeMap<String,PrivsNode>();

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
				new PrivsNode ();

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

		printFormat (
			"<script language=\"JavaScript\">\n");

		printFormat (
			"var newPrivData = {};\n");

		printFormat (
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
			"}\n");

		printFormat (
			"var ob = new SimpleTreeItemGenerator ('%j');\n",
			requestContext.resolveApplicationUrl (
				"/images/tree-object.gif"));

		printFormat (
			"ob.generateTail = function (itemData) {\n",
			"  return '<span class=\"node\">' + itemData [2] + '</span>';\n",
			"}\n");

		printFormat (
			"var pr = new SimpleTreeItemGenerator ('%j');\n",
			requestContext.resolveApplicationUrl (
				"/images/tree-priv.gif"));

		printFormat (
			"var nextPrivDocId = 0;\n");

		printFormat (
			"pr.generateTail = function (itemData) {\n",
			"  var privDocId = nextPrivDocId ++;\n",
			"  s = ' <span class=\"priv\">' + itemData[2] + '</span>'\n",
			"    + ' |'\n",
			"    + ' <a href=\"javascript:togglePriv (' + privDocId + ', &quot;can&quot;, ' + itemData[3] + ')\"'\n",
			"    + ' id=\"' + privDocId + '-can\"'\n",
			"    + ' class=\"' + (itemData[4]? 'can-on' : 'can-off') + '\">'\n",
			"    + 'can</a>';\n",
			"  return s;\n",
			"}\n");

		printFormat (
			"function togglePriv (id, type, privId) {\n",
			"  var a = document.getElementById (id + '-' + type);\n",
			"  if (a.className == type + '-off') {\n",
			"    a.className = type + '-on';\n",
			"    newPrivData[privId + '-' + type] = 1;\n",
			"  } else {\n",
			"    a.className = type + '-off';\n",
			"    newPrivData[privId + '-' + type] = 0;\n",
			"  }\n",
			"}\n");

		printFormat (
			"function getUpdateString () {\n",
			"  var s = '';\n",
			"  for (var key in newPrivData) {\n",
			"    if (s.length > 0) s += ',';\n",
			"      s += key + '=' + newPrivData[key];\n",
			"  }\n",
			"  return s;\n",
			"}\n");

		printFormat (
			"var treeData = [\n");

		goNode (
			rootNode);

		printFormat (
			"];\n");

		printFormat (
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
			PrivsNode node) {

		for (PrivRec priv
				: node.privs.values ()) {

			printFormat (
				"[pr, 0, '%j', %s, %s],\n",
				priv.getCode (),
				priv.getId (),
				canPrivIds.contains (
					priv.getId ())
						? "1"
						: "0");
		}

		for (Map.Entry<String,Map<String,PrivsNode>> typeNodesEntry
				: node.nodesByCodeByTypeCode.entrySet ()) {

			String typeCode =
				typeNodesEntry.getKey ();

			Map<String,PrivsNode> typeNodesByCode =
				typeNodesEntry.getValue ();

			boolean expanded = false;

			for (PrivsNode childNode
					: typeNodesByCode.values ()) {

				if (childNode.expanded) {
					expanded = true;
					break;
				}

			}

			printFormat (
				"[ob, %s, '%j', [\n",
				expanded
					? "2"
					: "1",
				typeCode);

			for (Map.Entry<String,PrivsNode> childNodeEntry
					: typeNodesByCode.entrySet ()) {

				String code =
					childNodeEntry.getKey ();

				PrivsNode childNode =
					childNodeEntry.getValue ();

				printFormat (
					"[ob, %s, '%j', [\n",
					childNode.expanded
						? "2"
						: "1",
					code);

				goNode (
					childNode);

				printFormat (
					"]],\n");

			}

			printFormat (
				"]],\n");

		}

	}

	public
	boolean doExpansion (
			PrivsNode node) {

		boolean expanded =
			false;

		// see if we have any active privs
		for (PrivRec priv : node.privs.values()) {
			if (canPrivIds.contains(priv.getId())) {
				expanded = true;
				break;
			}
		}

		// then call doExpansion for all child nodes
		for (Map<String, PrivsNode> typeNodesByCode : node.nodesByCodeByTypeCode
				.values()) {
			for (PrivsNode childNode : typeNodesByCode.values()) {
				if (doExpansion(childNode))
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
			requestContext.resolveLocalUrl ("/group.privs"),

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

	static
	class PrivsNode {

		Map<String,Map<String,PrivsNode>> nodesByCodeByTypeCode =
			new TreeMap<String,Map<String,PrivsNode>> ();

		Map<String,PrivRec> privs =
			new TreeMap<String,PrivRec> ();

		boolean expanded =
			false;

	}

}
