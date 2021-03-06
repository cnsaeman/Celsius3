//
// Celsius Library System
// (w) by C. Saemann
//
// RegisterThread.java
//
// This class contains a thread registering documents
//
// not typesafe
//
// checked 16.09.2007
//

package celsius.Threads;

import celsius.Item;
import celsius.Library;
import celsius.StructureNode;
import celsius.tools.MsgLogger;
import celsius.tools.Parser;
import celsius.tools.toolbox;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class ThreadRegister extends Thread {
    
    private final String TI;                        // ThreadIndicator
    
    private final Library Lib;                      // MF
    private final MsgLogger Msg1;                         // Protocol
    private final Item doc;
    private final ArrayList<String> Paper;          // Representation of the paper in plain txt
    private final HashMap<Character,Integer> Registers;

    private String currentAutoRegistration;

    public int newRegistrations;    // Number of new registrations
    public boolean done;         // Thread done?
    private boolean readin;      // Paper read in?
    
    private ArrayList<String> PreRegs;      // PreRegistrations
    private final int preRegsNumb;          // to quickly see if there were new registrations
    
    public int error;            // Flag: problems when reading in PaperData
    
    // Constructor for going through a single paper
    public ThreadRegister(Library lib, Item d, MsgLogger msg1) {
        TI="RGT"+toolbox.getThreadIndex()+">";
        Lib=lib; Msg1=msg1; doc=d;
        readin=false;
        String r1=doc.get("autoregistered");
        String r2=doc.get("registered");
        PreRegs=toolbox.listOf(r1);
        if ((r2!=null) && (r2.length()>0))
            PreRegs.addAll(Arrays.asList(r2.split("\\|")));
        PreRegs.remove("");
        preRegsNumb=PreRegs.size();
        Paper=new ArrayList<String>();
        Registers=new HashMap<Character,Integer>();
    }
    
    // Read in the complete document
    private void ReadInPaper(String s){
        try {
            Msg1.repS(TI+"Reading in plaintxt : "+s);
            
            // Open text file and initialise variables
            GZIPInputStream fis  = new GZIPInputStream(new FileInputStream(new File(s)));
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br=new BufferedReader(isr);
            
            String currentLine;
            StringBuffer currentPage=new StringBuffer(4000);
            
            // Lese Paper seitenweise ein und setzte references auf die Seite mit den References
            while ((currentLine=br.readLine())!=null) {
                currentLine=currentLine.toLowerCase();
                currentPage.append(" ").append(currentLine);
                if (currentLine.indexOf(toolbox.EOP)>-1) {
                    Paper.add(currentPage.toString());
                    currentPage=new StringBuffer(4000);
                }
            }
            Paper.add(currentPage.toString());
            Msg1.repS(TI+"Ready");
            readin=true;
            br.close(); isr.close(); fis.close();
        } catch (IOException e) { Msg1.repS(TI+"Error reading plain text: "+e.toString()); }
    }
    
    // To Clean
    private void parse() throws IOException {
        String d1,d2,d3; // Dummy strings
        Msg1.repS(TI+"Parsing...");
        // Go through all rules
        Lib.Rules.goTo(Lib.Rules.Root);
        for (Enumeration<StructureNode> e=Lib.Rules.Root.children();e.hasMoreElements();) {
            Lib.Rules.goTo(e.nextElement());

            // equals rule
            if (Lib.Rules.name.equals("equals")) {
                d1=Lib.Rules.get("tag");
                d2=Lib.Rules.get("value");
                d3=Lib.Rules.get("target");
                Msg1.repS(1,TI+"<equals/> found : "+d1+":"+d2+" to "+d3);
                if (doc.getS(d1).equals(d2)) preReg(d3);
            }

            // contains rule
            if (Lib.Rules.name.equals("contains")) {
                d1=Lib.Rules.get("tag");
                d2=Lib.Rules.get("value");
                d3=Lib.Rules.get("target");
                Msg1.repS(1,TI+"<contains/> found : "+d1+":"+d2+" to "+d3);
                if (d1.equals("plaintext") && readin) {
                    for (String t : Paper) {
                        if (t.indexOf(d2)>-1) {
                            preReg(d3);
                            break;
                        }
                    }
                } else if (doc.getS(d1).indexOf(d2)>-1) preReg(d3);
            }

            // contains-mainly rule
            if (Lib.Rules.name.equals("contains-mainly")) {
                d1=Lib.Rules.get("tag");
                d2=Lib.Rules.get("value");
                d3=Lib.Rules.get("target");
                Msg1.repS(1,TI+"<contains-mainly/> found : "+d1+":"+d2+" to "+d3);
                if ((doc.getS(d1).length()-d2.length())<(1.5*d2.length()))
                    if (doc.getS(d1).indexOf(d2)>-1) preReg(d3);
            }

            // test rule and subrules
            if (Lib.Rules.name.equals("test")) {
                String tmp;
                Registers.clear();
                Msg1.repS(1,TI+"Rule found");
                StructureNode push=Lib.Rules.Node; // node of the "test"-rule
                int a=0; int b=0; int c=0;                  // put registers to 0
                for (int j=0;j<push.getChildCount();j++) {
                    Lib.Rules.goTo(push.getChildAt(j));

                    // equals rule
                    if (Lib.Rules.name.equals("equals")) {
                        d1=Lib.Rules.get("tag");
                        d2=Lib.Rules.get("value");
                        d3=Lib.Rules.get("action");
                        if (doc.getS(d1).equals(d2)) doAction(d3);
                    }

                    // contains rule
                    if (Lib.Rules.name.equals("contains")) {
                        d1=Lib.Rules.get("tag");
                        d2=Lib.Rules.get("value");
                        d3=Lib.Rules.get("action");
                        if (d1.equals("plaintext") && readin) {
                            for (String t : Paper) {
                                if (t.indexOf(d2)>-1) {
                                    doAction(d3);
                                    break;
                                }
                            }
                        } else if (doc.getS(d1).indexOf(d2)>-1) doAction(d3);
                    }

                    // contains-mainly rule
                    if (Lib.Rules.name.equals("contains-mainly")) {
                        d1=Lib.Rules.get("tag");
                        d2=Lib.Rules.get("value");
                        d3=Lib.Rules.get("action");
                        if ((doc.getS(d1).length()-d2.length())<(1.5*d2.length()))
                            if (doc.getS(d1).indexOf(d2)>-1) doAction(d3);
                    }

                    // eachtime rule
                    if (Lib.Rules.name.equals("eachtime")) {
                        d1=Lib.Rules.get("tag");
                        d2=Lib.Rules.get("value");
                        d3=Lib.Rules.get("action");
                        Msg1.repS(1,TI+"<contains-mainly/> found : "+d1+":"+d2+" to "+d3);
                        int ki=Parser.HowOftenContains(doc.getS(d1), d2);
                        for (int ji=0;ji<ki;ji++)
                            doAction(d3);
                    }

                    // Register, if conditions are fulfilled
                    if (Lib.Rules.name.equals("if")) {
                        d1=Lib.Rules.get("conditions");
                        d2=Lib.Rules.get("target");
                        String conditions[]=d1.split("\\|");
                        boolean fulfilled=true;
                        for (int i=0;i<conditions.length;i++) {
                            char reg=conditions[i].charAt(0);
                            char cond=conditions[i].charAt(1);
                            int val=Integer.valueOf(conditions[i].substring(2));
                            if (!Registers.containsKey(reg)) Registers.put(reg,0);
                            if (cond=='=')
                                if (Registers.get(reg)!=val) {
                                    fulfilled=false;
                                    break;
                                }
                            if (cond=='>')
                                if (Registers.get(reg)<=val) {
                                    fulfilled=false;
                                    break;
                                }
                            if (cond=='<')
                                if (Registers.get(reg)>=val) {
                                    fulfilled=false;
                                    break;
                                }

                        }
                        if (fulfilled) preReg(d2);
                    }
                }
                Lib.Rules.goTo(push);
            }
        }
    }
    
    // Merke für Registrierung vor
    private void preReg(String s) throws IOException {
        String tmp;
        while (s.length()>0) {
            tmp=Parser.ReturnFirstItem(s);
            if (!PreRegs.contains(tmp)) {
                Msg1.repS(TI+"pre-registering in "+s);
                PreRegs.add(tmp);
            } else Msg1.repS(TI+"already registered:"+doc.get("title")+" in "+s);
            s=Parser.CutFirstItem(s);
        }
    }
    
    // Register current document in categories.
    private synchronized void Register() throws IOException {
        // if no new registrations are found:
        if (preRegsNumb==PreRegs.size()) return;
        // insert registration, if not yet registered
        Iterator<String> e=PreRegs.iterator();
        for (int i=0;i<preRegsNumb;i++) e.next();
        currentAutoRegistration="";
        while (e.hasNext()) {
            currentAutoRegistration+="|"+e.next();
            newRegistrations++;
            Lib.setChanged(true);
        }
        if (currentAutoRegistration.startsWith("|"))
            currentAutoRegistration=currentAutoRegistration.substring(1);
        doc.put("autoregistered",currentAutoRegistration);
        Msg1.repS(TI+"new registrations: "+currentAutoRegistration);
    }
    
    // Main running routine
    @Override
    public void run() {
        done=false;
        if (error==0) {
            try {
                Msg1.repS(TI+"Register Thread called for:"+doc.toText());
                if (!doc.getS("parse").equals("none")) {
                    // Prepare the file according to its parsing tag
                    if ((!doc.getS("parse").equals("header")) && (doc.get("plaintxt")!=null))
                        ReadInPaper(doc.getCompleteDirS("plaintxt"));

                    parse();

                    // Write registrations to the library index
                    Register();
                    
                    Msg1.repS(TI+"New registrations:"+Integer.toString(newRegistrations));
                    if (Lib.hasChanged())
                        Msg1.repS(TI+"Changes in library");
                    else
                        Msg1.repS(TI+"No changes in library");
                }
                done=true;
            } catch (IOException e) { Msg1.repS(TI+"Error in run of ThreadRegister: "+e.toString()); }
        } else Msg1.repS(TI+"Could not go to file with ID:"+doc.get("id"));
    }

    // Adjust registers according to action
    private void doAction(String d3) {
        if (d3.trim().length()==0) return;
        String[] actions=d3.split("\\|");
        //System.out.println(d3);
        for (int i=0;i<actions.length;i++) {
            char reg=actions[i].charAt(0);
            String op=Parser.CutTill(actions[i].substring(1), "=")+"=";
            int val=Integer.valueOf(Parser.CutFrom(actions[i], "="));
            int pre=0;
            /*System.out.print(reg);
            System.out.print(":"+op+":");
            System.out.println(val);*/
            if (Registers.containsKey(Character.valueOf(reg))) {
                pre=Registers.get(reg);
            }
            if (op.equals("=")) Registers.put(reg,val);
            if (op.equals("+=")) Registers.put(reg,pre+val);
            if (op.equals("-=")) Registers.put(reg,pre-val);
        }
    }
    
}