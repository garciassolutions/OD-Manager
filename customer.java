import java.util.Comparator;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.*;
import java.lang.System;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.*;
import java.util.Formatter;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.JTable;
import java.awt.Color;

import java.awt.font.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.text.*;

public class customer{
  public static final username = "ocean";
  public static final password = "digital";

  static TableRowSorter<MyTableModel> sorter;
  static JPanel pan[] = new JPanel[2];
  static JPanel listing = new JPanel();
  static TextField data[] = new TextField[18];
  static PrintWriter out = null;
  static BufferedReader in = null;
  static JRadioButton laptop = new JRadioButton("Laptop");
  static JRadioButton desktop = new JRadioButton("Desktop");
  static Socket server;
  static LinkedList info = new LinkedList();
  static class MyTableModel extends AbstractTableModel{
    String columnNames[] = {
      "ID", "Name", "Date", "Address", "City", "State", "Zip", "Phone", "Cell", "Email",
      "Type", "Brand", "Serial", "Service", "Laptop Adapter", "Condition", "Software",
      "OS", "Desctiption", "Comments"
    };
    public int getColumnCount(){
      return columnNames.length;
    }
    public int getRowCount(){
      return info.size();
    }
    public String getColumnName(int col){
      return columnNames[col];
    }
    public Object getValueAt(int row, int col){
      Object tmp[] = (Object[]) info.get(row);
      return tmp[col];
    }
    public Class getColumnClass(int c){
      return getValueAt(0, c).getClass();
    }
    public boolean isCellEditable(int row, int col){
      return false;
    }
    public void setValueAt(Object value, int row, int col){
      Object tmp[] = (Object[]) info.get(row);
      tmp[col] = value;
      info.set(row, tmp);
    }
    public void addRow(Object data[]){
      info.add(data);
    }
    public void delRow(int row){
		info.remove(row);
	}
  };

