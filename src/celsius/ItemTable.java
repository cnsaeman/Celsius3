
package celsius;

import celsius.Threads.ThreadRegistrar;
import celsius.tools.Parser;
import celsius.tools.toolbox;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * @author cnsaeman
 */
public final class ItemTable implements ListSelectionListener, MouseListener, TableColumnModelListener {

    private Resources RSC;

    public MProperties mproperties;

    public String title;

    public Library Lib;
    public final JTable jtable;
    public final ThumbNailView TNV;
    public MainFrame MF;
    public String lastHTMLview;
    public String creationHTMLview;
    public int creationType;
    public ItemTableModel DTM;
    public ThreadRegistrar registrar;
    private ArrayList<Integer> sizes;
    public int type; // 0: empty, 1: category, 2: author, 3: search
    // 4: citedin, 5: bibtexproblems, 6: search identifier

    public String header;

    private boolean resizable;

    public boolean tableview;

    public int selectedfirst;
    public int selectedlast;

    public int sorted;

    public final ThreadPoolExecutor TPE;
    public final LinkedBlockingQueue<Runnable> LBQ;

    public ItemTable(MainFrame mf,Library lib,String tit,int t) {
        super();
        sorted=-1;
        title=tit;
        mproperties=new MProperties();
        registrar=null;
        MF=mf;
        RSC=mf.RSC;
        type=t;
        LBQ=new LinkedBlockingQueue<Runnable>();
        TPE=new ThreadPoolExecutor(5, 5, 500L, TimeUnit.DAYS,LBQ);
        TNV=new ThumbNailView(this);
        resizable=false;
        lastHTMLview="";
        creationHTMLview="";
        creationType=t;
        tableview=true;
        jtable=new JTable();
        jtable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        jtable.setDragEnabled(true);
        jtable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jtable.getSelectionModel().addListSelectionListener(this);
        jtable.addMouseListener(this);
        jtable.getTableHeader().addMouseListener(this);
        jtable.getColumnModel().addColumnModelListener(this);
        jtable.setDefaultRenderer(Object.class, new CellRenderer());
        setLibrary(lib);
        resetTableProperties();
        setSizes(Lib.ColumnSizes);
        jtable.setVisible(true);
    }

    public ItemTable(ItemTable DT) {
        super();
        sorted=-1;
        title=DT.title;
        mproperties=new MProperties();
        registrar=null;
        resizable=false;
        type=DT.type;
        LBQ=new LinkedBlockingQueue<Runnable>();
        TPE=new ThreadPoolExecutor(5, 5, 500L, TimeUnit.DAYS,LBQ);
        TNV=new ThumbNailView(this);
        RSC=DT.RSC;
        MF=DT.MF;
        lastHTMLview=DT.lastHTMLview;
        creationHTMLview=DT.creationHTMLview;
        creationType=DT.creationType;
        jtable=new JTable();
        DTM.addTableModelListener(TNV);
        tableview=true;
        jtable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        jtable.setDragEnabled(true);
        jtable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jtable.getSelectionModel().addListSelectionListener(this);
        jtable.addMouseListener(this);
        jtable.getTableHeader().addMouseListener(this);
        jtable.getColumnModel().addColumnModelListener(this);
        setLibrary(DT.Lib);
        ItemTableModel tc=DT.getDTM();
        for (Item item : tc.Items)
            addItemFast(item);
        setType(DT.getType());
        jtable.setDefaultRenderer(Object.class, new CellRenderer());
        resetTableProperties();
        sizes=new ArrayList<Integer>();
        for (Integer i : DT.sizes)
            sizes.add(i);
        resizeTable(false);
        jtable.setVisible(true);
    }

    public void setHeader(String h) {
        header=h;
    }

    public ThreadRegistrar newRegistrar() {
        if (registrar!=null) {
            registrar.clear();
            registrar.interrupt();
        }
        registrar=new ThreadRegistrar(this);
        return(registrar);
    }

