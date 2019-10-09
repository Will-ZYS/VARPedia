package Application.Controllers;

import Application.Helpers.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Controller for the Add Audio Screen.
 *
 *
 */
public class Add_Audio_ScreenController extends Controller implements Initializable {
	@FXML private Button _mainMenuButton;
	@FXML private MediaView _mediaView;
	@FXML private SplitPane _entireScreenPane;

	// elements in the top half of the screen
	@FXML private Button _playTextButton;
	@FXML private Button _searchButton;
	@FXML private Button _createAudioButton;
	@FXML private ComboBox _voiceBox;
	@FXML private ListView _textDescription;
	@FXML private Slider _speedSlider;
	@FXML private Slider _pitchSlider;
	@FXML private TextField _searchTextField;
	@FXML private ProgressIndicator _progressIndicator;
	
	// elements in the bottom half
	@FXML private AnchorPane _bottomHalf;
	@FXML private Button _playAudioButton;
	@FXML private Button _deleteAudioButton;
	@FXML private TableColumn _termSearched;
	@FXML private TableColumn _numberOfLines;
	@FXML private TableColumn _voice;
	@FXML private TableColumn _speed;
	@FXML private TableColumn _pitch;
	@FXML private TableView _savedAudio;


	//directory for wiki text files
	private File audioDir = new File(".Audio_Directory");
	private File wikitDir = new File(".Wikit_Directory");
	private File wikitRaw = new File(wikitDir + System.getProperty("file.separator") + "raw.txt"); //raw content - where content is not separated to lines
	private File wikitTemp = new File(wikitDir + System.getProperty("file.separator") + "temp.txt"); //temp content - where content is separated

	private AudioPlayer _audioPlayer;
	private ExecutorService _playerExecutor = Executors.newSingleThreadExecutor();
	private ExecutorService _backgroundExecutor = Executors.newFixedThreadPool(5);
	private int _numberOfAudiosCreated = 0;
	private String _searchInput;


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//disables all except the search functionality
		disableCustomization();
		disableBottomHalf();

		_textDescription.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		_textDescription.setCellFactory(TextFieldListCell.forListView());

		_searchTextField.requestFocus();

		// prepares the TableView to be populated with Audio objects
		_termSearched.setCellValueFactory(new PropertyValueFactory<>("termSearched"));
		_numberOfLines.setCellValueFactory(new PropertyValueFactory<>("numberOfLines"));
		_voice.setCellValueFactory(new PropertyValueFactory<>("voiceDisplay"));
		_speed.setCellValueFactory(new PropertyValueFactory<>("speed"));
		_pitch.setCellValueFactory(new PropertyValueFactory<>("pitch"));

		// list of voices
		_voiceBox.getItems().add("English-USA-male");
		_voiceBox.getItems().add("English-USA-female");
		_voiceBox.getItems().add("English-UK-male");
		_voiceBox.getItems().add("English-UK-female");

