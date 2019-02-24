//
// Celsius Library System v2
// (w) by C. Saemann
//
// Plugin.java
//
// This class contains the interface for plugins
//
// not typesafe
// 
// checked 16.09.2007
//

package celsius;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author cnsaeman
 */
public class Plugin {
    
    public final String className;
    private final Class c;
    
    public final HashMap<String,String> metaData;
    public boolean needsFirstPage;
    
    private final Method Initialize;
    
    /** Creates a new instance of Plugin */
    public Plugin(String f) throws Exception {
        className=f;
        URLClassLoader ucl = new URLClassLoader(new URL[] { (new File("plugins")).toURI().toURL() });
        c = Class.forName(className,true,ucl);

        metaData=(HashMap<String,String>)c.getDeclaredField("metaData").get(c);
        needsFirstPage=metaData.get("needsFirstPage").equals("yes");
        
        Initialize=(Method)c.getDeclaredMethod("Initialize",MProperties.class,ArrayList.class);
    }
    
    /**
     * initialize: Information i, Messages m
     */
    public Thread Initialize(MProperties i, ArrayList<String> m) throws Exception {
        Thread thread=(Thread)c.newInstance();
        Object[] args=new Object[] { i, m };
        Initialize.invoke(thread,args);
        return(thread);
    }
    
}
