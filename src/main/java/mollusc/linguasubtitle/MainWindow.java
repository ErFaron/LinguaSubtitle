package mollusc.linguasubtitle;

import mollusc.linguasubtitle.db.ItemVocabulary;
import mollusc.linguasubtitle.db.Vocabulary;
import mollusc.linguasubtitle.filechooser.AdvancedSubStationAlphaSubtitleFilter;
import mollusc.linguasubtitle.filechooser.JFileChooserWithCheck;
import mollusc.linguasubtitle.filechooser.SubRipSubtitleFilter;
import mollusc.linguasubtitle.index.Indexer;
import mollusc.linguasubtitle.stemming.Stemmator;
import mollusc.linguasubtitle.subtitle.Subtitle;
import mollusc.linguasubtitle.subtitle.format.AdvancedSubStationAlphaRender;
import mollusc.linguasubtitle.subtitle.format.Render;
import mollusc.linguasubtitle.subtitle.format.SubRipRender;
import mollusc.linguasubtitle.subtitle.format.WordStyle;
import mollusc.linguasubtitle.subtitle.utility.CommonUtility;
import mollusc.linguasubtitle.table.*;
import mollusc.linguasubtitle.table.CellEditor;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * User: mollusc <MolluscLab@gmail.com>
 */
public class MainWindow implements PropertyChangeListener {

	//<editor-fold desc="Form controls">
	//<editor-fold desc="Private Field">
	private JPanel contentPanel;
	private JTable statisticTable;
	private JButton exportFromDatabaseButton;
	private JButton preferenceToSubtitleButton;
	private JButton preferenceFromDatabaseButton;
	private JEditorPane textSubtitleEditorPane;
	private JComboBox<String> languagesComboBox;
	private JLabel helpLinkLabel;
	private JLabel siteLinkLabel;
	private JButton openSubtitleButton;
	//</editor-fold>
	//<editor-fold desc="Public Field">
	public JTable mainTable;
	public JButton exportToSubtitleButton;
	//</editor-fold>
	//</editor-fold>

	//<editor-fold desc="Private Fields">
	/**
	 * Path to pathToSubtitle
	 */
	private String pathToSubtitle;
	/**
	 * Responsible for view subtitle in the text pane
	 */
	private SubtitleViewer subtitleViewer;
	/**
	 * Container for speeches
	 */
	private Subtitle subtitle;
	/**
	 * Index of the text subtitle
	 */
	private Indexer index;
	/**
	 * Array of hard words
	 */
	private ArrayList<String> hardWords;
	/**
	 * Settings of the program
	 */
	private final Settings settings;
	/**
	 * Map with available languages
	 */
	private Map<String, String> languages;
	/**
	 * Task for updating the database
	 */
	private TaskUpdateDatabase task;
	/**
	 * Parent frame
	 */
	private final JFrame frameParent;
	//</editor-fold>

	//<editor-fold desc="Public Fields">
	/**
	 * Display of progress complete
	 */
	public ProgressMonitor progressMonitor;
	/**
	 * Language of the subtitle
	 */
	public String language;
	//</editor-fold>


