package org.moomin.statemachine;

public abstract class SimpleCompositeState extends State implements RegionOwner {

	private Region ownedRegion;
	
	@Override
	protected final void doDeactivate() {
		ownedRegion.deactivate();
	}

	@Override
	protected final void doActivate() {
		ownedRegion.activate();
	}
	
	@Override
	public boolean consumeEvent(Event event) {
		boolean eventConsumed = ownedRegion.consumeEvent(event);
		if (eventConsumed && ownedRegion.hasReachedFinalState()) {
			dispatchCompletionEvent();
		}
		return eventConsumed;
	}

	private void dispatchCompletionEvent() {
		containingStateMachine().dispatchInternalEvent(new CompletionEvent(this));
	}
	
	@Override
	protected final void doActionClose() {
		// empty on purpose
	}
	
	@Override
	public void addRegion(Region region) {
		ownedRegion = region;	
	}
	
}
