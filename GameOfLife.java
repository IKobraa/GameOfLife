package application;

import java.util.Arrays;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;

public class GameOfLife extends Application implements EventHandler<ActionEvent> {

	Canvas c;
	int columns = 60;
	int rows = columns;
	int[][] states = new int[columns][rows];
	int[][] statesNextGen = new int[columns][rows];
	GridPane paneGame;
	Button btnStart;
	Button btnStep;
	Button btnExit;
	Button btnStop;
	Button btnRestart;
	Timeline oneSecond;
	Label genCount;
	int i = 0;
	boolean noChange = false;

	@Override
	public void start(Stage primaryStage) throws InterruptedException {

		// control buttons
		btnStart = new Button("Start");
		btnStep = new Button("Step");
		btnStop = new Button("Stop");
		btnRestart = new Button("Restart");
		btnExit = new Button("Exit");

		// generation display
		genCount = new Label("Generation: " + i);
		genCount.setStyle("-fx-text-fill: white");

		// button activate action in this class
		btnStart.setOnAction(e -> {
			
			oneSecond = new Timeline(new KeyFrame(Duration.millis(250), new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {

					// check if all cells are dead
					boolean dead = true;

					for (int col = 0; col < states.length; col++) {
						for (int r = 0; r < states[col].length; r++) {
							if (states[col][r] == 1)
								dead = false;
						}
					}

					if (dead) {
						btnStop.fire();
						btnRestart.fire();
						genCount.setText("DEAD");
						genCount.setStyle("-fx-text-fill: red");
					}

					checkNeighbours(paneGame);

					if (noChange && !dead) {
						btnStop.fire();
						genCount.setText("unchanged");
					}

					setNextGeneration(paneGame);
					i++;
					if (!noChange && !dead)
						genCount.setText("Generation: " + i);
				}
			}));
			oneSecond.setCycleCount(Timeline.INDEFINITE);
			oneSecond.play();
		});
		btnStep.setOnAction(this);
		btnRestart.setOnAction(this);
		btnExit.setOnAction(this);
		btnStop.setOnAction(e -> oneSecond.stop());

		btnStart.setStyle("-fx-background-color: white; -fx-text-fill: black;");
		btnStep.setStyle("-fx-background-color: white; -fx-text-fill: black;");
		btnStop.setStyle("-fx-background-color: darkred; -fx-text-fill: white;");
		btnExit.setStyle("-fx-background-color: darkred; -fx-text-fill: white;");
		btnRestart.setStyle("-fx-background-color: white; -fx-text-fill: black;");

		Group root = new Group();
		c = new Canvas();
		HBox box = new HBox();

