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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
/**
 * 
 * @author Marc-André Roussil
 *
 */
public class StarterMain {
	
	
	private final static Logger log = LoggerFactory.getLogger(StarterMain.class);

	public static void main( String[] args )
	{
		ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
		Starter starter =  context.getBean(Starter.class);   

		log.info("Running "+starter.getClass().getName()+" ....");
		long start = System.currentTimeMillis();
		try{
			starter.run(context);
		}catch(Exception e) {
			log.error("Opps something went wrong", e);
		}
		long end = System.currentTimeMillis();
		long timeMin = (end - start) / 1000 / 60 / 60;
		log.info(starter.getClass().getName()+" completed in "+timeMin+" sec.");
	}
	
}
