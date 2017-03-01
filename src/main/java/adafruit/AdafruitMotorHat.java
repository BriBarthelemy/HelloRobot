
package adafruit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

/**
 * 
 * This java class has been developed to command the Adafruit DC and Stepper Motor HAT
 * developed for the Raspberry Pi. Look here for technical details on the Motor HAT:
 * https://www.adafruit.com/products/2348
 * 
 * The AdafruitMotorHat class is responsible for commanding the PCA9685 PWM driver chip used in
 * the motor HAT. It also instantiates the AdafruitDcMotor and AdafruitStepperMotor 
 * classes fore each motor.
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
 */
public  class AdafruitMotorHat {
	
	/**
	 * Adafruit Motor Hat device address.
	 */
	public final int   DEVICE_ADDR;
	
	/** 
	 * The PCA9685 chip has 16 LED PWM pin sets used in the commanding of the motor
	 * controllers. These are the register addresses for commanding and setting
	 * values for PWM control.
	 * 
	 * From PCA9685 Product Data Sheet, Rev. 4 - 16 April 2015
	 * This is the register address layout as defined in table 4, section 7.3 
	 * on pages 10-13.
	 */	
	public final int MODE1 			=0X00; //Mode register 1
	public final int MODE2 			=0X01; //Mode register 2
	public final int SUBADR1 		=0X02; //I2C-bus subaddress 1
	public final int SUBADR2 		=0X03; //I2C-bus subaddress 2
	public final int SUBADR3 		=0X04; //I2C-bus subaddress 3
	public final int ALLCALLADR		=0X05; //LED all Call I2C-bus address
	public final int LED0_ON_L		=0X06; //LED0 ON low byte
	public final int LED0_ON_H		=0X07; //LED0 ON high byte
	public final int LED0_OFF_L		=0X08; //LED0 OFF low byte
	public final int LED0_OFF_H		=0X09; //LED0 OFF high byte
	public final int LED1_ON_L		=0X0A; //etc.....
	public final int LED1_ON_H		=0X0B;
	public final int LED1_OFF_L		=0X0C;
	public final int LED1_OFF_H		=0X0D;		
	public final int LED2_ON_L		=0X0E;
	public final int LED2_ON_H		=0X0F;
	public final int LED2_OFF_L		=0X10;
	public final int LED2_OFF_H		=0X11;
	public final int LED3_ON_L		=0X12;
	public final int LED3_ON_H		=0X13;
	public final int LED3_OFF_L		=0X14;
	public final int LED3_OFF_H		=0X15;
	public final int LED4_ON_L		=0X16;
	public final int LED4_ON_H		=0X17;
	public final int LED4_OFF_L		=0X18;
	public final int LED4_OFF_H		=0X19;
	public final int LED5_ON_L		=0X1A;
	public final int LED5_ON_H		=0X1B;
	public final int LED5_OFF_L		=0X1C;
	public final int LED5_OFF_H		=0X1D;
	public final int LED6_ON_L		=0X1E;
	public final int LED6_ON_H		=0X1F;
	public final int LED6_OFF_L		=0X20;
	public final int LED6_OFF_H		=0X21;	
	public final int LED7_ON_L		=0X22;
	public final int LED7_ON_H		=0X23;
	public final int LED7_OFF_L		=0X24;
	public final int LED7_OFF_H		=0X25;
	public final int LED8_ON_L		=0X26;
	public final int LED8_ON_H		=0X27;
	public final int LED8_OFF_L		=0X28;
	public final int LED8_OFF_H		=0X29;
	public final int LED9_ON_L		=0X2A;
	public final int LED9_ON_H		=0X2B;
	public final int LED9_OFF_L		=0X2C;
	public final int LED9_OFF_H		=0X2D;
	public final int LED10_ON_L		=0X2E;
	public final int LED10_ON_H		=0X2F;
	public final int LED10_OFF_L	=0X30;
	public final int LED10_OFF_H	=0X31;
	public final int LED11_ON_L		=0X32;
	public final int LED11_ON_H		=0X33;
	public final int LED11_OFF_L	=0X34;
	public final int LED11_OFF_H	=0X35;	
	public final int LED12_ON_L		=0X36;
	public final int LED12_ON_H		=0X37;
	public final int LED12_OFF_L	=0X38;
	public final int LED12_OFF_H	=0X39;
	public final int LED13_ON_L		=0X3A;
	public final int LED13_ON_H		=0X3B;
	public final int LED13_OFF_L	=0X3C;
	public final int LED13_OFF_H	=0X3D;
	public final int LED14_ON_L		=0X3E;
	public final int LED14_ON_H		=0X3F;
	public final int LED14_OFF_L	=0X40;
	public final int LED14_OFF_H	=0X41;
	public final int LED15_ON_L		=0X42;
	public final int LED15_ON_H		=0X43;
	public final int LED15_OFF_L	=0X44;
	public final int LED15_OFF_H	=0X45;
	// hex 46-F9 reserved 
	public final int ALL_LED_ON_L	=0XFA; //Load all LEDn_ON_L
	public final int ALL_LED_ON_H	=0XFB; //Load all LEDn_ON_H
	public final int ALL_LED_OFF_L	=0XFC; //Load all LEDn_OFF_L
	public final int ALL_LED_OFF_H	=0XFD; //Load all LEDn_OFF_H
	public final int PRE_SCALE		=0XFE; //prescale for PWM output frequency
	public final int TEST_MODE		=0XFF; //Defines test mode to be entered 	
	
