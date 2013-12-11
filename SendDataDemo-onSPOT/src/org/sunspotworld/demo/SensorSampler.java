package org.sunspotworld.demo;

import com.sun.spot.io.j2me.radiogram.*;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.resources.transducers.ITriColorLEDArray;
import com.sun.spot.resources.transducers.ILightSensor;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.Servo;
import com.sun.spot.util.Utils;
import java.util.Date;
import javax.microedition.io.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import java.lang.String;
import java.lang.Object;
import java.io.*;

/**
 * This application is the 'on SPOT' portion of the CrawlerControl. It
 * receives a datagram from the basestation running the onDesktop version
 * and controls the crawler based on the xtilt and ytilt values.
 *
 * @author: Sahil Mehta, Robert K. Lee, Maria Kromis, Sanjana Srinivasan
 */
public class SensorSampler extends MIDlet {

    private static final int HOST_PORT = 67;
    private static final int SAMPLE_PERIOD = 1 * 1000;  // in milliseconds
    private static final int SERVO_CENTER_VALUE = 1500;
    private static final int SERVO1_MAX_VALUE = 2000;
    private static final int SERVO1_MIN_VALUE = 1000;
    private static final int SERVO2_MAX_VALUE = 2000;
    private static final int SERVO2_MIN_VALUE = 0;
    private static final int SERVO1_HIGH = 100; //steering step high
    private static final int SERVO1_LOW = 80; //steering step low
    private static final int SERVO2_HIGH = 10; //speeding step high
    private static final int SERVO2_LOW = 5; //speeding step low
    private static final int PROG0 = 0; //default program
    private static final int PROG1 = 1; // reversed program
    private static final int THRESHOLD = 20; // difference between d1 & d2
    private static final int OFFSET_TURN = 1550;
    private static final int OFFSET_ESC = 1600;
    private static final int GO_FORWARD_SPEED = 1300;
    private static final int GO_REVERSE_SPEED = 1850;
    private static int CONTROL = 0;
    private static int GO = 0;
    
    
    private EDemoBoard eDemo = EDemoBoard.getInstance();
    
    private Servo servo1 = new Servo(eDemo.getOutputPins()[EDemoBoard.H1]);
    private Servo ESC = new Servo(eDemo.getOutputPins()[EDemoBoard.H2]);
    ITriColorLEDArray myLEDs = (ITriColorLEDArray) Resources.lookup(ITriColorLEDArray.class);
    ITriColorLED led0 = myLEDs.getLED(0);
    ITriColorLED led1 = myLEDs.getLED(1);
    ITriColorLED led2 = myLEDs.getLED(2);
    ITriColorLED led3 = myLEDs.getLED(3);
    ITriColorLED led4 = myLEDs.getLED(4);
    ITriColorLED led5 = myLEDs.getLED(5);
    ITriColorLED led6 = myLEDs.getLED(6);
    ITriColorLED led7 = myLEDs.getLED(7);
    
    
    protected void startApp() throws MIDletStateChangeException {
        RadiogramConnection rCon = null;
        Datagram dg = null;
        String ourAddress = System.getProperty("IEEE_ADDRESS");
       // ILightSensor lightSensor = (ILightSensor)Resources.lookup(ILightSensor.class);
        //ITriColorLED led = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED0");
       // ITriColorLEDArray myLEDs = (ITriColorLEDArray) Resources.lookup(ITriColorLEDArray.class);
        
        
        System.out.println("Starting sensor sampler application on " + ourAddress + " ...");
       
	// Listen for downloads/commands over USB connection
	new com.sun.spot.service.BootloaderListenerService().getInstance().start();

        try {
            // Open up a broadcast connection to the host port
            // where the 'on Desktop' portion of this demo is listening
            rCon = (RadiogramConnection) Connector.open("radiogram://:" + HOST_PORT);
            dg = rCon.newDatagram(50);  // only sending 12 bytes of data
        } catch (Exception e) {
            System.err.println("Caught " + e + " in connection initialization.");
            notifyDestroyed();
        }
       
        int count = 0;
        double xtilt = 0;
        double ytilt = 0;
        int last = 0;
        
        Utils.sleep(1000);
        
        /*(int i = 1300; i < 1700 ; i++)
        {
            forward(i);
            //Utils.sleep(20);
            
        }*/
        
        forward(1600);
        servo1.setValue(OFFSET_TURN);
        Utils.sleep(1000);
        
        myLEDs.setColor(LEDColor.YELLOW);
        myLEDs.setOn();
        Utils.sleep(500);
        myLEDs.setOff();
        
        
        while (true) 
        {
            try 
            {
                // Read sensor sample received over the radio
                rCon.receive(dg);
                String addr = dg.getAddress();  // read sender's Id
                String val = dg.readUTF();
                System.out.println("From: " + addr + "   value = " + val);
                count = 0;
                last = 0;
                int length = val.length();
                for(int i = 0; i < length; i++)
                {
                    if((val.charAt(i) == ',') && (count == 0))
                    {
                         count += 1;
                         last = i;
                         xtilt =  Double.parseDouble(val.substring(0,i).trim());
                         System.out.print("x_val = " + xtilt);
                    //     System.out.println("val = " + val);
                    }
                    else if((val.charAt(i) == ',') && (count == 1))
                    {
                        ytilt =  Double.parseDouble(val.substring(last+1,i).trim());
                        System.out.print(", y_val = " + ytilt);
                        System.out.println();
                        last = 0;
                        count = 0;
                        break;
                    }
                    length = val.length();
                    
                }
                
                Go(xtilt,ytilt);
                
            } 
            catch (Exception e) 
            {
                System.err.println("ERROR: " + e.getMessage());
            }
            
            //Go(xtilt,ytilt);
        }
        
    }
    
