package ca.roussil.ec2instancestarter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.renatodelgaudio.awsupdate.AWSRecordService;
import com.renatodelgaudio.awsupdate.Configuration;

public class Ec2InstanceStarter implements Starter {

	private final static Logger log = LoggerFactory
			.getLogger(Ec2InstanceStarter.class);

	@Autowired
	protected AWSRecordService recordService;

	@Autowired
	protected AWSEc2Service ec2Service;

	@Autowired
	protected Configuration config;

	/**
	 * This is the main implementation
	 */
	public void run(ApplicationContext context) {

		if (!config.isConfigOK()) {
			log.error("Unfortunately something is wrong with the config and the program will exit without performing any action");
			return;
		}
		
		String ec2InstanceId = config.getEc2InstanceId();
		
		try {
			ec2Service.startInstance(ec2InstanceId);
		} catch (InterruptedException e) {
			log.error("Ops. Something went wrong and the EC2 instance could not get started", e);
			return;
		}

		String ec2InstanceIp = ec2Service.getInstanceIp(ec2InstanceId);

		boolean success = recordService.updateRecord(ec2InstanceIp);
		if (!success) {
			log.error("Ops. Something went wrong and DNS was not updated");
			return;
		}
	}

}
