//
// Celsius Library System v2
// (w) by C. Saemann
//
// ThreadGetInfo.java
//
// This is the thread responsible for extracting plain text information and applying all available AutoPlugins to this plaintxt
//
// typesafe
//
// checked 16.09.2007
//

package celsius.Threads;

import celsius.Item;
import celsius.Plugin;
import celsius.Resources;
import celsius.tools.Parser;
import celsius.tools.toolbox;
import java.io.File;

public class ThreadGetDetails extends Thread {

    private final Item doc;
    private final Resources RSC;

    private String TI;

    public boolean done;

    public boolean plugins;
    
    public ThreadGetDetails(Item d,Resources rsc,boolean p) {
        doc=d;
        RSC=rsc;
        done=false;      // Current thread status
        plugins=p;
        
        TI="TGD"+toolbox.getThreadIndex()+">";   // Thread ID
    }
    
    // Runner
    @Override
    public synchronized void run() {
        try {
            RSC.Msg1.repS(TI+"ThreadGetDetails for "+doc.get("location"));
            GetText();
            if (plugins) {
                RSC.Msg1.repS(TI+"Enter loop");
                int i=0;
                for(Plugin plugin : RSC.Plugins.listPlugins("auto", RSC.getCurrentSelectedLib())) {
                    ThreadApplyPlugin TAP=new ThreadApplyPlugin(null,plugin,RSC.Plugins.parameters.get(plugin.metaData.get("title")),RSC, doc,false,false);
                    TAP.start();
                    TAP.join();
                    if ((doc.get("recognition")!=null) && (doc.get("recognition").equals("100"))) break;
                    i++;
                }
                RSC.Msg1.repS(TI+"Exit loop");
            }
            if (this.isInterrupted()) RSC.Msg1.repS(TI+"Interrupted!");
        } catch (Exception e) {             
            e.printStackTrace();
            RSC.Msg1.repS(TI+e.toString());
        }
        RSC.Msg1.repS(TI+"finished.");
        done=true;
    }
    
    // Complete plaintext information and read number of pages
    private synchronized void GetText() {
        try {
            // plaintxt already existing? Otherwise create it
            String fn=Parser.CutTillLast(doc.get("location"),toolbox.filesep)+toolbox.filesep+"."+Parser.CutFromLast(doc.get("location"),toolbox.filesep);
            doc.put("plaintxt",fn+".txt.gz");
            if (!(new File(doc.get("plaintxt"))).exists()) {
                RSC.Msg1.repS(TI + "Getting Plain Txt :: " + doc.get("location"));
                RSC.Configuration.ExtractText(TI, doc.get("location"), fn + ".txt");
            }
            if ((new File(doc.get("plaintxt"))).exists()) {
                if (doc.totalKeySet().indexOf("pages")>-1) {
                    RSC.Msg1.repS(TI + "Reading Number of Pages :: " + doc.get("location"));
                    doc.put("pages", Integer.toString(toolbox.ReadNumberOfPagesOf(RSC.Msg1, TI, doc.get("location"), doc.get("plaintxt"))));
                }
                doc.put("parse","all");
            } else {
                doc.put("plaintxt",null);
                doc.put("parse","header");
            }
        } catch (Exception e) {
            e.printStackTrace();
            RSC.Msg1.repS(TI + "Error creating or reading plaintxt:" + e.toString());
        }
    }
    
    @Override
    public String toString() {
        return("ThreadGetInfo.java\nFileName:"+doc.get("location")+"\nInformation:"+doc.toText());
    }    
    
}