package Application.Controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Application.Helpers.MusicAdder;
import Application.Helpers.Track;
import Application.Helpers.TrackPlayer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaView;

public class Background_Music_ScreenController extends Controller {
	@FXML private Button _playButton;
	@FXML private Button _nextButton;
	@FXML private ComboBox _musicComboBox;
	@FXML private TextField _nameInput;
	@FXML private Label _nameOfTrack;
	@FXML private Label _license;
	@FXML private Label __trackLink;
	@FXML private Label _author;
	@FXML private MediaView _mediaView;
	@FXML private StackPane _creditsPane;
	
	private TrackPlayer _trackPlayer;
	private ExecutorService _playerExecutor = Executors.newSingleThreadExecutor();
	private ExecutorService _backgroundExecutor = Executors.newFixedThreadPool(5);
	
	private final String AUDIO_DIR = ".Audio_Directory" + System.getProperty("file.separator");
	private final String MUSIC_DIR = ".Music_Directory" + System.getProperty("file.separator");
	private final List<Track> TRACK = new ArrayList<Track>();
	private final String NO_MUSIC = "No music";
	
	public void initialize() {
		// populate the combo box
		TRACK.add(new Track(NO_MUSIC));
		
		// extract all the tracks in the music directory
		List<String> trackFiles = extractFromDirectory();
		
		for (String track : trackFiles) {
			TRACK.add(new Track(track));
		}
		
		_musicComboBox.getItems().removeAll(TRACK);
		_musicComboBox.getItems().addAll(TRACK);
		
		// selects the default (No music)
		_musicComboBox.getSelectionModel().select(TRACK.get(0));
		_playButton.setDisable(true);
	}
	
	public void handleSelect() {
		// get the selected item
		Track track = (Track)_musicComboBox.getValue();
		
		if (track.getTrackName() != NO_MUSIC) {
			_playButton.setDisable(false);
			_creditsPane.setVisible(true);
			
			// display the author and trackName
			String trackFullName = track.getTrackName();

			//gets the occurrence of the file separator pattern
			int patternIndex = trackFullName.indexOf("_-_");
			int lengthPattern = 3;
			// get the occurrence of the extension .mp3
			int extPatternIndex = trackFullName.indexOf(".mp3");
			
			_nameOfTrack.setText(trackFullName.substring(patternIndex + lengthPattern, extPatternIndex));
			_author.setText(track.getAuthor());
		} else {
			_playButton.setDisable(true);
			_creditsPane.setVisible(false);
		}
	}
	
	public void handleNext() {
		// stop all preview/audio player
		terminatePlayers();
		
		// get the spoken audio file output
		String spokenAudio = "output" 
				+ ((Add_Audio_ScreenController)this.getParentController()).getAudioFileId()
				+ ".wav";

		// combine the audio and the background music scene and load the Image Selection Screen
		Track track = (Track) _musicComboBox.getValue();
		MusicAdder bgmAdder = new MusicAdder(track, spokenAudio, this);
		_backgroundExecutor.submit(bgmAdder);
	}
	
	public void handlePlay() {
		// allow you to play audio without waiting for the first to finish
		if (_musicComboBox.getValue().toString() != NO_MUSIC) {
			terminatePlayers();
			_trackPlayer = new TrackPlayer((Track)_musicComboBox.getValue(), this);
			_playerExecutor = Executors.newSingleThreadExecutor();
			_playerExecutor.submit(_trackPlayer);
		}
	}
	
	private void terminatePlayers() {
		if (_mediaView.getMediaPlayer() != null) {
			_mediaView.getMediaPlayer().dispose();
		}
		if (_trackPlayer != null) {
			Process process = _trackPlayer.getProcess();
			if (process != null){
				process.destroy();
			}
		}
	}
	
	private List<String> extractFromDirectory() {
		List<String> listOfFilenames = new ArrayList<>();
		File[] listOfFiles = new File(MUSIC_DIR).listFiles();

		for (File file : listOfFiles) {
			listOfFilenames.add(file.getName());
		}
		return listOfFilenames;
	}
	
	public Button getNextButton() {
		return _nextButton;
	}

	public MediaView getMediaView() {
		return _mediaView;
	}

}