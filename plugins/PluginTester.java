import java.util.ArrayList;
import java.util.HashMap;

public class PluginTester extends Thread {
 
    public static final HashMap<String,String> metaData=new HashMap<String,String>() {
        {
            put("title"             ,"Test-Plugin");
            put("author"            ,"Christian Saemann");
            put("version"           ,"1.0");
            put("help"              ,"This plugin is for testing purposes only and printlns the records' tags with its values.");
            put("needsFirstPage"    ,"no");
            put("longRunTime"       ,"no");
            put("requiredFields"    ,"fullpath");
            put("type"              ,"auto|manual");
            put("defaultParameters" ,"");
            put("parameter-help"    ,"none.");
        }
    };
    
    public celsius.MProperties Information;
    public ArrayList<String> Msgs;

    public void Initialize(celsius.MProperties i, ArrayList<String> m){
        Information=i; Msgs=m;
    }
  
    public void run() {
	for (String key : Information.keySet()) {
		System.out.println(key+" :: "+Information.get(key));
	}
    }
  
                      
}
