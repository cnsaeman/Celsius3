/*
 * Class acting as an icon wallet.
 */

package celsius;

import java.util.ArrayList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class KeyValueTableModel implements TableModel {

    private final ArrayList col1;
    private final ArrayList col2;
    private final ArrayList<TableModelListener> Listeners;
    private String ColName1, ColName2;

    public KeyValueTableModel(String n1, String n2) {
        col1=new ArrayList();
        col2=new ArrayList();
        Listeners=new ArrayList<TableModelListener>();
        ColName1=n1; ColName2=n2;
    }

    public void clear() {
        col1.clear();
        col2.clear();
    }

    public int getRowCount() {
        return(col1.size());
    }

    public int getColumnCount() {
        return(2);
    }

    public String getColumnName(int columnIndex) {
        if (columnIndex==0) return(ColName1);
        return(ColName2);
    }

    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex==1) return(true);
        return(false);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex==0) return(col1.get(rowIndex));
        return(col2.get(rowIndex));
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (col1.size()<=rowIndex) {
            for (int i=col1.size();i<=rowIndex;i++) {
                col1.add(null);
                col2.add(null);
            }
        }
        if (columnIndex==0) col1.set(rowIndex, aValue);
        if (columnIndex==1) col2.set(rowIndex, aValue);
        TableModelEvent e = new TableModelEvent( this, rowIndex, rowIndex,
                TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE );
        for(TableModelListener TML : Listeners)
            TML.tableChanged(e);

    }

    public void addTableModelListener(TableModelListener l) {
        Listeners.add(l);
    }

    public void removeTableModelListener(TableModelListener l) {
        Listeners.remove(l);
    }

    public void removeRow(int i) {
        col1.remove(i);
        col2.remove(i);
        TableModelEvent e = new TableModelEvent( this, i, i,
                TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
        for(TableModelListener TML : Listeners)
            TML.tableChanged(e);
    }

    public void addRow(String key, String value) {
        col1.add(key);
        col2.add(value);
        TableModelEvent e = new TableModelEvent( this, col1.size()-1, col1.size()-1,
                TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
        for(TableModelListener TML : Listeners)
            TML.tableChanged(e);
    }

}