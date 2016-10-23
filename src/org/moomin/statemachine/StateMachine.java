package org.moomin.statemachine;

import java.util.Deque;
import java.util.LinkedList;

public class StateMachine {

	private Region ownedRegion;
	Deque<Event> eventQueue = new LinkedList<>();
	
	public StateMachine() {
		
	}
	
	public void addRegion(Region region) {
		ownedRegion = region;
	}
	
	public void processEvent() {
		throwIfInIllegalInactiveState("Even processing not allowed when state machine is inactive, activate first.");
		
		while (!eventQueue.isEmpty()) {
			Event event = eventQueue.poll();
			
			ownedRegion.tryConsumingEvent(event);
		}
	}
	
	public void dispatchEvent(Event event) {
		eventQueue.addLast(event);
	}

	public void dispatchEventToQueueFront(Event event) {
		eventQueue.addFirst(event);
	}
	
	public void activate() {
		ownedRegion.activate();
	}

	// TODO - code duplication - get rid of it
	private void throwIfInIllegalInactiveState(String exceptionMessage) {
		if (!ownedRegion.isActive()) {
			throw new IllegalStateException(exceptionMessage);
		}
	}
}
