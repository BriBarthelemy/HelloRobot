package adafruit;

import java.util.Map;

import com.pi4j.component.motor.Motor;
import com.pi4j.component.motor.MotorState;

/**
 * This java class has been developed to command the Adafruit DC and Stepper Motor HAT
 * developed for the Raspberry Pi. Look here for technical details on the Motor HAT:
 * https://www.adafruit.com/products/2348
 * 
 * The AdafruitDCMotor class sets PWM values to control DC Motor speed and direction.
 * Commands are passed through the AdaFruitMotorHat class. A class needs to be 
 * instantiated for each motor in the HAT. 
 * 
 * 
 * An Adafruit Motor HAT can drive drive up to 4 DC or 2 Stepper motors with 
 * full PWM speed and direction control. The fully-dedicated PCA9685 PWM driver chip  
 * controls motor direction and speed. This chip handles all the motor and 
 * speed controls over I2C. Only two pins (SDA & SCL) are required to drive the 
 * multiple motors, and since it's I2C you can also connect any other I2C devices or 
 * HATs to the same pins.
 * 
 * This means up to 32 Adafruit Motor HATs can be stacked to control up to 128 DC motors or
 * 64 stepper motors. The default I2C address for a HAT is 0x60 but can be modified with
 * solder connections to alter the I2C Address. Motors are controlled with the TB6612 MOSFET driver 
 * with 1.2A per channel current capability (20ms long bursts of 3A peak).
 * 
 * @author Eric Eliason
 *
 */
public class AdafruitDcMotor implements Motor {
	//Adafruit Motor Hat used to command motor functions
	private AdafruitMotorHat motorHat;
	
	//Motor name "M1" through "M4"
	private String motor;
	
	//Register addresses for PWM that controls motor speed
	private int[] pwm;
	//Corresponding values to control motor speed for each PWM address
	private byte[] pwmValues;
	
	//Register addresses for first PWM that controls motor direction
	private int[] in1;
	//Corresponding values to control motor direction. 
	private byte[] in1Values;
	//Used in brakeMode to switch motor direction
	private byte[] in1Switch;
	
	
	//Register addresses for second PWM that controls motor direction
	private int[] in2;
	//Corresponding values to control motor direction
	private byte[] in2Values;
	//Used in brakeMode to switch motor direction.
	private byte[] in2Switch;
	
	//Speed value converted to raw PWM value
	private int  rawSpeed;
	//Low-order byte of rawSpeed
	private byte low;
	//High-order byte for raw speed
	private byte high;
	
	//Speed setting for motor (-1.0 to 1.0, - for reverse, + for forward)
	private float speed = 0.0f;
	//Power setting (0.0 to 1.0) used in conjunction with forward() and reverse() methods.
	private float power = 0.0f;
	
	//Indicates if motor is stopped, forward, or reverse state
	private MotorState motorState;
	
	/*
	 * The brakeMode is used in the stop method to abruptly stop a motor.
	 * If set to true, the motor is temporary reversed for a set number of
	 * milliseconds. This mode minimizes the coasting time for a motor.
	 * If brakeMode is set to false then the motor will coast to a stop. 
	 */
	private boolean brakeMode = false;
	
	/*
	 * The brakeModeValue specifies the number of milliseconds to reverse
	 * the motor direction to minimize motor coasting. This value can
	 * be set by the user with the method setBreakModeValue(milliseconds)
	 * This value is highly dependent on the properties of the motor in use
	 * and ideally should be set by the caller. 
	 */
	private long brakeModeValue = 35;
	
	//PWM values for setting a motor for power, stop, forward, and reverse directions
	private final byte[] pwmStop    = new byte[] {0X00, 0X00, 0X00, 0X00};
	private final byte[] pwmForward = new byte[] {0X00, 0X10, 0X00, 0X00};	
	private final byte[] pwmReverse = new byte[] {0X00, 0X00, 0X00, 0X10};	
	private byte[] pwmPower;
	
	/**
	 * Constructor 
	 * @param motorHat AdafruitMotorHat
	 * @param motor Motor value "M1", "M2", "M3", or "M4"
	 */
	public AdafruitDcMotor(AdafruitMotorHat motorHat, String motor) {
		this.motorHat = motorHat;
		this.motor  = motor;		
		setup();
	}

