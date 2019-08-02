/*
 * Resource-Class
 *
 * contains all the various resources and general data structures used by Celsius
 *
 */

package celsius;

import celsius.tools.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.FontUIResource;

/**
 *
 * @author cnsaeman
 */
public class Resources {

    public final String VersionNumber = "v3.3";
    public final String celsiushome = "http://www.christiansaemann.de/celsius/";
    public final String stdHTMLstring;
    public String HomeDirectory;

    public final String[] LibraryFields={"name","index","standardfields","tablecolumns","columnheaders","columntypes","columnsizes","autosortcolumn","people","plugins-manual","plugins-auto","plugins-export","filetypes","searchtags","hide","essential-fields","differentiating-fields","item-representation","item-sort-representation","naming-convention","choice-fields","icon-fields","icon-dictionary","default-add-method", "item-folder"};
    
    private final MainFrame MF;

    public final ArrayList<ItemTable> ItemTables;                  // document tabs

    public ArrayList<Library> Libraries;
    public int currentLib;

    public HashMap<String,String> JournalLinks;

    public RecentLibraryCache LastLibraries;

    public Plugins Plugins;                    // protocol class
    public MsgLogger Msg1;                    // protocol class
    public Icons Icons;       // class for all the icons
    public Configurator Configuration;    // configuration handler

    public HashMap<String,XMLHandler> LibraryTemplates;

    public String LookAndFeel;
    public double guiScaleFactor;

    public final ExecutorService SequentialExecutor;

    public StateManager SM;

    public double mylat;
    public double mylon;

    public boolean displayHidden;

    public Resources(MainFrame mf) {
        MF=mf;
        displayHidden=false;
        mylat=0;
        mylon=0;
        LookAndFeel=null;
        stdHTMLstring=createstdHTMLstring();
        HomeDirectory = Parser.CutTill((new File(".")).getAbsolutePath(), "/.");
        ItemTables = new ArrayList<ItemTable>();
        currentLib=-1;
        Libraries = new ArrayList<Library>();
        JournalLinks = new HashMap<String, String>();
        LastLibraries=new RecentLibraryCache();
        initMsg();
        SM=new StateManager();
        SequentialExecutor=java.util.concurrent.Executors.newSingleThreadExecutor();
    }

    public void initResources() {
        try {
            Msg1.repS("RES>Loading configuration file...");
            Configuration = new Configurator(Msg1);
            Icons=new Icons(Configuration.getDefault("iconfolder"));

            // LibraryTemplates
            if (!(new File("LibraryTemplates")).exists()) {
                Msg1.repS("RES>Creating simple library templates...");
                (new File("LibraryTemplates")).mkdir();
            }
            if (!(new File("LibraryTemplates/Default.xml")).exists()) {
                XMLHandler.Create("celsiusv2.2.librarytemplates", "LibraryTemplates/Default.xml");
                XMLHandler LibraryTemplatesX=new XMLHandler("LibraryTemplates/Default.xml");
                LibraryTemplatesX.addEmptyElement();
                LibraryTemplatesX.put("name", "Default");
                LibraryTemplatesX.put("index", "title|authors|pages|identifier|type|keywords");
                LibraryTemplatesX.put("tablecolumns", "type|title|authors|identifier");
                LibraryTemplatesX.put("people", "authors");
                LibraryTemplatesX.put("plugins","*");
                LibraryTemplatesX.put("filetypes","*");
                LibraryTemplatesX.writeBack();
            } 
            Msg1.repS("RES>Loading library templates...");
            String templates[]=(new File("LibraryTemplates")).list();
            LibraryTemplates=new HashMap<String,XMLHandler>();
            for (int i=0;i<templates.length;i++) {
                if (templates[i].endsWith(".xml")) {
                    XMLHandler XH = new XMLHandler("LibraryTemplates/" + templates[i]);
                    LibraryTemplates.put(XH.getS("name"), XH);
                }
            }
        } catch (Exception e) {
            Msg1.printStackTrace(e);
            Msg1.repS("RES>Initializing of resources failed");
            toolbox.Warning(MF,"Error while initializing of resources:\n" + e.toString()+"\nCelsius might not be started in the correct folder/directory.", "Exception:");
            System.exit(255);
        }
        Msg1.repS("RES>Setting Proxy server...");
        Configuration.setProxy(Msg1);
    }