    private void Go(double xtilt, double ytilt) {
        if (xtilt > 15) 
        {
           forward(GO_REVERSE_SPEED);
           for(int i = 0; i < 8; i++)
           {
               myLEDs.getLED(i).setColor(LEDColor.CHARTREUSE);
               myLEDs.getLED(i).setOn();
           }
           
        } 
        else if (xtilt < -15) 
        {
            forward(GO_FORWARD_SPEED);
            for(int i = 0; i < 8; i++)
           {
               myLEDs.getLED(i).setColor(LEDColor.CYAN);
               myLEDs.getLED(i).setOn();
           }
        }
        if(xtilt == 0)
        {
            forward(OFFSET_ESC);
            for(int i = 0; i < 8; i++)
            {
                myLEDs.getLED(i).setColor(LEDColor.CYAN);
                myLEDs.getLED(i).setOn();
            }
        }
        
       /* else
        {
            forward(OFFSET_ESC);
            for(int i = 0; i < 8; i++)
            {
                myLEDs.getLED(i).setColor(LEDColor.RED);
                myLEDs.getLED(i).setOn();
            }
        }*/
        
        if (ytilt > 15) 
        {
            right(OFFSET_TURN - (int)(ytilt * 8));
            for(int i = 4; i < 8; i++)
            {
               myLEDs.getLED(i).setOff();
               myLEDs.getLED(i).setColor(LEDColor.RED);
               myLEDs.getLED(i).setOn();
            }
        } 
        else if (ytilt < -15) 
        {
            left(OFFSET_TURN - (int)(ytilt * 8));
            for(int i = 0; i < 4; i++)
            {
               myLEDs.getLED(i).setOff();
               myLEDs.getLED(i).setColor(LEDColor.RED);
               myLEDs.getLED(i).setOn();
            }
        }
        /*else
        {
            right(OFFSET_TURN);
            for(int i = 0; i < 8; i++)
            {
                myLEDs.getLED(i).setColor(LEDColor.RED);
                myLEDs.getLED(i).setOn();
            }
        }*/
    }
    
    private void left(int myStep) 
    {
        System.out.println("left");
        servo1.setValue(myStep);
        //Utils.sleep(50);
    }

    private void right(int myStep) 
    {
        System.out.println("right");
        servo1.setValue(myStep);
        //Utils.sleep(50);
    }
    private void stop() 
    {
        ESC.setValue(SERVO_CENTER_VALUE);
    }
    
    private void forward(int myStep) 
    {
        System.out.println("Going forwards with speed: " + myStep);
        ESC.setValue(myStep);
        //Utils.sleep(50);
    }
    
    
    
    protected void pauseApp() {
        // This will never be called by the Squawk VM
    }
    
    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
        // Only called if startApp throws any exception other than MIDletStateChangeException
    }
}
