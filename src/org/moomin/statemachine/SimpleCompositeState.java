package org.moomin.statemachine;

public abstract class SimpleCompositeState extends State implements RegionOwner {

	private Region ownedRegion;
	
	@Override
	public void deactivate() {
		ownedRegion.deactivate();
	}

	@Override
	public void activate() {
		ownedRegion.activate();
	}
	
	@Override
	public boolean tryConsumingEvent(Event event) {
		boolean eventConsumed = ownedRegion.tryConsumingEvent(event);
		if (eventConsumed && ownedRegion.hasReachedFinalState()) {
			dispatchCompletionEvent();
		}
		return eventConsumed;
	}

	private void dispatchCompletionEvent() {
		containingStateMachine().dispatchInternalEvent(new CompletionEvent(this));
	}
	
	@Override
	public void doAction() {
		doActionBehaviour();
	}
	
	@Override
	public void addRegion(Region region) {
		ownedRegion = region;	
	}
	
}