	/* 
	 * Command values for PCA9685 chip 
	 * From PCA9685 Product Data Sheet, Rev. 4 - 16 April 2015
	 * See tables 5 & 6, pages 14-16
	 */
	private final int COMMAND_SLEEP   = 0X10; //MODE1 command, enable sleep, Oscillator off
	private final int COMMAND_ALLCALL = 0X01; //MODE1 command, enable LED ALLCALL 	
	private final int COMMAND_OUTDRV  = 0x04; //MODE2 command, 16 LED outputs are configured with totem pole structure
	//private final int COMMAND_INVRT   = 0X10; //MODE2 command, output logic  state is inverted
	//private final int COMMAND_RESTART = 0X80; //MODE1 command, enable restart mode

	private I2CBus motorHatI2C;
	private I2CDevice motorHatDevice;
	
	//Map tracks if a DC motor has already been allocated.
	private Map<String,Boolean> dcMotorAllocated = new HashMap<String,Boolean>();
	{
		dcMotorAllocated.put("M1",false);
		dcMotorAllocated.put("M2",false);
		dcMotorAllocated.put("M3",false);
		dcMotorAllocated.put("M4",false);
	} 
	
	//Default Adafruit Motor Hat Device Address
	private final int  DEFAULT_DEVICE_ADDR = 0X060;
	
	//I2C Bus Address
    private final int DEFAULT_I2C_BUS = I2CBus.BUS_1;
    private int I2C_BUS;
    
    //Register addresses for commanding all LED PWMs simultaneously
    private final int[]  pwmAll = new int[] {ALL_LED_ON_L,ALL_LED_ON_H,ALL_LED_OFF_L,ALL_LED_OFF_H};
    
    //Corresponding values to stop all LED PWMs
    private final byte[] pwmAllStop = new byte[] {0X00, 0X00, 0X00, 0X00};
   

    /**
     * Constructor assumes default Adafruit Motor Hat device address (0X0060)
     * and I2C Bus address I2CBus.BUS_1
     */
	public AdafruitMotorHat() {
		DEVICE_ADDR = DEFAULT_DEVICE_ADDR;
		I2C_BUS = DEFAULT_I2C_BUS;
		setup();
	}
	
	/**
	 * Pass Adafruit Motor Hat device address to constructor.
	 * Note up to 32 unique Motor Hats can be stacked on the Raspberry Pi.
	 * Each requires a unique device address that can be set by solder points
	 * on the hat.
	 * @param deviceAddr Valid addresses range 0X0060 to 0X007F
	 */
	public AdafruitMotorHat(int deviceAddr) {
		checkDeviceAddr(deviceAddr);
		DEVICE_ADDR = deviceAddr;
		I2C_BUS = DEFAULT_I2C_BUS;
		setup();
	}
	
	/**
	 * Pass I2C Bus number and Adafruit Motor Hat device address to constructor.
	 * Note up to 32 unique Motor Hats can be stacked on the Raspberry Pi.
	 * Each requires a unique device address that can be set by solder points
	 * on the hat. 
	 * @param deviceAddr Valid addresses range 0X0060 to 0X007F
	 * @param i2cBus Valid bus numbers are I2CBus.BUS_1 or I2CBus.BUS_2
	 */
	public AdafruitMotorHat(int deviceAddr, int i2cBus) {
		checkDeviceAddr(deviceAddr);
		DEVICE_ADDR = deviceAddr;
		checkBus(i2cBus);
		I2C_BUS = i2cBus;
		setup();
	}
	