    /**
     * Initialize the logging system
     */
    private void initMsg() {
        try {
            Msg1 = new MsgLogger("output.txt", false);
            Msg1.repS();
            Msg1.repS("RES>============================================");
            Msg1.repS("RES>Celsius Library System " + VersionNumber);
            Msg1.repS("RES>============================================");
            Msg1.repS("RES>Started at: " + toolbox.ActDatum());
            Msg1.repS();
        //Msg1.detail=100;
        } catch (final IOException e) {
            Msg1.printStackTrace(e);
            toolbox.Warning(MF,"Logging system initialization failed!", "Warning!");
        }
    }

    public void resetLogFile() {
        try {
            Msg1.reset();
            Msg1.repS();
            Msg1.repS("RES>============================================");
            Msg1.repS("RES>Celsius Library System " + VersionNumber);
            Msg1.repS("RES>============================================");
            Msg1.repS("RES>Reset at: " + toolbox.ActDatum());
            Msg1.repS();
        } catch (IOException ex) {
            toolbox.Warning(MF,"Error while resetting log file:\n" + ex.toString(), "Exception:");
            Msg1.printStackTrace(ex);
        }
    }

    public void loadPlugins() {
        Plugins=new Plugins(Msg1,this);
        Plugins.ReadIn();
    }

    /**
     * This fixes the standard html string output
     */
    private String createstdHTMLstring() {
        return ("<html><body><font size=\"6\" style=\"sans\">Celsius Library System " + VersionNumber + "</font><br>" +
                "<font size=\"5\" style=\"sans\">(w) by Christian Saemann</font><hr><br>" +
                "Welcome to the Celsius Library System, the flexible database and file storage system!<br>" +
                "<p align=\"justify\">Celsius can help you with the administration of your books, your movies, your " +
                "music files, your scientific papers, your BibTeX collection, your sheet music, your geocaches and many " +
                "other things.</p>" +
                "<p align=\"justify\">Below, you find quick start guides for various usage cases.</p>" +
                "<p><a href=\"#gettingstarted\">Getting started</a>&nbsp;&nbsp;&nbsp;<a href=\"#geocaching\">Geocaching with Celsius</a>&nbsp;&nbsp;&nbsp;<a href=\"#moreinfo\">More " +
                "information</a>&nbsp;&nbsp;&nbsp;</p>" +
                "<br><hr><br><a name=\"gettingstarted\"><font size=\"5\" style=\"sans\">Getting started</font></a>" +
                "<ul><li>When starting Celsius for the first time, click on the tool icon in the toolbar to enter the " +
                "configuration dialog. Make sure that file support is set up properly for any file type you might want " +
                "to use. Under Linux, e.g. the entries for \"Extract text\" might read as \"pdftotext -q '%from%' '%to%'\" and for " +
                "\"Viewer\", it might be \"okular '%from%'\". See the web page and the manual for more details, if you don't know what to do.</li>" +
                "<li>Next, you have to create a <i>library</i>, i.e. a database in which all your items will be stored.</li>" +
                "<li>Now you can start adding items by clicking on the green cross in the toolbar and choosing the " +
                "appropriate method.</li>" +
                "<br><hr><br><a name=\"geocaching\"><font size=\"5\" style=\"sans\">Geocaching with Celsius</font></a>" +
                "<p align=\"justify\">To import your collection of geocaches into Celsius, first create a library using the template \"Geocaches\". " +
                "Celsius can then import geocache information from zipped gpx files which you can download e.g. from geocaching.com. " +
                "For this, click on the green cross in the toolbar and select \"Import items from file\". On this tab, choose " +
                "\"zipped GPX-file\" as source and specify the file by clicking on \"Choose file\", then click on \"Read in file\". " +
                "You can now import all the geocaches or add just the currently selected ones. Note that waypoints will be handled as " +
                "hidden items in Celsius and will be associated to the geocache as links.</p><p align=\"justify\">To export geocaches, use the export tab in the " +
                "lower left corner.</p>"+
                "<br><hr><br><a name=\"moreinfo\"><font size=\"5\" style=\"sans\">More information</font></a>" +
                "<p align=\"justify\">Celsius's homepage is located at <a href=\"" + celsiushome + "\">" + celsiushome + "</a>. Its files are hosted <a href=\"http://sourceforge.net/projects/celsiusls/\">here" +
                "</a> at sourceforge, where you can submit reviews, ask questions in the forums and make suggestions for improvements. Updates to Celsius as " +
                "well as further plugins can be found there, too.</p>" +
                "<br><hr><br><a name=\"moreinfo\"><font size=\"5\" style=\"sans\">Copyright information</font></a>" +
                "<p align=\"justify\">Celsius is open source and released under the GNU General Public License v3.</p>" +
                "</body></html>");
    }

