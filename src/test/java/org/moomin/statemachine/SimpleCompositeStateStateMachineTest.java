package org.moomin.statemachine;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.moomin.statemachine.phone.ConnectEvent;
import org.moomin.statemachine.phone.DigitEvent;
import org.moomin.statemachine.phone.FinishDialingEvent;
import org.moomin.statemachine.phone.HangUpEvent;
import org.moomin.statemachine.phone.InvalidNumberEvent;
import org.moomin.statemachine.phone.LiftReceiverEvent;

@RunWith(MockitoJUnitRunner.class)
public class SimpleCompositeStateStateMachineTest extends StateMachineTestBase {

	private State phoneIdleState;
	private State connectingState;
	
	private SimpleCompositeState dialingState;
	private Region dialingStateRegion;
	private State startDialingSubstate;
	private State partialDialSubstate;
	private State dialingFinishedSubstate;

	@Override
	@Before
	public void setUp() {
		super.setUp();
		
		phoneIdleState = addState(spy(State.class));
		connectingState = addState(spy(State.class));
		dialingState = addSimpleCompositeState(spy(SimpleCompositeState.class));
		
		addTransition(phoneIdleState, dialingState, LiftReceiverEvent.class);
		addTransition(connectingState, phoneIdleState, HangUpEvent.class);
		
		dialingStateRegion = new RegionStateMachine(dialingState);
		dialingState.addRegion(dialingStateRegion);
		// start dialing and partial dial need to be simple states as they dispatch internal events - completion events
		startDialingSubstate = addSubstate(dialingStateRegion, spy(SimpleState.class));
		partialDialSubstate = addSubstate(dialingStateRegion, spy(SimpleState.class));
		dialingFinishedSubstate = addSubstate(dialingStateRegion, spy(State.class));

		dialingStateRegion.addTransition(new Transition(partialDialSubstate, partialDialSubstate, DigitEvent.class));
		dialingStateRegion.addTransition(new Transition(partialDialSubstate, dialingFinishedSubstate, FinishDialingEvent.class));
	}
	
	@Test
	public void simpleCompositeStateTest() {
		State invalidState = addState(spy(State.class));
		
		addTransition(dialingState, connectingState, ConnectEvent.class);
		addTransition(dialingState, invalidState, InvalidNumberEvent.class);
		addTransition(invalidState, phoneIdleState, HangUpEvent.class);
		
		dialingStateRegion.setInitialTransition(new PrimitiveTransition(startDialingSubstate, mock(TransitionEffect.class)));
		dialingStateRegion.addTransition(new Transition(startDialingSubstate, partialDialSubstate, DigitEvent.class));
		
		setInitialTransitionAndActivate(phoneIdleState);
		assertFalse(dialingState.isActive());
		
		// PhoneIdle -> Dialing::StartDialing
		dispatchThenProcessEventAndCheckActiveState(new LiftReceiverEvent(), dialingState);
		assertSame(startDialingSubstate, dialingStateRegion.activeState());
		verify(startDialingSubstate).onEntry(any());
		verify(startDialingSubstate, never()).onExit();
		assertTrue(dialingState.isActive());
		
		// Dialing::StartDialing -> Dialing::PartialDial
		dispatchThenProcessEventAndCheckActiveState(new DigitEvent(1) , dialingState);
		assertSame(partialDialSubstate, dialingStateRegion.activeState());
		verify(startDialingSubstate).onEntry(any());
		verify(startDialingSubstate).onExit();
		assertTrue(dialingState.isActive());
		
		// Dialing::PartialDial -> Dialing::PartialDial
		for (int digit = 2; digit <= 4; ++digit) {
			dispatchThenProcessEventAndCheckActiveState(new DigitEvent(digit) , dialingState);
			assertSame(partialDialSubstate, dialingStateRegion.activeState());
			assertTrue(dialingState.isActive());
		}
		
		// Dialing::PartialDial -> Dialing::DialingFinished
		dispatchThenProcessEventAndCheckActiveState(new FinishDialingEvent() , dialingState);
		assertSame(dialingFinishedSubstate, dialingStateRegion.activeState());
		assertTrue(dialingState.isActive());
		
		// Dialing::Finished -> Connecting
		dispatchThenProcessEventAndCheckActiveState(new ConnectEvent() , connectingState);
		assertFalse(dialingState.isActive());
		
		// Connecting -> PhoneIdle
		dispatchThenProcessEventAndCheckActiveState(new HangUpEvent() , phoneIdleState);
		
		// PhoneIdle -> Dialing::StartDialing
		dispatchThenProcessEventAndCheckActiveState(new LiftReceiverEvent(), dialingState);
		assertSame(startDialingSubstate, dialingStateRegion.activeState());
		
		// Dialing::PartialDial -> Dialing::PartialDial
		dispatchThenProcessEventAndCheckActiveState(new DigitEvent(2) , dialingState);
		assertSame(partialDialSubstate, dialingStateRegion.activeState());
		
		// Dialing::PartialDial -> Dialing::DialingFinished
		dispatchThenProcessEventAndCheckActiveState(new FinishDialingEvent() , dialingState);
		assertSame(dialingFinishedSubstate, dialingStateRegion.activeState());
		
		// Dialing::Finished -> InvalidNumber
		dispatchThenProcessEventAndCheckActiveState(new InvalidNumberEvent() , invalidState);
		
		// InvalidNumber -> PhoneIdle
		dispatchThenProcessEventAndCheckActiveState(new HangUpEvent() , phoneIdleState);
	}

