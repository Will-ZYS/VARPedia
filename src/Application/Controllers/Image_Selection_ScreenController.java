package Application.Controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Application.Helpers.ImageGenerator;
import Application.Helpers.ImageViewer;
import Application.Helpers.VideoGenerator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.util.Callback;

public class Image_Selection_ScreenController extends Controller {
	
	private final String IMAGE_DIR = ".Image_Directory" + System.getProperty("file.separator");
	
	@FXML private Button _createButton;
	@FXML private Button _generateButton;
	@FXML private Label _invalidInput;
	@FXML private ListView<Image> _listOfImages;
	@FXML private ImageView _imageView = new ImageView();
	@FXML private ProgressBar _pb;
	@FXML private TextField _nameInput;
	@FXML private TextField _input;
	
	private Add_Audio_ScreenController _controller;
	private ExecutorService _executor = Executors.newSingleThreadExecutor();
	private String _term;
	
	public void initialize() {
		_generateButton.setDisable(true);
		
		_input.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		    	// only allow digits to be typed
		        if (!newValue.matches("\\d+")) {
		            _input.setText(newValue.replaceAll("[^\\d]", ""));
		        }
		        // only allow up to 2 digits to be typed
		        if (newValue.length() > 2) {
		        	_input.setText(newValue.substring(0,2));
		        }
		        // enables or disables button if number is a valid input
		        if (!_input.getText().isEmpty()) {
		        	if (_input.getText().matches("\\d+")) {
		        		if (isValidNumber()) {
				        	_invalidInput.setVisible(false);
				        	_generateButton.setDisable(false);
				        } else {
				        	_invalidInput.setVisible(true);
				        	_generateButton.setDisable(true);
				        }
		        	}
		        } else {
		        	// if textField is empty, disable the button.
		        	_generateButton.setDisable(true);
		        }
		    }
		});

		// handles the checkboxes next to the images
        _listOfImages.setCellFactory(CheckBoxListCell.forListView(new Callback<Image, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(Image image) {
				image.onProperty().addListener((obs, wasOn, isNowOn) -> {
					image.setSelected(isNowOn);
            });
				return image.onProperty();
            }
        }));
	}

	// this is needed for the check box functionality
	public static class Image {
        private final StringProperty name = new SimpleStringProperty();
        private final BooleanProperty on = new SimpleBooleanProperty();
        private String fileName = "";
        private Boolean selected = false;

        public Image(String name, boolean on) {
            setName(name);
            setOn(on);
        }

        public final StringProperty nameProperty() {
            return this.name;
        }

        public final String getName() {
            return this.nameProperty().get();
        }

        public final void setName(final String name) {
            this.nameProperty().set(name);
        }

        public final BooleanProperty onProperty() {
            return this.on;
        }

        public final void setOn(final boolean on) {
            this.onProperty().set(on);
        }

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public Boolean getSelected() {
			return selected;
		}

		public void setSelected(Boolean selected) {
			this.selected = selected;
		}

		@Override
        public String toString() {
            return getName();
        }

    }
	
	public void handleGenerate() {
		if (!isValidNumber()) {
			return;
		}
		
		// progress bar
		_pb.progressProperty().unbind();
		_pb.setProgress(0);
		
		_term = ((Add_Audio_ScreenController)(this.getParentController())).getSearchInput();
		int numPics = Integer.parseInt(_input.getText());
		
		// retrieves images from Flickr
		ImageGenerator imgGen = new ImageGenerator(_term, numPics, this);
		_executor.submit(imgGen);

		_pb.progressProperty().bind(imgGen.progressProperty());
	}

	public void listImages() {
		_listOfImages.getItems().removeAll(_listOfImages.getItems());
		File dir = new File(".Image_Directory");
		File[] listOfFiles = dir.listFiles();

		if (listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				Image image = new Image( _term + " " + (i+1), false);
				image.setFileName(listOfFiles[i].getName());
				_listOfImages.getItems().add(image);
			}
		}
	}

	public void selectImage() {
		ImageViewer imageViewer = new ImageViewer(this);
		_executor.submit(imageViewer);
	}

	public void viewImage(){
		if (_listOfImages.getSelectionModel().getSelectedItem() != null) {
			String imagePath = "file:" + IMAGE_DIR + _listOfImages.getSelectionModel().getSelectedItem().getFileName();
			javafx.scene.image.Image image = new javafx.scene.image.Image(imagePath);
			_imageView.setImage(image);
		}
	}
	
	public void handleCreate() {
		if (!isNameValid()) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText(null);
			alert.setContentText("Please enter a valid name for the creation.");
			alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			alert.show();
			return;
		}
		// disallows user to click Create again
		_createButton.setDisable(false);
		
		// progress bar
		_pb.progressProperty().unbind();
		_pb.setProgress(0);

		// creates the creation
		VideoGenerator videoGen = new VideoGenerator(_term, this);
		videoGen.setCreationName(_nameInput.getText());
		int numPics = 0;
		for (Image image : _listOfImages.getItems()) {
			if (image.getSelected()){
				numPics++;
				videoGen.addImage(IMAGE_DIR + image.getFileName());
			}
		}
		if (numPics == 0) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText(null);
			alert.setContentText("Please select at least one image.");
			alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			alert.show();
			return;
		}
		videoGen.setNumPics(numPics);
		_executor.submit(videoGen);
		_pb.progressProperty().unbind();
		_pb.setProgress(0);
		_pb.progressProperty().bind(videoGen.progressProperty());

	}

	public boolean isValidNumber() {
		if (Integer.parseInt(_input.getText()) > 0 && Integer.parseInt(_input.getText()) < 11) {
			return true;
		}
		return false;
	}
	
	private boolean isNameValid() {
		// Disallows input of spaces or an empty string
		if (_nameInput.getText().trim().isEmpty()) {
			return false;
		}
		
		//prevents any special characters apart from "-", "_" and (space)
		if (_nameInput.getText().matches("[a-zA-Z0-9 ]*")) {
			return true;
		} else {
			return false;
		}
	}

	public Button getCreateButton() {
		return _createButton;
	}

	public ListView<Image> getListOfImages() {
		return _listOfImages;
	}
}

