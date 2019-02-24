//
// Celsius Library System v2
// (w) by C. Saemann
//
// Library.java
//
// This class combines all necessary data for a library
//
// typesafesh
//
// checked 16.09.2007
//

package celsius;

import celsius.Threads.ThreadRegister;
import celsius.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.DefaultTreeModel;

public final class Library implements Iterable<Item> {
    
    // Status messages after adding a document
    public static final String[] status={
        "Everything OK",  // 0
        "No title or author information found in record.", // 1
        "File with the same name existed. Adding aborted.", // 2
        "Error deleting document.", // 3
        "Item not found in library.", //4
        "Additional information could not be loaded in going to document.", // 5
        "Document id not found.", // 6
        "IOError with Index/addinfo file while adding document reference.", // 7
        "Error deleting library.", //8
        "Couldn't move document file", //9
        "Library index and data files may be out of synch." //10
    };

    public final Resources RSC;
    public XMLHandler MainFile;
    public XMLTree Structure;
    public XMLTree Rules;
    public IndexedXMLHandler Index;
    public XMLHandler PeopleRemarks;
    public XMLHandler CatRemarks;
    public XMLHandler HTMLtemplates;
    public Item lastAddedItem;
    private boolean Changed;
    private boolean PeopleOrKeywordsChanged;
    private boolean modfile;
    private int totalpages; // total number of pages in current library
    private int totalduration; // total duration in seconds

    // Metainformation on the Library
    public String name;
    public String basedir;
    public String celsiusbasedir;
    public ArrayList<String> IndexTags;
    private ArrayList<String> PeopleTags;
    public ArrayList<String> Hide;
    public ArrayList<String> TableTags;
    public ArrayList<String> TableHeaders;
    public ArrayList<String> StyleSheetRules;
    public ArrayList<String> IconFields;
    public LinkedHashMap<String,ArrayList<String>> usbdrives;
    public HashMap<String,ArrayList<String>> ChoiceFields;
    public HashMap<String,String> IconDictionary;
    public ArrayList<Integer> ColumnSizes;
    public ArrayList<String> ColumnTypes;

    // Buffered information on the Library
    public ArrayList<String> PeopleList;
    public ArrayList<String> PeopleLongList;
    public ArrayList<String> KeywordList;
    public ArrayList<String> Positions;      // List with String IDs storing the position
    public ArrayList<Integer> PositionsI;    // List with String IDs storing the position
    public HashMap<String,ArrayList<String>> Links; // Links is set from createLinksTree
    public HashMap<String,ArrayList<String>> LinksRef; // Links is set from createLinksTree

    public String CurrentCategory;
    public boolean catExists;
    public String CurrentPerson;
    public boolean personExists;
    
    public String LastErrorMessage;

    public int currentStatus;
    
    public Item marker;
    
    private final MsgLogger Msg1;

    private StructureNode createNode(int i,String s,String l) {
        StructureNode SN;
        if (i>-1) {
            SN=new StructureNode((new Item(this,i)).toText());
            SN.getData().put("id",Index.get(i,"id"));
        } else {
            SN=new StructureNode("Item not in Library: "+s);
            SN.getData().put("id","?");
        }
        SN.representation="/name/";
        SN.getData().put("pos",Integer.toString(i));
        SN.getData().put("ref",s);
        SN.getData().put("link",l);
        return(SN);
    }

    private StructureNode resRef(String s,String l) {
        String field=Parser.CutTill(s, ":");
        String value=Parser.CutFrom(s, ":");
        int i=0;
        int k=Index.XMLTags.indexOf(field);
        if (k>-1) {
            while (i<Index.getSize()) {
                if (value.equals(Index.getDataElement(i,k)))
                    return(createNode(i,s,l));
                i++;
            }
        }
        return(createNode(-1,s,l));
    }

    public DefaultTreeModel createLinksTree(Item doc) {
        Links=new HashMap<String,ArrayList<String>>();
        LinksRef=new HashMap<String,ArrayList<String>>();
        StructureNode root=new StructureNode("Available Links");
        if (doc.get("links")!=null) {
            String[] links=doc.get("links").split("\\|");
            if (links[0].length()!=0) {
                ArrayList<String> types=new ArrayList<String>();
                String type,target;
                for (int i=0;i<links.length;i++) {
                    type=Parser.CutTill(links[i],":");
                    target=Parser.CutFrom(links[i],":");
                    StructureNode SNT;
                    if (!Links.containsKey(type)) {
                        Links.put(type, new ArrayList<String>());
                        LinksRef.put(type, new ArrayList<String>());
                        SNT=new StructureNode(type);
                        root.add(SNT);
                    } else {
                        int n=0;
                        while (!root.getChildAt(n).getLabel().equals(type)) n++;
                        SNT=root.getChildAt(n);
                    }
                    StructureNode SN=resRef(target,links[i]);
                    Links.get(type).add(SN.getData("id"));
                    LinksRef.get(type).add(SN.getLabel());
                    SNT.add(SN);
                }
            }
        }
        return(new DefaultTreeModel(root));
    }

    private void createHTMLTemplates(String template) throws IOException {
        XMLHandler.Create("celsiusv2.2.htmltemplates",basedir+"htmltemplates.xml");
        HTMLtemplates=new XMLHandler(basedir+"htmltemplates.xml");
        XMLHandler XH=RSC.LibraryTemplates.get(template);
        for(int i=0;i<8;i++) {
            String n="infoMode-"+String.valueOf(i).trim();
            if (XH.get(n)!=null) {
                HTMLtemplates.addEmptyElement();
                HTMLtemplates.put("infoMode",String.valueOf(i));
                HTMLtemplates.put("template",XH.get(n));
            }
        }
        HTMLtemplates.writeBack();
    }