    private void resetTableProperties() {
        if (jtable.getColumnCount()<sizes.size()) return;
        jtable.setGridColor(Color.LIGHT_GRAY);
        if (!tableview) TNV.updateView();
    }

    public synchronized void close() {
        LBQ.clear();
        TPE.shutdownNow();
        int i=RSC.ItemTables.indexOf(this);
        RSC.ItemTables.remove(i);
        MF.jTPTabList.remove(i);
        if (MF.jTPTabList.getTabCount() == 0)
            MF.noTabAvailable();
    }

    public void switchView() {
        if (tableview) switchToThumbnails();
        else switchToTable();
    }

    public void switchToThumbnails() {
        if (!tableview) return;
        int i=MF.jTPTabList.indexOfComponent(jtable.getParent().getParent());
        final JScrollPane scrollpane = new JScrollPane(TNV);
        scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        MF.jTPTabList.setComponentAt(i,scrollpane);
        TNV.updateView();
        tableview=false;
    }

    public void switchToTable() {
        if (tableview) return;
        int i=MF.jTPTabList.indexOfComponent(TNV.getParent().getParent());
        final JScrollPane scrollpane = new JScrollPane(jtable);
        MF.jTPTabList.setComponentAt(i,scrollpane);
        tableview=true;
    }

    public void setLibrary(Library l) {
        clear();
        Lib=l;
        setSizes(Lib.ColumnSizes);
        DTM=new ItemTableModel(l);
        jtable.setModel(DTM);
        DTM.addTableModelListener(TNV);
    }

    public void setType(int t) {
        type=t;
    }

    public int getType() {
        return(type);
    }

    public ItemTableModel getDTM() {
        return(DTM);
    }

    public synchronized void removeItem(Item item) {
        removeID(item.id);
    }

    public synchronized void removeID(int id) {
        int i=DTM.IDs.indexOf(id);
        if (i>-1) {
            DTM.removeRow(i);

        }
    }

    public synchronized void clear() {
        TNV.removeAll();
        mproperties.clear();
        if (DTM!=null) {
            DTM.clear();
        }
    }

    public synchronized Item getItem(int row) {
        return(DTM.Items.get(row));
    }

    public int getSelectedRow() {
        return(jtable.getSelectedRow());
    }

    public ArrayList<Item> getSelectedItems() {
        ArrayList<Item> out=new ArrayList<Item>();
        int[] selRows=jtable.getSelectedRows();
        /*for (int i1 = selRows.length - 1; i1 > -1; i1--)
            out.add(DTM.Items.get(selRows[i1]));*/
         for (int i1 = 0; i1<selRows.length; i1++)
            out.add(DTM.Items.get(selRows[i1]));
        return(out);
    }

    public void adjustCurrentStats(ArrayList<Item> items) {
        DTM.CurrentPages=0;
        DTM.CurrentDuration=0;
        DTM.CurrentItems=0;
        for (Item item : items) {
            DTM.CurrentItems++;
            DTM.CurrentPages+=item.getPages();
            DTM.CurrentDuration+=item.getDuration();
        }
    }

    /**
     * Add a document to the table
     */
    public synchronized void addItemFast(Item item) {
        sorted=-1;
        //if (DTM.IDs.indexOf(item.get("id"))==-1) {
            DTM.addItem(item);
        //}
    }

    /**
     * Add a document to the table
     */
    public synchronized void addItem(Item item) {
        sorted=-1;
        if (DTM.IDs.indexOf(item.id)==-1) {
            DTM.addItem(item);
            resetTableProperties();
        }
    }

    public synchronized void replace(Item item) {
        DTM.update(item);
    }

    public synchronized void replace(int id) {
        int pos=DTM.IDs.indexOf(id);
        /*if (pos>-1)
            DTM.update(new Item(Lib,DTM.Items.get(pos).get("id")));*/
        if (pos>-1)
            DTM.update(new Item(Lib,String.valueOf(id)));
    }

