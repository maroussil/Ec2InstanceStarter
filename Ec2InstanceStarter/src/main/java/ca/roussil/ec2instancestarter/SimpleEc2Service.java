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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.renatodelgaudio.awsupdate.Configuration;

public class SimpleEc2Service implements AWSEc2Service {
	
	private final static Logger log = LoggerFactory.getLogger(SimpleEc2Service.class);
	
	@Autowired
	Configuration config;

	@Override
	public String getInstanceIp(String ec2InstanceId) {
		
		DescribeInstancesRequest describeRequest = new DescribeInstancesRequest();
		describeRequest.withInstanceIds(ec2InstanceId);
		DescribeInstancesResult result = config.getAmazonEC2Client().describeInstances(describeRequest);
		
		List<Reservation> reservations = result.getReservations();
		List<Instance> instances = reservations.get(0).getInstances();
		if( instances.size() == 0 ) throw new RuntimeException("There is a problem, there should be an ec2 instance for id=" + ec2InstanceId);
		
		return instances.get(0).getPublicIpAddress();
	}

}