    public void getFieldsFromMainFile() {
        IconFields=new ArrayList<String>();
        if (MainFile.get("icon-fields")!=null) {
            String[] iconfields=MainFile.get("icon-fields").split("\\|");
            IconFields.addAll(Arrays.asList(iconfields));
        }
        usbdrives=new LinkedHashMap<String,ArrayList<String>>();
        if (MainFile.get("usbdrives")!=null) {
            String[] usbfields=MainFile.get("usbdrives").split("\\|");
            for (int i=0;i<usbfields.length;i++) {
                ArrayList<String> list=new ArrayList<String>();
                String[] lst=Parser.CutFrom(usbfields[i],":").split("\\:");
                list.addAll(Arrays.asList(lst));
                usbdrives.put(Parser.CutTill(usbfields[i],":"),list);
            }
        }
        ChoiceFields=new HashMap<String,ArrayList<String>>();
        if (MainFile.get("choice-fields")!=null) {
            String[] choicefields=MainFile.get("choice-fields").split("\\|");
            for (int i=0;i<choicefields.length;i++) {
                String field=Parser.CutTill(choicefields[i],":");
                String[] possibilities=Parser.CutFrom(choicefields[i], ":").split(",");
                ArrayList<String> poss=new ArrayList<String>();
                poss.addAll(Arrays.asList(possibilities));
                ChoiceFields.put(field, poss);
            }
        }
        IconDictionary=new HashMap<String,String>();
        if (MainFile.get("icon-dictionary")!=null) {
            String[] icondict=MainFile.get("icon-dictionary").split("\\|");
            for (int i=0;i<icondict.length;i++) {
                String field=Parser.CutTill(icondict[i],":");
                String value=Parser.CutFrom(icondict[i],":");
                IconDictionary.put(field,value);
            }
        }
        initTableColumns();
    }
    
    
    /** Creates a new Library */
    public Library(String bd,String mainfile,String nm,Resources rsc, String template) throws Exception {
        currentStatus=0;
        lastAddedItem=null;
        PeopleTags=null;
        PeopleOrKeywordsChanged=true;
        RSC=rsc;
        Msg1=rsc.Msg1;
        celsiusbasedir=Parser.CutTillLast((new File(".")).getAbsolutePath(),".");
        StyleSheetRules=new ArrayList<String>();
        basedir=bd;
        if (!celsiusbasedir.endsWith(toolbox.filesep)) celsiusbasedir+=toolbox.filesep;
        if (!basedir.endsWith(toolbox.filesep)) basedir+=toolbox.filesep;
        name=nm;
        
        (new File(basedir)).mkdir();
        (new File(basedir+"information")).mkdir();

        XMLHandler.Create("celsiusv2.1.library", mainfile);
        MainFile=new XMLHandler(mainfile);
        MainFile.addEmptyElement();
        String p=Parser.CutTill((new File(mainfile)).getCanonicalPath(),(new File(mainfile)).getName());
        if (bd.startsWith(p)) bd=Parser.CutFrom(bd, p);
        MainFile.put("directory",bd);
        for (String field : RSC.LibraryFields) {
            MainFile.put(field,RSC.LibraryTemplates.get(template).get(field));
        }
        MainFile.put("name",name);
        MainFile.put("style", "LD::style.css");
        MainFile.writeBack();

        initIndexTags();

        TextFile TD=new TextFile(basedir+"style.css",false);
        TD.putString(RSC.LibraryTemplates.get(template).get("stylesheet"));
        TD.close();

        loadStyleSheetRules();
        
        TD=new TextFile(basedir+"librarystructure.xml",false);
        TD.putString(RSC.LibraryTemplates.get(template).get("librarystructure"));
        TD.close();
        
        Structure=new XMLTree(basedir+"librarystructure.xml","/title/");
        
        TD=new TextFile(basedir+"rules.xml",false);
        TD.putString(RSC.LibraryTemplates.get(template).get("libraryrules"));
        TD.close();
        
        Rules=new XMLTree(basedir+"rules.xml","$full");
        
        XMLHandler.Create("celsiusv3.0.libraryindexfile",basedir+"libraryindex.xml");
        Index=new IndexedXMLHandler(basedir+"libraryindex.xml",IndexTags);
        
        XMLHandler.Create("celsiusv3.0peopleremarksfile",basedir+"peopleremarks.xml");
        PeopleRemarks=new XMLHandler(basedir+"peopleremarks.xml");

        XMLHandler.Create("celsiusv3.0.categoryremarksfile",basedir+"categoryremarks.xml");
        CatRemarks=new XMLHandler(basedir+"categoryremarks.xml");

        createHTMLTemplates(template);
        
        PeopleList=new ArrayList<String>();
        PeopleLongList=new ArrayList<String>();
        KeywordList=new ArrayList<String>();
        Positions=new ArrayList<String>();
        PositionsI=new ArrayList<Integer>();
        
        TextFile TF = new TextFile(basedir + "/lock", false);
        TF.close();
        getFieldsFromMainFile();

        CurrentCategory="";
        
        setChanged(false);
    }
    
    /** Loads an existing library */
    public Library(MainFrame mf, String fn,Resources rsc) {
        currentStatus=0;
        lastAddedItem=null;
        PeopleTags=null;
        PeopleOrKeywordsChanged=true;
        RSC=rsc;
        Msg1=rsc.Msg1;
        try {
            String relbase=Parser.CutTill(fn,(new File(fn)).getName());
            if (relbase.endsWith(toolbox.filesep)) relbase=relbase.substring(0,relbase.length()-1);
            MainFile=new XMLHandler(fn);
            
            name=MainFile.get("name");
            for (Library lib : RSC.Libraries) {
                if (lib.name.equals(name)) {
                    (new SafeMessage("A library with this name is already loaded.","Library not loaded:",0)).showMsg();
                    name="??#$Library with this name is already loaded.";
                    return;
                }
            }
            celsiusbasedir=Parser.CutTillLast((new File(".")).getAbsolutePath(),".");
            basedir=MainFile.get("directory");
            if (!celsiusbasedir.endsWith(toolbox.filesep)) celsiusbasedir+=toolbox.filesep;
            if (!basedir.endsWith(toolbox.filesep)) basedir+=toolbox.filesep;
            if ((name.length()==0) || (basedir.length()==0)) {
                (new SafeMessage("The library file seems to be corrupt. Cancelling...","Warning:",0)).showMsg();
                name="??##Library file corrupt.";
                return;
            }
            if (!(new File(basedir)).exists())
            basedir=relbase+"/"+basedir;
            if ((new File(basedir + "/lock")).exists()) {
                int i=toolbox.QuestionAB(RSC.getMF(),"The library "+name+" is locked. If no other instance of Celsius is accessing it," +
                        "\nyou can select \"Ignore Lock\" and open it anyway. This can happen " +
                        "\nwhen Celsius has not been shut down properly.", "Library locked", "Cancel", "Ignore Lock");
                if (i==0) {
                    name="??##cancelled";
                    return;
                }
            }
            if ((new File(basedir + "/modified")).exists()) {
                (new SafeMessage("This library ("+name+") has not been closed properly. Please run the\n\"Synchronized Library\" command as soon as possible.", "Warning:", 0)).showMsg();
            }
            Changed=false;
            loadStyleSheetRules();
            for (String field : RSC.LibraryFields) {
                ensure(field);
            }
            if (Changed) {
                MainFile.writeBack();
                Changed=false;
            }

            initIndexTags();

            try {
            Index=new IndexedXMLHandler(basedir+"libraryindex.xml",IndexTags);
            } catch (Exception e) {
              Msg1.rep(Index.lastError);
            }
            if (Index.lastError.length()>0) {
                toolbox.Warning(null, Index.lastError, "Error in library index");
                Msg1.rep(Index.lastError);
            }

            if ((new File(basedir+"authorremarks.xml")).exists()) {
                TextFile.moveFile(basedir+"authorremarks.xml", basedir+"peopleremarks.xml");
            }

            try {
                PeopleRemarks=new XMLHandler(basedir+"peopleremarks.xml");
            } catch(IOException e) {
                XMLHandler.Create("celsiusv3.0.peopleremarksfile",basedir+"peopleremarks.xml");
                PeopleRemarks=new XMLHandler(basedir+"peopleremarks.xml");
            }
            try {
                CatRemarks=new XMLHandler(basedir+"categoryremarks.xml");
            } catch(IOException e) {
                XMLHandler.Create("celsiusv3.0.categoryremarksfile",basedir+"categoryremarks.xml");
                CatRemarks=new XMLHandler(basedir+"categoryremarks.xml");
            }
            Rules=new XMLTree(basedir+"rules.xml","$full");
            Structure=new XMLTree(basedir+"librarystructure.xml","/title/");
            if ((new File(basedir+"htmltemplates.xml")).exists()) {
                HTMLtemplates=new XMLHandler(basedir+"htmltemplates.xml");
            } else {
                createHTMLTemplates("Default");
            }

            Positions=new ArrayList<String>();
            PositionsI=new ArrayList<Integer>();
            
            updatePeopleAndKeywordsLists();

            CurrentCategory="";

            TextFile TF=new TextFile(basedir+"lock",false);
            TF.close();
            getFieldsFromMainFile();
            /* Old code to correct stuff...
             String[] files = (new File(basedir + "/information")).list();
            for (int i = 0; i < files.length; i++) {
                String r = Parser.CutTill(files[i], ".");
                if (r.length() == 7) {
                    TextFile.moveFile(basedir + "/information/" + files[i], basedir + "/information/1" + files[i].substring(1));
                }
            }*/

        } catch (Exception ex) {
            RSC.Msg1.printStackTrace(ex);
            name="??##Error"+ex.toString();
            CurrentCategory="";
        }
    }