	/**
	 * For the given motor (M1-M4) set up the LED PWM register address for that motor.
	 */
	private void setup() {
		//Check for valid motor value
		if (motor != "M1" && motor != "M2" && motor != "M3" && motor!= "M4") {
			System.out.println("*** Error *** Illegal motor value must be \"M1\",\"M2\",\"M3\",\"M4\"");
			motorHat.stopAll();
			throw new IllegalArgumentException(motor);
		}
		
		/*
		 * Each of the four DC motor controllers have different PCA9685 LED PWM pins to control power and motor direction
		 * Information on the PWM wiring can be found on the Adafruit motor hat schematics found here:
		 * https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/downloads
		 */
		if (motor == "M1") {
			pwm = new int[] {motorHat.LED8_ON_L,  motorHat.LED8_ON_H,  motorHat.LED8_OFF_L,  motorHat.LED8_OFF_H};
			in2 = new int[] {motorHat.LED9_ON_L,  motorHat.LED9_ON_H,  motorHat.LED9_OFF_L,  motorHat.LED9_OFF_H};
			in1 = new int[] {motorHat.LED10_ON_L, motorHat.LED10_ON_H, motorHat.LED10_OFF_L, motorHat.LED10_OFF_H};						
		}
		else if (motor == "M2") {
			pwm = new int[] {motorHat.LED13_ON_L, motorHat.LED13_ON_H, motorHat.LED13_OFF_L, motorHat.LED13_OFF_H};
			in2 = new int[] {motorHat.LED12_ON_L, motorHat.LED12_ON_H, motorHat.LED12_OFF_L, motorHat.LED12_OFF_H};
			in1 = new int[] {motorHat.LED11_ON_L, motorHat.LED11_ON_H, motorHat.LED11_OFF_L, motorHat.LED11_OFF_H};								
		}
		else if (motor == "M3") {
			pwm = new int[] {motorHat.LED2_ON_L,  motorHat.LED2_ON_H,  motorHat.LED2_OFF_L,  motorHat.LED2_OFF_H};
			in2 = new int[] {motorHat.LED3_ON_L,  motorHat.LED3_ON_H,  motorHat.LED3_OFF_L,  motorHat.LED3_OFF_H};
			in1 = new int[] {motorHat.LED4_ON_L,  motorHat.LED4_ON_H,  motorHat.LED4_OFF_L,  motorHat.LED4_OFF_H};								
		} 
		else {
			pwm = new int[] {motorHat.LED7_ON_L,  motorHat.LED7_ON_H,  motorHat.LED7_OFF_L,  motorHat.LED7_OFF_H};
			in2 = new int[] {motorHat.LED6_ON_L,  motorHat.LED6_ON_H,  motorHat.LED6_OFF_L,  motorHat.LED6_OFF_H};
			in1 = new int[] {motorHat.LED5_ON_L,  motorHat.LED5_ON_H,  motorHat.LED5_OFF_L,  motorHat.LED5_OFF_H};								
		}
		
		//Command the PC9865 to stop the motor
		pwmValues = pwmStop;
		in2Values = pwmStop;
		in1Values = pwmStop;
		sendCommands();
		motorState = MotorState.STOP;
		
	}
	
	/**
	 * Command the LED PWMs to set the motor speed and motor direction (forward or backward)
	 */
	private void sendCommands() {
		for (int i=0; i<4; i++) {
			motorHat.write(pwm[i],pwmValues[i]);
		}
		for (int i=0; i<4; i++) {
			motorHat.write(in2[i],in2Values[i]);
		}
		for (int i=0; i<4; i++) {
			motorHat.write(in1[i],in1Values[i]);
		}		
	}
	
	/**
	 * Convert the motor speed (-1.0 to 1.0) to the LED PWM values
	 * @param speed Valid range -1.0 to 1.0, positive numbers forward direction negative backward
	 */
	private void setHighLow(float speed) {
		rawSpeed = Math.round(Math.abs(speed)*255*16);		
		low  = (byte) (rawSpeed & 0xFF);  //Extract low-order byte
		high = (byte) (rawSpeed >> 8);    //Extract high-order byte
		pwmPower = new byte[] {0X00, 0X00, low, high};
	}
	
