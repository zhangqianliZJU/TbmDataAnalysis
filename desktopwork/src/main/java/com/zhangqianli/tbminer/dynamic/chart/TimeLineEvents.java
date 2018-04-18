package com.zhangqianli.tbminer.dynamic.chart;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.animation.Timeline;
import javafx.scene.text.Text;
import javafx.util.Duration;


public class TimeLineEvents extends Application {

    private Timeline timeLine;
    private AnimationTimer timer;
    private Integer i = 0;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Group p = new Group();
        Scene scene = new Scene(p);
        stage.setScene(scene);
        stage.setWidth(500);
        stage.setHeight(500);
        p.setTranslateX(80);
        p.setTranslateY(80);
        //create a circle with effect
        final Circle circle = new Circle(20, Color.rgb(156, 216, 255));
        circle.setEffect(new Lighting());
        //create a text inside a circle
        final Text text = new Text(i.toString());
        text.setStroke(Color.BLACK);
         //create a layout for circle with text inside
        final StackPane stack = new StackPane();
        stack.getChildren().addAll(circle,text);
        stack.setLayoutX(30);
        stack.setLayoutY(30);
        p.getChildren().add(stack);
        stage.show();
        //create a timeline for moving a circle
        timeLine = new Timeline();
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.setAutoReverse(true);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                text.setText(i.toString());
                i++;
            }
        };
//create a keyValue with factory:scaling the circle 2 times
        KeyValue keyValueX = new KeyValue(stack.scaleXProperty(), 2);
        KeyValue keyValueY = new KeyValue(stack.scaleYProperty(), 2);
        //create a keyFrame,the keyValue is reached at time 2s
        Duration duration = Duration.millis(2000);

        EventHandler<ActionEvent> onFinished = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stack.setTranslateX(Math.random()*200-100);
                i=0;
            }
        };
        KeyFrame keyFrame = new KeyFrame(duration,onFinished,keyValueX,keyValueY);
        timeLine.getKeyFrames().add(keyFrame);
        timeLine.play();
        timer.start();
    }
}
