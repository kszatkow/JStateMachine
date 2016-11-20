package org.moomin.statemachine;

import static org.junit.Assert.*;
import static org.hamcrest.core.StringContains.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
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
public class ChoiceStateStateMachineTest extends StateMachineTestBase {

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
