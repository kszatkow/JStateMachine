package org.moomin.statemachine;

public interface EventConsumer {

	public boolean consumeEvent(Event event);
	
}
