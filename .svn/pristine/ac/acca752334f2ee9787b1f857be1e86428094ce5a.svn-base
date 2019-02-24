//
// Celsius Library System v2
// (w) by C. Saemann
//
// FFilter.java
//
// This class represents a simple file filter
//
// typesafe
// 
// checked 16.09.2007
//

package celsius.tools;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * FileFilter implementation.
 * _DIR : only directories
 * _ALL : all files
 * otherwise extension 
 * @author cnsaeman
 *
 */
public class FFilter extends FileFilter {
	
	public String extension;
	public String description;
	
	public FFilter(String ext, String desc)	{
		extension=ext;
		description=desc;
	}

	public boolean accept(File arg0) {
		if (arg0.isDirectory()) return(true);
		if (extension.equals("_DIR")) return(false);
		if (extension.equals("_ALL")) return(true);
		try
		{
		return((arg0.getCanonicalPath()).endsWith(extension));
		} catch(Exception e) { }
		return(false);
	}
	
	public String getDescription() {
		return(description);
	}

}
