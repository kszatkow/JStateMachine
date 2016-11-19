package org.moomin.statemachine;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.core.StringContains.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
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
	public void containingStateMachineTest() {
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