    public boolean isPeopleOrKeywordTag(String t) {
        if (PeopleTags==null) {
            this.createPeopleTags();
        }
        if (PeopleTags.contains(t)) return(true);
        if (t.equals("keywords")) return(true);
        return(false);
    }

    /**
     * Notifies the Library of a change to a certain tag
     *
     * @param t - the tag whose value has been changed.
     */
    public void notifyChange(String t) {
        if (isPeopleOrKeywordTag(t)) PeopleOrKeywordsChanged=true;
    }

    public void ensure(String k) {
        if (MainFile.get(k)==null) {
            MainFile.put(k,RSC.LibraryTemplates.get("Default").get(k));
            Changed=true;
        }
    }

    public String[] listOf(String s) {
        String t=MainFile.get(s);
        if (t==null) return(new String[0]);
        return(t.split("\\|"));
    }

    private void initIndexTags() {
        Hide=new ArrayList<String>(Arrays.asList(listOf("hide")));
        IndexTags = new ArrayList<String>();
        if (MainFile.get("index") != null) {
            String[] list = listOf("index");
            IndexTags.addAll(Arrays.asList(list));
        } else {
            IndexTags.add("type");
            IndexTags.add("title");
            IndexTags.add("authors");
            IndexTags.add("pages");
            IndexTags.add("identifier");
            IndexTags.add("keywords");
        }
        IndexTags.add("id");
        IndexTags.add("registered");
        IndexTags.add("autoregistered");
        IndexTags.add("location");
        IndexTags.add("addinfo");
        IndexTags.add("filetype");
        IndexTags.add("attributes");
    }

    private void initTableColumns() {
        TableTags = new ArrayList<String>();
        TableHeaders = new ArrayList<String>();
        ColumnSizes = new ArrayList<Integer>();
        ColumnTypes = new ArrayList<String>();
        if (MainFile.isEmpty("tablecolumns")) {
            fillTableTagsWithDefaults();
        } else {
            String[] list = listOf("tablecolumns");
            TableTags.addAll(Arrays.asList(list));
            if (MainFile.get("columnsizes") != null) {
                list = listOf("columnsizes");
                for (int i = 0; i < list.length; i++) {
                    ColumnSizes.add(Integer.valueOf(list[i]));
                }
                if (list.length<TableTags.size()) {
                    for (int i=list.length;i<TableTags.size();i++)
                        ColumnSizes.add(1);
                }
            } else {
                for (int i=0;i<list.length; i++)
                    ColumnSizes.add(1);
            }
            if (MainFile.get("columntypes") != null) {
                list = listOf("columntypes");
                ColumnTypes.addAll(Arrays.asList(list));
                if (list.length<TableTags.size()) {
                    for (int i=list.length;i<TableTags.size();i++)
                    ColumnTypes.add("text");
                }
            } else {
                for (int i=0;i<list.length; i++)
                    ColumnTypes.add("text");
            }
            list = listOf("columnheaders");
            TableHeaders.addAll(Arrays.asList(list));
        }
    }

    public void setColumnSize(int c,int w) {
        ColumnSizes.set(c, Integer.valueOf(w));
        String s="";
        for (Integer i : ColumnSizes) {
            if (i==0) i=1;
            s+="|"+Integer.toString(i);
        }
        s=s.substring(1);
        MainFile.put("columnsizes", s);
        this.setChanged(true);
    }
    
    public void closeLibrary() {
            TextFile.Delete(basedir + "/lock");
            TextFile.Delete(basedir + "/modified");
    }
    
