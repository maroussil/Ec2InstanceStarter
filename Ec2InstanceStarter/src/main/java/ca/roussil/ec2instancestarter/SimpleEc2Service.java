/**
 * Copyright (c) 2015 Marc-André Roussil
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all 
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ca.roussil.ec2instancestarter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.renatodelgaudio.awsupdate.Configuration;

public class SimpleEc2Service implements AWSEc2Service {

	private static final int WAIT_FOR_TRANSITION_INTERVAL = 5000;

	private final static Logger log = LoggerFactory
			.getLogger(SimpleEc2Service.class);

	@Autowired
	Configuration config;

	@Override
	public String getInstanceIp(String ec2InstanceId) {

		return getSingleEc2InstanceById(ec2InstanceId).getPublicIpAddress();
	}

	@Override
	public void startInstance(String ec2InstanceId) throws InterruptedException {

		Instance instance = getSingleEc2InstanceById(ec2InstanceId);
		InstanceState state = instance.getState();

		// different possible states: pending, running, shutting-down,
		// terminated, stopping, stopped
		String stateName = state.getName();
		if (stateName.equalsIgnoreCase("pending")) {
			log.info("startInstance: instance with id= " + ec2InstanceId
					+ " state is pending, no action was taken.");
		} else if (stateName.equalsIgnoreCase("running")) {
			log.info("startInstance: instance with id= " + ec2InstanceId
					+ " state is running, no action was taken.");
		} else if (stateName.equalsIgnoreCase("shutting-down")) {
			log.info("startInstance: instance with id= " + ec2InstanceId
					+ " state is shutting-down, no action was taken.");

			// TODO maybe we should wait for the instance to shutdown before
			// starting it again.. ?
		} else if (stateName.equalsIgnoreCase("terminated")) {
			log.info("startInstance: instance with id= " + ec2InstanceId
					+ " state is terminated, no action was taken.");

			// TODO throw error ?
		} else if (stateName.equalsIgnoreCase("stopping")) {
			log.info("startInstance: instance with id= " + ec2InstanceId
					+ " state is stopping, no action was taken.");

			// TODO maybe we should wait for the instance to stop before
			// starting it again.. ? what is the difference between
			// shutting-down and stopping ??
		} else if (stateName.equalsIgnoreCase("stopped")) {
			log.info("startInstance: instance with id= "
					+ ec2InstanceId
					+ " state is stopped, the instance has been asked to start...");

			StartInstancesRequest startRequest = new StartInstancesRequest()
					.withInstanceIds(ec2InstanceId);
			StartInstancesResult startResult = config.getAmazonEC2Client()
					.startInstances(startRequest);
			List<InstanceStateChange> stateChangeList = startResult
					.getStartingInstances();

			waitForTransitionCompletion(stateChangeList, "running",
					ec2InstanceId);
		}
	}

	private Instance getSingleEc2InstanceById(String ec2InstanceId) {
		DescribeInstancesRequest describeRequest = new DescribeInstancesRequest();
		describeRequest.withInstanceIds(ec2InstanceId);
		DescribeInstancesResult result = config.getAmazonEC2Client()
				.describeInstances(describeRequest);

		List<Reservation> reservations = result.getReservations();
		List<Instance> instances = reservations.get(0).getInstances();
		if (instances.size() == 0)
			throw new RuntimeException(
					"There is a problem, could not find an ec2 instance for id="
							+ ec2InstanceId);
		return instances.get(0);
	}

	/**
	 * Wait for a instance to complete transitioning (i.e. status not being in
	 * INSTANCE_STATE_IN_PROGRESS_SET or the instance no longer existing).
	 * 
	 * @param stateChangeList
	 * @param instancebuilder
	 * @param instanceId
	 * @param BuildLogger
	 * @throws InterruptedException
	 * @throws Exception
	 */
	private final String waitForTransitionCompletion(
			List<InstanceStateChange> stateChangeList,
			final String desiredState, String instanceId)
			throws InterruptedException {

		Boolean transitionCompleted = false;
		InstanceStateChange stateChange = stateChangeList.get(0);
		String previousState = stateChange.getPreviousState().getName();
		String currentState = stateChange.getCurrentState().getName();
		String transitionReason = "";

		while (!transitionCompleted) {
			try {
				Instance instance = getSingleEc2InstanceById(instanceId);
				currentState = instance.getState().getName();
				if (previousState.equals(currentState)) {
					log.info("... '" + instanceId + "' is still in state "
							+ currentState + " ...");
				} else {
					log.info("... '" + instanceId + "' entered state "
							+ currentState + " ...");
					transitionReason = instance.getStateTransitionReason();
				}
				previousState = currentState;

				if (currentState.equals(desiredState)) {
					transitionCompleted = true;
				}
			} catch (AmazonServiceException ase) {
				log.error("Failed to describe instance '" + instanceId + "'!",
						ase);
				throw ase;
			}

			// Sleep for WAIT_FOR_TRANSITION_INTERVAL seconds until transition
			// has completed.
			if (!transitionCompleted) {
				Thread.sleep(WAIT_FOR_TRANSITION_INTERVAL);
			}
		}

		log.info("Transition of instance '"
				+ instanceId
				+ "' completed with state "
				+ currentState
				+ " ("
				+ (StringUtils.isEmpty(transitionReason) ? "Unknown transition reason"
						: transitionReason) + ").");

		return currentState;
	}

}
