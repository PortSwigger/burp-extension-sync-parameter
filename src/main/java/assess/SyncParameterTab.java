package assess;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Label;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.UnsupportedEncodingException;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

public class SyncParameterTab extends JPanel implements MouseMotionListener, MouseListener{

	private static final long serialVersionUID = -3439093083112839349L;
	private static SyncParameterTab panel;
	
	
	// ************************
	// * Common
	// ************************
	private JLabel encodingLabel;
	private JTextField encodingTextField;
	
	// ************************
	// * Sync
	// ************************

	private Label syncTitleLabel;
	private JLabel syncNoteLabel;
	private JCheckBox syncCheckBox;
	
	/** ScrollPane for Sync */
	private JScrollPane syncScrollPane;

	/** Table for Sync */
	private JTable syncTable;

	/** Note for Name(Req) */
	private JLabel syncNameReqNoteLabel;
	
	/** Table Model */
	private SyncTableModel tableModel;
	
	/** Triangle Label for Sync resize */
	JLabel triangleLabel;

	/** Start X of ScrollPane for Sync */
	private static final int SYNC_PANE_X = 10;

	/** Start Y of ScrollPane for Sync */
	private static final int SYNC_PANE_Y = 130;

	/** Width of ScrollPane for Sync */
	private static final int SYNC_PANE_WIDTH = 800;

	/** Height of ScrollPane for Sync */
	private static final int SYNC_PANE_HEIGHT = 110;

	/** Minimum Space between right side of syncScrollpane and that of this pane. */
	private static final int MINIMUM_SPACE_OF_SYNCPANE_RIGHTSIDE = 100;
	
	/** resize flag. If true, syncScrollPane can be resized */
	boolean resizeFlg = false;

	private SyncParameterTab() {
		//render();
	}
	
	public static SyncParameterTab getInstance() {
		if(panel == null)
			panel = new SyncParameterTab();
		return panel;
	}
	
	public void render() {
		setLayout(null);
		// Rendering Common
		commonRender();
		// Rendering Sync
		syncRender();
	}
	
	private void syncRender() {
		syncTitleLabel = new Label("Sync Setting");
		syncTitleLabel.setForeground(new Color(229, 137, 0));
		syncTitleLabel.setFont(new Font("Dialog", Font.BOLD, 15));
		syncNoteLabel = new JLabel("These settings let you configure Sync function.");
		syncCheckBox = new JCheckBox("Sync requests based on the following rules:");
		tableModel = new SyncTableModel();
		syncTable = new JTable(tableModel);
		syncNameReqNoteLabel = new JLabel("Note: You DO NOT NEED to set Name(Req) when request parameter name you want to sync is the same as name attribute in response.");
		syncNameReqNoteLabel.setFont(new Font("MS UI Gothic", Font.ITALIC, 13));
		syncNameReqNoteLabel.setForeground(new Color(229, 137, 0));
		
		// first column size should be fixed.
		syncTable.getColumnModel().getColumn(SyncTableModel.ENABLED_COLUMN_INDEX).setMinWidth(75);
		syncTable.getColumnModel().getColumn(SyncTableModel.HOST_COLUMN_INDEX).setPreferredWidth(150);
		syncTable.getColumnModel().getColumn(SyncTableModel.NAME_COLUMN_INDEX).setPreferredWidth(150);
		syncTable.getColumnModel().getColumn(SyncTableModel.NAME_REQ_COLUMN_INDEX).setPreferredWidth(150);
		syncTable.getColumnModel().getColumn(SyncTableModel.VALUE_COLUMN_INDEX).setPreferredWidth(275);
		
		// Generating ScrollPane for Sync Table
		syncScrollPane = new JScrollPane(syncTable);
		syncScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		// Setup triangle label for Sync ScrollPane resize.
		triangleLabel = new JLabel("▶");
		triangleLabel.setForeground(new Color(229, 137, 0));
		triangleLabel.setFont(new Font("Dialog", Font.BOLD, 18));

		// set location and size
		syncTitleLabel.setBounds(SYNC_PANE_X, 10, 145, 23);
		syncNoteLabel.setBounds(14, 40, 500, 15);
		syncCheckBox.setBounds(14, 100, 500, 21);
		syncScrollPane.setLocation(SYNC_PANE_X, SYNC_PANE_Y);
		syncScrollPane.setSize(SYNC_PANE_WIDTH, SYNC_PANE_HEIGHT);
		triangleLabel.setBounds(SYNC_PANE_X + SYNC_PANE_WIDTH +5, SYNC_PANE_Y, 20, 110);
		syncNameReqNoteLabel.setBounds(14, SYNC_PANE_Y + SYNC_PANE_HEIGHT + 10, 800 , 15);
		
		// add to pane
		add(syncTitleLabel);
		add(syncNoteLabel);
		add(syncCheckBox);
		add(syncScrollPane);
		add(triangleLabel);
		add(syncNameReqNoteLabel);

		// add event listener
		addMouseMotionListener(this);
		addMouseListener(this);
	
	}

	private void commonRender() {
		encodingLabel = new JLabel("Encoding:");
		encodingTextField = new JTextField("UTF-8");

		// location and size set
		encodingLabel.setBounds(15, 65, 69, 23);
		encodingTextField.setBounds(80, 65, 104, 28);
		
		// add to Pane
		add(encodingLabel);
		add(encodingTextField);
	}

	public SyncTableModel getTableModel() {
		return this.tableModel;
	}
	
	public boolean isSyncOn() {
		return syncCheckBox.isSelected();
	}
	
	public String getEncoding() {
		try {
			"dummy".getBytes(encodingTextField.getText());
			return encodingTextField.getText();
		} catch(UnsupportedEncodingException e) {
			return System.getProperty("file.encoding");
		}
	}
	
	@Override
	public void mouseDragged(java.awt.event.MouseEvent e) {
		// if resizeFlg is true and X of mouse pointer gt 150 and some space is rest on right side, resize is permitted.
		if (resizeFlg && e.getX() > 150 
				&& e.getX() < this.getWidth() - MINIMUM_SPACE_OF_SYNCPANE_RIGHTSIDE) {
			
			// change mouse cursor
			e.getComponent().setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
			
			// syncScrollPane is resized.
			syncScrollPane.setSize(new Dimension(e.getX() - SYNC_PANE_X, SYNC_PANE_HEIGHT));
			syncScrollPane.repaint();
			syncScrollPane.revalidate();
			
			// triangleLabel is also resized.
			triangleLabel.setBounds(e.getX() + 5, SYNC_PANE_Y, 20, 110);
			triangleLabel.repaint();
			triangleLabel.revalidate();
			
			// Repaint
			this.repaint();
			this.revalidate();
		}
	}

	@Override
	public void mouseMoved(java.awt.event.MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int w = SYNC_PANE_X + syncScrollPane.getWidth();
		
		// change cursor of mouse pointer
		e.getComponent().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
		// if mouse pointer is on resize area right to syncPane, cursor is changed.
		if ((x > w && x < w + 10) && (y > SYNC_PANE_Y && y < SYNC_PANE_Y + SYNC_PANE_HEIGHT)) {
			e.getComponent().setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
			// if pointer is on resize area, this flag is set to true. This flag is referred from mouseDragged.
			resizeFlg = true;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// if mouse pointer exits from this pane, cursor is changed to default one.
		e.getComponent().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// if mouse is released, cursor is changed to defalut one and resize flag should be always set to false.
		e.getComponent().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		resizeFlg = false;
	}

}