    public synchronized void updateAll() {
        DTM.updateAll();
    }


    /**
     * Moves to Library element which is selected in table and returns true, if
     * there really is such an element.
     */
    public boolean moveToSelectedinTable() {
        final int selectedRow = jtable.getSelectedRow();
        if (selectedRow == -1)
            return (false);
        if (jtable.getValueAt(selectedRow, 1) == null)
            return (false);
        int pos = DTM.Items.get(selectedRow).pos;
        Lib.marker=DTM.Items.get(selectedRow);
        MF.jIP.switchModeTo0(this,DTM.Items.get(selectedRow));
        return(true);
    }

    public void sortItems(int col, boolean force) {
        if (force) sorted=col;
        DTM.sortItems(col,sorted);
        if (col==sorted) sorted=-1;
        else sorted=col;
        resetTableProperties();
    }

    /**
     * Autoformat Table
     */
    public void resizeTable(boolean updateView) {
        synchronized (jtable) {
            if (jtable.getColumnCount() < sizes.size()) {
                return;
            }
            resizable = false;
            int fixedwidth=0;
            int totalwidth=0;
            for (Integer w : sizes) {
                if (w<0) {
                    totalwidth-=w;
                    fixedwidth-=w;
                } else {
                    totalwidth+=w;
                }
            }
            TableColumn column;
            int width = jtable.getWidth();
            /*System.out.println("--------");

            System.out.print("width:");
            System.out.println(width);
            System.out.print("fixedwidth:");
            System.out.println(fixedwidth);
            System.out.print("totalwidth:");
            System.out.println(totalwidth);*/

            double ratio = ((double) width-fixedwidth) / ((double) totalwidth-fixedwidth);
            /*System.out.print("ratio:");
            System.out.println(ratio);*/
            if (ratio<0) ratio=-ratio;
            int i = 0;
            for (Integer size : sizes) {
                column = jtable.getColumnModel().getColumn(i);
                if (size<0) {
                    column.setPreferredWidth(-size);
                    column.setMaxWidth(-size);
                    column.setMinWidth(-size);
                    column.setResizable(false);
                } else {
                    column.setResizable(true);
                    column.setPreferredWidth((int) (size * ratio));
                }
                i++;
            }
            jtable.setRowHeight(RSC.guiScale(24));
            if (updateView) {
                //if (!tableview) TNV.updateView(); //#### test to exclude
                MF.jIP.setDocumentTable(this);
                MF.jIP.updateHTMLview();
            }
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        MF.switchToLibrary(Lib);
        final ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if (!lsm.isSelectionEmpty()) {
            String ft=getItem(getSelectedRow()).get("filetype");
            MF.jMActions.removeAll();
            MF.jMActions.setEnabled(false);
            if (ft!=null) {
                String secondary=RSC.Configuration.SecondaryViewers(ft);
                if (secondary!=null) {
                    final String location=getItem(getSelectedRow()).getCompleteDirS("location");
                    final String[] viewers=secondary.split("\\|");
                    for (int i=0; i<viewers.length;i++) {
                        MF.jMActions.setEnabled(true);
                        final String actionName=Parser.CutTill(viewers[i], ":");
                        final ItemTable IT=this;
                        JMenuItem jMI=new JMenuItem(actionName);
                        jMI.addActionListener(new java.awt.event.ActionListener() {
                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                MF.JM.performAction(actionName, IT);
                            }
                        });
                        MF.jMActions.add(jMI);
                    }
                }
            }
            jtable.setComponentPopupMenu(MF.jPMDocuments);

            if (lsm.getMinSelectionIndex()==lsm.getMaxSelectionIndex()) {
                if (!moveToSelectedinTable()) {
                    toolbox.Warning(MF,"Could not find selected document.", "Warning:");
                }
            } else {
                adjustCurrentStats(getSelectedItems());
                MF.jIP.switchModeTo3(this);
            }
        } else {
            jtable.setComponentPopupMenu(null);
        }
        if (MF.jIP.jTPDoc.getSelectedIndex() == 4) {
            MF.jIP.jEPInspect.setContentType("text/html");
            MF.jIP.jEPInspect.setText("");
            MF.jIP.jEPInspect.setCaretPosition(0);
        }
    }