    public Library getCurrentSelectedLib() {
        if (currentLib==-1) return(null);
        return(Libraries.get(currentLib));
    }

    public int getCurrentSelectedLibNo() {
        return(currentLib);
    }

    public ItemTable getCurrentItemTable() {
        if (MF.jTPTabList.getSelectedIndex()==-1) return(null);
        return(ItemTables.get(MF.jTPTabList.getSelectedIndex()));
    }

    public MainFrame getMF() {
        return(MF);
    }

    public Color getLightGray() {
        return(new java.awt.Color(204,204,204));
    }

    public void closeResources() {
        for (Library Lib : Libraries)
            Lib.closeLibrary();
        WriteInitFile();
        try {
            Msg1.repS();
            Msg1.repS("RES>Application closed at: " + toolbox.ActDatum());
            Msg1.finalize();
        } catch (final IOException e) {
            Msg1.printStackTrace(e);
            (new SafeMessage("Protocol file finalization failed:" + e.toString(), "Exception:", 0)).showMsg();
            toolbox.Warning(MF,"RES>Messager finalization failed!", "Warning!");
        }
    }

    public void openLibrary(String s) {
        final Library NewLib = new Library(this.MF, s, this);
        if (NewLib.name.equals("??##cancelled")) return;
        if (NewLib.name.startsWith("??#$")) return;
        if (NewLib.name.startsWith("??##")) {
            (new SafeMessage("Error loading library, library file " + s + " has not been loaded:\n" + toolbox.stripError(NewLib.name), "Warning:", 0)).showMsg();
        } else {
            if (!NewLib.MainFile.type.startsWith("celsiusv2."))
                (new SafeMessage("The library "+NewLib.name+" is of an old format. Please convert it before adding new files.", "Warning:", 0)).showMsg();
            if (NewLib.currentStatus==10) {
                (new SafeMessage("The library "+NewLib.name+" may be out of synch. Please sychnronize it as soon as possible.", "Warning:", 0)).showMsg();
            }
            Libraries.add(NewLib);
            if (LastLibraries.containsKey(NewLib.name)) {
                LastLibraries.remove(NewLib.name);
                for (Component cmp : MF.jMRecent.getMenuComponents()) {
                    if (((JMenuItem)cmp).getText().equals(NewLib.name))
                        MF.jMRecent.remove(cmp);
                }
            }
            MF.addLib(NewLib);
            Msg1.repS("RSC>Library " + getCurrentSelectedLib().name + " loaded.");
        }
    }

