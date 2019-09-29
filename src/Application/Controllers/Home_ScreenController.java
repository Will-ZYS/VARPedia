package Application.Controllers;

import Application.Helpers.Creation;
import Application.Helpers.UpdateHelper;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Home_ScreenController extends Controller implements Initializable {


    @FXML private Button _playButton;
    @FXML private Button _addButton;
    @FXML private Button _deleteButton;
    @FXML private TableView _creationTable;
    @FXML private TableColumn _nameColumn;
    @FXML private TableColumn _termSearchedColumn;
    @FXML private TableColumn _dateModifiedColumn;
    @FXML private TableColumn _videoLengthColumn;
    @FXML private TabPane _videoTabs;


    private UpdateHelper _updateHelper;
    private ArrayList<Creation> _creations = new ArrayList<Creation>();
    private ExecutorService _executor = Executors.newSingleThreadExecutor();
    private List<MediaPlayer> _listOfMediaPlayer = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _creationTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        _nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        _termSearchedColumn.setCellValueFactory(new PropertyValueFactory<>("termSearched"));
        _dateModifiedColumn.setCellValueFactory(new PropertyValueFactory<>("dateModified"));
        _videoLengthColumn.setCellValueFactory(new PropertyValueFactory<>("videoLength"));
        Update();
    }

    @FXML
    public void handlePlay() {

        List<Creation> listOfCreations = _creationTable.getSelectionModel().getSelectedItems();
        if (listOfCreations != null) {
            for (Creation creation : listOfCreations) {
                Tab tab = new Tab();
                tab.setClosable(true);

                tab.setText(creation.getName());
                File fileUrl = new File("Creation_Directory/" + creation.getFileName());
                Media video = new Media(fileUrl.toURI().toString());
                MediaPlayer player = new MediaPlayer(video);
                player.setAutoPlay(true);
                tab.setOnCloseRequest(new EventHandler<Event>()
                {
                    @Override
                    public void handle(Event arg0)
                    {
                        player.dispose();
                    }
                });

                MediaView mediaView = new MediaView();
                mediaView.setMediaPlayer(player);
                mediaView.setFitHeight(350);
                mediaView.setFitWidth(500);
                VBox vbox = new VBox(mediaView);
                vbox.setPadding(new Insets(25, 50, 25, 50));
                vbox.setSpacing(25);
                tab.setContent(new AnchorPane(vbox));
                _videoTabs.getTabs().add(tab);
                _videoTabs.getSelectionModel().select(_videoTabs.getTabs().size() - 1);

                for (MediaPlayer pause : _listOfMediaPlayer) {
                    pause.pause();
                }
                _listOfMediaPlayer.add(player);
            }
        }
    }

    @FXML
    public void handleAdd() {
        Stage addAudioStage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/Add_Audio_Screen.fxml"));
            Parent root = loader.load();
            Add_Audio_ScreenController Add_Audio_ScreenController = loader.getController();
            Add_Audio_ScreenController.setCurrentController(Add_Audio_ScreenController);
            Scene scene = new Scene(root, 1013, 692);
            Add_Audio_ScreenController.setParentController(this);

            //once we have the css file for Add_Audio_Screen
            scene.getStylesheets().addAll(this.getClass().getResource("../css/Add_Audio_Screen.css").toExternalForm());
            addAudioStage.setTitle("VARpedia - Add Audio");
            addAudioStage.setScene(scene);
            addAudioStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Update();
    }

    @FXML
    public void handleDelete() {

        List<Creation> listOfCreations = _creationTable.getSelectionModel().getSelectedItems();
        if (listOfCreations != null) {
            List<Tab> listOfTabToBeRemoved = new ArrayList<>();
            for (Creation creation : listOfCreations) {
                File filePath = new File("Creation_Directory/" + creation.getFileName());
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setContentText("Are you sure you want to delete \"" + creation.getName() + "\"?");
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        for (Tab tab : _videoTabs.getTabs()) {
                            if (tab.getText().equals(creation.getName())) {
                                for (MediaPlayer player : _listOfMediaPlayer) {
                                    if (player.getMedia().getSource().equals("file:" + filePath.getAbsolutePath())) {
                                        player.dispose();
                                    }
                                }
                                listOfTabToBeRemoved.add(tab);
                            }
                        }
                        filePath.delete();
                        _videoTabs.getTabs().removeAll(listOfTabToBeRemoved);
                    }
                });
            }
        }

        Update();
    }

    public ArrayList<Creation> getCreations() {
        return _creations;
    }

    public TableView getCreationTable(){
        return _creationTable;
    }

    public void Update() {
        _updateHelper = new UpdateHelper(this);
        _executor.submit(_updateHelper);
    }
}
