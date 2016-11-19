package org.moomin.statemachine;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.core.StringContains.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.moomin.statemachine.idleactive.ActiveState;
import org.moomin.statemachine.idleactive.ActiveStateWithDoActionBehaviour;
import org.moomin.statemachine.idleactive.IdleState;
import org.moomin.statemachine.idleactive.IdleStateWithOnEntryAndOnExitBehaviours;
import org.moomin.statemachine.idleactive.IdleTimeoutEvent;
import org.moomin.statemachine.idleactive.KeyWakeupEvent;
import org.moomin.statemachine.idleactive.MouseWakeupEvent;
import org.moomin.statemachine.oddeven.EvenNumberGuard;
import org.moomin.statemachine.oddeven.EvenNumberEvent;
import org.moomin.statemachine.oddeven.FeedNumberEvent;
import org.moomin.statemachine.oddeven.OddNumberGuard;
import org.moomin.statemachine.oddeven.OddNumberEvent;
import org.moomin.statemachine.oddeven.ZeroNumberEvent;
import org.moomin.statemachine.onoff.OffEvent;
import org.moomin.statemachine.onoff.OnEvent;
import org.moomin.statemachine.phone.ConnectEvent;
import org.moomin.statemachine.phone.ConnectingState;
import org.moomin.statemachine.phone.DialingFinished;
import org.moomin.statemachine.phone.DialingState;
import org.moomin.statemachine.phone.DigitEvent;
import org.moomin.statemachine.phone.FinishDialingEvent;
import org.moomin.statemachine.phone.HangUpEvent;
import org.moomin.statemachine.phone.InvalidNumberEvent;
import org.moomin.statemachine.phone.InvalidNumberState;
import org.moomin.statemachine.phone.LiftReceiverEvent;
import org.moomin.statemachine.phone.PartialDial;
import org.moomin.statemachine.phone.PhoneIdleState;
import org.moomin.statemachine.phone.StartDialing;
import org.moomin.statemachine.taskrouter.NodeState;
import org.moomin.statemachine.taskrouter.NodeBusyGuard;
import org.moomin.statemachine.taskrouter.PublishResultsState;
import org.moomin.statemachine.taskrouter.RouteTaskState;
import org.moomin.statemachine.taskrouter.TaskAccomplishedEvent;
import org.moomin.statemachine.taskrouter.TaskReceivedEvent;
import org.moomin.statemachine.taskrouter.WaitForTaskState;

/**
 *  TODO:
 *  - refactor StateMachineTest into more specialized test case classes, 
 *  	e.g. JunctionTest, ChoiceTest etc. Start using Mockito:)
 */
@RunWith(MockitoJUnitRunner.class)
public class StateMachineTest extends StateMachineTestBase {

	@Test
	public void activateDeactivateTest() {
		State offState = addState(spy(State.class));
		State onState = addState(spy(State.class));
		
		addTransition(offState, onState, OnEvent.class);
		addTransition(onState, offState, OffEvent.class);
				
		assertFalse(stateMachine.isActive());
		setInitialTransitionAndActivate(offState);
		assertTrue(stateMachine.isActive());
		
		// off -> on
		dispatchThenProcessEventAndCheckActiveState(new OnEvent() , onState);
		assertTrue(stateMachine.isActive());
		
		// deactivate - machine goes into default state
		stateMachine.deactivate();
		assertFalse(stateMachine.isActive());
		
		// activate again
		stateMachine.activate();
		assertTrue(stateMachine.isActive());
		assertEquals(offState, stateMachineRegion.activeState());
		
		// off -> on
		dispatchThenProcessEventAndCheckActiveState(new OnEvent() , onState);
	}
	