    public void ReadInitFile() {
        try {
            TextFile TD = new TextFile("celsius.ini");
            String tmp;
            while (TD.ready()) {
                tmp = TD.getString();
                if (tmp.equals("Open Libraries:")) {
                    tmp = TD.getString();
                    while (!tmp.equals("---")) {
                        openLibrary(tmp);
                        tmp = TD.getString();
                    }
                }
                if (tmp.equals("Last Libraries:")) {
                    tmp = TD.getString();
                    while (!tmp.equals("---")) {
                        String s1=Parser.CutTill(tmp,"|");
                        String s2=Parser.CutFrom(tmp,"|");
                        LastLibraries.put(s1, s2);
                        addRecentLib(s1, s2);
                        tmp = TD.getString();
                    }
                }
                if (tmp.startsWith("Selected Library:")) {
                    tmp = Parser.CutFrom(tmp, "Selected Library:").trim();
                    int i = toolbox.intvalue(tmp);
                    MF.setSelectedLibrary(i);
                }
                if (tmp.startsWith("Current Position:")) {
                    tmp = Parser.CutFrom(tmp, "Current Position:").trim();
                    mylat=Double.valueOf(Parser.CutTill(tmp,",").trim());
                    mylon=Double.valueOf(Parser.CutFrom(tmp,",").trim());
                }
                if (tmp.startsWith("GUI-Scaling:")) {
                    tmp = Parser.CutFrom(tmp, "GUI-Scaling:").trim();
                }
            }
            TD.close();
        } catch (IOException ex) {
            Msg1.printStackTrace(ex);
            Msg1.repS("RES>No file celsius.ini found, started with default settings.");
        }
    }
    
    public int guiScale(int v) {
        return (int) (v*guiScaleFactor);
    }
    
    public URL getImageURL(String s) {
        if (guiScaleFactor>1.9) {
            s=s+".2x";
        }
        return getClass().getResource("/celsius/images/"+s+".png") ;
    }
    
    public Image getIcon(String s) {
        if (guiScaleFactor>1.9) {
            s=s+".2x";
        }
        return Icons.getIcon(s).getImage();
    }
    
    public Image getOriginalIcon(String s) {
        return Icons.getIcon(s).getImage();
    }
    
    public ImageIcon getScaledIcon(String s) {
        if (guiScaleFactor>1.9) {
            s=s+".2x";
        }
        //if (s==null) return(get("default"));
        if (s==null) return(null);
        if (s.equals("")) return(null);
        if (s.length()==0) return(Icons.get("default"));
        if (!Icons.containsKey(s)) return(Icons.get("notavailable"));
        return(Icons.get(s));
    }
    
    public void setComponentFont(Component[] comp)
    {
        for(int x = 0; x < comp.length; x++)
        {
          if(comp[x] instanceof Container) setComponentFont(((Container)comp[x]).getComponents());
          try{comp[x].setFont(new java.awt.Font("Arial", 0, guiScale(11)));}
          catch(Exception e){}//do nothing
        }
    }    
    
    

    public void WriteInitFile() {
        try {
            TextFile TD = new TextFile("celsius.ini", false);
            TD.putString("Open Libraries:");
            for (Library h : Libraries) {
                TD.putString(h.MainFile.source);
            }
            TD.putString("---");
            TD.putString("Last Libraries:");
            for (String l : LastLibraries.keySet()) {
                TD.putString(l+"|"+LastLibraries.get(l));
            }
            TD.putString("---");
            TD.putString("Selected Library: " + Integer.toString(getCurrentSelectedLibNo()));
            TD.putString("Current Position: " + Double.toString(mylat)+", "+Double.toString(mylon));
            if (LookAndFeel!=null) {
                TD.putString("Look and Feel: "+LookAndFeel);
            }
            // All directories
            TD.close();
        } catch (IOException ex) {
            toolbox.Warning(MF,"Error while writing .ini file:\n" + ex.toString(), "Exception:");
            Msg1.printStackTrace(ex);
        }
    }

    /**
     * Create the journal link command
     */
    public String getJournalLinkCmd(Item item) {
        if (item == null)
            return("");
        BibTeXRecord BR=new BibTeXRecord(item.get("bibtex"));
        if (BR == null)
            return("");
        String tag,gtag;
        String tmp1=JournalLinks.get(BR.get("journal"));
        if (tmp1 == null)
            return("");
        if (tmp1.length() > 0) {
            // Substitute addinfo tags
            for (String key : BR.keySet()) {
                gtag = BR.get(key);
                if (gtag.length()==0) {
                    gtag = "";
                }
                if (key.equals("pages")) {
                    if (gtag.indexOf('-')>0) gtag=Parser.CutTill(gtag, "-");
                    if (gtag.indexOf('-')>0) gtag=Parser.CutTill(gtag, "-");
                }
                tmp1 = tmp1.replace("#" + key + "#", gtag);
            }
        }
        return(tmp1);
    }

