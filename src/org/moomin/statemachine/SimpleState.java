package org.moomin.statemachine;

public abstract class SimpleState extends State {

	@Override
	public final boolean consumeEvent(Event event) {
		return false;
	}
	
	@Override
	protected final void doActivate() {
		// empty on purpose
	}

	@Override
	protected final void doDeactivate() {
		// empty on purpose
	}

	@Override
	public final void doActionClose() {
		containingStateMachine().dispatchInternalEvent(new CompletionEvent(this));
	}
	
}
