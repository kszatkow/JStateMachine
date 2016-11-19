package org.moomin.statemachine;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.junit.Test;
import org.moomin.statemachine.onoff.OffEvent;
import org.moomin.statemachine.onoff.OnEvent;

public class TransitionEffectStateMachineTest extends StateMachineTestBase {

	@Test
	public void transitionEffectTest() {
		State offState = addState(spy(State.class));
		State onState = addState(spy(State.class));
		
		// use two different transition constructors on purpose
		TransitionEffect turnOnEffect = mock(TransitionEffect.class);
		addTransition(offState, onState, OnEvent.class, turnOnEffect);
		TransitionEffect turnOffEffect = mock(TransitionEffect.class);
		addTransition(onState, offState, Collections.singletonList(OffEvent.class), turnOffEffect);
				
		setInitialTransitionAndActivate(offState);
		verify(turnOnEffect, never()).execute();
		verify(turnOffEffect, never()).execute();
		
		// turn off - not handled, already off
		dispatchThenProcessEventAndCheckActiveState(new OffEvent(), offState);
		verify(turnOnEffect, never()).execute();
		verify(turnOffEffect, never()).execute();
		
		// turn on
		dispatchThenProcessEventAndCheckActiveState(new OnEvent(), onState);
		verify(turnOnEffect).execute();
		verify(turnOffEffect, never()).execute();

		// turn off
		dispatchThenProcessEventAndCheckActiveState(new OffEvent(), offState);
		verify(turnOnEffect).execute();
		verify(turnOffEffect).execute();
	}

}
