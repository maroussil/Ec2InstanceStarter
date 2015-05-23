package ca.roussil.ec2instancestarter;

public interface AWSEc2Service {

	String getInstanceIp(String ec2InstanceId);

	void startInstance(String ec2InstanceId) throws InterruptedException;

}
