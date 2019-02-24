/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package celsius.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author cnsaeman
 */
public class StreamGobbler extends Thread {

    private InputStream is;
    private String type;
    private StringBuffer output;

    public StreamGobbler(InputStream is, String type) {
        this.is = is;
        this.type = type;
        output=new StringBuffer(1000);
    }

    public String getOutput() {
        return(output.toString());
    }

    public void clearOutput() {
        output=new StringBuffer(1000);
    }

    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line).append("\n");
                //if (output.length()>1000000) output=new StringBuffer(1000);
            }
            br.close();
            isr.close();
        } catch (IOException ioe) {
            //ioe.printStackTrace();
        }
    }

}