    @Override
    public synchronized void mouseClicked(MouseEvent e) {
        if ((e.getSource().getClass() == JLabel.class) || (e.getSource().getClass() == JTextField.class)) {
            TNV.requestFocus();
            int selectedRow = DTM.IDs.indexOf(Integer.valueOf(((JComponent) e.getSource()).getName()));
            selectedfirst=selectedRow;
            selectedlast=selectedRow;
            TNV.adjustSelection();
            jtable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
            if (e.getClickCount() == 2) {
                if (moveToSelectedinTable()) {
                    MF.JM.ViewSelected(null);
                }
            }
            if (e.getClickCount() == 1) {
                moveToSelectedinTable();
            }
        }
        if ((e.getSource() == jtable) && (e.getClickCount() == 2)) {
            if (moveToSelectedinTable()) {
                MF.JM.ViewSelected(null);
            }
        }
        if ((e.getSource() == jtable.getTableHeader())) {
            if (e.getButton()==MouseEvent.BUTTON1) {
                sortItems(jtable.getTableHeader().columnAtPoint(e.getPoint()),false);
                resizeTable(false);
            } else {
                int c=this.jtable.getTableHeader().columnAtPoint(e.getPoint());
                TableColumn[] cols=new TableColumn[jtable.getColumnCount()];
                for (int i=0;i<cols.length;i++) {
                    cols[i]=jtable.getColumnModel().getColumn(i);
                }
                (new TableHeaderPopUp(MF,Lib,c,cols,sizes)).show(jtable.getTableHeader(), e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        resizable=true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        resizable=false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void columnAdded(TableColumnModelEvent e) {
    }

    @Override
    public void columnRemoved(TableColumnModelEvent e) {
    }

    @Override
    public void columnMoved(TableColumnModelEvent e) {
    }

    @Override
    public synchronized void columnMarginChanged(ChangeEvent e) {
        if (resizable) {
            sizes=new ArrayList<Integer>();
            for (int i=0;i<jtable.getColumnCount();i++) {
                int w=jtable.getColumnModel().getColumn(i).getWidth();
                if (Lib.ColumnSizes.get(i)<0) w=-w;
                sizes.add(Integer.valueOf(w));
            }
        }
    }

    @Override
    public void columnSelectionChanged(ListSelectionEvent e) {
    }

    public synchronized void setSizes(ArrayList<Integer> prefsizes) {
        sizes=new ArrayList<Integer>();
        for (Integer i : prefsizes) sizes.add(i);
    }

    /**
     * Reload the current document table
     */
    public synchronized void refresh() {
        for (Item item : DTM.Items) {
            if (item.loadedaddinfo) item.reloadAI();
        }
    }

    public MProperties getData() {
        mproperties.put("currentpages", String.valueOf(DTM.CurrentPages));
        mproperties.put("currentduration", toolbox.formatSeconds(DTM.CurrentDuration));
        mproperties.put("currentitems", String.valueOf(DTM.CurrentItems));
        return(mproperties);
    }

    public class CellRenderer extends DefaultTableCellRenderer {

        public CellRenderer() {
            super();
        }

        @Override
        public void setValue(Object value) {
            if (ImageIcon.class.isInstance(value)) {
                setIcon((Icon)((ImageIcon)value));
                setText("");
            } else {
                this.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                setIcon(null);
                setText((String)value);
            }
        }

    }

    public class ItemTableModel extends AbstractTableModel {

        public ArrayList<String> Columns;
        public ArrayList<String> Headers;
        public ArrayList<Integer> IDs;
        public ArrayList<Item> Items;
        public Library Library;
        public int CurrentPages;
        public int CurrentDuration;
        public int CurrentItems;

        /**
         * Constructor
         */
        public ItemTableModel(Library lib) {
            super();
            Library=lib;
            Columns = new ArrayList<String>();
            Headers = new ArrayList<String>();
            if (lib.TableHeaders.isEmpty()) {
                for (String th : lib.TableTags) {
                    Headers.add(Parser.LowerEndOfWords2(Parser.CutTill(th,"&")));
                    Columns.add(th);
                }
            } else {
                for (String th : lib.TableTags)
                    Columns.add(th);
                for (String th : lib.TableHeaders)
                    Headers.add(th);
            }
            IDs = new ArrayList<Integer>();
            Items = new ArrayList<Item>();
            CurrentPages=0;
            CurrentDuration=0;
            CurrentItems=0;
        }

        public String value(Item item, int column) {
            String tag = Library.TableTags.get(column);
            return(item.getExtended(tag));
        }

        @Override
        public synchronized Object getValueAt(int row, int column) {
            if ((row < getRowCount()) && (column < getColumnCount())) {
                Item item=Items.get(row);
                String tag=Library.TableTags.get(column);
                if (Library.IconFields.indexOf(tag)>-1) {
                    return (RSC.Icons.getIcon(item.getIconField(tag)));
                }
                return(value(item,column));
            } 
            return ("ERR");
        }

        public synchronized void removeRow(int i) {
            IDs.remove(i);
            Items.remove(i);
            fireTableRowsDeleted(i, i);
        }

        public synchronized void clear() {
            int l=Items.size();
            if (l==0) return;
            IDs.clear();
            Items.clear();
            fireTableRowsDeleted(0, l-1);
            CurrentItems=0;
            CurrentPages=0;
            CurrentDuration=0;
        }

        /**
         * render the whole table non-editable
         */
        @Override
        public boolean isCellEditable(int row, int col) {
            return (false);
        }

        public synchronized void sortItems(final int i, int sorted) {
            final boolean invertSort=(i==sorted);
            final ArrayList<Item> tmp=new ArrayList<Item>();
            for (Item item : Items)
                tmp.add(item);
            if (tmp.size()<1) return;
            if (tmp==null) return;
            int type=0;
            if (i == 1000) {
                Collections.sort(tmp, Library.getComparator(null, invertSort, type));
            } else {
                if (Lib.ColumnTypes.get(i).startsWith("unit")) {
                    type = 1;
                }
                Collections.sort(tmp, Library.getComparator(Columns.get(i), invertSort, type));
            }
            Items=tmp;
            IDs=new ArrayList<Integer>();
            for (Item item : Items)
                IDs.add(item.id);
            fireTableRowsUpdated(0, Items.size()-1);
        }

        @Override
        public String getColumnName(int i) {
            if (Headers.size()<=i) return("");
            return(Headers.get(i));
        }

        private void addItem(Item item) {
            Items.add(item);
            IDs.add(item.id);
            CurrentItems++;
            CurrentPages+=item.getPages();
            CurrentDuration+=item.getDuration();
            if (!tableview) TNV.addDoc(getRowCount()-1);
        }

        public int getRowCount() {
            return(Items.size());
        }

        public int getColumnCount() {
            return(Library.TableTags.size());
        }

        private void update(Item item) {
            int row=IDs.indexOf(item.id);
            Items.set(row, item);
            fireTableRowsUpdated(row, row);
        }

        private void updateAll() {
            for (int r=0;r<IDs.size();r++) {
                Items.set(r,new Item(Lib,String.valueOf((int)IDs.get(r))));
            }
            fireTableRowsUpdated(0, IDs.size()-1);
        }

    }
}
