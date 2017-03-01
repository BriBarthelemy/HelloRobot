package adafruit;

import adafruit.*;


/**
 * Example program to demonstrate use of the AdafruitMotorHat and AdafruitDcMotor classes 
 * for information on the Adafruit DC motor and Stepper motor HAT see:
 * https://www.adafruit.com/products/2348 
 * 
 * @author Eric Eliason
 *
 */
public class MotorHatExample {
	 static AdafruitMotorHat motorHat;
	
	public static void main(String[] args)  {
		
		/*
		 * Because the Adafruit motor hat uses PWMs that pulse independently of
		 * the Raspberry Pi the motors will keep running at the current direction
		 * and power levels if the program abnormally terminates. 
		 * A shutdown hook like the one in this example is useful to stop the 
		 * motors when the program is abnormally interrupted.
		 */		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() { 
		    	System.out.println("Turn off all motors");
		    	motorHat.stopAll();		    	
		    }
		 });
		
		motorHat = new AdafruitMotorHat();
		
		AdafruitDcMotor motorLeft  = motorHat.getDcMotor("M1");
		AdafruitDcMotor motorRight = motorHat.getDcMotor("M2");
		AdafruitDcMotor motorWrist = motorHat.getDcMotor("M3");
		AdafruitDcMotor motorElbow = motorHat.getDcMotor("M4");			
		
		System.out.println(motorLeft.getName());
		
		/*
		 * Pass through several speeds in forward and reverse direction for all motors.
		 * Negative values for reverse direction, positive is forward direction. 
		 */	
//		for (float speed: new float[] {-1.0f, -0.75f, -0.5f, -0.25f, 0.0f,0.25f, 0.5f, 0.75f, 1.0f}) {
//			System.out.format("Set speed to: %6.2f\n", speed);
//			motorLeft.speed(speed);
////			motorRight.speed(speed);
////			motorWrist.speed(speed);
////			motorElbow.speed(speed);
//
//			//motorHat.sleep() will stop all motors if interrupted.
//			motorHat.sleep(1000);
//		}
		motorLeft.speed(-0.1f);
//			motorRight.speed(speed);
//			motorWrist.speed(speed);
//			motorElbow.speed(speed);

		//motorHat.sleep() will stop all motors if interrupted.
		motorHat.sleep(1000);

//		//stop all motors.
//		motorHat.stopAll();
//
//		//set power but do not set or change the motor state (stop, forward, reverse)
//		motorLeft.power(1.0f);
//
//		//move forward at power level specified above
//		System.out.println("Move foward then coast to a stop");
//		motorLeft.forward();
//		motorHat.sleep(1000);
//
//		//Coast to a stop
//		motorLeft.stop();
//		motorHat.sleep(1000);
//
//		//move reverse at current power level
		System.out.println("Move reverse then coast to a stop");


//
//		//Coast to a stop
//		motorLeft.stop();
//		motorHat.sleep(1000);
//
		//brakeMode will quickly stop motor movement to prevent coasting
		motorLeft.setBrakeMode(true);
		System.out.println("Move forward 5 seconds then brake rather than coast");
		motorLeft.forward(5000);
		motorLeft.stop();
		motorLeft.speed(-1f);
		motorLeft.forward(5000);


		
	}
}