	/**
	 * Check for a valid Adafruit Motor Hat device address
	 * @param deviceAddr Valid values range 0X0060 to 0X007F
	 */
	private void checkDeviceAddr(int deviceAddr) {
		if (deviceAddr < 0X0060 || deviceAddr > 0X007F) {
			System.out.println("*** Error *** Illegal AdafruitMotorHat device address must be in rage 0X0060 to 0X007F");
			throw new IllegalArgumentException(Integer.toString(deviceAddr));
		}
	}
	
	/**
	 * Check for valid I2C Bus address
	 * @param i2cBus Is this I2C Bus value valid?
	 */
	private void checkBus(int i2cBus) {
		if (i2cBus != I2CBus.BUS_1 && i2cBus != I2CBus.BUS_1) {
			System.out.println("*** Error *** - Illega I2C Bus address must be I2CBus.BUS_1 or I2CBus.BUS_2");
			throw new IllegalArgumentException(Integer.toString(i2cBus));
		}
	}
	
	/**
	 * Setup the MotorHat for commanding motors.
	 */
	private void setup() {	
		try {
			motorHatI2C = I2CFactory.getInstance(I2C_BUS);		
			motorHatDevice = motorHatI2C.getDevice(DEVICE_ADDR);
			
			//Enable the All Call mode to simultaneously command all LED PWMs
			motorHatDevice.write(MODE1, (byte) COMMAND_ALLCALL);
			
			//16 LED outputs are configured with totem pole structure
			motorHatDevice.write(MODE2, (byte) COMMAND_OUTDRV);
			//wait for oscillator
			sleep(5); 
			
			//read MODE1 Register to get existing state
			int mode1 = motorHatDevice.read(MODE1);
			if (mode1 < 0) {
				System.out.println("*** Error *** IC2 read returns negative value.");
				stopAll();
				throw new IOException(Integer.toString(mode1));
			}
			//No sleeping allowed
			mode1 = mode1 & ~COMMAND_SLEEP; 
			//Write back the MODE1 register with no sleep
			motorHatDevice.write(MODE1, (byte) mode1);
			//wait for oscillator
			sleep(5);
		}  catch (Exception e) {
			System.out.println("*** Error *** setup fails to commnicate with MotorHat device");
			stopAll();
			e.printStackTrace();
		}
   	
	}
	
	/**
	 * Write the 8-bit value to the indicated address
	 * @param addr - register address I2C device
	 * @param value - value to write at register address
	 */
	public void write(int addr, byte value) {		
		try {
			motorHatDevice.write(addr,value);
		} catch (IOException e) {
			System.out.println("*** ERROR *** Can not perform I2C write to AdafruitMotorHat Device");
			e.printStackTrace();
		}
	}

	/**
	 * Sleep and force all motors to stop if interrupted.
	 * @param milliseconds Sleep time
	 */
	public void sleep(long milliseconds) {		
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			System.out.println("*** ERROR *** Interrupted sleep");
			stopAll();
			e.printStackTrace();
		}
	}
	/**
	 * Stop all motors for this MotorHat. 
	 */
	public void stopAll() {
		try {
			for (int i=0; i<4; i++) {
				motorHatDevice.write(pwmAll[i], pwmAllStop[i]);
			}
		} catch (IOException e) {
			System.out.println("*** ERROR *** Can not perform I2C write to AdafruitMotorHat Device");
			e.printStackTrace();
		}
	}

	/**
	 * Create an AdafruitDcMotor instance for a motor.
	 * Check for a valid motor value and that the motor has not been
	 * previously allocated.
	 * @param motor Valid values are "M1", "M2", "M3", "M4"
	 * @return
	 */
    public AdafruitDcMotor getDcMotor(String motor) {
    	//Motor value is valid?   	
    	if (motor != "M1" && motor != "M2" && motor != "M3" && motor!= "M4") {
    		System.out.println("*** Error *** Motor specified not valid, must be \"M1\", \"M2\", \"M3\", or \"M4\"");
    		throw new IllegalArgumentException(motor);
    	}
    	//Has motor already been allocated?
    	if (dcMotorAllocated.get(motor)) {
    		System.out.println("*** Error *** Motor already allocated");
			throw new IllegalArgumentException(motor);
    	}
    	//Set flag to indicate motor has been allocated.
    	dcMotorAllocated.put(motor, true);
    	
    	//Create an instance for this motor.
    	return new AdafruitDcMotor(AdafruitMotorHat.this, motor);
    }
}
