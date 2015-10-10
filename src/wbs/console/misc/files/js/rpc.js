
// ============================================================ make xml http request

function rpc_makeXmlHttpRequest () {
	if (window.XMLHttpRequest)
		return new XMLHttpRequest ();
	if (window.ActiveXObject)
		return new ActiveXObject ("Microsoft.XmlHttp");
	throw 'Unable to make XmlHttpRequest';
}

// ============================================================ make xml document

function rpc_makeXmlDocument () {
	if (document.implementation.createDocument)
		return document.implementation.createDocument ('', '', null);
	if (window.ActiveXObject)
		return new ActiveXObject ("Microsoft.XmlDom");
	throw 'Unable to make XmlDocument';
}

// ============================================================ simple get

function rpc_simpleGet (url, handler) {

	var request = rpc_makeXmlHttpRequest ();

	request.onreadystatechange = function () {
		if (request.readyState != 4) return;
		if (request.status == 200)
			handler.onSuccess (request);
		else
			handler.onFailure (request);
	};

	request.open ('GET', url, true);
	request.send (null);

	return request;
}

// ============================================================ simple post

function rpc_simplePost (url, handler, body) {

	var request = rpc_makeXmlHttpRequest ();

	request.onreadystatechange = function () {
		if (request.readyState != 4) return;
		if (request.status == 200)
			handler.onSuccess (request);
		else
			handler.onFailure (request);
	};

	request.open ('POST', url, true);
	request.send (body);

	return request;
}

// ============================================================ to xml object

function rpc_toXml_object (doc, obj) {
	var elem = doc.createElement ('object');
	for (key in obj) {
		elem.appendChild (rpc_toXml_string (doc, key));
		elem.appendChild (rpc_toXml_auto (doc, obj[key]));
	}
	return elem;
}

// ============================================================ to xml boolean

function rpc_toXml_boolean (doc, obj) {
	return doc.createElement (obj? 'true' : 'false');
}

// ============================================================ to xml array

function rpc_toXml_array (doc, obj) {
	var elem = doc.createElement ('array');
	for (var i = 0; i < obj.length; i++) {
		elem.appendChild (rpc_toXml_auto (doc, obj [i]));
	}
	return elem;
}

// ============================================================ to xml string

function rpc_toXml_string (doc, str) {
	var elem = doc.createElement ('string');
	var text = doc.createTextNode (str.toString ());
	elem.appendChild (text);
	return elem;
}

// ============================================================ to xml number

function rpc_toXml_number (doc, num) {
	var elem = doc.createElement ('number');
	var text = doc.createTextNode (num.toString ());
	elem.appendChild (text);
	return elem;
}

// ============================================================ to xml undefined

function rpc_toXml_undefined (doc) {
	var elem = doc.createElement ('undefined');
	return elem;
}

// ============================================================ to xml date

function rpc_toXml_date (doc, obj) {
	var elem = doc.createElement ('date');
	var text = doc.createTextNode (obj.valueOf ());
	elem.appendChild (text);
	return elem;
}

// ============================================================ to xml auto

function rpc_toXml_auto (doc, obj) {
	switch (typeof (obj)) {
	case 'string': return rpc_toXml_string (doc, obj);
	case 'number': return rpc_toXml_number (doc, obj);
	case 'undefined': return rpc_toXml_undefined (doc);
	case 'boolean': return rpc_toXml_boolean (doc, obj);
	case 'object':
		switch (obj.constructor) {
		case String: return rpc_toXml_string (doc, obj.valueOf ());
		case Number: return rpc_toXml_number (doc, obj.valueOf ());
		case Array: return rpc_toXml_array (doc, obj.valueOf ());
		case Boolean: return rpc_toXml_boolean (doc, obj.valueOf ());
		case Date: return rpc_toXml_date (doc, obj);
		default: return rpc_toXml_object (doc, obj);
		}
	}
	throw 'Don\'t know what to do with a ' + typeof (obj);
}

// ============================================================ to xml

function rpc_toXml (obj) {
	var doc = rpc_makeXmlDocument ();
	var elem = doc.createElement ('jdata');
	doc.appendChild (elem);
	elem.appendChild (rpc_toXml_auto (doc, obj));
	return doc;
}