		// top half of the screen with buttons
		box.setMinSize(500, 20);
		box.setPadding(new Insets(10, 10, 10, 10));
		box.setAlignment(Pos.TOP_CENTER);
		box.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
				+ "-fx-border-insets: 5;" + "-fx-border-radius: 5;"
				+ "-fx-border-color: white;-fx-background-color: black;-fx-spacing: 10;");
		box.getChildren().add(btnStart);
		box.getChildren().add(btnStep);
		box.getChildren().add(btnRestart);
		box.getChildren().add(btnExit);
		box.getChildren().add(btnStop);
		box.getChildren().add(genCount);

		// Gameboard
		paneGame = new GridPane();
		paneGame.setStyle("-fx-background-color: black;");
		paneGame.setMinSize(500, 500);
		paneGame.setAlignment(Pos.BOTTOM_CENTER);
		// 225%
		paneGame.setLayoutY(2.25 * 20);
		paneGame.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent e) {
				Node n = e.getPickResult().getIntersectedNode();
				Integer col = paneGame.getColumnIndex(n);
				Integer row = paneGame.getRowIndex(n);
				states[(int) col][(int) row] = 1;

				Pane p = (Pane) n;
				p.setStyle("-fx-background-color: white");

			}

		});

		// generate cell size depending on rows and columns
		for (int i = 0; i < columns; i++) {
			ColumnConstraints cc = new ColumnConstraints();
			cc.setPercentWidth(100.0 / columns);
			paneGame.getColumnConstraints().add(cc);
		}

		for (int i = 0; i < rows; i++) {
			RowConstraints rc = new RowConstraints();
			rc.setPercentHeight(100.0 / columns);
			paneGame.getRowConstraints().add(rc);
		}

		// fill states with zeros
		for (int col = 0; col < states.length; col++) {
			for (int r = 0; r < states[col].length; r++) {
				states[col][r] = 0;
			}
		}

		// fill nextGeneration with zeros
		for (int col = 0; col < statesNextGen.length; col++) {
			for (int r = 0; r < statesNextGen[col].length; r++) {
				statesNextGen[col][r] = 0;
			}
		}

		for (int col = 0; col < states.length; col++) {
			for (int r = 0; r < states[col].length; r++) {

				Pane p = new Pane();

				paneGame.add(p, col, r);

			}

		}

		Scene scene = new Scene(root, 500, 545, Color.RED);
		root.getChildren().add(paneGame);
		root.getChildren().add(box);

		primaryStage.getIcons().add(new Image("C:\\Users\\Kobra\\Downloads\\skull-icon-5247.png"));
		primaryStage.setTitle("Game of Life");
		primaryStage.setScene(scene);
		primaryStage.show();

	}

	public void checkNeighbours(GridPane gp) {

		// leaving out border so you dont have to check sides because side 0 -1 is not
		// possible

		for (int i = 1; i < states.length - 1; i++) {
			for (int j = 1; j < states[i].length - 1; j++) {

				int countN = 0;

//					// left
//					if (states[i][j - 1] == 1) {
//						countN++;
//					}
//					// right
//					if (states[i][j + 1] == 1) {
//						countN++;
//					}
//					// bottom
//					if (states[i + 1][j] == 1) {
//						countN++;
//					}
//					// top
//					if (states[i - 1][j] == 1) {
//						countN++;
//					}
//					// top right
//					if (states[i - 1][j + 1] == 1) {
//						countN++;
//					}
//					// bottom right
//					if (states[i + 1][j + 1] == 1) {
//						countN++;
//					}
//					// bottom left
//					if (states[i + 1][j - 1] == 1) {
//						countN++;
//					}
//					// top left
//					if (states[i - 1][j - 1] == 1) {
//						countN++;
//					}

				/* optimized without if-statements because neighbour is 1 or 0 so if you want 
				 * to count neighbour i can just sum it and dont need to ++ the counter */
				countN += states[i][j - 1];
				countN += states[i][j + 1];
				countN += states[i + 1][j];
				countN += states[i - 1][j];
				countN += states[i - 1][j + 1];
				countN += states[i + 1][j + 1];
				countN += states[i + 1][j - 1];
				countN += states[i - 1][j - 1];

				if (states[i][j] == 1 && countN < 2) {
					statesNextGen[i][j] = 0;
				}
				if (states[i][j] == 1 && countN > 3) {
					statesNextGen[i][j] = 0;
				}
				if (states[i][j] == 1 && (countN == 3 || countN == 2)) {
					statesNextGen[i][j] = 1;
				}
				if (states[i][j] == 0 && countN == 3) {
					statesNextGen[i][j] = 1;
				}

			}

		}

		int[][] checkChange = new int[columns][rows];

		for (int i = 0; i < checkChange.length; i++) {
			for (int j = 0; j < checkChange[i].length; j++) {
				checkChange[i][j] = states[i][j];
			}
		}

		for (int col = 0; col < statesNextGen.length; col++) {
			for (int r = 0; r < statesNextGen[col].length; r++) {
				states[col][r] = statesNextGen[col][r];
			}
		}

		if (Arrays.deepEquals(states, checkChange)) {
			noChange = true;
		}

		for (int col = 0; col < statesNextGen.length; col++) {
			for (int r = 0; r < statesNextGen[col].length; r++) {
				statesNextGen[col][r] = 0;
			}
		}

	}

	public Node getNode(GridPane gp, int col, int row) {

		for (Node node : gp.getChildren()) {
			if (gp.getColumnIndex(node) == col && gp.getRowIndex(node) == row) {
				return node;
			}
		}

		return null;

	}

	public void setNextGeneration(GridPane gp) {

		for (int col = 0; col < states.length; col++) {
			for (int r = 0; r < states[col].length; r++) {

				Pane p = (Pane) getNode(gp, col, r);

				if (states[col][r] == 1) {
					p.setStyle("-fx-background-color: white;");
				} else if (states[col][r] == 0)
					p.setStyle("-fx-background-color: black;");

			}
		}

	}

	public static void main(String[] args) {
		launch(args);

	}

	@Override
	public void handle(ActionEvent arg0) {

		if (arg0.getSource() == btnStart) {

			oneSecond = new Timeline(new KeyFrame(Duration.millis(250), new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {

					// check if all cells are dead
					boolean dead = true;

					for (int col = 0; col < states.length; col++) {
						for (int r = 0; r < states[col].length; r++) {
							if (states[col][r] == 1)
								dead = false;
						}
					}

					if (dead) {
						btnStop.fire();
						btnRestart.fire();
						genCount.setText("DEAD");
						genCount.setStyle("-fx-text-fill: red");
					}

					checkNeighbours(paneGame);

					if (noChange && !dead) {
						btnStop.fire();
						genCount.setText("unchanged");
					}

					setNextGeneration(paneGame);
					i++;
					if (!noChange && !dead)
						genCount.setText("Generation: " + i);
				}
			}));
			oneSecond.setCycleCount(Timeline.INDEFINITE);
			oneSecond.play();
		}

//		if (arg0.getSource() == btnStop) {
//			oneSecond.stop();
//		}

		if (arg0.getSource() == btnStep) {
			Task<Void> task = new Task<Void>() {

				@Override
				protected Void call() throws Exception {
					return null;
				}

				@Override
				public void run() {
					try {

						checkNeighbours(paneGame);
						setNextGeneration(paneGame);

					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

			};

			Thread bt = new Thread(task);
			bt.setDaemon(true);
			bt.run();
		}

		if (arg0.getSource() == btnRestart) {

			// fill states with zeros
			for (int col = 0; col < states.length; col++) {
				for (int r = 0; r < states[col].length; r++) {
					states[col][r] = 0;
				}
			}

			// fill nextGeneration with zeros
			for (int col = 0; col < statesNextGen.length; col++) {
				for (int r = 0; r < statesNextGen[col].length; r++) {
					statesNextGen[col][r] = 0;
				}
			}

			setNextGeneration(paneGame);
			i = 0;
			genCount.setStyle("-fx-text-fill: white");
			genCount.setText("Generation: " + i);
			noChange = false;

		}

		if (arg0.getSource() == btnExit) {

			Platform.exit();

		}

	}

}
