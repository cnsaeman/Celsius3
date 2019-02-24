//
// Celsius Library System
// (w) by C. Saemann
//
// MProperties.java
//
// This class contains the thread for searching...
//
// typesafe
//
// checked: 11/2009
//


package celsius;

import celsius.tools.Parser;
import celsius.tools.toolbox;
import java.util.HashMap;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cnsaeman
 */
public class MProperties extends HashMap<String,String> {
    
    public boolean changed;

    public MProperties() {
        super();
        changed=false;
    }

    public MProperties(BibTeXRecord btr) {
        super();
        changed=false;
        for (String k : btr.keySet()) {
            put(k,btr.get(k));
        }
    }

    @Override
    public String put(String key,String value) {
        if (value==null) return(null);
        if ((super.get(key)==null) || ((super.get(key)!=null) && (!value.equals(super.get(key))))) {
            if (!key.startsWith("##") && !key.startsWith("$$")) changed=true;
            return(super.put(key, value));
        }
        return(value);
    }

    public void putNT(String key,String value) {
        if (value.length()!=0) super.put(key,value);
    }

    public void reset() {
        changed=false;
    }

    public boolean isNotSet(String s) {
        return(!this.containsKey(s));
    }

    public boolean isEmpty(String s) {
        if (!this.containsKey(s)) return(true);
        return(getS(s).equals(""));
    }

    public String getS(String k) {
        String ret=get(k);
        if (ret==null) return("");
        return(ret);
    }

    public String fillOut(String s) {
        for (String k : this.keySet()) {
            s=Parser.Substitute(s, "#"+k+"#", getS(k));
            int i=s.indexOf("#"+k+"&");
            if (i>-1) {
                s=Parser.Substitute(s, "#"+k+"&1#", toolbox.shortenNames(get(k)));
                s=Parser.Substitute(s, "#"+k+"&2#", toolbox.ToBibTeXAuthors(get(k)));
                s=Parser.Substitute(s, "#"+k+"&3#", toolbox.Authors3FromCelAuthors(get(k)));
                s=Parser.Substitute(s, "#"+k+"&4#", toolbox.Authors4FromCelAuthors(get(k)));
            }
        }
        String res=new String("");
        int i,j;
        i=0; j=0;
        while (s.indexOf('#',i)>-1) {
            i=s.indexOf('#',i);
            j=s.indexOf('#',i+1);
            if ((j>-1) && (j-i<20)) {
                res=new String("");
                if (i>0) res=s.substring(0,i-1);
                if (j+1<s.length()) res+=s.substring(j+1);
                s=res;
            } else i++;
        }
        return(s);
    }
           
}
