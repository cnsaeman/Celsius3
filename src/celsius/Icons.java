/*
 * Class acting as an icon wallet.
 */

package celsius;

import celsius.tools.Parser;
import celsius.tools.toolbox;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;

/**
 *
 * @author cnsaeman
 */
public class Icons extends HashMap<String,ImageIcon> {

    public final ArrayList<String> Types;
    public String basefolder;

    private void readIn(String folder,String pre) {
        String[] flist = (new File(folder)).list();
        String n;
        for (int i = 0; (i < flist.length); i++) {
            if (flist[i].endsWith(".png")) {
                try {
                    ImageIcon I = new ImageIcon(Toolkit.getDefaultToolkit().getImage(folder+toolbox.filesep+flist[i]));
                    n=Parser.CutTillLast(flist[i], ".png");
                    I.setDescription(n);
                    put(pre+n, I);
                    Types.add(pre+n);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(flist[i]);
                }
            }
            if (new File(folder+toolbox.filesep+flist[i]).isDirectory()) {
                readIn(folder+toolbox.filesep+flist[i],flist[i]+toolbox.filesep);
            }
        }        
    }

    public Icons(String bf) {
        super();
        basefolder=bf;
        if (basefolder.endsWith(toolbox.filesep)) basefolder.substring(0,basefolder.length()-1);
        loadIcon("notavailable");
        loadIcon("magnifier");
        loadIcon("image_edit");
        loadIcon("folder");
        loadIcon("folder_table");
        loadIcon("folder_link");
        loadIcon("folder_explore");
        loadIcon("folder_star");
        loadIcon("arrow_right");
        loadIcon("bullet_sparkle");
        loadIcon("book_add");
        loadIcon("book_key");
        loadIcon("book_edit");
        loadIcon("bookmark_go");
        loadIcon("book_key");
        loadIcon("cross");
        loadIcon("closebtn");
        loadIcon("star");
        loadIcon("lightning_add");
        loadIcon("lightning_go");
        loadIcon("information");
        loadIcon("pencil");
        loadIcon("default");
        loadIcon("wrench_orange");
        loadIcon("arrow_out");
        loadIcon("user_b");
        loadIcon("plugin");
        loadIcon("arrow_switch_bluegreen");
        loadIcon("application_edit");
        loadIcon("application_view_tile");
        loadIcon("Add Icon");
        loadIcon("Add Icon.2x");
        loadIcon("search2");
        loadIcon("search2.2x");
        loadIcon("home");
        loadIcon("home.2x");
        loadIcon("config");
        loadIcon("config.2x");
        loadIcon("closebtn");
        loadIcon("closebtn.2x");
        loadIcon("notavailable.2x");
        loadIcon("magnifier.2x");
        loadIcon("image_edit.2x");
        loadIcon("folder.2x");
        loadIcon("folder_table.2x");
        loadIcon("folder_link.2x");
        loadIcon("folder_explore.2x");
        loadIcon("folder_star.2x");
        loadIcon("arrow_right.2x");
        loadIcon("bullet_sparkle.2x");
        loadIcon("book_add.2x");
        loadIcon("book_key.2x");
        loadIcon("book_edit.2x");
        loadIcon("bookmark_go.2x");
        loadIcon("book_key.2x");
        loadIcon("cross.2x");
        loadIcon("closebtn.2x");
        loadIcon("star.2x");
        loadIcon("lightning_add.2x");
        loadIcon("lightning_go.2x");
        loadIcon("information.2x");
        loadIcon("pencil.2x");
        loadIcon("default.2x");
        loadIcon("wrench_orange.2x");
        loadIcon("arrow_out.2x");
        loadIcon("user_b.2x");
        loadIcon("plugin.2x");
        loadIcon("arrow_switch_bluegreen.2x");
        loadIcon("application_edit.2x");
        loadIcon("application_view_tile.2x");
        Types=new ArrayList<String>();
        readIn(basefolder,"");
    }

    private void loadIcon(String s) {
       try {
        ImageIcon I=new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("images/"+s+".png")));
        I.setDescription(s);
        put(s,I);
       } catch(Exception e) {
           e.printStackTrace();
       }
    }

    public ImageIcon getIcon(String s) {
        //if (s==null) return(get("default"));
        if (s==null) return(null);
        if (s.equals("")) return(null);
        if (s.length()==0) return(get("default"));
        if (!containsKey(s)) return(get("notavailable"));
        return(get(s));
    }

    public DefaultComboBoxModel getDCBM() {
        DefaultComboBoxModel DCBM = new DefaultComboBoxModel();
        for (String ft : Types) {
            DCBM.addElement(ft);
        }
        return(DCBM);
    }
   
}
