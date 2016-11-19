package org.moomin.statemachine;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.moomin.statemachine.idleactive.IdleTimeoutEvent;
import org.moomin.statemachine.idleactive.KeyWakeupEvent;
import org.moomin.statemachine.idleactive.MouseWakeupEvent;
import org.moomin.statemachine.oddeven.EvenNumberEvent;
import org.moomin.statemachine.oddeven.EvenNumberGuard;
import org.moomin.statemachine.oddeven.FeedNumberEvent;
import org.moomin.statemachine.oddeven.OddNumberEvent;
import org.moomin.statemachine.oddeven.OddNumberGuard;
import org.moomin.statemachine.oddeven.ZeroNumberEvent;
import org.moomin.statemachine.onoff.OffEvent;
import org.moomin.statemachine.onoff.OnEvent;

@RunWith(MockitoJUnitRunner.class)
public class TransitionsStateMachineTest extends StateMachineTestBase {

	@Test
	public void initialTransitionTest() {
		State normalState = addState(mock(State.class));
		
		TransitionEffect initialTransitionEffectMock = mock(TransitionEffect.class);
		setInitialTransition(normalState, initialTransitionEffectMock);
		
		verify(initialTransitionEffectMock, never()).execute();
		
		stateMachine.activate();
		assertSame(normalState, stateMachineRegion.activeState());
		verify(initialTransitionEffectMock).execute();
	}

	@Test
	public void twoStatesNoTransitionTest() {
		State idleState = addState(spy(State.class));
		State activeState = addState(spy(State.class));
		
		addTransition(idleState, activeState, KeyWakeupEvent.class);
		addTransition(activeState, idleState, IdleTimeoutEvent.class);
		
		setInitialTransitionAndActivate(idleState);	
		
		// no event dispatched - no transition
		stateMachine.processEvent();
		assertSame(idleState, stateMachineRegion.activeState());
	}
	
	@Test
	public void transitionsWithoutGuardsTest() {
		State offState = addState(spy(State.class));
		State onState = addState(spy(State.class));
		
		addTransition(offState, onState, OnEvent.class);
		addTransition(onState, offState, OffEvent.class);
				
		setInitialTransitionAndActivate(offState);
	
		// off -> on
		dispatchThenProcessEventAndCheckActiveState(new OnEvent() , onState);
		// no transition - unhandled event
		dispatchThenProcessEventAndCheckActiveState(new UnhandledEvent() , onState);
		// on -> off
		dispatchThenProcessEventAndCheckActiveState(new OffEvent() , offState);
		// off -> off
		dispatchThenProcessEventAndCheckActiveState(new OffEvent() , offState);
		// no transition - unhandled event
		dispatchThenProcessEventAndCheckActiveState(new UnhandledEvent() , offState);
		// off -> on
		dispatchThenProcessEventAndCheckActiveState(new OnEvent() , onState);
	}

	
	@Test
	public void transitionsWithGuardsTest() {
		State oddState = addState(spy(State.class));
		State evenState = addState(spy(State.class));
		
		// use two different transition constructors on purpose
		addTransition(oddState, evenState, FeedNumberEvent.class, new EvenNumberGuard());
		addTransition(evenState, oddState, Collections.singleton(FeedNumberEvent.class), new OddNumberGuard());
				
		setInitialTransitionAndActivate(oddState);
		
		// odd -> odd
		dispatchThenProcessEventAndCheckActiveState(new FeedNumberEvent(11) , oddState);
		// no transition - unhandled event
		dispatchThenProcessEventAndCheckActiveState(new UnhandledEvent() , oddState);
		// odd -> even
		dispatchThenProcessEventAndCheckActiveState(new FeedNumberEvent(4) , evenState);
		// even -> even
		dispatchThenProcessEventAndCheckActiveState(new FeedNumberEvent(10) , evenState);
		// no transition - unhandled event
		dispatchThenProcessEventAndCheckActiveState(new UnhandledEvent() , evenState);
		// even -> odd
		dispatchThenProcessEventAndCheckActiveState(new FeedNumberEvent(5) , oddState);
	}

