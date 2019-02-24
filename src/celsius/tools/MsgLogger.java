//
// Celsius Library System
// (w) by C. Saemann
//
// Msg.java
//
// This class contains the logging system
//
// typesafe
//
// checked 16.09.2007
//

package celsius.tools;

import java.io.IOException;
import java.util.Arrays;

public class MsgLogger {
    
    public TextFile repfile;
    public final String name;
    public final boolean out;
    public int detail=0;
    
    public MsgLogger(String s,boolean b) throws IOException {
        name=s;
        repfile=new TextFile(s,true);
        out=b;
    }
    
    public void repS() throws IOException {
        repfile.putString("");
        if (out) System.out.println();
    }
    
    public void repS(int i) throws IOException {
        if (i>detail) return;
        System.out.println("---");
        System.out.println(i);
        System.out.println(detail);
        repfile.putString("");
        if (out) System.out.println();
    }
    
    // Output which is also forced to the screen
    public void repF() throws IOException {
        repfile.putString("");
        System.out.println();
    }
    
    // Output which is also forced to the screen
    public void repF(String s) throws IOException {
        repfile.putString(s);
        System.out.println(s);
    }
    
    // Ordinary output to report file
    public void rep(String s) throws IOException {
        repfile.putString(s);
        if (out) System.out.println(s);
    }

    // Output without returning IOExceptions
    public void repS(String s) {
        try {
            repfile.putString(s);
        } catch (IOException ex) { ex.printStackTrace(); }
        if (out) System.out.println(s);
    }
    
    public void rep(int i,String s) throws IOException {
        if (i>detail) return;
        repfile.putString(s);
        if (out) System.out.println(s);
    }

    public void repS(int i,String s) throws IOException {
        if (i>detail) return;
            try {
                repfile.putString(s);
            } catch (IOException ex) { ex.printStackTrace(); }
        if (out) System.out.println(s);
    }

    
    public void repS(boolean b) throws IOException {
        if (b) repfile.putString("true"); else repfile.putString("false");
        if (out) System.out.println(b);
    }
    
    public void repO(String s) throws IOException {
        repfile.putStringO(s);
        if (out) System.out.print(s);
    }
    
    public void repO(int i,String s) throws IOException {
        if (i>detail) return;
        repfile.putStringO(s);
        if (out) System.out.print(s);
    }
    
    @Override
    public void finalize() throws IOException {
        repfile.close();
    }

    public void reset() throws IOException {
        repfile.close();
        TextFile.Delete(name);
        repfile=new TextFile(name,true);
    }

    public void printStackTrace(Exception ex) {
        ex.printStackTrace();
        String tmp="";
        for (StackTraceElement trace : Arrays.asList(ex.getStackTrace())) {
            tmp+=trace.toString()+"\n";
        }
        this.repS("-----------------Stack Trace--------------");
        this.repS(tmp);
        this.repS("------------------------------------------");
    }

    public void printStackTrace(Error e) {
        e.printStackTrace();
        String tmp="";
        for (StackTraceElement trace : Arrays.asList(e.getStackTrace())) {
            tmp+=trace.toString()+"\n";
        }
        this.repS("-----------------Stack Trace--------------");
        this.repS(tmp);
        this.repS("------------------------------------------");
    }
    
}