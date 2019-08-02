//
// Celsius Library System
// (w) by C. Saemann
//
// BibTeXRecord.java
//
// This class reflects a bibtex record
//
// typesafe
//
// checked: 
// 11/2009
// testing a modification

package celsius;

import java.util.LinkedHashMap;
import celsius.tools.Parser;

/**
 *
 * @author cnsaeman
 */
public class BibTeXRecord extends LinkedHashMap<String,String> {

    // parse errors: 255: null string, 254: empty string
    
    // Status messages after adding a document
    public static final String[] status={
        "Everything OK",  // 0
        "Unknown parse Error", // 1
        "Empty tag or type", // 2
        "Bracketing mismatch", // 3
        "Empty key or value" // 4
    };

    private static final String spaces="                                                ";
    public String type;
    public String celtype;
    public String tag;
    
    public int parseError;

    /**
     * Returns an empty BibTeX record
     */
    public BibTeXRecord() {
        super();
        type = "Article";
        celtype= "Other";
        tag = "";
        put("author", "");
        put("title", "");
        put("journal", "");
        put("volume", "");
        put("year", "");
        put("pages", "");
        parseError=0;
    }
    
    public BibTeXRecord (String bibtex) {
        super();
        parseError = 0;
        if (bibtex==null) { parseError=255; return; }
        bibtex=bibtex.replaceAll("(?m)^%.+?$", "");
        if (bibtex.trim().equals("")) {
            parseError = 254;
            type = "empty";
            tag = "empty";
            return;
        }
        try {
            if (bibtex.trim().equals("")) {
                type = "empty";
                tag = "empty";
                return;
            }
            type = Parser.CutFrom(Parser.CutTill(bibtex, "{"), "@").trim();
            tag = Parser.CutFrom(Parser.CutTill(bibtex, ","), "{");
            if (type.equals("") || tag.equals("")) {
                type = "empty";
                tag = "empty";
                parseError=2;
                return;
            }
            if (Parser.HowOftenContains(bibtex,"{")!=Parser.HowOftenContains(bibtex,"}")) {
                type = "empty";
                tag = "empty";
                parseError=3;
                return;
            }
            String remainder = Parser.CutFrom(bibtex, ",");
            String key, value;
            int i,j,k,l,c,s,e;
            while (remainder.length()>1) {
                // cut key
                i=remainder.indexOf('=');
                key=remainder.substring(0, i).trim().toLowerCase();
                remainder=remainder.substring(i+1).trim();

                // cut value
                j=remainder.indexOf('{');
                k=remainder.indexOf('\"');
                l=remainder.indexOf(',');
                if (l==-1) l=remainder.length();
                if (j==-1) j=remainder.length();
                if (k==-1) k=remainder.length();
                if ((j<l) && (k>j)) {
                    // enclosed in { }
                    s=1;
                    c=1; i=j;
                    while (c!=0) {
                        i++;
                        if (remainder.charAt(i)=='{') c++;
                        if (remainder.charAt(i)=='}') c--;
                    }
                } else {
                    if (k<l) {
                        // enclosed in " "
                        s=1;
                        i=remainder.indexOf('\"',k+1);
                        if (i>0) {
                            while (remainder.charAt(i-1)=='\\') i=remainder.indexOf('\"',i+1);
                        } else i=1;
                    } else {
                        // not enclosed in delimeters
                        s=0;
                        i=l;
                    }
                }
                value=remainder.substring(s,i).trim();
                l=remainder.indexOf(',',i)+1;
                if (l==0) l=remainder.length();

                // adjust value
                value = value.replace('\n', ' ');
                while (value.indexOf("  ") > -1) {
                    value = value.replace("  ", " ");
                }
                if (key.equals("") || value.equals("")) {
                    //parseError=4;
                }
                put(key, value);
                remainder = remainder.substring(l);
            }
            adjustCelType();
        } catch (Exception e) {
            e.printStackTrace();
            parseError=1;
        }
    }

    private void adjustCelType() {
        String t=type.toLowerCase();
        if (t.equals("article") || t.equals("unpublished")) celtype="Preprint";
        if (keySet().contains("journal")) celtype="Paper";
        if (t.indexOf("thesis")>-1) celtype="Thesis";
        if (t.indexOf("book")>-1) celtype="Book";
    }
        
    private String filler(String key) {
        int maxKeyLength=0;
        for (String s : keySet()) {
            if (s.length()>maxKeyLength) maxKeyLength=s.length();
        }
        int l=maxKeyLength-key.length();
        if (l>spaces.length()) l=spaces.length();
        return(key+(spaces.substring(0,l)));
    }


    public boolean isNotSet(String s) {
        return(!this.containsKey(s));
    }

    public boolean isEmpty(String s) {
        if (!this.containsKey(s)) return(true);
        return(getS(s).equals(""));
    }

    public String getS(String s) {
        String tmp=get(s);
        if (tmp==null) tmp=new String("");
        return(tmp);
    }

    public String getIdentifier() {
        String identifier=new String("");
        if (get("journal")!=null) {
            identifier=get("journal");
            if (get("volume")!=null) identifier+=" "+get("volume");
            if (get("year")!=null) identifier+=" ("+get("year")+")";
            if (get("pages")!=null) identifier+=" "+get("pages");
        }
        identifier=identifier.trim();
        return(identifier);
    }
    
    @Override
    public String toString() {
        if (type.equals("empty")) return("");
        String tmp="@"+type+"{"+tag;
        for (String key : keySet()) {
            tmp+=",\n   "+filler(key)+" = \""+get(key)+"\"";
        }
        tmp+="\n}";
        return(tmp);
    }
    
    public boolean equals(BibTeXRecord btr) {
        if (!tag.equals(btr.tag)) return(false);
        if (!type.equals(btr.type)) return(false);
        if (keySet().size()!=btr.keySet().size()) return(false);
        for (String key : this.keySet()) {
            if ((btr.get(key)==null) || (!get(key).equals(btr.get(key)))) return(false);
        }
        return(true);
    }    

    /**
     * Checks whether a bibtex entry is formed consistently
     */
    public static boolean BibTeXconsistency(String bibtex) {
        boolean consistent=true;
        try {
            BibTeXRecord btr=new BibTeXRecord(bibtex);
            consistent=((btr.parseError>0) || (btr.parseError<250));
            if (btr.tag.indexOf(" ")>-1) consistent=false;
            // check matching brackets
            String lines[] = Parser.CutTillLast(Parser.CutFrom(bibtex,"{"),"}").split("\",");
            for(String l:lines) {
                if (Parser.HowOftenContains(l, "{")!=Parser.HowOftenContains(l, "}")) consistent=false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            consistent=false;
        }
        return(consistent);
    }

    /**
     * Creates a Celsius author string from a BibTeX one
     */
    public static String authorsBibTeX2Cel(String tmp) {
        String authors=new String("");
        if (tmp.indexOf(',')>-1) {
            authors=tmp.replace(" and ","|");
        } else {
            String tmp2;
            while (tmp.length()>0) {
                tmp2=Parser.CutTill(tmp," and ").trim();
                authors+="|"+Parser.CutFromLast(tmp2," ")+", "+Parser.CutTillLast(tmp2, " ");
                tmp=Parser.CutFrom(tmp," and ").trim();
            }
            authors=authors.substring(1);
        }
        return(authors);
    }

}