	public static void main(String[] args) {
		JFrame frame = new JFrame("MainWindow");
		frame.setContentPane(new MainWindow(frame).contentPanel);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	//<editor-fold desc="Constructor">

	/**
	 * Constructor of the class MainWindow
	 *
	 * @param frameParent parent frame
	 */
	private MainWindow(JFrame frameParent) {
		this.frameParent = frameParent;
		this.frameParent.setTitle("LinguaSubtitle 2");

		initializeLanguages();
		settings = getSettings();
		initializeExportToSubtitle();
		initializeTableMain();
		initializeTableStatistic();
		initializeLanguagesComboBox();
		initializeExportFromDatabase();
		initializeLinks();

		openSubtitleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				openSubtitleActionPerformed();
			}
		});
	}
	//</editor-fold>

	//<editor-fold desc="Private Methods">

	//<editor-fold desc="Initialization parameters">
	private void initializeLanguages() {
		language = null;
		languages = new HashMap<String, String>();
		languages.put("English", "english");
		languages.put("Français", "french");
		languages.put("Deutsch", "german");
		languages.put("Italiano", "italian");
		languages.put("Português", "portuguese");
		languages.put("Русский", "russian");
		languages.put("Español", "spanish");
		languages.put("Türkçe", "turkish");
	}

	private void initializeExportFromDatabase() {
		exportFromDatabaseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dumpDatabase();
			}
		});
		preferenceFromDatabaseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				openPreference(1);
			}
		});
	}

	private void initializeExportToSubtitle() {
		exportToSubtitleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				exportToSubtitleButtonActionPerformed();
			}
		});
		preferenceToSubtitleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				openPreference(0);
			}
		});
	}

	private void initializeTableStatistic() {
		statisticTable.setModel(new StatisticTableModel());
		statisticTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		statisticTable.getColumnModel().getColumn(1).setMaxWidth(150);
		statisticTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		statisticTable.getColumnModel().getColumn(2).setMaxWidth(150);
	}

	private void initializeTableMain() {
		mainTable.setModel(new MainTableModel());
		mainTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		mainTable.getColumnModel().getColumn(0).setMaxWidth(40);
		mainTable.getColumnModel().getColumn(1).setPreferredWidth(40);
		mainTable.getColumnModel().getColumn(1).setMaxWidth(40);
		mainTable.getColumnModel().getColumn(2).setPreferredWidth(60);
		mainTable.getColumnModel().getColumn(2).setMaxWidth(60);
		mainTable.getColumnModel().getColumn(3).setPreferredWidth(100);
		mainTable.getColumnModel().getColumn(4).setPreferredWidth(100);
		mainTable.getColumnModel().getColumn(5).setPreferredWidth(50);
		mainTable.getColumnModel().getColumn(5).setMaxWidth(80);
		mainTable.getColumnModel().getColumn(6).setPreferredWidth(50);
		mainTable.getColumnModel().getColumn(6).setMaxWidth(80);
		mainTable.setRowHeight(20);
		mainTable.setDefaultEditor(Object.class, new CellEditor());
		mainTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				tableMainMouseClicked(evt);
			}
		});
	}

	private void initializeLanguagesComboBox() {
		for (String language : languages.keySet())
			languagesComboBox.addItem(language);

		if (languages.containsValue(settings.getLanguage())) {
			for (String key : languages.keySet()) {
				String value = languages.get(key);
				if (value.equals(settings.getLanguage()))
					languagesComboBox.setSelectedItem(key);
			}
		}
	}

	private void initializeLinks() {
		helpLinkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		helpLinkLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(new URI("http://sourceforge.net/p/linguasubtitle/wiki/Home/"));
				} catch (Exception ignored) {
				}
			}
		});


		siteLinkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		siteLinkLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(new URI("http://sourceforge.net/projects/linguasubtitle/"));
				} catch (Exception ignored) {
				}
			}
		});
	}

	//</editor-fold>

	//<editor-fold desc="Handle for event">

	/**
	 * Handle clicks on open Preference button.
	 *
	 * @param index which table activate
	 */
	private void openPreference(int index) {
		Preferences preferencesDialog = new Preferences(frameParent,this.settings, this.languages);
		preferencesDialog.activateTab(index);
		preferencesDialog.setVisible(true);
		preferencesDialog.pack();
	}

	/**
	 * Invoked when the mouse button has been clicked on mainTable
	 */
	private void tableMainMouseClicked(MouseEvent evt) {
		Point point = evt.getPoint();
		int columnIndex = mainTable.columnAtPoint(point);
		int rowIndex = mainTable.rowAtPoint(point);
		if ((SwingUtilities.isRightMouseButton(evt))  && columnIndex == 3)
			highlightWord(rowIndex);
		if (SwingUtilities.isLeftMouseButton(evt) && (columnIndex == 0 || columnIndex == 1 || columnIndex == 2))
			updateStatistic();
		if (SwingUtilities.isRightMouseButton(evt) && (columnIndex == 3 ||columnIndex == 4))
		{
			mainTable.changeSelection(rowIndex, columnIndex, false, false);
			mainTable.editCellAt(rowIndex, columnIndex);
			mainTable.requestFocus();
			CellEditor c = (CellEditor) mainTable.getCellEditor(rowIndex,columnIndex);
		}
	}

	/**
	 * Handle clicks on openSubtitleButton button.
	 */
	private void openSubtitleActionPerformed() {
		language = languages.get(languagesComboBox.getSelectedItem());
		JFileChooser fileOpen = new JFileChooser();
		fileOpen.setFileFilter(new SubRipSubtitleFilter());
		if (subtitleViewer != null && new File(pathToSubtitle).exists())
			fileOpen.setCurrentDirectory(new File(pathToSubtitle));
		int returnValue = fileOpen.showDialog(frameParent, "Open");
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			frameParent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			File file = fileOpen.getSelectedFile();
			String path = file.getAbsolutePath();
			if (path != null) {
				pathToSubtitle = path;
				subtitleViewer = null;
				subtitle = new Subtitle(pathToSubtitle);
				if (subtitle.size() == 0) {
					JOptionPane.showMessageDialog(frameParent, "Could not read subtitle: File in wrong format", "Format error", JOptionPane.ERROR_MESSAGE);
					frameParent.setCursor(Cursor.getDefaultCursor());
					return;
				}
				index = new Indexer(subtitle, language);
				subtitleViewer = new SubtitleViewer(subtitle, index);
				if (loadTextPane()) {
					loadTable();
					frameParent.setTitle("LinguaSubtitle 2 - " + file.getName());
				}
			}
			frameParent.setCursor(Cursor.getDefaultCursor());
		}
	}

	/**
	 * Handle clicks on exportToSubtitle button.
	 */
	private void exportToSubtitleButtonActionPerformed() {
		JFileChooser fileOpen = new JFileChooserWithCheck(true);
		FileFilter srt = new SubRipSubtitleFilter();
		FileFilter ass = new AdvancedSubStationAlphaSubtitleFilter();
		fileOpen.addChoosableFileFilter(ass);
		fileOpen.addChoosableFileFilter(srt);
		if(settings.getDefaultFileFilter().equals("ass"))
			fileOpen.setFileFilter(ass);
		else if (settings.getDefaultFileFilter().equals("srt"))
			fileOpen.setFileFilter(srt);

		fileOpen.setSelectedFile(new File(pathToSubtitle));
		int returnValue = fileOpen.showSaveDialog(frameParent);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File file = fileOpen.getSelectedFile();
			String pathGeneratedSubtitle = file.getAbsolutePath();

			WordStyle style = getWordStyle();
			Render render = null;
			String extension = CommonUtility.getExtension(file);
			settings.setDefaultFileFilter(extension.toLowerCase());
			if (extension.equals("srt"))
				render = new SubRipRender(subtitle, style, index, settings);
			else if (extension.toLowerCase().equals("ass"))
				render = new AdvancedSubStationAlphaRender(subtitle, style, index, settings);
			if (render != null){
				render.save(pathGeneratedSubtitle);
				updateDatabase();
			}
		}
	}
	//</editor-fold>

	/**
	 * Updating records in the database
	 */
	private void updateDatabase() {
		exportToSubtitleButton.setEnabled(false);
		progressMonitor = new ProgressMonitor(frameParent,
				"Updating records in the Database...",
				"", 0, 100);
		progressMonitor.setProgress(0);

		updateSettings();
		task = new TaskUpdateDatabase(this);
		task.addPropertyChangeListener(this);
		task.execute();
	}

	/**
	 * Create dump of the database
	 */
	private void dumpDatabase() {
		Vocabulary db = new Vocabulary();
		db.createConnection();
		int meeting = settings.getExportMoreThan();
		JFileChooser fileOpen = new JFileChooserWithCheck(false);
		int returnValue = fileOpen.showSaveDialog(frameParent);
		String pathGeneratedSubtitle = null;
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File file = fileOpen.getSelectedFile();
			pathGeneratedSubtitle = file.getAbsolutePath();

		}
		ArrayList<ItemVocabulary> result;
		result = db.getDump(
				settings.getExportUnknownWords(),
				settings.getExportKnownWords(),
				settings.getExportStudyWords(),
				settings.getNoBlankTranslation(),
				meeting,
				settings.getExportLanguage());
		db.closeConnection();

		saveDump(pathGeneratedSubtitle, result);
		updateSettings();
	}

	/**
	 * Save dump of the database in a file
	 *
	 * @param path  path to the file
	 * @param items saved items
	 */
	private static void saveDump(String path, ArrayList<ItemVocabulary> items) {
		if (path != null && items != null) {
			try {
				OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path, false), "UTF8");
				for (ItemVocabulary item : items)
					writer.write(item.word + "\t" + item.translate + "\n");
				writer.close();
			} catch (Exception e) {
				System.err.println("Error: " + e.getMessage());
			}
		}
	}

	/**
	 * Save all settings in the Database
	 */
	private void updateSettings() {
		Vocabulary db = new Vocabulary();
		settings.setLanguage(language);
		db.createConnection();
		db.updateSettings(settings.getMap());
		db.closeConnection();
	}

	/**
	 * Get words style
	 *
	 * @return words style
	 */
	private WordStyle getWordStyle() {
		ArrayList<WordInfo> wordInfos = new ArrayList<WordInfo>();
		for (int i = 0; i < mainTable.getRowCount(); i++) {
			boolean isName = (Boolean) mainTable.getModel().getValueAt(i, 0);
			boolean isStudy = (Boolean) mainTable.getModel().getValueAt(i, 1);
			boolean isKnown = (Boolean) mainTable.getModel().getValueAt(i, 2);
			String word = mainTable.getModel().getValueAt(i, 3).toString();
			Stemmator stemmator = new Stemmator(word, language);
			String translate = (String) mainTable.getModel().getValueAt(i, 4);
			wordInfos.add(new WordInfo(word, stemmator.getStem(), translate, isKnown, isStudy, isName));
		}
		return new WordStyle(wordInfos, hardWords, settings);
	}


	/**
	 * Get settings from the database
	 *
	 * @return pairs a parameter name and value
	 */
	private Settings getSettings() {
		Vocabulary db = new Vocabulary();
		db.createConnection();
		Map<String, String> mapSettings = db.getSettings();
		db.closeConnection();
		Settings s = new Settings(mapSettings);
		// Set default value for settings if it isn't set
		Preferences defaultSettings = new Preferences(frameParent, s, this.languages);
		defaultSettings.updateSettings();

		return s;
	}

	/**
	 * Highlight a selected word in the text pane
	 *
	 * @param rowNumber selected row
	 */
	private void highlightWord(int rowNumber) {
		int rowIndex = mainTable.convertRowIndexToModel(rowNumber);
		if (rowIndex != -1 && subtitleViewer != null) {
			String word = mainTable.getModel().getValueAt(rowIndex, 3).toString();
			Stemmator stemmator = new Stemmator(word, language);
			textSubtitleEditorPane.setText("");
			Document document = textSubtitleEditorPane.getDocument();
			subtitleViewer.print(stemmator.getStem(), document);
			textSubtitleEditorPane.setCaretPosition(subtitleViewer.getPositionStem(stemmator.getStem()));
		}
	}

	/**
	 * Fill mainTable
	 */
	private void loadTable() {
		Map<Stemmator, Integer> stems = index.getListStems();
		DefaultTableModel tableModel = ((DefaultTableModel) mainTable.getModel());
		tableModel.setRowCount(0);
		Vocabulary db = new Vocabulary();
		db.createConnection();
		hardWords = db.getHardWords();
		mainTable.setDefaultRenderer(Object.class, new CellRender(hardWords, language));
		mainTable.setDefaultRenderer(Integer.class, new CellRender(hardWords, language));
		mainTable.setDefaultRenderer(Boolean.class, new CheckBoxRenderer());
		for (Stemmator key : stems.keySet()) {
			ItemVocabulary itemDatabase = db.getItem(key.getStem(), language);
			boolean known = false;
			boolean study = false;
			int meeting = 0;
			String translate = "";
			String word = key.getWord();
			if (itemDatabase != null) {
				known = itemDatabase.known;
				study = itemDatabase.study;
				meeting = itemDatabase.meeting;
				translate = itemDatabase.translate;

				if (itemDatabase.word.length() < word.length())
					word = itemDatabase.word;

				if (Character.isLowerCase(itemDatabase.word.charAt(0)) || Character.isLowerCase(key.getWord().charAt(0)))
					word = word.toLowerCase();
			}
			tableModel.addRow(new Object[]{false, study, known, word, translate, stems.get(key), meeting});
		}
		db.closeConnection();
		tableDefaultSort();
		updateStatistic();
	}

	/**
	 * Sort mainTable in the original condition
	 */
	private void tableDefaultSort() {
		DefaultRowSorter sorter = ((DefaultRowSorter) mainTable.getRowSorter());
		ArrayList<RowSorter.SortKey> list = new ArrayList<RowSorter.SortKey>();
		list.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
		list.add(new RowSorter.SortKey(5, SortOrder.DESCENDING));
		list.add(new RowSorter.SortKey(6, SortOrder.ASCENDING));
		list.add(new RowSorter.SortKey(3, SortOrder.ASCENDING));
		sorter.setSortKeys(list);
		sorter.sort();
	}

	/**
	 * Update statistic
	 */
	private void updateStatistic() {
		int totalWords = 0;
		int totalUnique = mainTable.getRowCount();

		int unknownWords = 0;
		int unknownUnique = 0;

		int knownWords = 0;
		int knownUnique = 0;

		int studyWords = 0;
		int studyUnique = 0;

		int newWords = 0;
		int newUnique = 0;

		for (int i = 0; i < totalUnique; i++) {
			boolean isName = (Boolean) mainTable.getModel().getValueAt(i, 0);
			boolean isStudy = (Boolean) mainTable.getModel().getValueAt(i, 1);
			boolean isKnown = (Boolean) mainTable.getModel().getValueAt(i, 2);
			int count = (Integer) mainTable.getModel().getValueAt(i, 5);
			int mentioned = (Integer) mainTable.getModel().getValueAt(i, 6);
			totalWords += count;

			if (mentioned == 0) {
				newUnique++;
				newWords += count;
			}

			if (isKnown) {
				knownUnique++;
				knownWords += count;
				continue;
			}

			if (isStudy) {
				studyUnique++;
				studyWords += count;
				continue;
			}

			if (!isName) {
				unknownUnique++;
				unknownWords += count;
			}
		}

		statisticTable.setValueAt(totalUnique, 0, 1);
		statisticTable.setValueAt(totalWords, 0, 2);

		statisticTable.setValueAt(String.valueOf(unknownUnique) + " (" + String.format("%.1f", 100f * (float) unknownUnique / (float) totalUnique) + "%)", 1, 1);
		statisticTable.setValueAt(String.valueOf(unknownWords) + " (" + String.format("%.1f", 100f * (float) unknownWords / (float) totalWords) + "%)", 1, 2);

		statisticTable.setValueAt(String.valueOf(knownUnique) + " (" + String.format("%.1f", 100f * (float) knownUnique / (float) totalUnique) + "%)", 2, 1);
		statisticTable.setValueAt(String.valueOf(knownWords) + " (" + String.format("%.1f", 100f * (float) knownWords / (float) totalWords) + "%)", 2, 2);

		statisticTable.setValueAt(String.valueOf(studyUnique) + " (" + String.format("%.1f", 100f * (float) studyUnique / (float) totalUnique) + "%)", 3, 1);
		statisticTable.setValueAt(String.valueOf(studyWords) + " (" + String.format("%.1f", 100f * (float) studyWords / (float) totalWords) + "%)", 3, 2);

		statisticTable.setValueAt(String.valueOf(newUnique) + " (" + String.format("%.1f", 100f * (float) newUnique / (float) totalUnique) + "%)", 4, 1);
		statisticTable.setValueAt(String.valueOf(newWords) + " (" + String.format("%.1f", 100f * (float) newWords / (float) totalWords) + "%)", 4, 2);
	}

	/**
	 * Insert subtitle's text to textSubtitleEditorPane
	 *
	 * @return true if it is success, otherwise false
	 */
	private boolean loadTextPane() {
		if (subtitleViewer != null) {
			textSubtitleEditorPane.setText("");
			subtitleViewer.print(textSubtitleEditorPane.getDocument());

			return true;
		}
		return false;
	}
	//</editor-fold>

	//<editor-fold desc="Public Methods">
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress".equals(evt.getPropertyName())) {
			int progress = (Integer) evt.getNewValue();
			progressMonitor.setProgress(progress);
			String message =
					String.format("Completed %d%%.\n", progress);
			progressMonitor.setNote(message);
			if (progressMonitor.isCanceled() || task.isDone()) {
				if (progressMonitor.isCanceled()) {
					task.cancel(true);
				}
				exportToSubtitleButton.setEnabled(true);
			}
		}
	}
	//</editor-fold>
}
