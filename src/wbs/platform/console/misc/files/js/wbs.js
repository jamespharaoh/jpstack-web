
function tdcheck_update (name) {
	var chk = document.getElementById (name);
	var td = document.getElementById (name + '_td');
	var theclass = chk.checked? 'selected' : 'unselected';
	td.className = theclass;
}

function tdcheck_checkbox (name, e) {
	e = new MyEvent (e);
	e.stopPropagation ();
	tdcheck_update (name);
}

function tdcheck_td (name) {
	var chk = document.getElementById (name);
	chk.checked = ! chk.checked;
	tdcheck_update (name);
}

function tdcheck_focus (name) {
	var td = document.getElementById (name + '_td');
	td.className = 'hover';
}

function elemX (elem) {
	var x = 0
	while (elem.offsetParent) {
		x += elem.offsetLeft
		elem = elem.offsetParent
	}
	return x
}

function elemY (elem) {
	var y = 0
	while (elem.offsetParent) {
		y += elem.offsetTop
		elem = elem.offsetParent
	}
	return y
}

function autoScrollTo (elem) {
	window.scrollTo (elemX (elem), elemY (elem))
}

function autoScrollDownTo (elem) {
	window.scrollTo (0, elemY (elem) - (window.innerHeight - elem.offsetHeight) / 2)
}

var wbs = {};

wbs.nextUniqueId = 0;

wbs.uniqueId = function () {
	return "wbs" + String (wbs.nextUniqueId ++);
}