	@Test
	public void initialPseudostateTest() {
		State normalState = addState(mock(State.class));
		
		TransitionEffect initialTransitionEffectMock = mock(TransitionEffect.class);
		setInitialTransition(normalState, initialTransitionEffectMock);
		
		verify(initialTransitionEffectMock, never()).execute();
		
		stateMachine.activate();
		assertSame(normalState, stateMachineRegion.activeState());
		verify(initialTransitionEffectMock).execute();
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
		State idleState = addState(new IdleState("Idle"));
		State activeState = addState(new ActiveState("Active"));
		
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
	public void twoStatesNoTransitionTest() {
		State idleState = addState(new IdleState("Idle"));
		State activeState = addState(new ActiveState("Active"));
		
		addTransition(idleState, activeState, KeyWakeupEvent.class);
		addTransition(activeState, idleState, IdleTimeoutEvent.class);
		
		setInitialTransitionAndActivate(idleState);	
		
		// no event dispatched - no transition
		stateMachine.processEvent();
		assertSame(idleState, stateMachineRegion.activeState());
	}
	
	@Test
	public void processUndispatchedEvent() {
		State idleState = addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		setInitialTransitionAndActivate(idleState);	
		
		// no transition possible
		dispatchThenProcessEventAndCheckActiveState(new MouseWakeupEvent() , idleState);		
		// no transition possible
		dispatchThenProcessEventAndCheckActiveState(new KeyWakeupEvent() , idleState);
	}
	
	@Test
	public void illegalSourceAndTargetStateTransitionTest() {
		// legal states
		State idleState = addState(new IdleState("Idle"));
		
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
		State idleState = addState(new IdleState("Idle"));
		State activeState = addState(new ActiveState("Active"));
		
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
		State idleState = addState(new IdleState("Idle"));
		
		// illegal states
		State onState = mock(State.class);
		
		setInitialTransition(idleState, null);
		
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(containsString("Invalid source or destination state"));
		
		// illegal transition - target state invalid
		stateMachineRegion.addTransition(new Transition(idleState, onState, OffEvent.class));
	}

	@Test
	public void duplicateStatesTest() {
		// legal states
		State idleState = addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(containsString("Duplicate state"));

		addState(idleState);
	}
	
	@Test 
	public void invalidDefaultStateTest() {
		// legal states
		addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));

		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(containsString("Invalid default state"));
		
