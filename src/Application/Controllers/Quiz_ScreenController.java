package Application.Controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.List;

import Application.Helpers.Creation;
import Application.Helpers.EasyQuiz;
import Application.Helpers.Quiz;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Quiz_ScreenController extends Controller {
	@FXML private AnchorPane _pane;
	@FXML private Button _mainMenuButton;
	@FXML private Button _nextButton;
	@FXML private Button _startButton;
	@FXML private Button _tryAgainButton;
	@FXML private ComboBox _difficulty;
	@FXML private String[] _difficultyLevels = {"Easy", "Medium", "Hard"};
	
	private Quiz _quiz;
	private int _currentQuestionNumber;
	private int _numOfCreations;
	
	public void initialize() {
		if (_pane.getChildren().contains(_startButton)) {
			_difficulty.getItems().removeAll(_difficulty.getItems());
			_difficulty.getItems().addAll(_difficultyLevels);
			_difficulty.getSelectionModel().select(_difficultyLevels[0]);
		}
		
		if (_pane.getChildren().contains(_nextButton)) {
			// extract the current quiz
			extractQuizContent();
		}
	}
	
	public void handleStart() {
		String difficulty = _difficulty.getValue().toString();
		
		// get a list of the creations
		List<Creation> listOfCreations = ((Home_ScreenController)this.getParentController()).getCreationTable().getItems();
		
		// get the total number of creations
		int numOfCreations = ((Home_ScreenController)this.getParentController()).getCreationTable().getItems().size();
		_numOfCreations = numOfCreations;
		
		_quiz = new Quiz(numOfCreations, listOfCreations);
		if (difficulty.equals("Easy")) {
			loadScreen("Quiz", "/Application/fxml/Quiz_Easy.fxml","");
			reset();
			
			// then we play each creation one by one
		} else if (difficulty.equals("Medium")) {
		} else {
		}
		
		// closes
		Stage stage = (Stage) _startButton.getScene().getWindow();
        stage.close();
	}
	
	public void handleNextCreation() {
		writeIntoNextCreation();
		Stage stage = (Stage) _nextButton.getScene().getWindow();
        stage.close();
        
	if (_currentQuestionNumber > _numOfCreations) {
			// changes button text to Finish! when all creations have played
			// at the moment we are just going to the score screen
			loadScreen("Quiz", "/Application/fxml/Quiz_Score.fxml","");
			reset();
		} else {
			// load the same screen but with a different video
			loadScreen("Quiz", "/Application/fxml/Quiz_Easy.fxml","");
		}
	}
	
	public void handleBack() {
		Stage stage = (Stage) _mainMenuButton.getScene().getWindow();
        stage.close();
	}
	
	public void handleTryAgain() {
		loadScreen("home", "/Application/fxml/Quiz_Start.fxml","");
		
		Stage stage = (Stage) _tryAgainButton.getScene().getWindow();
        stage.close();
	}
	
	public Quiz getQuiz() {
		return _quiz;
	}
	
	public void extractQuizContent() {
			try {
				String quiz = System.getProperty("user.dir") + System.getProperty("file.separator") + "quiz.txt";
				File q = new File(quiz);
				BufferedReader file = new BufferedReader(new FileReader(q));
				
				String line;
				while ((line = file.readLine()) != null) {
					System.out.println(line);
	                if (line.trim().startsWith("Number of creations")) {
	                    _numOfCreations = Integer.parseInt(line.substring(line.indexOf("=") + 1));
	                    System.out.println(_numOfCreations);
	                }
	                if (line.trim().startsWith("Current quiz number")) {
	                	_currentQuestionNumber = Integer.parseInt(line.substring(line.indexOf("=") + 1));
	                	System.out.println(_currentQuestionNumber);
	                }
	            }
	            file.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	public void reset() {
		try {
            String quiz = System.getProperty("user.dir") + System.getProperty("file.separator") + "quiz.txt";
            File q = new File(quiz);
            BufferedReader file = new BufferedReader(new FileReader(q));
            StringBuilder inputBuffer = new StringBuilder();
            
            String line;
            while ((line = file.readLine()) != null) {
                if (line.trim().startsWith("Number of creations")) {
                    line = "Number of creations=" + _numOfCreations;
                }
                if (line.trim().startsWith("Current quiz number")) {
                	line = "Current quiz number=" + 1;
                }
                inputBuffer.append(line);
                inputBuffer.append('\n');
            }
            file.close();

            // write the new string with the replaced line OVER the same file
            FileOutputStream fileOut = new FileOutputStream("quiz.txt");
            fileOut.write(inputBuffer.toString().getBytes());
            fileOut.close();

        } catch (Exception e) {
            System.out.println("Problem reading file.");
        }
	}
	
	public void writeIntoNextCreation() {
		try {
            String quiz = System.getProperty("user.dir") + System.getProperty("file.separator") + "quiz.txt";

            File q = new File(quiz);
            BufferedReader file = new BufferedReader(new FileReader(q));
            StringBuilder inputBuffer = new StringBuilder();
            
            String line;
            while ((line = file.readLine()) != null) {
                if (line.trim().startsWith("Current quiz number")) {
                	_currentQuestionNumber++;
                	line = "Current quiz number=" + (_currentQuestionNumber);
                }
                inputBuffer.append(line);
                inputBuffer.append('\n');
            }
            file.close();

            // write the new string with the replaced line OVER the same file
            FileOutputStream fileOut = new FileOutputStream("quiz.txt");
            fileOut.write(inputBuffer.toString().getBytes());
            fileOut.close();

        } catch (Exception e) {
            System.out.println("Problem reading file.");
        }
	}
}