		_voiceBox.getSelectionModel().select(0);
		_textDescription.getItems().add("No content found.");
		_textDescription.setDisable(true);
	}

	@FXML
	public void handlePlayText() {
		if (!_textDescription.getSelectionModel().getSelectedItems().isEmpty()) {
			// allows you to preview text without waiting for the first one to finish
			terminatePlayers();
			Audio audio = new Audio();
			setUpAudio(audio);
			_audioPlayer = new AudioPlayer(audio, this);
			_playerExecutor.submit(_audioPlayer);
		}

	}

	@FXML
	public void handlePlayAudio() {
		// allow you to play audio without waiting for the first to finish
		if (_savedAudio.getSelectionModel().getSelectedItem() != null) {
			terminatePlayers();
			_audioPlayer = new AudioPlayer((Audio) _savedAudio.getSelectionModel().getSelectedItem(), this);
			_playerExecutor = Executors.newSingleThreadExecutor();
			_playerExecutor.submit(_audioPlayer);
		}
	}

	public void handleSearch() {
		// progress indicator
		_entireScreenPane.setDisable(true);
		_progressIndicator.setProgress(-1);
		_progressIndicator.setVisible(true);
		
		_searchInput = _searchTextField.getText();

		if (!validateSearch(_searchInput)) {
			return;
		}

		try {
			_textDescription.getItems().clear();

			wikitSearch(_searchInput);
		} catch (Exception e) {
			e.printStackTrace();
		}
		_textDescription.getSelectionModel().select(0);
		_textDescription.setDisable(false);
	}

	public void handleCreateAudio() {
		// when the 'Save' button is pressed
		if (_textDescription.getSelectionModel().getSelectedItems().size() > 5) {
			AlertMessage alert = new AlertMessage("Please select 5 lines or less");
			Platform.runLater(alert);
			return;
		}

		// add an Audio object to the TableView
		if (!_textDescription.getSelectionModel().getSelectedItems().isEmpty()) {
			_numberOfAudiosCreated++;
			Audio audio = new Audio();
			audio.setTermSearched(_searchInput);
			setUpAudio(audio);
			audio.setNumberOfLines(String.valueOf(_textDescription.getSelectionModel().getSelectedItems().size()));
			AudioCreator audioCreator = new AudioCreator(_numberOfAudiosCreated, audio, this);
			_backgroundExecutor.submit(audioCreator);
		}
		// disallow searching for another term to avoid saving audio with different terms
		if (_savedAudio.getItems().isEmpty()) {
			_searchTextField.setDisable(false);
		}
	}

	public void handleDeleteAudio() {
		//get the item or items selected and remove from list
		if (_mediaView.getMediaPlayer().getMedia().getSource().equals("file:" + audioDir.getAbsolutePath() + System.getProperty("file.separator") + ((Audio)_savedAudio.getSelectionModel().getSelectedItem()).getFilename())) {
			_mediaView.getMediaPlayer().dispose();
		}
		_savedAudio.getItems().remove(_savedAudio.getSelectionModel().getSelectedItem());
		if (_savedAudio.getItems().isEmpty()) {
			_searchTextField.setDisable(false);
			disableBottomHalf();
		}
	}

	public void handleNext() {
		// stop all preview/audio player
		terminatePlayers();
		// combine the audios and load the Image Selection Screen
		ObservableList<Audio> allAudio = _savedAudio.getItems();
		AudioCombiner combiner = new AudioCombiner(allAudio, this);
		_backgroundExecutor.submit(combiner);

	}


	public void handleBackToMainMenu() {
		// stop all preview/audio player
		terminatePlayers();

		// removes the audio directory contents (all files are temporary)
		Cleaner cleaner = new Cleaner();
		cleaner.cleanAudio();

		// closes audio screen
		Stage stage = (Stage) _mainMenuButton.getScene().getWindow();
		stage.close();
	}

	private void wikitSearch(String searchInput) {
		try {
			//create raw.txt for raw wikit content (has not been separated)
			Writer rawFileWriter = new FileWriter(wikitRaw, false);

			String cmd = "wikit " + searchInput;

			// Runs the wikit command on a worker thread
			WikitWorker wikitWorker = new WikitWorker(cmd, searchInput, rawFileWriter, wikitRaw, wikitTemp, this);
			_backgroundExecutor.submit(wikitWorker);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteLines(KeyEvent key) {
		// allows the deletion of lines in the TextArea
		if (key.getCode().equals(KeyCode.DELETE)) {
			ObservableList<Object>  linesSelected, lines;
			lines = _textDescription.getItems();
			linesSelected = _textDescription.getSelectionModel().getSelectedItems();
			lines.removeAll(linesSelected);

			//disable top half except for search functionality when the content list is empty
			if (_textDescription.getItems().isEmpty()) {
				disableCustomization();
			}
		}
	}

	private boolean validateSearch(String searchInput) {
		// checks for textfield being an empty string or only spaces
		if (searchInput.trim().isEmpty()) {
			return false;
		}
		return true;
	}

	private void terminatePlayers() {
		if (_mediaView.getMediaPlayer() != null) {
			_mediaView.getMediaPlayer().dispose();
		}
		if (_audioPlayer != null) {
			Process process = _audioPlayer.getProcess();
			if (process != null){
				process.destroy();
			}
		}
	}

	public void disableCustomization() {
		_voiceBox.setDisable(true);
		_speedSlider.setDisable(true);
		_pitchSlider.setDisable(true);
	}

	public void enableCustomization() {
		_voiceBox.setDisable(false);
		_speedSlider.setDisable(false);
		_pitchSlider.setDisable(false);
	}

	public void disablePlayCreateText() {
		_createAudioButton.setDisable(true);
		_playTextButton.setDisable(true);
	}

	public void enablePlayCreateText() {
		_createAudioButton.setDisable(false);
		_playTextButton.setDisable(false);
	}

	public void disableBottomHalf() {
		_bottomHalf.setDisable(true);
	}

	public void enableBottomHalf() {
		_bottomHalf.setDisable(false);
	}

	public void setUpAudio(Audio audio) {
		audio.setContent(_textDescription.getSelectionModel().getSelectedItems());
		audio.setVoice((String) (_voiceBox.getSelectionModel().getSelectedItem()));
		audio.setSpeed((int)_speedSlider.getValue());
		audio.setPitch((int) (_pitchSlider.getValue()));
	}


	public ListView getContent() {
		return _textDescription;
	}

	public TableView getAudioList() {
		return _savedAudio;
	}

	public MediaView getMediaView() {
		return _mediaView;
	}

	public Button getPlayTextButton() {
		return _playTextButton;
	}

	public Button getPlayAudioButton() {
		return _playAudioButton;
	}

	public String getSearchInput() {
		return _searchInput;
	}

	public TextField getSearchTextField() {
		return _searchTextField;
	}
	
	public ProgressIndicator getProgressIndicator() {
		return _progressIndicator;
	}
	
	public SplitPane getEntireScreenPane() {
		return _entireScreenPane;
	}
}
