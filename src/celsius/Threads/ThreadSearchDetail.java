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

import celsius.*;
import celsius.tools.MsgLogger;
import celsius.tools.Parser;
import celsius.tools.toolbox;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

public class ThreadSearchDetail extends Thread {

    private final MainFrame MF;
    private final Resources RSC;
    private final ItemTable DT;
    private final Library Lib;
    private final MsgLogger Msg1;

    private Item currentdoc;
    
    private String[] search;        // string to search for
    private String[] searchtmp;     // string to search for tmp
    private Date date;
    private String CatFilter;     // filter for categories
    private int DateFilter;	 // filter for date added
    private String FileFilter;     // filter for categories
    private String DocType;     // filter for document type
    private double DistFilter;  // Distance Filter
    private final boolean SrchAll;     // flag to search in text
    private final boolean SrchText;     // flag to search in text
    private final boolean SrchInCurrent;  // flag to search in current
    private final boolean SrchHidden;  // flag to search in current
    private final boolean forwards;
    private int searchpages;    // code for page restrictions
    private int when;       // when the paper: -1: before, 0: match, 1: after
    private boolean SrchDate;
    private boolean SrchSize;
    private double size;
    private ArrayList<Integer> currentResults;
    
    private SimpleDateFormat SDF;
    private ThreadRegistrar reg;

    private int maxpos;
    private int done;

    public boolean running;

    private HashMap<String,JCheckBox> checkBoxes;
    private HashMap<String,String> boundaryConditions;
   
    /**
     *  Constructor, read in information
     *  Mother, documenttablemodel, search string, class
     */
    public ThreadSearchDetail(MainFrame mf, Library lib, ItemTable dt, String tmp) {
        MF=mf;
        RSC=MF.RSC;
        Lib=lib;
        maxpos=Lib.getSize();
        Msg1=MF.RSC.Msg1;
        DT=dt;
        DT.setLibrary(RSC.getCurrentSelectedLib());
        SrchAll=MF.deepSearch.jCBAll.isSelected();
        RangeEditor RE=MF.deepSearch.rangeEditors.get("date");
        if ((RE!=null) && (RE.getValue().length()==10)) {
            SDF=new SimpleDateFormat("yyyy-MM-dd");
            try {
                date=SDF.parse(RE.getValue());
                SrchDate=true;
                if (!RE.isBiggerSelected()) when=-1;
                if (RE.isBiggerSelected()) when=1;
            } catch ( ParseException e ) {
                e.printStackTrace();
                SrchDate=false;
            }
            //System.out.println(date.toString());
        }
        RE=MF.deepSearch.rangeEditors.get("filesize");
        if ((RE!=null) && (RE.getValue().trim().length()>0)) {
            try {
                SrchSize=true;
                String tmp2=RE.getValue().trim();
                int i=0;
                while ((i<tmp2.length()) && (Character.isDigit(tmp2.charAt(i)) || (tmp2.toLowerCase().charAt(i)=='e'))) i++;
                size=Double.parseDouble(tmp2.substring(0,i));
                tmp2=tmp2.substring(i).trim().toLowerCase();
                if (tmp2.equals("kb")) size=size*1000;
                if (tmp2.equals("mb")) size=size*1000*1000;
                if (tmp2.equals("gb")) size=size*1000*1000*1000;
                if (tmp2.equals("kib")) size=size*1024;
                if (tmp2.equals("mib")) size=size*1024*1024;
                if (tmp2.equals("gib")) size=size*1024*1024*1024;
            } catch (Exception e) {
                e.printStackTrace();
                SrchSize=false;
            }
        }
        checkBoxes=MF.deepSearch.checkBoxes;
        SrchText=checkBoxes.containsKey("plaintext") && checkBoxes.get("plaintext").isSelected();
        SrchInCurrent=MF.deepSearch.jCBScurrent.isSelected();
        SrchHidden=MF.deepSearch.jCBHidden.isSelected();
        searchpages=0;
        JComboBox CB=MF.deepSearch.comboBoxes.get("pages");
        if (CB!=null) searchpages=CB.getSelectedIndex();

        DateFilter=0;
        CB=MF.deepSearch.comboBoxes.get("lastmodified");
        if (CB!=null) DateFilter=CB.getSelectedIndex();

        FileFilter="arbitrary";
        CB=MF.deepSearch.comboBoxes.get("filetype");
        if (CB!=null) FileFilter=(String)CB.getSelectedItem();

        DistFilter=0;
        RE=MF.deepSearch.rangeEditors.get("distance");
        if (RE!=null) {
            if (RE.getValue().trim().length()>0) {
                DistFilter=toolbox.doubleFromDistance(RE.getValue());
                if (!RE.isBiggerSelected()) DistFilter=-DistFilter;
            }
        }

        boundaryConditions=new HashMap<String,String>();
        for (String k : MF.deepSearch.comboBoxes.keySet()) {
            if (MF.deepSearch.comboBoxes.get(k).getSelectedIndex()>0)
                boundaryConditions.put(k,(String)MF.deepSearch.comboBoxes.get(k).getSelectedItem());
        }
        boundaryConditions.remove("pages");
        boundaryConditions.remove("lastmodified");
        boundaryConditions.remove("filetype");

        forwards=MF.deepSearch.jRBforwards.isSelected();
        if (MF.deepSearch.jCBSselected.isSelected()) {
            CatFilter=RSC.getCurrentSelectedLib().Structure.get("title");
            if (CatFilter.length()==0) CatFilter="##";
        } else {
            CatFilter="##";
        }

        reg=DT.newRegistrar();
        if (MF.deepSearch.jCBexactmatch.isSelected()) {
            search=new String[1];
            search[0]=tmp.toLowerCase();
        } else {
            search=tmp.toLowerCase().split(" ");
        }
        MF.jPBSearch.setMaximum(Lib.getSize());
    }

