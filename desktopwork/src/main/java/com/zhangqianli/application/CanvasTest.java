package com.zhangqianli.application;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class CanvasTest extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Stage stage = new Stage();
		Group root = new Group();
		Scene scene = new Scene(root, 700, 700,Color.ALICEBLUE);
		Canvas canvas = new Canvas(300, 300);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.BLUE);
		stage.setScene(scene);
		root.getChildren().add(canvas);
		Circle circle = new Circle(50, Color.AQUAMARINE);
		Circle center = new Circle(5,Color.RED);
		circle.setCenterX(350);
		circle.setCenterY(350);
		center.setCenterX(circle.getCenterX());
		center.setCenterY(circle.getCenterY());
		circle.setOnMouseDragged(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				// TODO Auto-generated method stub
				circle.setCenterX(event.getSceneX());
				circle.setCenterY(event.getSceneY());
				center.setCenterX(event.getSceneX());
				center.setCenterY(event.getSceneY());
			}
		});
		root.getChildren().addAll(circle,center);
		stage.show();
		
	}
}