	/**
	 * Set the speed for the DC motor
	 * @param speed Valid ranges -1.0 to 1.0
	 * 
	 */
	public void speed(float speed) {
		if (speed < -1.0 || speed > 1.0) {
			System.out.println("*** Error *** Speed value must be in range -1.0 to 1.0");
			motorHat.stopAll();
			throw new IllegalArgumentException(Float.toString(speed));
		}
		this.speed = speed;
		this.power = Math.abs(this.speed);
		setHighLow(this.speed);
		pwmValues = pwmPower;
		
		//sets up the commanding values for the LED PWMs 
		if (this.speed == 0.0) {
			//turn off PWMs
			stop();
			motorState = MotorState.STOP;
		}
		else if (this.speed > 0.0) {
			//turn on PWMs for forward direction
			in2Values = pwmForward;
			in1Values = pwmReverse;
			motorState = MotorState.FORWARD;
		}
		else {
			//turn on PWMS for backward direction
			in2Values = pwmReverse;	
			in1Values = pwmForward;
			motorState = MotorState.REVERSE;
			
		}
		//Command the PCA9685 for setting speed and direction of the DC motor
		sendCommands();		
	}
	
	/**
	 * Return speed value for the motor
	 * @return Valid range (-1.0 maximum reverse speed to 1.0 maximum forward speed)
	 */
	public float getSpeed() {
		return speed;
	}

	/**
	 * Set the power value (speed) for the DC Motor. This method is used in combination
	 * with the forward() and reverse() methods
	 * @param power Valid value range (0.0-stop to 1.0-maximum power)
	 */
	public void power(float power) {
		if (power < 0.0 || power > 1.0) {
			System.out.println("*** Error *** Power value must be in range 0.0 to 1.0");
			motorHat.stopAll();
			throw new IllegalArgumentException(Float.toString(power));
		}
		//set low-order and high-order bytes for power settings.
		setHighLow(power);
	}
	
	/**
	 * Return power value for the motor
	 * @return Valid range 0.0 (no power) to 1.0 (maximum power)
	 */
	public float getPower() {
		return power;
	}

	/**
	 * Set the brake mode for the stop() method.
	 * If true then the stop() method will temporarily switch the motor direction
	 * at the same power level to quickly stop the motor movement.
	 * If false the stop() motor will allow the motor to coast to a stop.
	 * @param brakeMode 
	 */
	public void setBrakeMode(boolean brakeMode) {
		this.brakeMode = brakeMode;
		
	}
	/**
	 * The method specifies the number of milliseconds to use in
	 * in brake mode for switching the motor direction to
	 * quickly stop the motor movement. This value is highly
	 * dependent on the motor being commanded and should be
	 * set by the caller if precise braking is required.
	 * 
	 * @param brakeModeValue in milliseconds
	 */
	public void setBrakeModeValue(long brakeModeValue) {
		if (brakeModeValue < 1 || brakeModeValue > 100) {
			System.out.println("*** Error *** brakeModeValue must be in range 1 - 100 milliseconds");
			motorHat.stopAll();
			throw new IllegalArgumentException(Long.toString(brakeModeValue));
		}
		this.brakeModeValue = brakeModeValue;
	}
	
	/**
	 * Command the DC motor to go in the forward direction
	 */
//	@Override
	public void forward() {
		pwmValues = pwmPower;
		in2Values = pwmForward;
		in1Values = pwmReverse;
		//Command the PCA9685 for forward direction
		sendCommands();
		motorState = MotorState.FORWARD;
	}

	/**
	 * Command the DC motor to go in the forward direction for the time
	 * specified
	 */
//	@Override
	public void forward(long milliseconds) {
		pwmValues = pwmPower;
		in2Values = pwmForward;
		in1Values = pwmReverse;
		//Command the PCA9685 for forward direction
		sendCommands();
		motorState = MotorState.FORWARD;
		
		//Time to go to sleep
		motorHat.sleep(milliseconds);
		
		stop();
		motorState = MotorState.STOP;
	}