    @Override
    public void interrupt() {
        super.interrupt();
        reg.interrupt();
    }

    private boolean TestIt(String s) {
        if ((s==null) || (s.length()==0)) return(false);
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
        if (found) reg.add(currentdoc);
        return found;
    }

    // Examine current paper
    @SuppressWarnings("empty-statement")
    private void SeeThroughItem() throws Exception {
        Msg1.repS(1,"SCM>Seeing through header of paper "+currentdoc.toText());
        searchtmp = new String[search.length];
        System.arraycopy(search, 0, searchtmp, 0, search.length);
        // Only header information
        if (SrchAll) {
            ArrayList<String> keys=currentdoc.getAITags();
            for (int i=0;(i<keys.size() && !(TestIt(currentdoc.getS(keys.get(i)))));i++);
        } else {
            for (String k : checkBoxes.keySet()) {
                if (checkBoxes.get(k).isSelected()) {
                    if (TestIt(currentdoc.getS(k))) return;
                }
            }
        }
        if (SrchText) {
            try {
                // Deep search
                Msg1.repS(1,"SCM>Seeing through body of paper "+currentdoc.toText());
                if (currentdoc.get("plaintxt")!=null) {
                    String tmp;
                    GZIPInputStream fis  = new GZIPInputStream(new FileInputStream(new File(currentdoc.getCompleteDirS("plaintxt"))));
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
            } catch (IOException e) {
                e.printStackTrace();
                // Addinfo entry present, error reading file
                Msg1.repS("SCM>Addinfo entry present, error reading document "+currentdoc.toText()+" : "+e.toString());
            }
        }
    }

    @Override
    public synchronized void run() {
        running=true;
        done=0;
        reg.start();

        // If supposed to look only through current results, create Vector of IDs
        if (SrchInCurrent) {
            currentResults = new ArrayList<Integer>();
            for (Item doc : DT.getSelectedItems()) {
                currentResults.add(doc.id);
            }
        }

        // Clearing result table
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                MF.deepSearch.jBtnSearch.setEnabled(false);
                MF.deepSearch.jBtnStop.setEnabled(true);
                MF.setThreadMsg("Searching ...");
                MF.noDocSelected();
            }
        });
        Msg1.repS("SCM>Celsius Library::Search module");
        Msg1.repS("SCM>" + toolbox.ActDatum());

        // Go through library
        if (forwards) {
            done = 0;
        } else {
            done = Lib.getSize()-1;
        }
        boolean stp;
        Calendar rightNow = Calendar.getInstance();

        while (((!(done >= maxpos) && forwards) || (!(done <= 0) && !forwards)) && (!this.isInterrupted())) {
            try {
                //System.out.println(done);
                stp = true;
                currentdoc=new Item(Lib,done);
                if (!SrchHidden && (currentdoc.hasAttribute("hidden"))) stp=false;
                if (!FileFilter.equals("arbitrary")) {
                    if (FileFilter.equals("item ref")) {
                        stp = (currentdoc.get("filetype") == null);
                    } else {
                        if (currentdoc.get("filetype") == null) {
                            stp = false;
                        } else {
                            stp = currentdoc.get("filetype").equals(FileFilter);
                        }
                    }
                }
                if (!CatFilter.equals("##")) {
                    if (CatFilter.equals("Library")) {
                        if ((!currentdoc.isEmpty("autoregistered")) || (!currentdoc.isEmpty("registered"))) stp=false;
                    } else if (!(Parser.EnumContains(currentdoc.getS("autoregistered"), CatFilter) || Parser.EnumContains(currentdoc.getS("registered"), CatFilter))) {
                        stp = false;
                    }
                }
                if (SrchInCurrent) {
                    if (!(currentResults.indexOf(currentdoc.id) > -1)) {
                        stp = false;
                    }
                }
                if (searchpages > 0) {
                    int pages = toolbox.intvalue(currentdoc.getS("pages"));
                    if ((searchpages == 1) && (pages > 5)) {
                        stp = false;
                    }
                    if ((searchpages == 2) && (pages > 15)) {
                        stp = false;
                    }
                    if ((searchpages == 3) && ((pages < 15) || (pages > 40))) {
                        stp = false;
                    }
                    if ((searchpages == 4) && ((pages < 40) || (pages > 100))) {
                        stp = false;
                    }
                    if ((searchpages == 5) && ((pages < 100) || (pages > 200))) {
                        stp = false;
                    }
                    if ((searchpages == 6) && (pages < 200)) {
                        stp = false;
                    }
                }
                if ((stp) && (DateFilter > 0)) {
                    Calendar added = Calendar.getInstance();
                    added.setTimeInMillis((new File(currentdoc.getCompleteDirS("addinfo"))).lastModified());
                    if (DateFilter == 1) {
                        added.add(Calendar.DAY_OF_MONTH, 1);
                    }
                    if (DateFilter == 2) {
                        added.add(Calendar.DAY_OF_MONTH, 7);
                    }
                    if (DateFilter == 3) {
                        added.add(Calendar.DAY_OF_MONTH, 30);
                    }
                    if (added.compareTo(rightNow) < 0) {
                        stp = false;
                    }
                }
                if (SrchDate) {
                    String cfdate=currentdoc.get("date");
                    Date date2=new Date();
                    try {
                        date2=SDF.parse(cfdate);
                    } catch ( ParseException e ) {
                        e.printStackTrace();
                        try {
                            date2=SDF.parse("1977-04-23");
                        } catch ( ParseException exc ) {
                            exc.printStackTrace();
                        }
                    }
                    if (!date2.before(date) && (when<0)) stp=false;
                    if (!date2.after(date) && (when>0)) stp=false;
                }
                if (SrchSize) {
                    long actsize=0;
                    if (currentdoc.get("location")!=null) {
                        try {
                            actsize=(new File(currentdoc.getCompleteDirS("location"))).length();
                        } catch (Exception e) {

                        }
                    }
                    if (!MF.deepSearch.rangeEditors.get("filesize").isBiggerSelected() && (!(actsize<size))) stp=false;
                    if (MF.deepSearch.rangeEditors.get("filesize").isBiggerSelected() && (!(actsize>size))) stp=false;
                }
                if (DistFilter!=0) {
                    double actdist=toolbox.doubleFromDistance(currentdoc.getExtended("distance&"));
                    if ((DistFilter>0) && (actdist<DistFilter)) stp=false;
                    if ((DistFilter<0) && (actdist>-DistFilter)) stp=false;
                }
                for (String bc : boundaryConditions.keySet()) {
                    String res=currentdoc.get(bc);
                    if (res==null) {
                        stp=false;
                        break;
                    }
                    if (!currentdoc.get(bc).equals(boundaryConditions.get(bc))) {
                        stp=false;
                        break;
                    }
                }
                if (stp) {
                    SeeThroughItem();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Msg1.repS("SCM>Error in " + currentdoc.getS("location") + e.toString());
            }
            if (forwards) {
                done++;
            } else {
                done--;
            }
            MF.jPBSearch.setValue(done);
        }
        Msg1.repS("SCM>finished:" + toolbox.ActDatum());
        if (!this.isInterrupted()) {
            reg.end = true;
            try {
                reg.join();
            } catch (InterruptedException ex) {
            }            
        } else {
            reg.interrupt();
        }
        SwingUtilities.invokeLater(new Runnable() { public void run() { MF.setThreadMsg("Ready.");MF.deepSearch.jBtnSearch.setEnabled(true); MF.deepSearch.jBtnStop.setEnabled(false); } });
        running=false;
    }
    
    
}