/*
 * SendDataDemoHostApplication.java
 *
 * Copyright (c) 2008-2009 Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.sunspotworld.demo;

import com.sun.spot.io.j2me.radiogram.*;

import com.sun.spot.peripheral.ota.OTACommandServer;
import java.text.DateFormat;
import java.util.Date;
import javax.microedition.io.*;
import com.sun.spot.util.Utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * This application is the 'on Desktop' portion of the SendDataDemo. 
 * This host application collects sensor samples sent by the 'on SPOT'
 * portion running on neighboring SPOTs and just prints them out. 
 *   
 * @author: Vipul Gupta
 * modified: Ron Goldman
 */
public class SendDataDemoHostApplication {
    // Broadcast port on which we listen for sensor samples
    private static final int HOST_PORT = 67;
    private static final int SAMPLE_PERIOD = 1 * 1000;  // in milliseconds
    private static boolean sendFlag = false;
        
    private void run() throws Exception {
        RadiogramConnection rCon;
        Datagram dg;
        DateFormat fmt = DateFormat.getTimeInstance();
        
        try 
        {
            // Open up a server-side broadcast radiogram connection
            // to listen for sensor readings being sent by different SPOTs
            rCon = (RadiogramConnection) Connector.open("radiogram://broadcast:" + HOST_PORT);
            dg = rCon.newDatagram(rCon.getMaximumLength());
        } 
        catch (Exception e) 
        {
             System.err.println("setUp caught " + e.getMessage());
             throw e;
        }
        
        ServerSocket server_socket = null;
        System.out.print("Socket created\n");
        try
        {
            server_socket = new ServerSocket(8888);
        }
        catch(IOException e)
        {
            System.out.println("Could not listen on port: 8888");
            System.exit(-1);
        }
        
        //Socket client_socket = null;
        BufferedReader in = null;
        
        while (true) 
        {
            
            if(sendFlag)
            {
                sendFlag = false;
                Socket client_socket = null;
                try 
                {
                    client_socket = server_socket.accept();
                
                    in = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
                    System.out.println("Buffer Reader Made");
                    String cmd  = in.readLine();
                
                    System.out.println("Socket Rcvd:" + cmd);
                
                    long now = System.currentTimeMillis();
                
                // Package the x and y reading into a radio datagram and send it.
                    dg.reset();
                
                    dg.writeUTF(cmd);
                    rCon.send(dg);

                // Go to sleep to conserve battery
                //Utils.sleep(SAMPLE_PERIOD - (System.currentTimeMillis() - now));
                } 
                catch (Exception e) 
                {
                    System.err.println("Caught " + e + " while collecting/sending sensor sample.");
                }
            
                client_socket.close();
                in.close();
            }
            else
                sendFlag = true;
        }
       
        
        // Main data collection loop
        /*while (true) {
            try {
                // Read sensor sample received over the radio
                rCon.receive(dg);
                String addr = dg.getAddress();  // read sender's Id
                long time = dg.readLong();      // read time of the reading
                int val = dg.readInt();         // read the sensor value
                System.out.println(fmt.format(new Date(time)) + "  from: " + addr + "   value = " + val);
            } catch (Exception e) {
                System.err.println("Caught " + e +  " while reading sensor samples.");
                throw e;
            }
        }*/
    }
    
    /**
     * Start up the host application.
     *
     * @param args any command line arguments
     */
    public static void main(String[] args) throws Exception {
        // register the application's name with the OTA Command server & start OTA running
        OTACommandServer.start("SendDataDemo");

        SendDataDemoHostApplication app = new SendDataDemoHostApplication();
        app.run();
    }
}
