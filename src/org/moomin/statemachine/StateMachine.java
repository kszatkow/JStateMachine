package org.moomin.statemachine;

import java.util.Deque;
import java.util.LinkedList;

public class StateMachine extends SinglyActivatableObject implements RegionOwner {

	private Region ownedRegion;
	Deque<Event> eventQueue = new LinkedList<>();
	
	@Override
	public void addRegion(Region region) {
		assertInactive("Region addition is not allowed when state machine is active.");
		
		ownedRegion = region;
	}
	
	@Override
	public StateMachine containingStateMachine() {
		return this;
	}
	
	public void processEvent() {
		assertActive("Event processing not allowed when state machine is inactive, activate first.");
		
		while (!eventQueue.isEmpty()) {
			Event event = eventQueue.poll();
			
			ownedRegion.tryConsumingEvent(event);
		}
	}
	
	public void dispatchEvent(Event event) {
		assertActive("Event dispatching not allowed when state machine is inactive, activate first.");
		
		eventQueue.addLast(event);
	}

	public void dispatchInternalEvent(Event event) {
		assertActive("Event dispatching not allowed when state machine is inactive, activate first.");
		
		eventQueue.addFirst(event);
	}

	@Override
	protected void doDeactivate() {
		ownedRegion.deactivate();
	}

	@Override
	protected void doActivate() {
		ownedRegion.activate();
	}
}