		setInitialTransitionAndActivate(mock(State.class));
	}
	
	@Test 
	public void illegalStateAdditionAfterActivationTest() {
		State idleState = addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		setInitialTransitionAndActivate(idleState);

		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("State addition is not allowed when state machine is active"));

		stateMachineRegion.addState(State.NULL_STATE);
	}
	
	@Test 
	public void illegalInitialTransitionSetupAfterActivationTest() {
		State idleState = addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		setInitialTransitionAndActivate(idleState);
		
		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("Initial transition setup is not allowed when state machine is active"));

		setInitialTransition(mock(State.class), null);
	}
	
	@Test 
	public void illegalTransitionAdditionAfterActivationTest() {
		State idleState = addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		setInitialTransitionAndActivate(idleState);
		
		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("Transition addition is not allowed when state machine is active"));

		addTransition(idleState, mock(State.class), OnEvent.class);
	}

	@Test 
	public void illegalRegionAdditionAfterActivationTest() {
		State idleState = addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		setInitialTransitionAndActivate(idleState);

		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("Region addition is not allowed when state machine is active"));

		stateMachine.addRegion(new RegionStateMachine(stateMachine));
	}
	
	@Test 
	public void illegalDoubleActivationTest() {
		State idleState = addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		setInitialTransitionAndActivate(idleState);
		
		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("Already active"));

		stateMachine.activate();
	}
	
	@Test 
	public void illegalDoubleDeactivationTest() {
		State idleState = addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		setInitialTransitionAndActivate(idleState);
		stateMachine.deactivate();

		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("Already inactive"));

		stateMachine.deactivate();
	}
	
	@Test 
	public void illegalEventProcessingBeforeActivationTest() {
		addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("Event processing is not allowed when state machine is inactive"));

		stateMachine.processEvent();
	}
	
	@Test 
	public void illegalActiveStateBeforeActivationTest() {
		addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));

		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("State machine is inactive"));

		stateMachineRegion.activeState();
	}
	

	@Test
	public void illegalEventDispatchBeforeActivationTest() {
		addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("Event dispatching is not allowed when state machine is inactive"));

		stateMachine.dispatchEvent(new OnEvent());
	}
	
	@Test
	public void illegalInternalEventDispatchBeforeActivationTest() {
		addState(new IdleState("Idle"));
		addState(new ActiveState("Active"));
		
		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("Event dispatching is not allowed when state machine is inactive"));

		stateMachine.dispatchInternalEvent(new OnEvent());
	}
	
	@Test
	public void stateBehavioursTest() {
		IdleStateWithOnEntryAndOnExitBehaviours idleState = 
				(IdleStateWithOnEntryAndOnExitBehaviours) addState(new IdleStateWithOnEntryAndOnExitBehaviours("Idle"));
		ActiveStateWithDoActionBehaviour activeState = 
				(ActiveStateWithDoActionBehaviour) addState(new ActiveStateWithDoActionBehaviour("ActiveState"));
		
		addTransition(idleState, activeState, KeyWakeupEvent.class);
		addTransition(activeState, idleState, IdleTimeoutEvent.class);
		
		assertFalse(idleState.hasOnEntryBeenExecuted());
		assertFalse(idleState.hasOnExitBeenExecuted());
		
		setInitialTransitionAndActivate(idleState);
		assertTrue(idleState.hasOnEntryBeenExecuted());
		assertFalse(idleState.hasOnExitBeenExecuted());
		
		idleState.clearExecutionStateFlags();
		
		// initial state - active state onAction not executed yet
		assertFalse(activeState.hasOnActionBeenExecuted());
		
		// transition to active state - onExit of source state and onAction of target state executed
		dispatchThenProcessEventAndCheckActiveState(new KeyWakeupEvent() , activeState);
		assertFalse(idleState.hasOnEntryBeenExecuted());
		assertTrue(idleState.hasOnExitBeenExecuted());
		assertTrue(activeState.hasOnActionBeenExecuted());
		
		// transition back to idle state - onEntry of target state executed
		dispatchThenProcessEventAndCheckActiveState(new IdleTimeoutEvent() , idleState);
		assertTrue(idleState.hasOnEntryBeenExecuted());
	}
	
	@Test
	public void simpleCompositeStateTest() {
		State phoneIdleState = addState(new PhoneIdleState("PhoneIdle"));
		SimpleCompositeState dialingState = addSimpleCompositeState(new DialingState("Dialing"));
		State invalidState = addState(new InvalidNumberState("InvalidNumber"));
		State connectingState = addState(new ConnectingState("Connecting"));
		
		addTransition(phoneIdleState, dialingState, LiftReceiverEvent.class);
		addTransition(dialingState, connectingState, ConnectEvent.class);
		addTransition(dialingState, invalidState, InvalidNumberEvent.class);
		addTransition(invalidState, phoneIdleState, HangUpEvent.class);
		addTransition(connectingState, phoneIdleState, HangUpEvent.class);
		
		Region dialingStateRegion = new RegionStateMachine(dialingState);
		dialingState.addRegion(dialingStateRegion);
		StartDialing startDialingSubstate = (StartDialing) addSubstate(dialingStateRegion, new StartDialing("StartDialing"));
		State partialDialSubstate = addSubstate(dialingStateRegion, new PartialDial("PartialDial"));
		State dialingFinishedSubstate = addSubstate(dialingStateRegion, new DialingFinished("DialingFinished"));
		
		dialingStateRegion.setInitialTransition(new PrimitiveTransition(startDialingSubstate, mock(TransitionEffect.class)));
		dialingStateRegion.addTransition(new Transition(startDialingSubstate, partialDialSubstate, DigitEvent.class));
		dialingStateRegion.addTransition(new Transition(partialDialSubstate, partialDialSubstate, DigitEvent.class));
		dialingStateRegion.addTransition(new Transition(partialDialSubstate, dialingFinishedSubstate, FinishDialingEvent.class));
		
		setInitialTransitionAndActivate(phoneIdleState);
		assertFalse(dialingState.isActive());
		
		// PhoneIdle -> Dialing::StartDialing
		dispatchThenProcessEventAndCheckActiveState(new LiftReceiverEvent(), dialingState);
		assertSame(startDialingSubstate, dialingStateRegion.activeState());
		assertTrue(startDialingSubstate.isDialToneOn());
		assertTrue(dialingState.isActive());
		
		// Dialing::StartDialing -> Dialing::PartialDial
		dispatchThenProcessEventAndCheckActiveState(new DigitEvent(1) , dialingState);
		assertSame(partialDialSubstate, dialingStateRegion.activeState());
		assertFalse(startDialingSubstate.isDialToneOn());
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
		State phoneIdleState = addState(new PhoneIdleState("PhoneIdle"));
		SimpleCompositeState dialingState = addSimpleCompositeState(new DialingState("Dialing"));
		State connectingState = addState(new ConnectingState("Connecting"));
		
		addTransition(phoneIdleState, dialingState, LiftReceiverEvent.class);
		addTransition(dialingState, connectingState, ConnectEvent.class);
		addTransition(connectingState, phoneIdleState, HangUpEvent.class);
		
		Region dialingStateRegion = new RegionStateMachine(dialingState);
		dialingState.addRegion(dialingStateRegion);
		StartDialing startDialingSubstate = (StartDialing) addSubstate(dialingStateRegion, new StartDialing("StartDialing"));
		State partialDialSubstate = addSubstate(dialingStateRegion, new PartialDial("PartialDial"));
		State dialingFinishedSubstate = addSubstate(dialingStateRegion, new DialingFinished("DialingFinished"));
		
		dialingStateRegion.setInitialTransition(new PrimitiveTransition(startDialingSubstate, mock(TransitionEffect.class)));
		dialingStateRegion.addTransition(new CompletionTransition(startDialingSubstate, partialDialSubstate));
		dialingStateRegion.addTransition(new Transition(partialDialSubstate, partialDialSubstate, DigitEvent.class));
		dialingStateRegion.addTransition(new Transition(partialDialSubstate, dialingFinishedSubstate, FinishDialingEvent.class));
		
		setInitialTransitionAndActivate(phoneIdleState);
		
		// PhoneIdle -> Dialing::PartialDial
		dispatchThenProcessEventAndCheckActiveState(new LiftReceiverEvent(), dialingState);
		assertSame(partialDialSubstate, dialingStateRegion.activeState());
		assertFalse(startDialingSubstate.isDialToneOn());
		
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
	public void completionTransitionFromCompositeStateTest() {
		State phoneIdleState = addState(new PhoneIdleState("PhoneIdle"));
		SimpleCompositeState dialingState = addSimpleCompositeState(new DialingState("Dialing"));
		State connectingState = addState(new ConnectingState("Connecting"));
		
		addTransition(phoneIdleState, dialingState, LiftReceiverEvent.class);
		addCompletionTransition(dialingState, connectingState);
		addTransition(connectingState, phoneIdleState, HangUpEvent.class);
		
		Region dialingStateRegion = new RegionStateMachine(dialingState);
		dialingState.addRegion(dialingStateRegion);
		StartDialing startDialingSubstate = (StartDialing) addSubstate(dialingStateRegion, new StartDialing("StartDialing"));
		State partialDialSubstate = addSubstate(dialingStateRegion, new PartialDial("PartialDial"));
		State dialingFinishedSubstate = addSubstate(dialingStateRegion, new DialingFinished("DialingFinished"));
		dialingStateRegion.setFinalState(dialingFinishedSubstate);
		
		dialingStateRegion.setInitialTransition(new PrimitiveTransition(startDialingSubstate));
		dialingStateRegion.addTransition(new CompletionTransition(startDialingSubstate, partialDialSubstate));
		dialingStateRegion.addTransition(new Transition(partialDialSubstate, partialDialSubstate, DigitEvent.class));
		dialingStateRegion.addTransition(new Transition(partialDialSubstate, dialingFinishedSubstate, FinishDialingEvent.class));
		
		setInitialTransitionAndActivate(phoneIdleState);
		
		// PhoneIdle -> Dialing::PartialDial
		dispatchThenProcessEventAndCheckActiveState(new LiftReceiverEvent(), dialingState);
		assertSame(partialDialSubstate, dialingStateRegion.activeState());
		assertFalse(startDialingSubstate.isDialToneOn());
		
		// Dialing::PartialDial -> Dialing::PartialDial
		dispatchThenProcessEventAndCheckActiveState(new DigitEvent(2) , dialingState);
		assertSame(partialDialSubstate, dialingStateRegion.activeState());
		
		// Dialing::PartialDial -> Connecting
		dispatchThenProcessEventAndCheckActiveState(new FinishDialingEvent() , connectingState);
		
		// Connecting -> PhoneIdle
		dispatchThenProcessEventAndCheckActiveState(new HangUpEvent() , phoneIdleState);
	}	
	
	@Test
	public void containingStateMachinenTest() {
		State phoneIdleState = addState(new PhoneIdleState("PhoneIdle"));
		SimpleCompositeState dialingState = addSimpleCompositeState(new DialingState("Dialing"));
		State connectingState = addState(new ConnectingState("Connecting"));
		
		Transition phoneIdleToDialingTransition = addTransition(phoneIdleState, dialingState, LiftReceiverEvent.class);
		CompletionTransition dialingToConnectionTransition = addCompletionTransition(dialingState, connectingState);
		Transition connectingToPhoneIdleTransition = addTransition(connectingState, phoneIdleState, HangUpEvent.class);
		
		Region dialingStateRegion = new RegionStateMachine(dialingState);
		dialingState.addRegion(dialingStateRegion);
		StartDialing startDialingSubstate = (StartDialing) addSubstate(dialingStateRegion, new StartDialing("StartDialing"));
		State partialDialSubstate = addSubstate(dialingStateRegion, new PartialDial("PartialDial"));
		State dialingFinishedSubstate = addSubstate(dialingStateRegion, new DialingFinished("DialingFinished"));
		dialingStateRegion.setFinalState(dialingFinishedSubstate);
		
		PrimitiveTransition initialDialingTransition = new PrimitiveTransition(startDialingSubstate, mock(TransitionEffect.class));
		dialingStateRegion.setInitialTransition(initialDialingTransition);
		CompletionTransition startDialingToPartialDialTransition = new CompletionTransition(startDialingSubstate, partialDialSubstate);
		dialingStateRegion.addTransition(startDialingToPartialDialTransition);
		Transition partialDialInternalTransition = new Transition(partialDialSubstate, partialDialSubstate, DigitEvent.class);
		dialingStateRegion.addTransition(partialDialInternalTransition);
		Transition partialDialToDialingFinishedTransition = new Transition(partialDialSubstate, dialingFinishedSubstate, FinishDialingEvent.class);
		dialingStateRegion.addTransition(partialDialToDialingFinishedTransition);
		
		List<StateMachinePart> stateMachineParts = new ArrayList<>();
		stateMachineParts.add(phoneIdleState);
		stateMachineParts.add(dialingState);
		stateMachineParts.add(connectingState);
		stateMachineParts.add(phoneIdleToDialingTransition);
		stateMachineParts.add(dialingToConnectionTransition);
		stateMachineParts.add(connectingToPhoneIdleTransition);
		stateMachineParts.add(startDialingSubstate);
		stateMachineParts.add(partialDialSubstate);
		stateMachineParts.add(dialingFinishedSubstate);
		stateMachineParts.add(initialDialingTransition);
		stateMachineParts.add(startDialingToPartialDialTransition);
		stateMachineParts.add(partialDialInternalTransition);
		stateMachineParts.add(partialDialToDialingFinishedTransition);
		stateMachineParts.add(stateMachineRegion);
		stateMachineParts.add(dialingStateRegion);
		
		for(StateMachinePart part : stateMachineParts) {
			assertSame(stateMachine, part.containingStateMachine());
		}
	}
	
	@Test
	public void testChoiceState() {
		State waitForTaskState = addState(new WaitForTaskState("Waiting"));
		State routeTaskState = addState(new RouteTaskState("Routing"));
		NodeState node1State = (NodeState) addState(new NodeState("Node1"));
		NodeState node2State = (NodeState) addState(new NodeState("Node2"));
		NodeState node3State = (NodeState) addState(new NodeState("Node3"));
		State publishResultsState = addState(new PublishResultsState("Publishing"));
		
		addTransition(waitForTaskState, routeTaskState, TaskReceivedEvent.class);
		addCompletionTransition(routeTaskState, node1State, new NodeBusyGuard(node1State));
		addCompletionTransition(routeTaskState, node2State, new NodeBusyGuard(node2State));
		addCompletionTransition(routeTaskState, node3State, new NodeBusyGuard(node3State));
		addTransition(node1State, publishResultsState, TaskAccomplishedEvent.class);
		addTransition(node2State, publishResultsState, TaskAccomplishedEvent.class);
		addTransition(node3State, publishResultsState, TaskAccomplishedEvent.class);
		addCompletionTransition(publishResultsState, waitForTaskState);
		
		setInitialTransitionAndActivate(waitForTaskState);
		
		// Waiting -> Node1
		dispatchThenProcessEventAndCheckActiveState(new TaskReceivedEvent(), node1State);
		// Node1 -> Publishing -> Waiting
		dispatchThenProcessEventAndCheckActiveState(new TaskAccomplishedEvent(), waitForTaskState);
		assertTrue(node1State.isBusy());
		// Waiting -> Node2
		dispatchThenProcessEventAndCheckActiveState(new TaskReceivedEvent(), node2State);
		// Node2 -> Publishing -> Waiting
		dispatchThenProcessEventAndCheckActiveState(new TaskAccomplishedEvent(), waitForTaskState);
		assertTrue(node2State.isBusy());
		// Waiting -> Node3
		dispatchThenProcessEventAndCheckActiveState(new TaskReceivedEvent(), node3State);
		// Node3 -> Publishing -> Waiting
		dispatchThenProcessEventAndCheckActiveState(new TaskAccomplishedEvent(), waitForTaskState);
		assertTrue(node3State.isBusy());
		
		node2State.setIdle();
		node3State.setIdle();
		
		// Waiting -> Node2
		dispatchThenProcessEventAndCheckActiveState(new TaskReceivedEvent(), node2State);
		// Node2 -> Publishing -> Waiting
		dispatchThenProcessEventAndCheckActiveState(new TaskAccomplishedEvent(), waitForTaskState);
		assertTrue(node2State.isBusy());
		
		node1State.setIdle();
		
		// Waiting -> Node1
		dispatchThenProcessEventAndCheckActiveState(new TaskReceivedEvent(), node1State);
		// Node1 -> Publishing -> Waiting
		dispatchThenProcessEventAndCheckActiveState(new TaskAccomplishedEvent(), waitForTaskState);
		assertTrue(node1State.isBusy());
		// Waiting -> Node3
		dispatchThenProcessEventAndCheckActiveState(new TaskReceivedEvent(), node3State);
		// Node3 -> Publishing -> Waiting
		dispatchThenProcessEventAndCheckActiveState(new TaskAccomplishedEvent(), waitForTaskState);
		assertTrue(node3State.isBusy());		
		
		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("Ill formed state machine"));
		exception.expectMessage(containsString("choice state must have at least one guard evaluating to true"));

		dispatchThenProcessEventAndCheckActiveState(new TaskReceivedEvent(), node1State);
	}
	
}
