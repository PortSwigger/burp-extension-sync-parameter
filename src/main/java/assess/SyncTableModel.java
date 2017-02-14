package assess;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

public class SyncTableModel extends AbstractTableModel implements TableModelListener {

	/** Table Date */
	private Object[][] tableData;
	
	/** Table Column Name */
	private static final String[] COLUMN_NAMES = {"Enabled", "Host", "Name", "Name(Req)", "Value"};
	
	/** Table Row Count */
	public static final int TABLE_ROW_COUNT = 10;
	
	/** Table Column Count */
	public static final int TABLE_COLUMN_COUNT = 5;
	
	/** Table column index */
	public static final int ENABLED_COLUMN_INDEX = 0;
	public static final int HOST_COLUMN_INDEX = 1;
	public static final int NAME_COLUMN_INDEX = 2;
	public static final int NAME_REQ_COLUMN_INDEX = 3;
	public static final int VALUE_COLUMN_INDEX = 4;
	
	public SyncTableModel() {
		tableData = new Object[TABLE_ROW_COUNT][TABLE_COLUMN_COUNT];
		// Set initial value to tabledata.
		for(int i = 0; i < TABLE_ROW_COUNT; i++) {
			// first column is CheckBox
			tableData[i][ENABLED_COLUMN_INDEX] = new Boolean(false);

			for(int j = 1; j < TABLE_COLUMN_COUNT; j++)
				tableData[i][j] = new String("");
		}
		
		tableData[0][NAME_COLUMN_INDEX] = "javax.faces.ViewState";
		tableData[1][NAME_COLUMN_INDEX] = "controlParamKey";

		// add listener
		addTableModelListener(this);
	}
	
	public Object[][] getTabledata() {
		return this.tableData;
	}
 			
	@Override
	public int getColumnCount() {
		return TABLE_COLUMN_COUNT;
	}

	@Override
	public int getRowCount() {
		return TABLE_ROW_COUNT;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return tableData[rowIndex][columnIndex];
	}

	@Override
	public String getColumnName(int columnIndex) {
		return COLUMN_NAMES[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return getValueAt(0, columnIndex).getClass();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		tableData[rowIndex][columnIndex]=aValue;
		fireTableDataChanged();
	}

	//
	// implement TableModelListener
	//
	@Override
	public void tableChanged(TableModelEvent e) {
		String message = null;
		int row;
		
		for(row = 0; row < TABLE_ROW_COUNT; row++) {
			if((Boolean)tableData[row][ENABLED_COLUMN_INDEX]) {
				if(tableData[row][HOST_COLUMN_INDEX].equals("")) {
					message = COLUMN_NAMES[HOST_COLUMN_INDEX] + " is empty.";
					break;
				} else if(tableData[row][NAME_COLUMN_INDEX].equals("")) {
					message = COLUMN_NAMES[NAME_COLUMN_INDEX] + " is empty.";
					break;
				}
			}
		}
		
		if(message != null) {
			setValueAt(false, row, ENABLED_COLUMN_INDEX);
			URL iconUrl = 
					  getClass().getClassLoader().getResource("mocomaru.png");
			ImageIcon imageIcon = new ImageIcon(iconUrl);
			JLabel label = new JLabel(message);
			JOptionPane.showMessageDialog(null, label, "Message", JOptionPane.PLAIN_MESSAGE, imageIcon);
		}
	}

}