    /**
     * Deletes all files associated with library
     */
    public int deleteLibrary() {
        String TI = "LIB" + name + ">";
        try {
            Msg1.repS(TI + "Removing base folder " + basedir);
            if (!(TextFile.removeFolder(basedir))) {
                Msg1.repS(TI + "failed!");
                return (8);
            }
            Msg1.repS(TI + "Removing main file " + MainFile.source);
            if (!(TextFile.Delete(MainFile.source))) {
                Msg1.repS(TI + "failed!");
                return (8);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Msg1.repS(TI + "failed!");
            return (8);
        }
        return (0);
    }

    /**
     * Update the list of people in the library
     */
    public void updatePeopleAndKeywordsLists() {
        if (!PeopleOrKeywordsChanged) return;
        PeopleList=new ArrayList<String>();
        PeopleLongList=new ArrayList<String>();
        KeywordList=new ArrayList<String>();
        String a1;
        String[] people;
        String[] keywords;
        String[] peopletags=listOf("people");
        for (Item item : this) {
            try {
                for (int j=0;j<peopletags.length;j++) {
                    if (item.getI(peopletags[j])!=null) {
                    people = item.getI(peopletags[j]).split("\\|");
                        for (int i = 0; i < people.length; i++) {
                            a1 = people[i];
                            if (PeopleLongList.indexOf(a1) == -1) {
                                PeopleLongList.add(a1);
                                a1 = Parser.CutTill(a1, ",");
                                if (PeopleList.indexOf(a1) == -1) {
                                    PeopleList.add(a1);
                                }
                            }
                        }
                    }
                }
                if (item.get("keywords") != null) {
                    keywords = item.getI("keywords").split("\\|");
                    for (int i = 0; i < keywords.length; i++) {
                        if (KeywordList.indexOf(keywords[i].toLowerCase()) == -1) {
                            KeywordList.add(keywords[i].toLowerCase());
                        }
                    }
                }
                if (item.error==5) {
                    currentStatus=10;
                }
            } catch (Exception e) {
                currentStatus=10;
            }
        }
        Collections.sort(PeopleList);
        Collections.sort(PeopleLongList);
        Collections.sort(KeywordList);
        PeopleOrKeywordsChanged=false;
    }

    /**
     * Save the whole library
     * @throws IOException
     */
    public void writeBack() throws IOException {
        TextFile.Delete(Index.source+".bck");
        TextFile.moveFile(Index.source, Index.source+".bck");
        Index.writeBack();
        TextFile.Delete(Rules.source+".bck");
        TextFile.moveFile(Rules.source, Rules.source+".bck");
        Rules.writeBack();
        TextFile.Delete(Structure.source+".bck");
        TextFile.moveFile(Structure.source, Structure.source+".bck");
        Structure.writeBack();
        TextFile.Delete(MainFile.source+".bck");
        TextFile.moveFile(MainFile.source, MainFile.source+".bck");
        MainFile.writeBack();
        setChanged(false);
        createPeopleTags();
    }
    
    // Status string for status bar.
    public String Status(boolean count) {
        String tmp = "Current library "+name+":";
        if (Index.XMLTags.contains("pages")) {
            if (count) {
                totalpages = 0;
                if (Index.getSize()==0)
                    return("No documents in current library.");
                for (Item doc : this)
                    totalpages += doc.getPages();
            }
            tmp+=Integer.toString(totalpages) + " pages in ";
        } else if (Index.XMLTags.contains("duration")) {
            if (count) {
                totalduration = 0;
                if (Index.getSize()==0)
                    return("No documents in current library.");
                for (Item doc : this)
                    totalduration += doc.getDuration();
            }
            tmp+=toolbox.formatSeconds(totalduration) + " in ";
        }
        tmp+=Integer.toString(Index.getSize()) + " items";
        if (Changed)
            tmp += ", main index modified.";
        else tmp += ".";
        return(tmp);
    }

    public int getSize() {
        return(Index.getSize());
    }

    /**
     * Check for doublettes...
     *
     * Return values: 10 : exact doublette, 5: file might be doublette, 4 : apparent Doublette,  0 : no doublette
     */
    public int Doublette(Item testdoc) throws IOException {
        // Look for doublettes
        Msg1.rep("Starting Doublette for "+testdoc.toText());
        String[] ef = listOf("differentiating-fields");
        String fn = testdoc.getCompleteDirS("location");
        if (fn==null) return(0);
        if (fn.equals("")) return(0);
        //System.out.println(fn);
        for (Item doc : this) {
            // Literal doublette found?
            if (doc.get("location") != null) {
                int i = TextFile.IsDoublette(doc.getCompleteDirS("location"), fn);
                if (i == 1) {
                    marker = doc;
                    Msg1.rep("Ending Doublette for "+testdoc.toText());
                    return (10);
                }
                if (i == 0) {
                    marker = doc;
                    Msg1.rep("Ending Doublette for "+testdoc.toText());
                    return (5);
                }
            }
            // Check all essential fields
            int i = 0;
            for (; i < ef.length; i++) {
                String t = testdoc.getS(ef[i]);
                if (t != null) {
                    if (!(t.toLowerCase().equals(doc.getS(ef[i]).toLowerCase()))) {
                        i = ef.length + 2;
                    }
                }
            }
            if (i == ef.length) {
                marker = doc;
                    Msg1.rep("Ending Doublette for "+testdoc.toText());
                return (4);
            }
        }
                    Msg1.rep("Ending Doublette for "+testdoc.toText());
        return(0);
    }

    /**
     * Check for doublettes...
     *
     * Return values: 10 : exact doublette, 4 : apparent Doublette,  0 : no doublette
     */
    public int Droublette(String fn, MProperties Information) throws IOException {
        // Look for doublettes
        String[] ef=listOf("essential-fields");
        for (Item doc : this) {
            // Literal doublette found?
            if ((fn.length()>0) && (doc.get("location")!=null)) {
                int i=TextFile.IsDoublette(doc.getCompleteDirS("location"),fn);
                if (i==1) {
                    marker=doc;
                    return(10);
                }
                if (i==0) {
                    return(4);
                }
            }
            if (Information!=null) {
                // Check all essential fields
                int i=0;
                for (;i<ef.length;i++) {
                    if (!(Information.get(ef[i]).toLowerCase().equals(doc.get(ef[i]).toLowerCase()))) i=ef.length+2;
                }
                if (i==ef.length) return(4);
            }
        }
        return (0);
    }
    
    /**
     * Inserts an empty record into the Index and returns a doc object refering there
     * @return the document object
     */
    public Item createEmptyItem() {
        Item item;
        String id;
        // synchronize Library, such that a taken slot is not overwritten.
        synchronized(Index) {
            int k=Index.position;
            // Get free registration space and look for already existing papers
            // File does not exists yet, make entry
            id=String.valueOf(Index.addEmptyElement());
            item=new Item(this,id);
            Index.position=k;
        }
        item.put("id",id);
        return(item);
    }

    public void acquireCopyOfDocument(Item src) {
        String TI="LIB"+name+">";
        String[] essentialFields=MainFile.get("essential-fields").split("\\|");
        for (int i=0; i<essentialFields.length;i++) {
            if (src.get(essentialFields[i])==null) {
                toolbox.Warning(RSC.getMF(),"The item "+src.toText()+"\ncould not be copied, as the field "+essentialFields[i]+",\nrequired by the library "+this.name+" is not set.", "Copying cancelled...");
                return;
            }
        }
        Msg1.repS(TI+"Copying document "+src.toText()+" from library "+src.Lib.name+" to "+name);

        String filename="";
        String fullfilename;
        
        Item doc;

        if (src.get("location").startsWith("LD")) {
            filename=src.standardFileName(this);
            if (!src.guaranteeStandardFolder(this)) {
                toolbox.Warning(RSC.getMF(),"Item folder could not be created.", "Warning:");
                return;
            }
            doc=createEmptyItem();
            doc.guaranteeStandardFolder(this);
            filename=doc.getStandardFolder(this)+toolbox.filesep+filename;
            fullfilename=doc.completeDir(filename);
        } else {
            doc=createEmptyItem();
            fullfilename=src.getCompleteDirS("location");
        }

        String id=doc.get("id");
        doc.put("location",filename);
        
        // The actual move document procedure
        // Document
        Msg1.repS(TI+"Registering under name: "+filename+", "+fullfilename);
        try {
            if (filename.startsWith("LD::"))
                TextFile.CopyFile(src.getCompleteDirS("location"),fullfilename);
            ArrayList<String> tags=src.getAITags();
            for (String key : tags) {
                if ((src.get(key)!=null) && (!key.equals("id")) && (!key.equals("location"))) {
                    if (src.get(key).indexOf("::")==2) {
                        String from=src.getCompleteDirS(key);
                        String end=Parser.Substitute(src.get(key),src.get("id"),doc.get("id"));
                        try {
                            TextFile.CopyFile(src.getCompleteDirS(key),doc.completeDir(end));
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                        doc.put(key,end);
                    } else {
                        doc.put(key,src.get(key));
                    }
                }
            }
            // Plaintext
            if (src.get("plaintxt")!=null) {
                doc.put("parse","all");
            } else {
                doc.put("parse","header");
            }
            doc.save();
        } catch (Exception ex) {
            RSC.Msg1.printStackTrace(ex);
        }
        
        
        // update AuthorList
        addPeople(doc);
        
        // Notify MF of change in library and write addinfo
        setChanged(true);
    }
    
    /**
     * Adds the people contained in the given string to the list of authors
     */
    public void addPeople(Item doc) {
        boolean added=false;
        String[] p=listOf("people");
        String[] people;
        String a1;
        for(int j=0; j<p.length;j++) {
            if (doc.get(p[j])!=null) {
                people=doc.get(p[j]).split("\\|");
                for (int i=0;i<people.length;i++) {
                    a1=people[i];
                    if (PeopleLongList.indexOf(a1)==-1) {
                        added=true;
                        PeopleLongList.add(a1);
                        a1=Parser.CutTill(a1,",");
                        if (PeopleList.indexOf(a1)==-1)
                            PeopleList.add(a1);
                    }
                }
            }
        }
        if (added) {
            Collections.sort(PeopleList);
            Collections.sort(PeopleLongList);
        }
    }

    public String compressDir(String s) {
        if (s.startsWith(basedir)) return("LD::"+Parser.CutFrom(s,basedir));
        if (s.startsWith(celsiusbasedir)) return("BD::"+Parser.CutFrom(s,celsiusbasedir));
        return(s);
    }
    
    public String rawData(int i) {
        if (i==1) {
            IndexedXMLHandler xh=Index;
            String tmp = "";
            String s2;
            if (xh == null) {
                return ("Corresponding XMLHandler does not exist.");
            }
            for (String tag : xh.XMLTags) {
                s2 = xh.get(tag);
                if ((s2 != null) && !(s2.indexOf("\n") > -1)) {
                    tmp += tag + ": " + s2 + toolbox.linesep;
                }
            }
            return (tmp);
        } 
        XMLHandler xh = PeopleRemarks;
        String tmp = "";
        String s2;
        if (xh == null) {
            return ("Corresponding XMLHandler does not exist.");
        }
        for (String tag : xh.XMLTags) {
            s2 = xh.get(tag);
            if ((s2 != null) && !(s2.indexOf("\n") > -1)) {
                tmp += tag + ": " + s2 + toolbox.linesep;
            }
        }
        return (tmp);
    }

    public void moveToPerson(String person) {
        CurrentPerson=person;
        personExists=true;
        PeopleRemarks.toFirstElement();
        while (!PeopleRemarks.endReached && !PeopleRemarks.getS("person").equals(person))
            PeopleRemarks.nextElement();
        if (!PeopleRemarks.getS("person").equals(person)) personExists=false;
    }
    
    // Make sure, peopleremarks.xml is ready to receive information
    public void guaranteePerson() {
        if (!personExists) {
            PeopleRemarks.addEmptyElement();
            PeopleRemarks.put("person",CurrentPerson);
        }
    }
    
    public void moveToCat(String category) {
        String catname=category;
        if (category.equals("::root")) catname="Items not registered";
        CurrentCategory=category;
        catExists=true;
        CatRemarks.toFirstElement();
        while (!CatRemarks.endReached && !CatRemarks.getS("category").equals(catname))
            CatRemarks.nextElement();
        if (!CatRemarks.getS("category").equals(category)) catExists=false;
    }

    // Make sure, peopleremarks.xml is ready to receive information
    public void guaranteeCat() {
        if (!catExists) {
            CatRemarks.addEmptyElement();
            CatRemarks.put("category",CurrentCategory);
        }
    }

    public void autoSortColumn(ItemTable DT) {
        int c=0;
        if (MainFile.get("autosortcolumn")!=null)
            c=Integer.valueOf(MainFile.get("autosortcolumn"));
        DT.sortItems(c,true);
    }

    /**
     * Add all documents in category tmp to the item table IT
     */
    public void showItemsInCategory(String tmp,ItemTable IT) {
        moveToCat(tmp);
        IT.setLibrary(this);
        for (Item doc : this) {
            if (Parser.EnumContains(doc.get("autoregistered"), tmp) || Parser.EnumContains(doc.get("registered"), tmp)
                    || ((tmp.equals("::root")) && (!doc.hasAttribute("hidden") || RSC.displayHidden) && (doc.isEmpty("autoregistered")) && (doc.isEmpty("registered")))) {
                IT.addItemFast(doc);
            }
        }
        autoSortColumn(IT);
        IT.DTM.fireTableDataChanged();
        IT.resizeTable(true);
    }
    
    /**
     * Adds the documents with author <tmp> to the datamodel dataModel.
     */
    public void showDocsPerson(String tmp,ItemTable DT) {
        DT.setLibrary(this);
        String people;
        String[] peopletags=listOf("people");
        for (Item doc : this) {
            if (!doc.hasAttribute("hidden"))
                for (int i=0;i<peopletags.length;i++) {
                    people = doc.get(peopletags[i]);
                    if (people!=null) {
                        if (Parser.EnumContains(people, tmp) || Parser.EnumContains2(people, tmp)) {
                            DT.addItemFast(doc);
                            i=peopletags.length;
                        }
                    }
                }
        }
        autoSortColumn(DT);
        DT.resizeTable(true);
    }

    /**
     * Adds the documents with keyword <tmp> to the datamodel dataModel.
     */
    public void showDocsKey(String tmp,ItemTable DT) {
        DT.setLibrary(this);
        String keys;
        for (Item doc : this) {
            keys=doc.getS("keywords").toLowerCase();
            if (keys.length()>0) {
                if (Parser.EnumContains(keys, tmp) || Parser.EnumContains2(keys, tmp)) {
                    DT.addItemFast(doc);
                }
            }
        }
        autoSortColumn(DT);
        DT.resizeTable(true);
    }
    
    public void reloadDisplayString() {
        try {
            HTMLtemplates=new XMLHandler(basedir+"htmltemplates.xml");
        } catch(IOException e) { e.printStackTrace(); }
    }

    public void setDisplayString(int infoMode, String ds) {
        if (!HTMLtemplates.goToFirst("infoMode", String.valueOf(infoMode).trim())) {
            HTMLtemplates.addEmptyElement("infoMode", String.valueOf(infoMode).trim());
        }
        HTMLtemplates.put("template", ds);
        try {
            HTMLtemplates.writeBack();
        } catch (IOException ex) {
            RSC.Msg1.printStackTrace(ex);
        }
    }

    public String getDisplayString(int infoMode) {
        boolean ok=HTMLtemplates.goToFirst("infoMode", String.valueOf(infoMode));
        if (ok) return(HTMLtemplates.get("template"));
        else {
            String n="infoMode-"+String.valueOf(infoMode).trim();
            if (RSC.LibraryTemplates.get(n)!=null) {
                return(RSC.LibraryTemplates.get("Default").get(n));
            } else return("Error loading display strings from HTMLtemplates!");
        }
    }

    public String getCollaborators(String person) {
        final ArrayList<String> coll = new ArrayList<String>();
        String collabs,colab;
        boolean ok;
        for (Item item : this) {
            for (String peopletag : listOf("people")) {
                collabs = item.get(peopletag)+"|";
                int i=collabs.indexOf(person);
                int j=i+person.length();
                if (i>-1) {
                    ok=false;
                    if ((i==0) && ((collabs.charAt(j)==',') || (collabs.charAt(j)=='|'))) ok=true;
                    if ((i>0) && (collabs.charAt(i-1)=='|') && ((collabs.charAt(j)==',') || (collabs.charAt(j)=='|'))) ok=true;
                    if (ok) {
                        while (collabs.length() > 0) {
                            colab=Parser.ReturnFirstItem(collabs);
                            if (!coll.contains(colab)) {
                                coll.add(colab);
                            }
                            collabs = Parser.CutFirstItem(collabs);
                        }
                    }
                }
            }
        }
        coll.remove(person);
        Collections.sort(coll);
        for (int i=0;i<coll.size();i++) {
            collabs=coll.get(i);
            coll.set(i, Parser.CutFrom(collabs,",").trim()+" "+Parser.CutTill(collabs, ","));
        }
        String collaborators = coll.toString();
        collaborators = collaborators.substring(1, collaborators.length() - 1);
        return(collaborators);
    }

    /**
     * Fills in an HTML template according to the properties given
     * @param s the template
     * @param properties the properties
     * @return the filled-out HTML string
     */
    private String replaceInTemplate(String s, MProperties properties) {
        String template=s;
        String line,tag,value;
        String out="";

        while (template.length() > 0) {
            line = Parser.CutTill(template, "\n");
            template = Parser.CutFrom(template, "\n");
            if (line.startsWith("#if#")) {
                while (line.startsWith("#if#")) {
                    line=Parser.CutFrom(line,"#if#");
                    tag=Parser.CutTill(line,"#");
                    if (tag.charAt(0)=='!') {
                        tag=tag.substring(1);
                        if ((!properties.containsKey(tag)) || (properties.get(tag).length()==0))
                            line=Parser.CutFrom(line,"#");
                        else line="";
                    } else {
                        if ((properties.containsKey(tag)) && (properties.get(tag).length()>0))
                            line=Parser.CutFrom(line,"#");
                        else line="";
                    }
                }
            } else {
                out+="\n";
            }
            if (line.trim().length() > 0) {
                for (String key : properties.keySet()) {
                    value = properties.get(key);
                    line=line.replace("#" + key + "#", value);
                    line=line.replace("#|" + key + "#", "<ul><li>"+Parser.Substitute(value,"|", "</li><li>")+"</li></ul>");
                    line=line.replace("#$" + key + "#", value);
                }
                out+=line;
            }
        }
        return(out.trim());
    }

    public String addLinks(String s) {
        String tmp = "";
        String a;
        try {
            while (s.indexOf(",") > 0) {
                a = Parser.CutTill(s, ",");
                tmp += "<a href='http://$$author." + URLEncoder.encode(a, "UTF-8") + "'>" + a + "</a>, ";
                s = Parser.CutFrom(s, ",");
            }
            if (s.indexOf(" and ") == -1) {
                tmp += "<a href='http://$$author." + URLEncoder.encode(s.trim(), "UTF-8") + "'>" + s.trim() + "</a>";
            } else {
                while (s.length() > 0) {
                    a = Parser.CutTill(s, " and ");
                    tmp += "<a href='http://$$author." + URLEncoder.encode(a, "UTF-8") + "'>" + a + "</a> and ";
                    s = Parser.CutFrom(s, " and ");
                }
            }
        } catch (UnsupportedEncodingException ex) {
        }
        return (Parser.CutTillLast(tmp, " and "));
    }

    public String fileSize(String n) {
        long l=new File(n).length();
        if (l<1024) return(String.valueOf(l)+" Bytes");
        DecimalFormat df = new DecimalFormat("0.000") ;
        if (l<(1024*1024)) return(df.format(l/1024.0)+" KiB");
        return(df.format(l/1024.0/1024.0)+" MiB");
    }
    
    /**
     * Create the String, an HTML-file should be represented by
     */
    public String displayString(int mode, Item doc, HashMap<String,String> jl) {
        String out,template;
        template=this.getDisplayString(mode);
        MProperties data=RSC.getCurrentItemTable().getData();
        switch (mode) {
            case 0:
                // Single document view
                if (doc == null)
                    return ("No document selected.");
                data=doc.getMProperties(true);
                for (String k : data.keySet()) {
                    if (data.get(k).indexOf("::")==2) {
                        data.put(k, doc.getCompleteDirS(k));
                    }
                }
                // set all the relevant data
                if (template.indexOf("altversions")>-1) {
                    String altversions="";
                    String nmb;
                    for (String key : doc.getAITags()) {
                        if ((key.startsWith("altversion-label-")) && (doc.get(key)!=null)) {
                            nmb=Parser.CutFromLast(key, "-");
                            altversions+="<a href=\"http://$$view-alt-"+nmb+"\">"+doc.get(key)+"</a>&nbsp;";
                        }
                        String val=doc.getCompleteDirS(key);
                        data.put(key, val);
                    }
                    if (altversions.length()>0) data.put("altversions",altversions);
                }
                if (template.indexOf("#listsimilar#")>-1) {
                    String output="<ul>";
                    for (Item doc2 : doc.getCombined()) {
                        output+="<li><a href='http://cid-"+doc2.get("id")+"'>"+doc2.toText()+"</a></li>";
                    }
                    output+="</ul>";
                    data.put("listsimilar",output);
                }
                if (template.indexOf("#$$links#")>-1) {
                    if (doc.getS("links").trim().length()>0) data.put("$$links","yes");
                }
                if (template.indexOf("#$$links-")>-1) {
                    int j=0;
                    int i=template.indexOf("#$$links-",j);
                    while ((i>-1) && (j>-1)) {
                        j=template.indexOf("#",i+1);
                        if ((j>-1) && (doc.getS("links").trim().length()>0)) {
                            String linktype=template.substring(i+9,j);
                            if (doc.getS("links").trim().length()>0) data.put("$$links-"+linktype,"yes");
                        }
                        i=template.indexOf("#$$links-",j);
                    }
                }
                boolean updatedLinks=false;
                if (template.indexOf("#$$linksList#")>-1) {
                    RSC.getMF().jIP.updateLinks();
                    updatedLinks=true;
                    String output="<ul>";
                    for (String linktype : Links.keySet()) {
                        for (int c=0;c<Links.get(linktype).size();c++) {
                            output+="<li><a href='http://cid-"+Links.get(linktype).get(c)+"'>"+LinksRef.get(linktype).get(c)+"</a></li>";
                        }
                    }
                    output+="</ul>";
                    data.put("$$linksList",output);
                }
                if (template.indexOf("#$$linksList-")>-1) {
                    if (!updatedLinks) RSC.getMF().jIP.updateLinks();
                    int j=0;
                    int i=template.indexOf("#$$linksList-",j);
                    while ((i>-1) && (j>-1)) {
                        j=template.indexOf("#",i+1);
                        if ((j>-1) && (doc.getS("links").trim().length()>0)) {
                            String linktype=template.substring(i+13,j);
                            String output="<ul>";
                            if (Links.get(linktype)!=null) //#####
                                for (int c=0;c<Links.get(linktype).size();c++) {
                                    output+="<li><a href='http://cid-"+Links.get(linktype).get(c)+"'>"+LinksRef.get(linktype).get(c)+"</a></li>";
                                }
                            output+="</ul>";
                            data.put("$$linksList-"+linktype,output);
                        }
                        i=template.indexOf("#$$links-",j);
                    }
                }
                for (String p : listOf("people")) {
                    data.put(p+"&1", addLinks(toolbox.shortenNames(doc.getS(p))));
                    data.put(p+"&2", addLinks(toolbox.ToBibTeXAuthors(doc.getS(p))));
                    data.put(p+"&3", addLinks(toolbox.Authors3FromCelAuthors(doc.getS(p))));
                    data.put(p+"&4", addLinks(toolbox.Authors4FromCelAuthors(doc.getS(p))));
                }
                for (String p : listOf("icon-fields")) {
                    data.put(p+"&id", RSC.Icons.basefolder+toolbox.filesep+doc.getIconField(p)+".png");
                }
                if (doc.get("location")!=null) {
                    data.put("filesize", fileSize(doc.getCompleteDirS("location")));
                } else {
                    data.put("filesize","0 Bytes");
                }
                String dur=doc.getS("duration");
                if (dur.length()>0) {
                    try {
                    if (dur.endsWith("min")) {
                        data.put("duration&",toolbox.formatSeconds(Integer.parseInt(Parser.CutTill(dur,"min").trim())*60));
                    } else data.put("duration&",toolbox.formatSeconds(Integer.parseInt(dur)));
                    } catch (Exception e) {
                        
                    }
                }
                if (IndexTags.contains("lat")) {
                    data.put("distance&",toolbox.getDistance(RSC.mylat, RSC.mylon, Double.valueOf(doc.getS("lat")), Double.valueOf(doc.getS("lon"))));
                    data.put("heading&",toolbox.getBearing(RSC.mylat, RSC.mylon, Double.valueOf(doc.getS("lat")), Double.valueOf(doc.getS("lon"))));
                    data.put("lat&",toolbox.latitudeToString(Double.valueOf(doc.getS("lat"))));
                    data.put("lon&",toolbox.longitudeToString(Double.valueOf(doc.getS("lon"))));
                }
                data.put("autoregistered", doc.get("autoregistered"));
                data.put("registered", doc.get("registered"));
                BibTeXRecord BTR=new BibTeXRecord(doc.get("bibtex"));
                if ((BTR!=null) && (BTR.parseError==0)) {
                    if (jl.containsKey(BTR.get("journal")))
                        data.put("journallink", "yes");
                    for (String key : BTR.keySet())
                        data.put("bibtex."+key,BTR.get(key));
                }
                out=replaceInTemplate(template,data);
                break;
            case 1:
                // single author view
                if (CurrentPerson == null)
                    return ("No author selected.");
                data.put("person", CurrentPerson);
                data.put("collaborators", addLinks(getCollaborators(CurrentPerson)));
                if (personExists)
                    for (String key : PeopleRemarks.XMLTags)
                        data.put(key,PeopleRemarks.get(key));
                out=replaceInTemplate(template,data);
                break;
            case 2:
                // single category view
                if (CurrentCategory == null)
                    return ("No category selected.");
                if (CurrentCategory.equals("::root"))
                    data.put("category","Items not registered");
                else
                    data.put("category", CurrentCategory);
                if (catExists)
                    for (String key : CatRemarks.XMLTags)
                        data.put(key,CatRemarks.get(key));
                out=replaceInTemplate(template,data);
                break;
            case 3:
                //multiple papers selected
                out=replaceInTemplate(template,data);
                break;
            case 4:
                // search identifiers
                out=replaceInTemplate(template,data);
                break;
            case 5:
                // search general
                out=replaceInTemplate(template,data);
                break;
            case 6:
                // search keywords
                out=replaceInTemplate(template,data);
                break;
            case 7:
                // links
                out=replaceInTemplate(template,data);
                break;
            case 8:
                // just added
                out=replaceInTemplate(template,data);
                break;
            default:
                out="Unknown infoMode!";
        }
        return(out);
    }

    public boolean hasChanged() {
        return(Changed);
    }

    public void setChanged(boolean ch) {
        if (!Changed && ch)  {
            try {
                TextFile TF=new TextFile(basedir+"modified",false);
                TF.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        if (Changed && !ch)  {
            try {
                TextFile.Delete(basedir+"modified");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        Changed=ch;
    }


    public Iterator<Item> iterator() {
        return(new LibraryIterator(this));
    }

    private void fillTableTagsWithDefaults() {
        TableTags.add("type");
        TableTags.add("title");
        TableTags.add("authors");
        TableTags.add("identifier");
        ColumnSizes.add(-20);
        ColumnSizes.add(200);
        ColumnSizes.add(100);
        ColumnSizes.add(80);
    }
    
    /*
     * Routines that might be used in the future to split up directory size
    public String adj(int i) {
        String tmp=Integer.toString(i);
        return(tmp+"000".substring(3-tmp.length()));
    }
    
    public String getTargetFolderNumber() {
        int tfn=0;
        String basepath=this.compressDir("LD::documents");
        boolean complete=false;
        while (!complete) {
            String dir=basepath+adj(tfn);
            if (!(new File(dir)).exists()) {
                (new File(dir)).mkdir();
            }
            if ((new File(dir)).listFiles().length>999) {
                tfn++;
            } else {
                complete=true;
            }
        }
        return(adj(tfn));
    }
     */

    /**
     * modes : 0: move to doc folder, 1: leave where it is
     * return: 0: success, 1: name occupied, 2: couldn't create folder
     */
    public int addItem(Item doc,String prereg,int mode) {
        String tmp="location";
        if (listOf("essential-fields").length>0) tmp=listOf("essential-fields")[0];
        String TI="LIB"+name+"::"+doc.get(tmp)+">";

        // deal with preparation for file
        boolean dealwithfile=false;
        String maintrg="";
        if (doc.totalKeySet().contains("location") && (doc.get("location")!=null)) {
            dealwithfile=true;
            if ((mode ==0) && (!doc.guaranteeStandardFolder(this))) {
                toolbox.Warning(RSC.getMF(),"Item folder could not be created.", "Warning:");
                return(2);
            }
            maintrg=doc.getStandardFolder(this)+toolbox.filesep+doc.standardFileName(this);

            if ((mode==0) && (new File(completeDir(maintrg,""))).exists()) {
                Msg1.repS(TI+"There is already a file named "+completeDir(maintrg,"")+"!");
                int j=toolbox.QuestionYN(RSC.getMF(),"There is already a file named "+maintrg+"!\n Overwrite?","Warning");
                if (j==JOptionPane.NO_OPTION) {
                    return(1);
                }
            }
        }
        Msg1.repS(TI+"Creating new doc and preregistering.");
        Item newdoc=createEmptyItem();
        if (prereg.length()!=0) newdoc.put("registered",prereg);

        // Copying all relevant information.
        Msg1.repS(TI+"Copying relevant information.");
        for (String k : doc.totalKeySet()) {
//            System.out.println("Writing::"+k+"::"+doc.get(k));
            if ((!k.startsWith("$$")) && (!k.equals("id"))) if (doc.get(k)!=null) newdoc.put(k,doc.get(k));
        }
        newdoc.put("fullpath", null);
        newdoc.save();

        // move addinfo files
        for (String key : newdoc.getAITags()) {
            if ((newdoc.get(key)!=null) && newdoc.get(key).startsWith("/$")) {
                String a1=newdoc.get(key).substring(2);
                String a2=Parser.CutFrom(a1,"/$");
                a1=Parser.CutTill(a1,"/$");
                if ((new File(a1)).exists()) {
                    tmp = "LD::information/" + newdoc.get("id");
                    // check if this file is already existing
                    while ((new File(completeDir(tmp + a2,"")).exists())) {
                        tmp += "X";
                    }
                    if (tmp.indexOf('X')==-1) tmp="AI::";
                    try {
                        Msg1.repS(TI + "Moving file " + a1 + " to " + newdoc.completeDir(tmp + a2));
                        TextFile.moveFile(a1, newdoc.completeDir(tmp + a2));
                        newdoc.put(key, tmp + a2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    newdoc.put(key, null);
                }
            } 
        }

        // move document
        if (mode==0) {
            if (dealwithfile) {
                Msg1.repS(TI+"Moving document and plaintxt.");
                try {
                    TextFile.moveFile(newdoc.get("location"), newdoc.completeDir(maintrg));
                    newdoc.put("location", maintrg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // move plaintxt
                String fn=newdoc.get("plaintxt");
                if (fn!=null) {
                    tmp = "LD::information/" + newdoc.get("id");
                    // check if this file is already existing
                    while ((new File(completeDir(tmp + ".txt.gz","")).exists())) {
                        tmp += "X";
                    }
                    if (tmp.indexOf('X')==-1) tmp="AI::";
                    try {
                        TextFile.moveFile(fn, newdoc.completeDir(tmp+".txt.gz"));
                        newdoc.put("plaintxt", tmp+".txt.gz");
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            if (dealwithfile) {
                tmp=newdoc.get("location");
                newdoc.put("location",compressDir(tmp));
            }
        }
        newdoc.save();
        Msg1.repS(TI+"done.");

        // Register document and update AuthorList
        ThreadRegister RT=new ThreadRegister(this,newdoc,Msg1);
        RT.run();
        // Add new authors to AuthorList
        addPeople(newdoc);

        // Notify MF of change in library and write addinfo
        lastAddedItem=newdoc;
        setChanged(true);
        return(0);
    }

    /**
     * Replace a found doublette with file contained in doc.
     * @param doc
     */
    public void replaceItem(Item item) {
        Item item2=marker;
        if (item.getS("location").length()>0) {
            String f1=item2.getCompleteDirS("location");
            String ft;
            if (f1.equals("")) {
                f1=completeDir(item.getStandardFolder(this)+toolbox.filesep+item.standardFileName(this),null);
                ft=item.get("filetype");
                item2.put("filetype",ft);
                item2.put("plaintxt", "AI::.txt.gz");
            } else {
                ft=TextFile.getFileType(item.get("location"));
            }
            String f3=item2.getCompleteDirS("plaintxt");
            TextFile.Delete(f1);
            TextFile.Delete(f3);
            if ((ft!=null) && (ft.length()>1)){
                f1=Parser.CutTillLast(f1,item2.get("filetype"))+ft;
                String src1=item.get("location");
                String src2=item.get("plaintxt");
                item2.put("filetype",ft);
                item2.put("location",compressDir(f1));
                try {
                    TextFile.moveFile(src1, f1);
                    if (src2!=null) TextFile.moveFile(src2, f3);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        for (String t : item.totalKeySet()) {
            if (!(t.equals("location") || (t.equals("plaintxt"))))
                if ((item.get(t)!=null) && (item.get(t).length()>0))
                    item2.put(t,item.get(t));
        }
        setChanged(true);
        item2.save();
        lastAddedItem=item2;
    }

    public String completeDir(String s,String id) {
        if ((s==null) || (s.indexOf("::")==-1)) return(s);
        if (s.indexOf("::")>2) return(s);
        String sig=Parser.CutTill(s,"::");
        s=Parser.CutFrom(s,"::");
        String s2=s;
        if (s2.startsWith(toolbox.filesep)) s2=s2.substring(1);
        if (sig.equals("AI")) {
            if (s2.charAt(0)=='.') s2=s2.substring(1);
            return(basedir+"information"+toolbox.filesep+id+"."+s2);
        }
        if (sig.equals("LD")) return(basedir+s2);
        if (sig.equals("BD")) return(celsiusbasedir+s2);
        return(s);
    }

    public void adjustStyleSheet(StyleSheet styleSheet) {
        for (String rule : StyleSheetRules) {
            styleSheet.addRule(rule);
        }
    }

    public void loadStyleSheetRules() throws IOException {
        StyleSheetRules = new ArrayList<String>();
        if (MainFile.get("style") != null) {
            TextFile style = new TextFile(completeDir(MainFile.get("style"),""));
            while (style.ready()) {
                StyleSheetRules.add(style.getString());
            }
            style.close();
        }
    }

    public DefaultComboBoxModel getTypesDCBM() {
        DefaultComboBoxModel DCBM=new DefaultComboBoxModel();
        DCBM.addElement("arbitrary");
        for (String ft : RSC.Icons.Types) {
            DCBM.addElement(ft);
        }
        return(DCBM);
    }

    public Comparator getComparator(String t,boolean invertSort, int ty) {
        if (ChoiceFields.containsKey(t)) {
            return(new CompChoice(t,invertSort,ChoiceFields.get(t)));
        } 
        return (new CompTable(t, invertSort,ty));
    }

    public int getPosition(String id) {
        return(Index.getPosition(id));
    }

    public int getPosition(int id) {
        return(Index.getPosition(id));
    }

    private void createPeopleTags() {
        String[] lop=listOf("people");
        PeopleTags=new ArrayList<String>();
        PeopleTags.addAll(Arrays.asList(lop));
    }

    class CompTable implements Comparator<Item> {

        private String tag;
        private boolean forwards;
        private int type;

        public CompTable(final String t,boolean f, int ty) {
            tag=t;forwards=f;type=ty;
        }

        private int compare(String a, String b) {
            if (type==1) {
                int i=0;
                while ((i<a.length()) && (!Character.isLetter(a.charAt(i)))) i++;
                double d1=Double.valueOf(a.substring(0,i).trim());
                i=0;
                while ((i<b.length()) && (!Character.isLetter(b.charAt(i)))) i++;
                double d2=Double.valueOf(b.substring(0,i).trim());
                if (d1>d2) return(1);
                if (d1<d2) return(-1);
                return(0);
            }
            return(a.compareTo(b));
        }

        @Override
        public int compare(final Item A, final Item B) {
            if (tag==null) {
                if (!forwards) return(compare(B.toSort(),A.toSort()));
                return(compare(A.toSort(),B.toSort()));
            } else {
                if (!forwards) return(compare(B.getExtended(tag),A.getExtended(tag)));
            }
            return(compare(A.getExtended(tag),B.getExtended(tag)));
        }

        public boolean equals() {
            return (false);
        }

    }

    class CompChoice implements Comparator<Item> {

        private String tag;
        private boolean forwards;
        private ArrayList<String> fields;

        public CompChoice(final String t,boolean f,ArrayList<String> fl) {
            tag=t;forwards=f;fields=fl;
        }

        @Override
        public int compare(final Item A, final Item B) {
            if (!forwards) return(fields.indexOf(A.getExtended(tag))-fields.indexOf(B.getExtended(tag)));
            return (fields.indexOf(B.getExtended(tag))-fields.indexOf(A.getExtended(tag)));
        }

        public boolean equals() {
            return (false);
        }

    }

}