	@Test
	public void completionTransitionInsideCompositeStateTest() {
		addTransition(dialingState, connectingState, ConnectEvent.class);
		
		dialingStateRegion.setInitialTransition(new PrimitiveTransition(startDialingSubstate, mock(TransitionEffect.class)));
		dialingStateRegion.addTransition(new CompletionTransition(startDialingSubstate, partialDialSubstate));
		
		setInitialTransitionAndActivate(phoneIdleState);
		
		verify(startDialingSubstate, never()).onEntry(any());
		verify(startDialingSubstate, never()).onExit();
		
		// PhoneIdle -> Dialing::PartialDial
		dispatchThenProcessEventAndCheckActiveState(new LiftReceiverEvent(), dialingState);
		assertSame(partialDialSubstate, dialingStateRegion.activeState());
		verify(startDialingSubstate).onEntry(any());
		verify(startDialingSubstate).onExit();
		
		// Dialing::PartialDial -> Dialing::PartialDial
		dispatchThenProcessEventAndCheckActiveState(new DigitEvent(2) , dialingState);
		assertSame(partialDialSubstate, dialingStateRegion.activeState());
		
		// Dialing::PartialDial -> Dialing::DialingFinished
		dispatchThenProcessEventAndCheckActiveState(new FinishDialingEvent() , dialingState);
		assertSame(dialingFinishedSubstate, dialingStateRegion.activeState());
		
		// Dialing::Finished -> Connecting
		dispatchThenProcessEventAndCheckActiveState(new ConnectEvent() , connectingState);
		
		// Connecting -> PhoneIdle
		dispatchThenProcessEventAndCheckActiveState(new HangUpEvent() , phoneIdleState);
	}		
	
	@Test
	public void completionTransitionFromSimpleCompositeStateTest() {
		addCompletionTransition(dialingState, connectingState);

		dialingStateRegion.setFinalState(dialingFinishedSubstate);
		
		dialingStateRegion.setInitialTransition(new PrimitiveTransition(startDialingSubstate));
		dialingStateRegion.addTransition(new CompletionTransition(startDialingSubstate, partialDialSubstate));
		
		setInitialTransitionAndActivate(phoneIdleState);
		
		verify(startDialingSubstate, never()).onEntry(any());
		verify(startDialingSubstate, never()).onExit();
		
		// PhoneIdle -> Dialing::PartialDial
		dispatchThenProcessEventAndCheckActiveState(new LiftReceiverEvent(), dialingState);
		assertSame(partialDialSubstate, dialingStateRegion.activeState());
		verify(startDialingSubstate).onEntry(any());
		verify(startDialingSubstate).onExit();
		
		// Dialing::PartialDial -> Dialing::PartialDial
		dispatchThenProcessEventAndCheckActiveState(new DigitEvent(2) , dialingState);
		assertSame(partialDialSubstate, dialingStateRegion.activeState());
		
		// Dialing::PartialDial -> Connecting
		dispatchThenProcessEventAndCheckActiveState(new FinishDialingEvent() , connectingState);
		
		// Connecting -> PhoneIdle
		dispatchThenProcessEventAndCheckActiveState(new HangUpEvent() , phoneIdleState);
	}	
	
}
