import java.util.ArrayList;
import java.util.HashMap;

public class PluginEnumerate extends Thread {

    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Enumerate items");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin sets the number field of the selected items to consecutive numbers.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"");
            put("type"              ,"manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };

    public celsius.MProperties Information;
    public ArrayList<String> Msgs;
    String no;

    public void Initialize(celsius.MProperties i, ArrayList<String> m) {
        Information = i;
        Msgs = m;
        if (Information.containsKey("$$keep-no")) {
            no=String.valueOf((int)Integer.valueOf(Information.get("$$keep-no"))+1).trim();
            if (no.length()<2)no="0"+no;
        } else {
            no="01";
        }
    }

    public void run() {
        String output=new String("");
        Information.put("number",no);
        Information.put("$$keep-no",no);
    }

}