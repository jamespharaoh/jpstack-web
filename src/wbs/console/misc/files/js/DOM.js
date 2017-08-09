
function MyEvent_stopPropagation () {
	if (this.event.stopPropagation)
		return this.event.stopPropagation ();
	this.event.cancelBubble = true;
	return true;
}

function MyEvent (_event) {
	this.event = _event;
}

MyEvent.prototype.stopPropagation = MyEvent_stopPropagation;

function dom_event (event) {
	if (! event.stopPropagation)
		event.stopPropagation = new Function ('this.cancelBubble = true');
}
