//
// Celsius Library System
// (w) by C. Saemann
//
// SynchronizeLibThread.java
//
// This class synchronizes the physical files with the library
//
// typesafe
//
// checked: 16.09.2007
//

package celsius.Threads;

import celsius.Item;
import celsius.Library;
import celsius.MainFrame;
import celsius.SafeMessage;
import celsius.tools.InteractiveFileCopy;
import celsius.tools.Parser;
import celsius.tools.TextFile;
import celsius.tools.toolbox;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.SwingUtilities;

public class ThreadSynchronizeUSBdev extends Thread {
    
    private final Library Lib;
    private final MainFrame MF;
    private final String device;
    private final String targetDir;
    private final ArrayList<String> categories;
        
    // Constructor
    public ThreadSynchronizeUSBdev(MainFrame mf, Library L, String dev) {
        MF=mf; Lib=L; device=dev;
        targetDir=Lib.usbdrives.get(device).get(0);
        categories=Lib.usbdrives.get(device);
    }

    // Main Routine
    @Override
    public void run() {
        setStatusBegin();

        String[] list=(new File(targetDir)).list();
        ArrayList<String> trg=new ArrayList<String>();
        try {
            trg=new ArrayList<String>(Arrays.asList(list));
        } catch (Exception e) {
            e.printStackTrace();
            warning("The path to the USB device "+device+" was not found.\nIs the device connected?");
            return;
        }

        ArrayList<String> src=new ArrayList<String>();
        for (Item doc : Lib) {
            for (String cat : categories) {
                if (Parser.EnumContains(doc.get("autoregistered"), cat) || Parser.EnumContains(doc.get("registered"), cat)) {
                    if (!src.contains(doc.getCompleteDirS("location"))) src.add(doc.getCompleteDirS("location"));
                }
            }
        }
        ArrayList<String> src2=new ArrayList<String>();
        for (String s : src)
            src2.add((new File(s)).getName());
        String bd=targetDir;
        if (!bd.endsWith(toolbox.filesep)) bd+=toolbox.filesep;
        boolean doit=false;
        int h=0;
        for(int i=0;i<trg.size();i++) {
            if (src2.indexOf(trg.get(i))==-1) {
                if (!doit) {
                    SafeMessage SM=new SafeMessage("Delete " + trg.get(i) + " from "+device+"?", "Please confirm:", "No", "Yes", "Yes to all", "Cancel");
                    SM.type=2;
                    SM.showMsg();
                    h=SM.returnCode;
                }
                if (h==3) return;
                if (h==2) doit=true;
                if (doit || (h==1)) {
                        TextFile.Delete(bd+trg.get(i));
                }
            }
        }

        doit=false;
        for(int i=0;i<src.size();i++) {
            if (trg.indexOf(src2.get(i))==-1) {
                if (!doit) {
                    SafeMessage SM=new SafeMessage("Copy " + src2.get(i) + " to "+device+"?", "Please confirm:", "No", "Yes", "Yes to all","Cancel");
                    SM.type=2;
                    SM.showMsg();
                    h=SM.returnCode;
                }
                if (h==3) return;
                if (h==2) doit=true;
                if (doit || (h==1)) {
                    (new InteractiveFileCopy(MF,src.get(i), bd + src2.get(i),MF.RSC)).go();
                }
            }
        }        

        setStatusDone();
    }

    private void warning(final String s) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                toolbox.Warning(null,s, "Warning");
            }
        });
    }

    private void setStatusBegin() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MF.setThreadMsg("Synchronizing USB device ...");
                MF.jPBSearch.setIndeterminate(true);
            }
        });
    }
    
    private void setStatusDone() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MF.setThreadMsg("Ready.");
                MF.jPBSearch.setIndeterminate(false);
                toolbox.Information(null,"Synchronization complete.", "Done:");
            }
        });
    }

}