    public void rememberDir(String dir, JFileChooser FC) {
        File f=FC.getSelectedFile();
        if (!f.isDirectory()) f=f.getParentFile();
        if (getCurrentSelectedLib()==null) return;
        XMLHandler mf=getCurrentSelectedLib().MainFile;
        if (!mf.getS("dir::"+dir).equals(f.getAbsolutePath())) {
            mf.put("dir::"+dir, f.getAbsolutePath());
            try {
                mf.writeBack();
            } catch(Exception e) {
                Msg1.printStackTrace(e);
            }
        }
    }

    public String getDir(String dir) {
        if (getCurrentSelectedLib()==null) return(".");
        String ret=getCurrentSelectedLib().MainFile.get("dir::"+dir);
        if ((ret==null) || (ret.length()==0)) ret=".";
        return(ret);
    }

    void setCurrentDT(String title, String icon) {
        getCurrentItemTable().title=title;
        MF.jTPTabList.setTabComponentAt(MF.jTPTabList.getSelectedIndex(), new TabLabel(title,icon,this,getCurrentItemTable(),true));
    }

    public void addRecentLib(String name, final String source) {
            JMenuItem jmi = new JMenuItem(name);
            jmi.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    (new Thread("LoadingLib") {

                        @Override
                        public void run() {
                            MF.setThreadMsg("Opening library...");
                            MF.jPBSearch.setIndeterminate(true);
                            try {
                                openLibrary(source);
                            } catch (Exception e) {
                                toolbox.Warning(MF,"Loading library failed:\n" + e.toString(), "Warning:");
                            }
                            MF.setThreadMsg("Ready.");
                            MF.jPBSearch.setIndeterminate(false);
                            MF.updateStatusBar(false);
                        }
                    }).start();
                }
            });
            jmi.setFont(MF.jMCopyToDiff.getFont());
            MF.jMRecent.add(jmi);
    }

    public void setLookAndFeel() {
        try {
            TextFile TD = new TextFile("celsius.ini");
            String tmp;
            while (TD.ready()) {
                tmp = TD.getString();
                if (tmp.startsWith("Look and Feel:")) {
                    tmp = Parser.CutFrom(tmp, "Look and Feel:").trim();
                    try {
                        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                            if (tmp.equals(info.getName())) {
                                UIManager.setLookAndFeel(info.getClassName());
                                LookAndFeel=info.getName();
                            }
                        }
                    } catch (Exception e) {
                        Msg1.printStackTrace(e);
                    }
                }
            }
            // All directories
            TD.close();
        } catch (IOException ex) {
            Msg1.printStackTrace(ex);
            Msg1.repS("RES>No file celsius.ini found, started with default settings.");
        }
        // Below: Adjust font sizes for menus etc.
        // For some reaons, the ofnt for menu change itself doesn't work, done manually in MainFrame.java
        UIDefaults defaults = UIManager.getDefaults();
        final FontUIResource fnt11 = new FontUIResource(new java.awt.Font("SansSerif", 0, guiScale(11)));
        final FontUIResource fnt12 = new FontUIResource(new java.awt.Font("SansSerif", 0, guiScale(12)));
        Enumeration enumer = UIManager.getLookAndFeelDefaults().keys();
        while(enumer.hasMoreElements())
        {
          Object key = enumer.nextElement();
          Object value = UIManager.get(key);
          if (value instanceof javax.swing.plaf.FontUIResource)
          {
            UIManager.put( key, new javax.swing.plaf.FontUIResource(fnt12) );
          }
        }
        UIManager.getLookAndFeelDefaults().put("MenuBar.font",fnt12); 
        UIManager.getLookAndFeelDefaults().put("Menu.font",fnt12); 
        UIManager.getLookAndFeelDefaults().put("MenuItem.font",fnt12); 
        UIManager.getLookAndFeelDefaults().put("RadioButtonMenuItem.font",fnt12); 
        UIManager.getLookAndFeelDefaults().put("CheckBoxMenuItem.font",fnt12); 
        UIManager.getLookAndFeelDefaults().put("PopupMenu.font",fnt12); 
        UIManager.getLookAndFeelDefaults().put("PopupMenuItem.font",fnt12); 
        UIManager.getLookAndFeelDefaults().put("Button.font", fnt11);
        UIManager.getLookAndFeelDefaults().put("ToggleButton.font", fnt11);
        UIManager.getLookAndFeelDefaults().put("RadioButton.font", fnt11);
        UIManager.getLookAndFeelDefaults().put("CheckBox.font", fnt11);
        UIManager.getLookAndFeelDefaults().put("ComboBox.font", fnt11);
        UIManager.getLookAndFeelDefaults().put("List.font", fnt11);
        UIManager.getLookAndFeelDefaults().put("Label.font", fnt11);
        UIManager.getLookAndFeelDefaults().put("EditorPane.font", fnt11);
        UIManager.getLookAndFeelDefaults().put("Tree.font", fnt11);
        UIManager.getLookAndFeelDefaults().put("TitledBorder.font", fnt11);
        UIManager.getLookAndFeelDefaults().put("Table.font", fnt12);
        UIManager.getLookAndFeelDefaults().put("TableHeader.font", fnt12);
        UIManager.getLookAndFeelDefaults().put("TextField.font", fnt12);
        UIManager.getLookAndFeelDefaults().put("TextArea.font", fnt12);
        UIManager.getLookAndFeelDefaults().put("Menu.margin", new javax.swing.plaf.InsetsUIResource(guiScale(2),guiScale(2),guiScale(2),guiScale(2)));
    }

    // guarantees tab and turns it into one with given description
    public ItemTable makeTabAvailable(int infoMode, String title, String icon) {
        ItemTable IT;
        if (!MF.isTabAvailable) {
            IT=new ItemTable(MF,getCurrentSelectedLib(),title,-1);
            IT.setHeader(title);
            ItemTables.add(IT);
            MF.TabAvailable();
            MF.buildingNewTab=true;
            final JScrollPane scrollpane = new JScrollPane(IT.jtable);
            MF.jTPTabList.add(scrollpane);
            MF.jTPTabList.setTabComponentAt(MF.jTPTabList.getTabCount() - 1, new TabLabel(title,icon,this,IT,true));
            MF.jTPTabList.setSelectedComponent(scrollpane);
            MF.noDocSelected();
            MF.buildingNewTab=false;            
        } else {
            IT=getCurrentItemTable();
            setCurrentDT(title,icon);
            IT.setType(infoMode);
        }
        IT.Lib=getCurrentSelectedLib();
        MF.jIP.switchModeTo(infoMode);
        return(IT);
    }

    // creates a new tab and turns it into one with given description
    public ItemTable makeNewTabAvailable(int infoMode, String title, String icon) {
        ItemTable IT;
        IT=new ItemTable(MF,getCurrentSelectedLib(),title,-1);
        IT.setHeader(title);
        ItemTables.add(IT);
        MF.TabAvailable();
        MF.buildingNewTab=true;
        final JScrollPane scrollpane = new JScrollPane(IT.jtable);
        MF.jTPTabList.add(scrollpane);
        MF.jTPTabList.setTabComponentAt(MF.jTPTabList.getTabCount() - 1, new TabLabel(title,icon,this,IT,true));
        MF.jTPTabList.setSelectedComponent(scrollpane);
        MF.noDocSelected();
        MF.buildingNewTab=false;
        IT.Lib=getCurrentSelectedLib();
        MF.jIP.switchModeTo(infoMode);
        return(IT);
    }

}