	/** 
	 * Command the DC motor to go in the reverse direction
	 */
//	@Override
	public void reverse() {
		pwmValues = pwmPower;
		in2Values = pwmReverse;
		in1Values = pwmForward;
		//Command the PCA9685 for reverse direction
		sendCommands();
		motorState = MotorState.REVERSE;
	}

	/**
	 * Command the DC motor to go in the reverse direction for the time
	 * specified
	 */
//	@Override
	public void reverse(long milliseconds) {
		pwmValues = pwmPower;
		in2Values = pwmReverse;
		in1Values = pwmForward;
		//Command the PCA9685 for reverse direction
		sendCommands();
		motorState = MotorState.REVERSE;
		
		//Time to sleep
		motorHat.sleep(milliseconds);
		
		in2Values = pwmStop;
		in1Values = pwmStop;
		//Command the PC9685 to stop the motor
		sendCommands();
		motorState = MotorState.STOP;
	}

	/**
	 * Stop the motor
	 */
//	@Override
	public void stop() {
		//if brakeMode then temporary switch direction to quickly brake motor.
		if (brakeMode) {
			in2Switch = in1Values;
			in1Switch = in2Values;
			in1Values = in1Switch;
			in2Values = in2Switch;
			sendCommands();
			motorHat.sleep(brakeModeValue);			
		}
		in2Values = pwmStop;
		in1Values = pwmStop;
		sendCommands();
		motorState = MotorState.STOP;
	}

	/**
	 * Returns motor name
	 */
////	@Override
	public String getName() {
		//Here's our generated device
		return String.format("Adafuit DcMotor Device: 0X%04X Motor: %s", motorHat.DEVICE_ADDR, motor);
	}
	
	/**
	 * Is the motor state the value passed to this method?
	 * returns true or false
	 */
////	@Override
	public boolean isState(MotorState state) {
		return (motorState == state);
	}

	/**
	 * Is the motor stopped?
	 * returns true of false
	 */
////	@Override
	public boolean isStopped() {
		return (motorState == MotorState.STOP);
	}
	
	/**
	 * Return the motor state. Values returned:
	 * MotorState.STOP
	 * MotorState.FORWARD
	 * MotorState.BACKWARD
	 */
////	@Override
	public MotorState getState() {
		//We're tracking the motor state with this variable
		return motorState;
	}
	

	/****************************************************
	 * Methods below are place holders 
	 ***************************************************/
	
	/**
	 * Place holder, does nothing
	 */
//	@Override
	public void setName(String name) {
		//Nothing to do here.	
	}
	
	/**
	 * Place holder, does nothing
	 */	
//	@Override
	public void setTag(Object tag) {
		//Nothing to do here
	}


	/**
	 * Place holder, does nothing
	 */
//	@Override
	public Object getTag() {
		//nothing to do here
		return null;
	}


	/**
	 * Place holder, does nothing
	 */
//	@Override
	public void setProperty(String key, String value) {
		//nothing to do here
	}


	/**
	 * Place holder, does nothing
	 */
//	@Override
	public boolean hasProperty(String key) {
		//nothing to do here
		return false;
	}


	/**
	 * Place holder, does nothing
	 */
//	@Override
	public String getProperty(String key, String defaultValue) {
		//nothing to do here
		return null;
	}


	/**
	 * Place holder, does nothing
	 */
//	@Override
	public String getProperty(String key) {
		//nothing to do here
		return null;
	}


	/**
	 * Place holder, does nothing
	 */
//	@Override
	public Map<String, String> getProperties() {
		//nothing to do here
		return null;
	}


	/**
	 * Place holder, does nothing
	 */
//	@Override
	public void removeProperty(String key) {
		//nothing to do here		
	}


	/**
	 * Place holder, does nothing
	 */
//	@Override
	public void clearProperties() {
		//nothing to do here		
	}
	

	/**
	 * Place holder, does nothing
	 */
//	@Override
	public void setState(MotorState state) {
		//We don't want callers to set the motor state this way
	}
}