  static class MyTableCellRenderer extends JLabel implements TableCellRenderer {
	  // This method is called each time a cell in a column using this renderer needs to be rendered.
	  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex){
		  setOpaque(true);
		  if(value.toString().matches("poor")){
			  setBackground(Color.red);
		  }
		  else if(value.toString().matches("okay")){
			  setBackground(Color.yellow);
		  }
		  else if(value.toString().matches("good")){
			  setBackground(Color.green);
		  }
		  else{
			  setBackground(Color.black);
		  }
		  // Since the renderer is a component, return itself
		  return this;
	  }
	  public void validate(){}
	  public void revalidate(){}
	  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue){}
	  public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue){}
  }

  static JFrame win = new JFrame("Ocean Digital - Service Request");
  static JTable table;
  static JTextField filter_text = new JTextField("", 15);
  static final MyTableModel model = new MyTableModel();

  public static void main(String args[]) throws IOException{
    JTabbedPane pane = new JTabbedPane();
    Toolkit Tk = Toolkit.getDefaultToolkit();
    Dimension screen = Tk.getScreenSize(); // Get the screen size.
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenu infoMenu = new JMenu("Server");
    JMenuItem aboutAction = new JMenuItem("* About");
    JMenuItem serverAction = new JMenuItem("* Login");
    JMenuItem quitAction = new JMenuItem(" * Quit");
    JButton submit = new JButton(" Submit ");
    JButton clear = new JButton(" Clear ");
    JButton r_btn = new JButton(" Report ");
    JButton d_btn = new JButton(" Delete ");
    JButton print = new JButton(" Print ");

    table = new JTable(model){
      public String getToolTipText(MouseEvent e){ // Set the hover tool tip
        String tip = null;
        java.awt.Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);
        tip = "" + getValueAt(rowIndex, colIndex);
        return tip;
      }
    };

    table.addMouseListener(new MouseAdapter(){ // Set up double clicking listener.
      public void mouseClicked(MouseEvent e){
        if(e.getClickCount() == 2){
          java.awt.Point p = e.getPoint();
          int colIndex = table.columnAtPoint(p);
          if(colIndex != 0){ // User cannot change the ID.
	          int rowIndex = table.rowAtPoint(p);
	          String tmp = (String)model.getValueAt(table.convertRowIndexToModel(rowIndex), table.convertColumnIndexToModel(colIndex));
	          String tmp2 = JOptionPane.showInputDialog("Enter a new value: ", tmp);
	          if(tmp != tmp2 && tmp2 != null){ // The user entered a change and didnt hit cancle.
	            out.println("!edit " + table.getValueAt(rowIndex, 0) + " " + colIndex + " " + tmp2);
	            // Also send the edit to the server.
	            // Check that the edit was successful? Return the ID?
				table.setValueAt(tmp2, table.convertRowIndexToModel(rowIndex), colIndex);
	            win.repaint();
	          }
	      }
		}
      };
    });
    sorter = new TableRowSorter<MyTableModel>(model);
    JScrollPane scrollPane = new JScrollPane(table);
    table.setPreferredScrollableViewportSize(new Dimension(700, 500));
    table.setFillsViewportHeight(true);
    table.setDragEnabled(false);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setRowSorter(sorter); // Do not allow sorting for getValueAt()???
    table.getTableHeader().setReorderingAllowed(false);

    TableColumn col = table.getColumnModel().getColumn(15);
    col.setCellRenderer(new MyTableCellRenderer());

    try{
      server = new Socket("localhost", 1337);
      out = new PrintWriter(server.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(server.getInputStream()));
    }
    catch(UnknownHostException e){
      System.err.println("[!] We don't know about localhost.");
      System.exit(1);
    }
    catch(IOException e){
      System.err.println("Couldn't get I/O for localhost.");
      e.printStackTrace();
      System.exit(1);
    }
    
    // Send the server its username and password.
    out.println(username);
    out.println(password);

    filter_text.getDocument().addDocumentListener(
      new DocumentListener(){
        public void changedUpdate(DocumentEvent e) {
          newFilter();
        }
        public void insertUpdate(DocumentEvent e) {
          newFilter();
        }
        public void removeUpdate(DocumentEvent e) {
          newFilter();
        }
      }
    );

    JComboBox state_list = new JComboBox();
    state_list.addItem("Alabama	AL");
	state_list.addItem("Alaska	AK");
	state_list.addItem("Arizona	AZ");
	state_list.addItem("Arkansas	AR");
	state_list.addItem("California	CA");
	state_list.addItem("Colorado	CO");
	state_list.addItem("Connecticut	CT");
	state_list.addItem("Delaware	DE");
	state_list.addItem("Florida	FL");
	state_list.addItem("Georgia	GA");
	state_list.addItem("Hawaii	HI");
	state_list.addItem("Idaho	ID");
	state_list.addItem("Illinois	IL");
	state_list.addItem("Indiana	IN");
	state_list.addItem("Iowa	IA");
	state_list.addItem("Kansas	KS");
	state_list.addItem("Kentucky	KY");
	state_list.addItem("Louisiana	LA");
	state_list.addItem("Maine	ME");
	state_list.addItem("Maryland	MD");
	state_list.addItem("Massachusetts MA");
	state_list.addItem("Michigan	MI");
	state_list.addItem("Minnesota	MN");
	state_list.addItem("Mississippi	MS");
	state_list.addItem("Missouri	MO");
	state_list.addItem("Montana	MT");
	state_list.addItem("Nebraska	NE");
	state_list.addItem("Nevada	NV");
	state_list.addItem("New Hampshire	NH");
	state_list.addItem("New Jersey	NJ");
	state_list.addItem("New Mexico	NM");
	state_list.addItem("New York	NY");
	state_list.addItem("North Carolina	NC");
	state_list.addItem("North Dakota	ND");
	state_list.addItem("Ohio	OH");
	state_list.addItem("Oklahoma	OK");
	state_list.addItem("Oregon	OR");
	state_list.addItem("Pennsylvania	PA");
	state_list.addItem("Rhode Island	RI");
	state_list.addItem("South Carolina	SC");
	state_list.addItem("South Dakota	SD");
	state_list.addItem("Tennessee	TN");
	state_list.addItem("Texas	TX");
	state_list.addItem("Utah	UT");
	state_list.addItem("Vermont	VT");
	state_list.addItem("Virginia	VA");
	state_list.addItem("Washington	WA");
	state_list.addItem("West Virginia	WV");
	state_list.addItem("Wisconsin	WI");
	state_list.addItem("Wyoming	WY");

	TableColumn state_column = table.getColumnModel().getColumn(5);
	state_column.setCellEditor(new DefaultCellEditor(state_list));

    data[0] = new TextField(50);
    data[1] = new TextField(50);
    data[2] = new TextField(25);
    data[3] = new TextField(15);
    data[4] = new TextField(5);
    data[5] = new TextField(14);
    data[6] = new TextField(14);
    data[7] = new TextField(25);
    data[8] = new TextField(50);
    data[9] = new TextField(50);
    data[10] = new TextField(50);
    data[11] = new TextField(50);
    data[12] = new TextField(50);
    data[13] = new TextField(50);
    data[14] = new TextField(25);
    data[15] = new TextField(50);
    data[16] = new TextField(50);
    data[17] = new TextField(50);

    pan[0] = new JPanel(new GridBagLayout());
    pan[1] = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;
    c.gridx=0;
    c.gridy=0;
    pan[0].add(new JLabel("Name: "), c);
    c.gridx=1;
    pan[0].add(data[0], c);
    c.gridx=0;
    c.gridy=1;
    pan[0].add(new JLabel("Address: "), c);
    c.gridx=1;
    pan[0].add(data[1], c);
    c.gridx=0;
    c.gridy=2;
    pan[0].add(new JLabel("City: "), c);
    c.gridx=1;
    pan[0].add(data[2], c);

    c.gridx=0;
    c.gridy=3;
    pan[0].add(new JLabel("State:"), c);
    c.gridx=1;
    pan[0].add(data[3], c);
    c.gridx=2;
    pan[0].add(new JLabel("Zip:"), c);
    c.gridx=3;
    pan[0].add(data[4], c);

    c.gridx=0;
    c.gridy=4;
    pan[0].add(new JLabel("Phone No.: "), c);
    c.gridx=1;
    pan[0].add(data[5], c);
    c.gridx=0;
    c.gridy=5;
    pan[0].add(new JLabel("Cell Phone No.: "), c);
    c.gridx=1;
    pan[0].add(data[6], c);
    c.gridx=0;
    c.gridy=6;
    pan[0].add(new JLabel("Email: "), c);
    c.gridx=1;
    pan[0].add(data[7], c);

    c.gridx=0;
    c.gridy=7;
    pan[0].add(new JLabel("Computer Type: "), c);
    c.gridx=1;
    pan[0].add(desktop, c);
    c.gridx=2;
    pan[0].add(laptop, c);

    c.gridx=0;
    c.gridy=8;
    pan[0].add(new JLabel("Brand & Model: "), c);
    c.gridx=1;
    pan[0].add(data[8], c);
    c.gridx=0;
    c.gridy=9;
    pan[0].add(new JLabel("Serial No.: "), c);
    c.gridx=1;
    pan[0].add(data[9], c);
    c.gridx=0;
    c.gridy=10;
    pan[0].add(new JLabel("Service Tag number: "), c);
    c.gridx=1;
    pan[0].add(data[10], c);
    c.gridx=0;
    c.gridy=11;
    pan[0].add(new JLabel("Laptop AC Adapter: "), c);
    c.gridx=1;
    pan[0].add(data[11], c);
    c.gridx=0;
    c.gridy=12;
    pan[0].add(new JLabel("Condition of Laptop: "), c);
    c.gridx=1;
    pan[0].add(data[12], c);
    c.gridx=0;
    c.gridy=13;
    pan[0].add(new JLabel("Software Provided: "), c);
    c.gridx=1;
    pan[0].add(data[13], c);
    c.gridx=0;
    c.gridy=14;
    pan[0].add(new JLabel("Operating System: "), c);
    c.gridx=1;
    pan[0].add(data[14], c);
    c.gridx=0;
    c.gridy=15;
    pan[0].add(new JLabel("Description of problem: "), c);
    c.gridx=1;
    pan[0].add(data[15], c);
    c.gridx=0;
    c.gridy=16;
    pan[0].add(new JLabel("Comments: "), c);
    c.gridx=1;
    pan[0].add(data[16], c);

    c.gridx=0;
    c.gridy=17;
    pan[0].add(submit, c);
    c.gridx=2;
    pan[0].add(clear, c);

    listing.add(scrollPane); // Add the table to a panel.

    c.weightx = 0.2;
    c.weighty = 0.2;
    c.gridheight=2;
    c.gridwidth=3;
    c.gridx=0;
    c.gridy=0;
    c.anchor = GridBagConstraints.NORTHWEST;
    pan[1].add(listing, c);
    c.fill = GridBagConstraints.NONE;
    c.gridy=0;
    c.gridx=0;
    c.anchor = GridBagConstraints.SOUTHWEST;
    pan[1].add(new JLabel("Filter: "), c);
    c.gridx=1;
    c.anchor = GridBagConstraints.PAGE_END;
    pan[1].add(filter_text, c);
    c.anchor = GridBagConstraints.SOUTHEAST;
    c.gridx=2;
    pan[1].add(print, c);
    c.gridx=3;
    pan[1].add(d_btn, c);

    pane.addTab("New Entry", pan[0]);
    pane.addTab("Database", pan[1]);

    win.add(pane, BorderLayout.CENTER); // Add the tab pane to the window.

    infoMenu.add(aboutAction);

    fileMenu.add(serverAction);
    fileMenu.add(quitAction);

    menuBar.add(fileMenu);
    menuBar.add(infoMenu);

    desktop.setSelected(true);
    check_box("Desktop");

    win.setSize(850, 655); // Set the window size.

    win.setJMenuBar(menuBar);
    win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Also close sockets.
    //win.pack();
    win.setVisible(true);

    clear.addActionListener(
      new ActionListener(){
        public void actionPerformed(ActionEvent e){
          for(int i=0;i<17;i++) data[i].setText("");
          desktop.setSelected(true);
          check_box("Desktop");
        }
      }
    );
    r_btn.addActionListener( // Report button listener.
      new ActionListener(){
        public void actionPerformed(ActionEvent e){
          // Select whats selected in the table (if anything).
          // Display the selection in the first tab.
          // model.getValueAt(); // Set all the textfields in a loop.
          // Check for the checkbox.
        }
      }
    );
  	print.addActionListener(
		new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int rowIndex = table.getSelectedRow();
				for(int i=0;i<table.getColumnCount();i++) table.getValueAt(rowIndex, i); // Get and return the row for printing.
				PrinterJob printJob = PrinterJob.getPrinterJob();
				PageFormat format = new PageFormat();
				format = printJob.pageDialog(format);
				Book book = new Book();
				book.append(new PrintText(), format);
				printJob.setPageable(book);
				if(printJob.printDialog()){
					try{
						printJob.print();
					}
					catch(PrinterException exc){
						System.err.println("Printing error: " + exc);
					}
				}
			}
		}
	);
    d_btn.addActionListener(
		new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    int rowIndex = table.getSelectedRow();
			    //int colIndex = table.getSelectedColumn();
			    out.println("!del " + table.getValueAt(rowIndex, 0));
			    // Read in from the server to make sure it got deleted.
			    // Server should return deleted ID???
			    model.delRow(rowIndex);
			    win.repaint();
			}
		}
	);
    submit.addActionListener( // Check that fields are set? Name, number, problem desc, cond
      new ActionListener(){
        public void actionPerformed(ActionEvent e){
		  String x = "abc";
          String nop = ":::::";
          int type = desktop.isSelected()?0:1;
          out.println("!add " + data[0].getText() + nop + data[1].getText() + nop + data[2].getText() + nop + data[3].getText() + nop + data[4].getText() + nop + data[5].getText() + nop + data[6].getText() + nop + data[7].getText() + nop + type + nop + data[8].getText() + nop + data[9].getText() + nop + data[10].getText() + nop + data[11].getText() + nop + data[12].getText() + nop + data[13].getText() + nop + data[14].getText() + nop + data[15].getText() + nop + data[16].getText() + nop + data[17].getText());
          // Send the server our info.
          try{
	          if((x=in.readLine()) == null){ // The user ID gets returned.
	            JFrame win = new JFrame("Error");
	            JOptionPane.showMessageDialog(win, "[*] Server error. Could not update database.");
	          }
	          else{ // Set the real date?
	            model.addRow(new Object[]{x, data[0].getText(), data[1].getText(), "date", data[2].getText(), data[3].getText(), data[4].getText(), data[5].getText(), data[6].getText(), data[7].getText(), data[8].getText(), type, data[9].getText(), data[10].getText(), data[11].getText(), data[12].getText(), data[13].getText(), data[14].getText(), data[15].getText(), data[16].getText(), data[17].getText()});
	            for(int i=0;i<17;i++) data[i].setText("");
	            desktop.setSelected(true);
	            check_box("Desktop");
	          }
          }
          catch(IOException f){
            System.out.println("Error caught.");
          }
          win.repaint();
        }
      }
    );
    desktop.addActionListener(
      new ActionListener(){
        public void actionPerformed(ActionEvent e){
          check_box(e.getActionCommand());
        }
      }
    );
    laptop.addActionListener(
      new ActionListener(){
        public void actionPerformed(ActionEvent e){
          check_box(e.getActionCommand());
        }
      }
    );
    aboutAction.addActionListener(
      new ActionListener(){
        public void actionPerformed(ActionEvent e){
          menu_clicked(e.getActionCommand());
        }
      }
    );
    serverAction.addActionListener(
      new ActionListener(){
        public void actionPerformed(ActionEvent e){
          menu_clicked(e.getActionCommand());
        }
      }
    );
    quitAction.addActionListener(
      new ActionListener(){
        public void actionPerformed(ActionEvent e){
          JFrame frame = new JFrame("Quitting?");
          int n = JOptionPane.showConfirmDialog(frame, "Are you sure you want to quit?", "Quit", JOptionPane.YES_NO_OPTION);
          if(n==0) System.exit(0);
        }
      }
    );
    fill_table(); // Show the servers data in the table.
  }

  private static void newFilter() {
    RowFilter<MyTableModel, Object> rf = null;
    try {
      rf = RowFilter.regexFilter(filter_text.getText());
    }
    catch(java.util.regex.PatternSyntaxException e){
      return;
    }
    sorter.setRowFilter(rf);
  }
  public static void menu_clicked(String cl_ck){
    if(cl_ck.matches("\\* About")){
      JFrame about_win = new JFrame("About");
      about_win.setSize(200, 100);
      about_win.add(new JLabel("Ocean Digital - Service manager."), BorderLayout.NORTH);
      about_win.add(new JLabel("Written by Anthony Garcia."), BorderLayout.CENTER);
      about_win.setVisible(true);
    }
    else if(cl_ck.matches("\\* Logon")){
      JFrame about_win = new JFrame("Server info");
      // Add ip address and other information.
      // Entries in the database
      about_win.setSize(200, 150);
      about_win.setVisible(true);
    }
  }

  public static void check_box(String box){
    if(box.matches("Desktop")){
      data[11].setVisible(false);
      data[11].setText("N/A");
      data[12].setVisible(false);
      data[12].setText("N/A");
      laptop.setSelected(false);
    }
    else{
  	  data[11].setText("");
      data[11].setVisible(true);
      data[12].setText("");
      data[12].setVisible(true);
      desktop.setSelected(false);
      win.repaint();
    }
  }

  public static void quitter() throws IOException{
    JFrame frame = new JFrame("Quitting?");
    int n = JOptionPane.showConfirmDialog(frame, "Are you sure you want to quit?", "Quit", JOptionPane.YES_NO_OPTION);
    try{
      server.close();
    }
    catch(IOException i){}
    if(n==0) System.exit(0);
  }

  public static void fill_table(){
	 String line = "abc";
	 out.println("!get"); // Request the information from the server.
	 try{
		 while((line=in.readLine()) != null && (line.trim()).length() > 0){ // Read in from socket and add rows untill 0 is read in.
		 	Object stfu[] = (line.trim()).split(":::::");
			info.add(stfu);
		 }
	 }
	 catch(IOException e){
	   System.out.println("Caught error in fill_table()");
	 }
  }
  public static class PrintText implements Printable{
	private static AttributedString mStyledText;
	  // Add space for a header and footer?

		public int print(Graphics graphics, PageFormat pageFormat, int pageIndex){
			Graphics2D g2d = (Graphics2D) graphics;
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			g2d.setPaint(Color.black);

			Point2D.Float pen = new Point2D.Float();

			int selected = table.convertRowIndexToModel(table.getSelectedRow()); // Translate it from the view to our data.
			System.out.println("Row selected: " + selected);
			for(int i=0;i<table.getColumnCount();i++){
				mStyledText = new AttributedString(table.getColumnName(i) + ": " + table.getValueAt(selected, i).toString());
				AttributedCharacterIterator charIterator = mStyledText.getIterator();
				LineBreakMeasurer measurer = new LineBreakMeasurer(charIterator, g2d.getFontRenderContext());
				float wrappingWidth = (float) pageFormat.getImageableWidth();

				while (measurer.getPosition() < charIterator.getEndIndex()){
					TextLayout layout = measurer.nextLayout(wrappingWidth);
					pen.y += layout.getAscent();
					float dx = layout.isLeftToRight()? 0 : (wrappingWidth - layout.getAdvance());
					layout.draw(g2d, pen.x + dx, pen.y);
					pen.y += layout.getDescent() + layout.getLeading();
				}

			}
			return Printable.PAGE_EXISTS;
		}
  }
}

//try {
//	MessageFormat headerFormat = new MessageFormat("Ocean Digital - Computers Technology Consulting\n246 Waverly Ave. Patchogue New York 11772 Phone: 631-289-5390 Fax: 631-289-5391");
//    MessageFormat footerFormat = new MessageFormat("Ocean Digital will not back up any data unless requested to by the customer.");
//    table.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);
//} catch (PrinterException pe) {
//	System.err.println("Error printing: " + pe.getMessage());
//}

