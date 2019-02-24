//
// Celsius Library System
// (w) by C. Saemann
//
// SearchThread.java
//
// This class contains the thread for searching...
//
// typesafe
//
// checked: 16.09.2007
//

package celsius.Threads;

import celsius.tools.Parser;
import celsius.*;
import celsius.tools.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import javax.swing.SwingUtilities;

public class ThreadSearch extends Thread {

    private final MainFrame MF;
    private final Resources RSC;
    private final ItemTable DT;
    private final Library Lib;
   
    private final IndexedXMLHandler Index;   // Library from parent
    private XMLHandler CurrentFile;// Current File Information
    private final MsgLogger Msg1;             // Msg1 from parent
    
    private String[] search;        // string to search for
    private String[] searchtmp;     // string to search for tmp
    private int mode;
    private ArrayList<String> currentResults;
    
    private SimpleDateFormat SDF;
    private ThreadRegistrar reg;

    private int maxpos;
    private int done;

    public boolean running;

    /**
     *  Constructor, read in information
     *  Mother, documenttablemodel, search string, class
     */
    public ThreadSearch(MainFrame mf, Library lib, ItemTable dt, String tmp, int m) {
        super();
        //System.out.println(String.valueOf(System.currentTimeMillis())+"Initialized: "+this.toString());
        MF=mf;
        RSC=MF.RSC;
        Lib=lib;
        Index=Lib.Index;
        maxpos=Lib.getSize();
        mode=m;
        Msg1=RSC.Msg1;
        DT=dt;
        DT.setLibrary(RSC.getCurrentSelectedLib());
        reg=DT.newRegistrar();
        search=tmp.toLowerCase().split(" ");
    }

    @Override
    public void interrupt() {
        super.interrupt();
        reg.interrupt();
    }

    private boolean TestIt(String s) {
        if (s==null) return(false);
        s=s.toLowerCase();
        boolean found=true;
        for(int i=0;i<searchtmp.length;i++) {
            if (searchtmp[i]!=null)
                if(s.indexOf(searchtmp[i])>-1) {
                    searchtmp[i]=null;
                } else {
                    found=false;
                }
        }
        if (found) {
            final int di=done;
            reg.add(new Item(Lib,done));
        }
        return found;
    }

    private String completeDir(String s) {
        if ((s==null) || (s.indexOf("::")==-1)) return(s);
        if (s.indexOf("::")>2) return(s);
        String sig=Parser.CutTill(s,"::");
        s=Parser.CutFrom(s,"::");
        String s2=s;
        if (s2.startsWith(toolbox.filesep)) s2=s2.substring(1);
        if (sig.equals("AI")) {
            if (s2.charAt(0)=='.') s2=s2.substring(1);
            return(Lib.basedir+"information"+toolbox.filesep+Index.get("id")+"."+s2);
        }
        if (sig.equals("LD")) return(Lib.basedir+s2);
        if (sig.equals("BD")) return(Lib.celsiusbasedir+s2);
        return(s);
    }
    
    // Examine current paper
    private void SeeThroughPaper() throws Exception {
        Msg1.repS(1,"SCM>Seeing through header of item "+Index.get(done,"location"));
        searchtmp = new String[search.length];
        System.arraycopy(search, 0, searchtmp, 0, search.length);
        // Only header information
        boolean bk=false;
        for (String k : Lib.IndexTags) {
            if (TestIt(Index.get(done,k))) {
                bk=true;
                break;
            }
        }
        if (bk || (mode==0)) return;
        CurrentFile=new XMLHandler(completeDir(Index.get(done,"addinfo")));
        bk=false;
        for (String k : CurrentFile.XMLTags) {
            if (TestIt(CurrentFile.get(k))) {
                bk=true;
                break;
            }
        }
        if (bk || (mode==1)) return;
        if (CurrentFile.get("plaintxt")!=null) {
            String tmp;
            GZIPInputStream fis  = new GZIPInputStream(new FileInputStream(new File(completeDir(CurrentFile.get("plaintxt")))));
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br=new BufferedReader(isr);
            while ((tmp=br.readLine())!=null) {
                tmp=tmp.toLowerCase();
                if (TestIt(tmp)) {
                    br.close(); isr.close(); fis.close();
                    return;
                }
            }
            br.close(); isr.close(); fis.close();
        }
    }

    public String getS(int pos,String key) {
        String tmp=Index.get(pos,key);
        if (tmp==null) tmp="";
        return(tmp);
    }
    
    @Override
    public synchronized void run() {
        running=true;
        done=0;
        reg.start();
        //System.out.println(String.valueOf(System.currentTimeMillis())+"Started: "+this.toString());

        // Clearing result table
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MF.jPBSearch.setMaximum(Index.getSize());
                MF.setThreadMsg("Searching...");
                MF.noDocSelected();
            }
        });
        Msg1.repS("SCM>Celsius Library::Search module");
        Msg1.repS("SCM>" + toolbox.ActDatum());

        done = 0;

        while (!(done >= maxpos) && !(isInterrupted())) {
            try {
               if (!Parser.EnumContains(Index.get(done,"attributes"),"hidden"))
                  SeeThroughPaper();
            } catch (Exception e) {
                e.printStackTrace();
                Msg1.repS("SCM>Error in " + getS(done, "location") + e.toString());
            }
            done++;
           yield();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    MF.jPBSearch.setValue(done);
                }
            });
       }
        //System.out.println(String.valueOf(System.currentTimeMillis())+"Out of loop: "+this.toString());
        reg.add(null);

        Msg1.repS("SCM>finished:" + toolbox.ActDatum());
        if (!isInterrupted()) {
            reg.end = true;
        } else {
            reg.interrupt();
        }
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                MF.setThreadMsg("Ready.");
            }
        });
        running = false;
        //System.out.println(String.valueOf(System.currentTimeMillis())+"Thread ended: "+this.toString());

    }
    
    
}