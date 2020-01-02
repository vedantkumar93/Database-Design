package javaSwing;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class Book {

	private JFrame frame;
	private Connection databaseConnection = null;
	private JTable table;
	private JTextField isbnTextField;
	private JTextField titleTextField;
	private JTextField editionTextField;
	private JTextField copiesTextField;
	private JTextField authorTextField;
	private JTextField deweyNumberTextField;
	private JTextField ccnTextField;
	private JTextField genreTextField;
	private JTextField pagesTextField;
	private JTextField datePublishedTextField;
	private JTextField locationTextField;
	private JComboBox<String> publisherDropDown;
	private JComboBox<String> typeDropDown;
	private JLabel errorMessage;
	private JButton btnClear;
	private JButton btnSearch;
	int selectedIndex;
	private JButton btnUpdate;
	private JButton btnRemove;

	/**
	 * Launch the application.
	 */

	public static void main(String[] args) throws Exception {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Book book = new Book();
					book.frame.setTitle("Library Book Entry");
					book.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	public Book() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings("serial")
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent arg0) {
				loadSearchData();
			}
		});
		frame.setBounds(0, 0, 1366, 768); // component's top left will be at x,y; component size width, height
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // default action that will call system.exit
		frame.getContentPane().setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(16, 364, 1279, 260);
		frame.getContentPane().add(scrollPane);

		table = new JTable();

		// Table Header
		table.setModel(new DefaultTableModel(new Object[][] {},
				new String[] { "ISBN", "Title", "Resource Type", "Edition", "Author", "Dewey Decimal Number", "Genre",
						"Publication Date", "Pages", "Available Copy", "Congress Catalogue Number", "Location",
						"Date Added", "Publisher" }) {
			/**
			 * This method does not allow the cells of row to be editable
			 */
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});

		scrollPane.setViewportView(table);

		// ******** Application Title
		JLabel labelBookManagement = new JLabel("BOOK MANAGEMENT");
		labelBookManagement.setHorizontalAlignment(SwingConstants.CENTER);
		labelBookManagement.setForeground(Color.BLACK);
		labelBookManagement.setFont(new Font("Tahoma", Font.PLAIN, 22));
		labelBookManagement.setBounds(242, 26, 822, 46);
		frame.getContentPane().add(labelBookManagement);

		// ******** ISBN Field

		JLabel labelIsbn = new JLabel("ISBN");
		labelIsbn.setBounds(60, 90, 60, 16);
		frame.getContentPane().add(labelIsbn);

		isbnTextField = new JTextField();
		isbnTextField.setBounds(250, 90, 130, 19);
		frame.getContentPane().add(isbnTextField);
		isbnTextField.setColumns(10);
		isbnTextField.setDocument(new JTextFieldLimit(13));
		isbnTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent evt) {
				char c = evt.getKeyChar();
				if (!(Character.isLetterOrDigit(c) || c == KeyEvent.VK_MINUS)) {
					evt.consume();
				}
			}
		});

		// ******** Title Field
		JLabel labelTitle = new JLabel("Title");
		labelTitle.setBounds(520, 90, 60, 16);
		frame.getContentPane().add(labelTitle);

		titleTextField = new JTextField();
		titleTextField.setColumns(10);
		titleTextField.setBounds(665, 90, 260, 19);
		titleTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent evt) {
				char c = evt.getKeyChar();
				if (!(Character.isLetterOrDigit(c) || c == KeyEvent.VK_MINUS || c == KeyEvent.VK_SPACE)) {
					evt.consume();
				}
			}
		});
		frame.getContentPane().add(titleTextField);
		titleTextField.setDocument(new JTextFieldLimit(64));

		// ******** Congress Catalogue Number Field
		JLabel labelCCN = new JLabel("Cong CN");
		labelCCN.setBounds(950, 90, 60, 16);
		frame.getContentPane().add(labelCCN);

		ccnTextField = new JTextField();
		ccnTextField.setColumns(10);
		ccnTextField.setBounds(1040, 90, 130, 19);
		frame.getContentPane().add(ccnTextField);
		ccnTextField.setDocument(new JTextFieldLimit(16));

		// ******** Date Published Field
		JLabel labelDatePublished = new JLabel("Date Published\r\n(yyyy-mm-dd)");
		labelDatePublished.setBounds(60, 150, 200, 16);
		frame.getContentPane().add(labelDatePublished);

		datePublishedTextField = new JTextField();
		datePublishedTextField.setColumns(10);
		datePublishedTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent evt) {
				char c = evt.getKeyChar();
				if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE)
						|| (c == KeyEvent.VK_MINUS))) {
					evt.consume();
				}
			}
		});
		datePublishedTextField.setBounds(250, 151, 130, 19);
		datePublishedTextField.setDocument(new JTextFieldLimit(10));
		frame.getContentPane().add(datePublishedTextField);

		/**
		 * This method does not allow the user to type any characters other than numbers
		 * in the phone number field If the key is other than a digit or a backspace or
		 * a delete the event is consumed.
		 */
		KeyAdapter numberKeyAdapter = new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent evt) {
				char c = evt.getKeyChar();
				if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
					evt.consume();
				}
			}
		};

		// ******** Edition Field - Maximum allowed edition is 99.
		JLabel labelEdition = new JLabel("Edition (0-99)");
		labelEdition.setBounds(520, 154, 200, 16);
		frame.getContentPane().add(labelEdition);

		editionTextField = new JTextField();
		editionTextField.addKeyListener(numberKeyAdapter);
		editionTextField.setColumns(10);
		editionTextField.setBounds(665, 150, 130, 19);
		frame.getContentPane().add(editionTextField);
		editionTextField.setDocument(new JTextFieldLimit(2));

		// ******** Pages Field - Maximum allowed pages is 9999.
		JLabel labelPages = new JLabel("Pages (1-9999)");
		labelPages.setBounds(950, 150, 100, 16);
		frame.getContentPane().add(labelPages);

		pagesTextField = new JTextField();
		pagesTextField.addKeyListener(numberKeyAdapter);
		pagesTextField.setColumns(10);
		pagesTextField.setBounds(1040, 150, 130, 19);
		frame.getContentPane().add(pagesTextField);
		pagesTextField.setDocument(new JTextFieldLimit(4));

		// ******** Publisher Field
		JLabel labelPublisher = new JLabel("Publisher");
		labelPublisher.setBounds(60, 211, 96, 16);
		frame.getContentPane().add(labelPublisher);

		List<String> pubList = new ArrayList<String>();
		List<String> typeList = new ArrayList<String>();
		try {
			databaseConnection = getConnection();
			if (databaseConnection != null) {
				PreparedStatement stmt = databaseConnection.prepareStatement("select publishername from publisher");
				ResultSet rs = stmt.executeQuery();
				while (rs.next())
					pubList.add(rs.getString(1));
				stmt = databaseConnection.prepareStatement("select resourcetype from resources");
				rs = stmt.executeQuery();
				while (rs.next())
					typeList.add(rs.getString(1));

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error Trace in getConnection() : " + e.getMessage());

		}

		String pubArray[] = new String[pubList.size() + 1];
		String typeArray[] = new String[typeList.size() + 1];
		typeArray[0] = "";
		pubArray[0] = "";
		for (int i = 0; i < pubList.size(); i++) {
			pubArray[i + 1] = pubList.get(i);
		}
		for (int i = 0; i < typeList.size(); i++) {
			typeArray[i + 1] = typeList.get(i);
		}

		publisherDropDown = new JComboBox<String>(pubArray);
		publisherDropDown.setBounds(250, 210, 260, 19);
		frame.getContentPane().add(publisherDropDown);

		JTextField addPublisherField = new JTextField();
		addPublisherField.setColumns(10);
		addPublisherField.setBounds(520, 210, 280, 19);
		addPublisherField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent evt) {
				char c = evt.getKeyChar();
				if (!(Character.isLetter(c) || c == KeyEvent.VK_SPACE)) {
					evt.consume();
				}
			}
		});
		addPublisherField.setDocument(new JTextFieldLimit(64));
		frame.getContentPane().add(addPublisherField);

		JButton addPublisherButton = new JButton("ADD");
		addPublisherButton.setBounds(830, 210, 90, 19);
		addPublisherButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Add new publisher in the database
				publisherDropDown.addItem(addPublisherField.getText());
				addPublisherField.setText("");

			}
		});
		frame.getContentPane().add(addPublisherButton);

		// ******** Type Field
		JLabel labelType = new JLabel("Type");
		labelType.setBounds(950, 210, 60, 16);
		frame.getContentPane().add(labelType);

		typeDropDown = new JComboBox<String>(typeArray);
		typeDropDown.setBounds(1040, 210, 130, 19);
		frame.getContentPane().add(typeDropDown);

		// ******** Author Field - Maximum 4 CSV authors with average 64 characters =
		// 256 char length
		JLabel labelAuthor = new JLabel("Author (if many use CSV)");
		labelAuthor.setBounds(60, 270, 150, 16);
		frame.getContentPane().add(labelAuthor);

		authorTextField = new JTextField();
		authorTextField.setColumns(10);
		authorTextField.setBounds(250, 270, 260, 19);
		authorTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent evt) {
				char c = evt.getKeyChar();
				if (!(Character.isLetter(c) || c == KeyEvent.VK_SPACE || c == KeyEvent.VK_COMMA)) {
					evt.consume();
				}
			}
		});
		authorTextField.setDocument(new JTextFieldLimit(256));
		frame.getContentPane().add(authorTextField);

		// ******** Dewey Decimal Number
		JLabel labelDeweyNumber = new JLabel("Dewey Decimal Number");
		labelDeweyNumber.setBounds(520, 270, 200, 16);
		frame.getContentPane().add(labelDeweyNumber);

		deweyNumberTextField = new JTextField();
		deweyNumberTextField.setColumns(10);
		deweyNumberTextField.setBounds(665, 270, 130, 19);
		deweyNumberTextField.setDocument(new JTextFieldLimit(16));
		frame.getContentPane().add(deweyNumberTextField);

		// ******** Genre Field
		JLabel labelGenre = new JLabel("Genre");
		labelGenre.setBounds(950, 270, 60, 16);
		frame.getContentPane().add(labelGenre);

		genreTextField = new JTextField();
		genreTextField.setColumns(10);
		genreTextField.setBounds(1040, 270, 130, 19);
		genreTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent evt) {
				char c = evt.getKeyChar();
				if (!Character.isLetter(c)) {
					evt.consume();
				}
			}
		});
		genreTextField.setDocument(new JTextFieldLimit(32));
		frame.getContentPane().add(genreTextField);

		// ******** Copies Field - Maximum allowed edition is 99.
		JLabel labelCopies = new JLabel("Copies (1-99)");
		labelCopies.setBounds(60, 330, 100, 16);
		frame.getContentPane().add(labelCopies);

		copiesTextField = new JTextField();
		copiesTextField.addKeyListener(numberKeyAdapter);
		copiesTextField.setColumns(10);
		copiesTextField.setBounds(250, 330, 130, 19);
		frame.getContentPane().add(copiesTextField);
		copiesTextField.setDocument(new JTextFieldLimit(2));

		// ******** Location Field
		JLabel labelLocation = new JLabel("Location");
		labelLocation.setBounds(520, 330, 100, 16);
		frame.getContentPane().add(labelLocation);

		locationTextField = new JTextField();
		locationTextField.setColumns(10);
		locationTextField.setBounds(665, 330, 130, 19);
		frame.getContentPane().add(locationTextField);
		locationTextField.setDocument(new JTextFieldLimit(16));

		errorMessage = new JLabel("");
		errorMessage.setHorizontalAlignment(SwingConstants.CENTER);
		errorMessage.setForeground(Color.RED);
		errorMessage.setFont(new Font("Tahoma", Font.PLAIN, 22));
		errorMessage.setBounds(16, 637, 1279, 46);
		frame.getContentPane().add(errorMessage);

		/**
		 * This method is called when the Insert button is clicked. It inserts all the
		 * data present in the text fields in the respective tables. It also inserts
		 * data into the relationship tables. Once the insertion is completed it reloads
		 * the data onto the table and clears all the fields. The data is added into
		 * four tables Publisher(if not present), Composer(if not present) and
		 * BookResourceDetails, and the tables connecting them ResourceComposer.
		 */
		JButton btnInsert = new JButton("INSERT");
		btnInsert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					// Check for errors in all the fields entered by the user.
					if (!errorCheck()) {
						errorMessage.setText("");
						try {

							databaseConnection = getConnection();
							if (databaseConnection != null) {
								PreparedStatement stmt = databaseConnection
										.prepareStatement("select isbn from bookresourcedetails where isbn = ?");
								stmt.setString(1, isbnTextField.getText());
								ResultSet rs = stmt.executeQuery();
								if (!rs.next()) {
									String statement = "CALL insert_book(?,?,?,?,?,?,?,?,?,?,?,?)";
									CallableStatement callablestmt = databaseConnection.prepareCall(statement);
									callablestmt.setString(1, isbnTextField.getText().trim());
									callablestmt.setString(2, titleTextField.getText().trim());
									callablestmt.setString(3, ccnTextField.getText().trim());
									callablestmt.setInt(4, Integer.parseInt(pagesTextField.getText()));
									if (!editionTextField.getText().isEmpty()) {
										callablestmt.setInt(5, Integer.parseInt(editionTextField.getText()));
									} else {
										callablestmt.setNull(5, 1);
									}
									callablestmt.setString(6, deweyNumberTextField.getText().trim());
									Date publicationDate = new SimpleDateFormat("yyyy-MM-dd")
											.parse(datePublishedTextField.getText());
									java.sql.Date pubDate = new java.sql.Date(publicationDate.getTime());
									callablestmt.setDate(7, pubDate);
									callablestmt.setString(8, genreTextField.getText().trim());
									callablestmt.setString(9,
											publisherDropDown.getItemAt(publisherDropDown.getSelectedIndex()));
									callablestmt.setString(10, typeDropDown.getItemAt(typeDropDown.getSelectedIndex()));
									callablestmt.setInt(11, Integer.parseInt(copiesTextField.getText()));
									callablestmt.setString(12, locationTextField.getText().trim());
									callablestmt.execute();

									String[] authors = authorTextField.getText().trim().split("\\,");
									for (String str : authors) {
										if (str.trim().length() > 0) {
											statement = "CALL insert_author(?,?)";
											callablestmt = databaseConnection.prepareCall(statement);
											callablestmt.setString(1, isbnTextField.getText().trim());
											callablestmt.setString(2, str.trim());
											callablestmt.execute();
										}
									}

									errorMessage.setText("Successfully inserted book in the system.");
									clearAllFields();
									callablestmt.close();
									loadSearchData();
									table.clearSelection();
								} else {
									errorMessage.setText("ISBN of the book already present in the system.");
								}
								stmt.close();
								databaseConnection.close();
							}

						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("Error Trace in getConnection() : " + e.getMessage());
						}

					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		btnInsert.setBounds(20, 650, 97, 25);
		frame.getContentPane().add(btnInsert);

		btnUpdate = new JButton("UPDATE");
		btnUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				try {
					// Check for errors in all the fields entered by the user.
					if (!errorCheck()) {
						errorMessage.setText("");
						try {
							databaseConnection = getConnection();
							if (databaseConnection != null) {
								PreparedStatement stmt = databaseConnection
										.prepareStatement("select isbn from bookresourcedetails where isbn = ?");
								stmt.setString(1, isbnTextField.getText());
								ResultSet rs = stmt.executeQuery();
								if (rs.next()) {
									String statement = "CALL update_book(?,?,?,?,?,?,?,?,?,?,?,?)";
									CallableStatement callablestmt = databaseConnection.prepareCall(statement);
									callablestmt.setString(1, isbnTextField.getText().trim());
									callablestmt.setString(2, titleTextField.getText().trim());
									callablestmt.setString(3, ccnTextField.getText().trim());
									callablestmt.setInt(4, Integer.parseInt(pagesTextField.getText()));
									if (!editionTextField.getText().isEmpty()) {
										callablestmt.setInt(5, Integer.parseInt(editionTextField.getText()));
									} else {
										callablestmt.setInt(5, 1);
									}
									callablestmt.setString(6, deweyNumberTextField.getText().trim());
									Date publicationDate = new SimpleDateFormat("yyyy-MM-dd")
											.parse(datePublishedTextField.getText());
									java.sql.Date pubDate = new java.sql.Date(publicationDate.getTime());
									callablestmt.setDate(7, pubDate);
									callablestmt.setString(8, genreTextField.getText().trim());
									callablestmt.setString(9,
											publisherDropDown.getItemAt(publisherDropDown.getSelectedIndex()));
									callablestmt.setString(10, typeDropDown.getItemAt(typeDropDown.getSelectedIndex()));
									callablestmt.setInt(11, Integer.parseInt(copiesTextField.getText()));
									callablestmt.setString(12, locationTextField.getText().trim());
									callablestmt.execute();

									stmt = databaseConnection
											.prepareStatement("delete from resourcecomposer where title = ?");
									stmt.setString(1, isbnTextField.getText());
									stmt.executeUpdate();

									String[] authors = authorTextField.getText().trim().split("\\,");
									for (String str : authors) {
										if (str.trim().length() > 0) {
											statement = "CALL insert_author(?,?)";
											callablestmt = databaseConnection.prepareCall(statement);
											callablestmt.setString(1, isbnTextField.getText().trim());
											callablestmt.setString(2, str.trim());
											callablestmt.execute();
										}
									}

									clearAllFields();
									errorMessage.setText("Successfully updated book in the system.");
									callablestmt.close();
									loadSearchData();
									table.clearSelection();
									isbnTextField.setEditable(true);
								} else {
									errorMessage.setText("Book doesn't exist in the system.");
								}
								stmt.close();
								databaseConnection.close();
							}

						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("Error Trace in getConnection() : " + e.getMessage());
						}

					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		btnUpdate.setBounds(20, 680, 97, 25);
		frame.getContentPane().add(btnUpdate);

		btnRemove = new JButton("REMOVE");
		btnRemove.setToolTipText("Takes only ISBN as input and delete the record");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				if (isbnTextField.getText().trim().isEmpty()) {
					errorMessage.setText("Please enter ISBN of the book.");
				} else {
					if (!(isbnTextField.getText().trim().length() == 10
							|| isbnTextField.getText().trim().length() == 13)) {
						errorMessage.setText("Please enter valid ISBN of the book (10 or 13 chars only).");
					} else {
						try {
							databaseConnection = getConnection();
							if (databaseConnection != null) {
								PreparedStatement stmt = databaseConnection
										.prepareStatement("delete from resourcecomposer where title = ?");
								stmt.setString(1, isbnTextField.getText().trim());
								int executeUpdate = stmt.executeUpdate();
								if (executeUpdate > 0) {
									stmt = databaseConnection
											.prepareStatement("delete from bookresourcedetails where title = ?");
									stmt.setString(1, isbnTextField.getText());
									executeUpdate = stmt.executeUpdate();
								}
								errorMessage.setText("Successfully deleted " + executeUpdate + " record");
								clearAllFields();
								stmt.close();
								loadSearchData();
								table.clearSelection();
								isbnTextField.setEditable(true);
							}
							databaseConnection.close();

						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("Error Trace in getConnection() : " + e.getMessage());
						}

					}
				}
			}
		});
		btnRemove.setBounds(140, 680, 97, 25);
		frame.getContentPane().add(btnRemove);

		btnClear = new JButton("CLEAR");
		btnClear.setToolTipText("Clear all fields");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				clearAllFields();
			}
		});
		btnClear.setBounds(140, 650, 97, 25);
		frame.getContentPane().add(btnClear);

		btnSearch = new JButton("Search");
		btnSearch.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				try {
					errorMessage.setText("");
					loadSearchData();
					table.clearSelection();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		btnSearch.setBounds(260, 650, 97, 25);
		frame.getContentPane().add(btnSearch);

		/**
		 * This function is executed when a row in the table is clicked. The values of
		 * the selected row are populated in the respective text fields. The Id of the
		 * Person selected is stored as a global integer.
		 */
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				clearAllFields();
				errorMessage.setText("");
				selectedIndex = table.getSelectedRow();

				TableModel model = table.getModel();
				isbnTextField.setText(model.getValueAt(selectedIndex, 0).toString());
				isbnTextField.setEditable(false);
				titleTextField.setText(model.getValueAt(selectedIndex, 1).toString());
				for (int i = 0; i < typeArray.length; i++) {
					if (typeArray[i].equals(model.getValueAt(selectedIndex, 2).toString())) {
						typeDropDown.setSelectedIndex(i);
						break;
					}
				}
				if (model.getValueAt(selectedIndex, 3) != null) {
					editionTextField.setText(model.getValueAt(selectedIndex, 3).toString());
				}
				authorTextField.setText(model.getValueAt(selectedIndex, 4).toString());
				if (model.getValueAt(selectedIndex, 5) != null) {
					deweyNumberTextField.setText(model.getValueAt(selectedIndex, 5).toString());
				}
				genreTextField.setText(model.getValueAt(selectedIndex, 6).toString());
				datePublishedTextField.setText(model.getValueAt(selectedIndex, 7).toString());
				pagesTextField.setText(model.getValueAt(selectedIndex, 8).toString());
				copiesTextField.setText(model.getValueAt(selectedIndex, 9).toString());
				ccnTextField.setText(model.getValueAt(selectedIndex, 10).toString());
				locationTextField.setText(model.getValueAt(selectedIndex, 11).toString());
				for (int i = 0; i < pubArray.length; i++) {
					if (pubArray[i].equals(model.getValueAt(selectedIndex, 13).toString())) {
						publisherDropDown.setSelectedIndex(i);
						break;
					}
				}
			}
		});

	}

	/**
	 * This function checks for errors in each of the text fields. It returns true
	 * if there are any errors and displays the relevant message in the error
	 * message label.
	 */
	public boolean errorCheck() throws Exception {

		// ******** ISBN cannot be Null and should be either 10 or 13 digits
		if (isbnTextField.getText().trim().isEmpty()) {
			errorMessage.setText("Please enter ISBN of the book.");
			return true;
		} else {
			if (!(isbnTextField.getText().trim().length() == 10 || isbnTextField.getText().trim().length() == 13)) {
				errorMessage.setText("Please enter valid ISBN of the book (10 or 13 chars only).");
				return true;
			}
		}

		// ******** Title cannot be Null
		if (titleTextField.getText().trim().isEmpty()) {
			errorMessage.setText("Please enter title of the book.");
			return true;
		}

		// ******** CCN cannot be Null
		if (ccnTextField.getText().trim().isEmpty()) {
			errorMessage.setText("Please enter Congree Catalogue Number of the book.");
			return true;
		}

		// ******** if the publication date cannot be null & should be before
		// currentDate
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateFormat.format(new Date()));
		Date datePublished = new Date();
		if (!datePublishedTextField.getText().trim().isEmpty()) {
			if (!isDateValid(datePublishedTextField.getText())) {
				errorMessage.setText("Please enter valid publication date in the mentioned format.");
				return true;
			}
			datePublished = new SimpleDateFormat("yyyy-MM-dd").parse(datePublishedTextField.getText());
			if (datePublished.after(currentDate)) {
				errorMessage.setText("Please enter publication date in the valid range(0001-01-01 to Current Date)");
				return true;
			}
		} else {
			errorMessage.setText("Please enter publication date of the book.");
			return true;
		}

		// ******** Pages cannot be null
		if (pagesTextField.getText().trim().isEmpty()) {
			errorMessage.setText("Please enter pages of the book.");
			return true;
		} else if (Integer.valueOf(pagesTextField.getText().trim()) == 0) {
			errorMessage.setText("Please enter positive number in pages.");
			return true;
		}

		// ******** Publisher cannot be null
		if (publisherDropDown.getItemAt(publisherDropDown.getSelectedIndex()).isEmpty()) {
			errorMessage.setText("Please select publisher of the book.");
			return true;
		}

		// ******** Type cannot be null
		if (typeDropDown.getItemAt(typeDropDown.getSelectedIndex()).isEmpty()) {
			errorMessage.setText("Please select resource type of the book.");
			return true;
		}

		// ******** Location cannot be null except e-books
		if (locationTextField.getText().toString().isEmpty()
				&& !typeDropDown.getItemAt(typeDropDown.getSelectedIndex()).equals("e-Books")) {
			errorMessage.setText("Please enter location of the book.");
			return true;
		}

		// ******** Genre cannot be Null
		if (genreTextField.getText().trim().isEmpty()) {
			errorMessage.setText("Please enter genre of the book.");
			return true;
		} else if (!deweyNumberTextField.getText().trim().isEmpty()
				&& genreTextField.getText().trim().equalsIgnoreCase("Fiction")) {
			errorMessage.setText("Fiction cannot have Dewey Decimal Number");
			return true;
		}

		// ******** DDN cannot be Null
		if (deweyNumberTextField.getText().trim().isEmpty()
				&& !genreTextField.getText().trim().equalsIgnoreCase("Fiction")) {
			errorMessage.setText("Please enter Dewey Decimal Number of the book.");
			return true;
		}

		// ******** Copies cannot be null
		if (copiesTextField.getText().trim().isEmpty()) {
			errorMessage.setText("Please enter available copies of the book.");
			return true;
		} else if (Integer.valueOf(copiesTextField.getText().trim()) == 0) {
			errorMessage.setText("Please enter positive number in copies.");
			return true;
		}

		// ******** Author cannot be null
		if (authorTextField.getText().trim().isEmpty()) {
			errorMessage.setText("Please enter author of the book. If multiple use CSV.");
			return true;
		} else {
			String[] authors = authorTextField.getText().trim().split("\\,");
			if (authors.length == 0) {
				errorMessage.setText("Please enter atleast one author of the book.");
				return true;
			} else {
				for (String s : authors) {
					if (s.trim().length() > 0)
						return false;
				}
				errorMessage.setText("Please enter atleast one author of the book.");
				return true;
			}
		}
	}

	/**
	 * This function checks if the date is in the valid format or not. It returns
	 * true if the date is valid and false otherwise.
	 */
	public boolean isDateValid(String date) throws Exception {
		String Date_Format = "yyyy-MM-dd";
		try {
			DateFormat df = new SimpleDateFormat(Date_Format);
			df.setLenient(false);
			df.parse(date);
			return true;
		} catch (ParseException e) {
			System.out.println("Exception " + e);
			return false;
		}
	}

	/**
	 * This function clears all the text fields in the UI. It sets the radio button
	 * group to default selection which is Male.
	 */
	public void clearAllFields() {
		isbnTextField.setEditable(true);
		errorMessage.setText("");
		isbnTextField.setText("");
		titleTextField.setText("");
		editionTextField.setText("");
		authorTextField.setText("");
		deweyNumberTextField.setText("");
		genreTextField.setText("");
		datePublishedTextField.setText("");
		pagesTextField.setText("");
		typeDropDown.setSelectedIndex(0);
		publisherDropDown.setSelectedIndex(0);
		ccnTextField.setText("");
		copiesTextField.setText("");
		locationTextField.setText("");
	}

	/**
	 * This method loads all the data from the database on to the table.
	 *
	 * public void loadData() { databaseConnection = getConnection(); try { String
	 * query = "select isbn, title, resourcetype, edition, DeweyDecimalSystemNumber,
	 * GenreName, \r\n" + "PublicationDate, Pages, AvailableCopy,
	 * CongressCatalogueNumber, Location, DateAdded, PublisherName from
	 * bookresourcedetails \r\n" + "natural join resources natural join genre
	 * natural join publisher"; String query1 = "select `name` from composer natural
	 * join resourcecomposer where title=?"; PreparedStatement pst =
	 * databaseConnection.prepareStatement(query); PreparedStatement pst1 =
	 * databaseConnection.prepareStatement(query1); ResultSet rs =
	 * pst.executeQuery(); ResultSet rs1; DefaultTableModel model =
	 * (DefaultTableModel) table.getModel(); model.setRowCount(0); String authors;
	 * while (rs.next()) { authors = ""; pst1.setString(1, rs.getString("isbn"));
	 * rs1 = pst1.executeQuery(); while (rs1.next()) { authors = authors +
	 * rs1.getString("name") + ","; } authors = authors.substring(0,
	 * authors.length() - 1); model.addRow(new Object[] { rs.getString("isbn"),
	 * rs.getString("title"), rs.getString("resourcetype"), rs.getString("edition"),
	 * authors, rs.getString("DeweyDecimalSystemNumber"), rs.getString("GenreName"),
	 * rs.getString("PublicationDate"), rs.getString("Pages"),
	 * rs.getString("AvailableCopy"), rs.getString("CongressCatalogueNumber"),
	 * rs.getString("Location"), rs.getString("DateAdded"),
	 * rs.getString("PublisherName") }); } rs.close(); pst.close(); pst1.close();
	 * databaseConnection.close(); table.setModel(model); } catch (Exception e) {
	 * 
	 * } }
	 */

	/**
	 * This method loads data from the database on to the table based on the
	 * searched fields.
	 */
	public void loadSearchData() {
		databaseConnection = getConnection();
		try {

			String query = "SELECT \r\n"
					+ "b.isbn, b.title, r.resourcetype, b.edition, c.`name` , b.DeweyDecimalSystemNumber, \r\n"
					+ "g.genrename, b.publicationdate, b.pages, b.availablecopy, b.CongressCatalogueNumber, \r\n"
					+ "b.location, b.dateadded, p.publishername    \r\n" + "FROM bookresourcedetails as b \r\n"
					+ "join resources r on b.resourceid =r.resourceid \r\n"
					+ "join resourcecomposer rc on b.isbn = rc.title \r\n"
					+ "join composer c on c.composerid = rc.composerid\r\n"
					+ "join Genre g on g.genreid = b.genreid\r\n"
					+ "join Publisher p on p.PublisherID = b.PublisherID \r\n"
					+ "where b.isbn like ? and b.title like ? and b.availablecopy >= ? \r\n"
					+ "	and b.DeweyDecimalSystemNumber like ? and b.CongressCatalogueNumber like ? \r\n"
					+ "	and b.pages>=? and r.ResourceType like ? and p.PublisherName like ? \r\n"
					+ "    and g.GenreName like ? and b.Location like ? and b.PublicationDate like ? " + "and (";

			boolean flag = false;
			String[] authors = authorTextField.getText().trim().split("\\,");
			for (int i = 0; i < authors.length; i++) {
				if (authors[i].trim().length() > 0) {
					flag = true;
					query = query.concat("c.`name` like ? or ");
				}
			}
			if (flag) {
				query = query.substring(0, query.length() - 4);
				query = query.concat(")");
			} else {
				query = query.concat("c.`name` like '%')");
			}

			if (!editionTextField.getText().toString().isEmpty())
				query = query.concat(" and b.Edition = ?");

			PreparedStatement pst = databaseConnection.prepareStatement(query);

			// ******** ISBN should be either 10 or 13 digits if present
			if (isbnTextField.getText().trim().isEmpty())
				pst.setString(1, "%");
			else if (!(isbnTextField.getText().trim().length() == 10
					|| isbnTextField.getText().trim().length() == 13)) {
				errorMessage.setText("ISBN should be valid (10 or 13 chars only) if present.");
				return;
			} else
				pst.setString(1, isbnTextField.getText().trim());

			// ******** Title - partial match
			if (titleTextField.getText().trim().isEmpty())
				pst.setString(2, "%");
			else
				pst.setString(2, "%".concat(titleTextField.getText().trim()).concat("%"));

			// ******** Copies should be valid if present
			if (copiesTextField.getText().trim().isEmpty())
				pst.setInt(3, 1);
			else if (Integer.valueOf(copiesTextField.getText().trim()) == 0) {
				errorMessage.setText("Please enter positive number in copies.");
				return;
			} else
				pst.setInt(3, Integer.valueOf(copiesTextField.getText().trim()));

			// ******** Dewey Decimal Number
			if (deweyNumberTextField.getText().trim().isEmpty())
				pst.setString(4, "%");
			else
				pst.setString(4, deweyNumberTextField.getText().trim());

			// ******** Congress Catalogue Number
			if (ccnTextField.getText().trim().isEmpty())
				pst.setString(5, "%");
			else
				pst.setString(5, ccnTextField.getText().trim());

			// ******** Pages should be valid if present
			if (pagesTextField.getText().trim().isEmpty())
				pst.setInt(6, 1);
			else if (Integer.valueOf(pagesTextField.getText().trim()) == 0) {
				errorMessage.setText("Please enter positive number in copies.");
				return;
			} else
				pst.setInt(6, Integer.valueOf(pagesTextField.getText().trim()));

			// ******** Publisher
			if (publisherDropDown.getItemAt(publisherDropDown.getSelectedIndex()).isEmpty())
				pst.setString(8, "%");
			else
				pst.setString(8, typeDropDown.getItemAt(typeDropDown.getSelectedIndex()));

			// ******** Type
			if (typeDropDown.getItemAt(typeDropDown.getSelectedIndex()).isEmpty())
				pst.setString(7, "%");
			else
				pst.setString(7, typeDropDown.getItemAt(typeDropDown.getSelectedIndex()));

			// ******** Genre cannot be Null
			if (genreTextField.getText().trim().isEmpty())
				pst.setString(9, "%");
			else
				pst.setString(9, genreTextField.getText().trim());

			// ******** Location
			if (locationTextField.getText().trim().isEmpty())
				pst.setString(10, "%");
			else
				pst.setString(10, locationTextField.getText().trim());

			// ******** publication date if present should be before currentDate
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateFormat.format(new Date()));
			Date datePublished = new Date();
			if (!datePublishedTextField.getText().trim().isEmpty()) {
				if (!isDateValid(datePublishedTextField.getText())) {
					errorMessage.setText("Please enter valid publication date in the mentioned format.");
					return;
				}
				datePublished = new SimpleDateFormat("yyyy-MM-dd").parse(datePublishedTextField.getText());
				if (datePublished.after(currentDate)) {
					errorMessage
							.setText("Please enter publication date in the valid range(0001-01-01 to Current Date)");
					return;
				}
				pst.setString(11, datePublishedTextField.getText().trim());
			} else {
				pst.setString(11, "%");
			}

			int i = 12;
			for (String str : authors) {
				if (str.trim().length() > 0)
					pst.setString(i++, "%".concat(str.trim()).concat("%"));
			}

			if (!editionTextField.getText().toString().isEmpty())
				pst.setInt(i, Integer.valueOf(editionTextField.getText().toString()));

			ResultSet rs = pst.executeQuery();
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			model.setRowCount(0);
			String author = "";
			Stack<Object[]> stack = new Stack<Object[]>();
			Object[] arr;
			if (rs.next()) {
				author = author.concat(rs.getString("name"));
				arr = new Object[] { rs.getString("isbn"), rs.getString("title"), rs.getString("resourcetype"),
						rs.getString("edition"), author, rs.getString("DeweyDecimalSystemNumber"),
						rs.getString("GenreName"), rs.getString("PublicationDate"), rs.getString("Pages"),
						rs.getString("AvailableCopy"), rs.getString("CongressCatalogueNumber"),
						rs.getString("Location"), rs.getString("DateAdded"), rs.getString("PublisherName") };
				stack.push(arr);
			} else {
				errorMessage.setText("No records found");
				return;
			}
			while (rs.next()) {
				if (stack.peek()[0].toString().equals(rs.getString("isbn"))) {
					author = author.concat(",").concat(rs.getString("name"));
					stack.pop();
					arr = new Object[] { rs.getString("isbn"), rs.getString("title"), rs.getString("resourcetype"),
							rs.getString("edition"), author, rs.getString("DeweyDecimalSystemNumber"),
							rs.getString("GenreName"), rs.getString("PublicationDate"), rs.getString("Pages"),
							rs.getString("AvailableCopy"), rs.getString("CongressCatalogueNumber"),
							rs.getString("Location"), rs.getString("DateAdded"), rs.getString("PublisherName") };
					stack.push(arr);
				} else {
					author = rs.getString("name");
					arr = new Object[] { rs.getString("isbn"), rs.getString("title"), rs.getString("resourcetype"),
							rs.getString("edition"), author, rs.getString("DeweyDecimalSystemNumber"),
							rs.getString("GenreName"), rs.getString("PublicationDate"), rs.getString("Pages"),
							rs.getString("AvailableCopy"), rs.getString("CongressCatalogueNumber"),
							rs.getString("Location"), rs.getString("DateAdded"), rs.getString("PublisherName") };
					stack.push(arr);
				}
			}

			while (!stack.isEmpty())
				model.addRow(stack.pop());

			rs.close();
			pst.close();
			databaseConnection.close();
			table.setModel(model);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This function returns a connection to the database or null, if no connection
	 * is found
	 */
	private java.sql.Connection getConnection() {
		try {
			// Load the driver. This is specific to MySQL.
			Class.forName("com.mysql.cj.jdbc.Driver");
			// Use a static method of DriverManager to get a connection to the
			// database.
			databaseConnection = DriverManager.getConnection(getConnectionUrl());
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return databaseConnection;
	}

	/**
	 * This function returns the connection string
	 */
	private String getConnectionUrl() {
		return "jdbc:mysql://localhost/library?user=root&password=admin";
	}
}
