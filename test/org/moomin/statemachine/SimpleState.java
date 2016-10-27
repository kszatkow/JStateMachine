package org.moomin.statemachine;

public abstract class SimpleState extends State {

	@Override
	public final boolean tryConsumingEvent(Event event) {
		return false;
	}
	
	@Override
	public final void activate() {
		// empty on purpose
	}

	@Override
	public final void deactivate() {
		// empty on purpose
	}

	public void doAction() {
		doActionBehaviour();
		owningRegion.dispatchInternalEvent(new CompletionEvent(this));
	}
	
}