	@Test
	public void multipleTransitionsFromOneStateTest() {
		State zeroState = addState(spy(State.class));
		State oddState = addState(spy(State.class));
		State evenState = addState(spy(State.class));
		
		addTransition(zeroState, oddState, OddNumberEvent.class);
		addTransition(zeroState, evenState, EvenNumberEvent.class);
		addTransition(oddState, evenState, EvenNumberEvent.class);
		addTransition(oddState, zeroState, ZeroNumberEvent.class);
		addTransition(evenState, oddState, OddNumberEvent.class);
		addTransition(evenState, zeroState, ZeroNumberEvent.class);
		
		setInitialTransitionAndActivate(zeroState);
	
		// zero -> zero
		dispatchThenProcessEventAndCheckActiveState(new ZeroNumberEvent() , zeroState);
		// zero -> odd
		dispatchThenProcessEventAndCheckActiveState(new OddNumberEvent() , oddState);
		// odd -> odd
		dispatchThenProcessEventAndCheckActiveState(new OddNumberEvent() , oddState);
		// odd -> zero
		dispatchThenProcessEventAndCheckActiveState(new ZeroNumberEvent() , zeroState);
		// zero -> even
		dispatchThenProcessEventAndCheckActiveState(new EvenNumberEvent() , evenState);
		// even -> even
		dispatchThenProcessEventAndCheckActiveState(new EvenNumberEvent() , evenState);
		// even -> zero
		dispatchThenProcessEventAndCheckActiveState(new ZeroNumberEvent() , zeroState);
		// zero -> odd
		dispatchThenProcessEventAndCheckActiveState(new OddNumberEvent() , oddState);
		// odd -> even
		dispatchThenProcessEventAndCheckActiveState(new EvenNumberEvent() , evenState);
		// even -> odd
		dispatchThenProcessEventAndCheckActiveState(new OddNumberEvent() , oddState);
	}
	
		
	@Test
	public void transitionWithMultipleTriggersTest() {
		State idleState = addState(spy(State.class));
		State activeState = addState(spy(State.class));
		
		Set<Class<? extends Event>> triggerableBy = new HashSet<>();
		triggerableBy.add(KeyWakeupEvent.class);
		triggerableBy.add(MouseWakeupEvent.class);
		addTransition(idleState, activeState, triggerableBy);
		addTransition(activeState, idleState, IdleTimeoutEvent.class);
				
		setInitialTransitionAndActivate(idleState);	
		
		// idle -> idle
		dispatchThenProcessEventAndCheckActiveState(new IdleTimeoutEvent(), idleState);		
		// idle -> active (key event)
		dispatchThenProcessEventAndCheckActiveState(new KeyWakeupEvent(), activeState);
		// active -> idle
		dispatchThenProcessEventAndCheckActiveState(new IdleTimeoutEvent(), idleState);	
		// idle -> active (mouse event)
		dispatchThenProcessEventAndCheckActiveState(new MouseWakeupEvent(), activeState);
		// active -> idle
		dispatchThenProcessEventAndCheckActiveState(new IdleTimeoutEvent(), idleState);	
		// no transition - unhandled event
		dispatchThenProcessEventAndCheckActiveState(new UnhandledEvent(), idleState);
	}
	
	@Test
	public void illegalSourceAndTargetStateTransitionTest() {
		// legal states
		State idleState = addState(mock(State.class));
		
		// illegal states
		State offState = mock(State.class);
		State onState = mock(State.class);
		
		setInitialTransition(idleState, null);
		
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(containsString("Invalid source or destination state"));
		
		// illegal transition - both states invalid
		stateMachineRegion.addTransition(new Transition(offState, onState, OnEvent.class));
	}

	@Test
	public void illegalSourceStateTransitionTest() {
		// legal states
		State idleState = addState(mock(State.class));
		State activeState = addState(mock(State.class));
		
		// illegal states
		State offState = mock(State.class);
		
		setInitialTransition(idleState, null);
		
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(containsString("Invalid source or destination state"));
		
		// illegal transition - source state invalid
		stateMachineRegion.addTransition(new Transition(offState, activeState, OnEvent.class));
	}
	
	@Test
	public void illegalDestinationStateTransitionTest() {
		// legal states
		State idleState = addState(mock(State.class));
		
		// illegal states
		State onState = mock(State.class);
		
		setInitialTransition(idleState, null);
		
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(containsString("Invalid source or destination state"));
		
		// illegal transition - target state invalid
		stateMachineRegion.addTransition(new Transition(idleState, onState, OffEvent.class));
	}

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
