package org.moomin.statemachine;

import static org.junit.Assert.*;

import org.junit.Test;
import org.moomin.statemachine.phone.DialingState;

public class RegionOwnerTest {

	@Test
	public void stateMachineRegionOwnerTest() {
		StateMachine stateMachine = new StateMachine();
		Region primitiveStateMachineRegion = new PrimitiveStateMachine(stateMachine);
		
		assertSame(stateMachine, primitiveStateMachineRegion.getOwner());
	}

	@Test
	public void stateRegionOwnerTest() {
		SimpleCompositeState state = new DialingState("RegionOwningState");
		Region primitiveStateMachineRegion = new PrimitiveStateMachine(state);
		
		assertSame(state, primitiveStateMachineRegion.getOwner());
	}
	
}