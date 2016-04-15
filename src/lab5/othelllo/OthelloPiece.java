package lab5.othelllo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.Serializable;
import java.util.Stack;

/**
 * Created by domien on 15/04/2016.
 */
public class OthelloPiece implements Serializable, ChangeListener<Number>
{
    private ObjectProperty<Integer> state = new SimpleObjectProperty<>(0);
    private StackPane cell = new StackPane();
    private Circle[] circles = { new Circle(25, Color.BLACK), new Circle(25,Color.GRAY), new Circle(25,Color.WHITE)};

    public OthelloPiece(){
        // intialisatie ...
        cell.setStyle("-fx-background-color: gray");
        cell.setPrefSize(50,50);
        cell.setMinSize(50,50);
        // default grijze cirkel
        cell.setOnMouseClicked((e) -> {
            if (this.getFoo() == -1) {
                this.setFoo(1);
            } else {
                this.setFoo(getFoo() - 1);
            }
        }
        );

    }

    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

    }

    public synchronized Integer getFoo(){
        return state.getValue();
    }

    public synchronized void setFoo(int newValue){
        state.setValue(newValue);
        cell.getChildren().clear();
        cell.getChildren().add(circles[newValue + 1]);
        cell.setAlignment(Pos.CENTER);
    }

    public ObjectProperty<Integer> fooProperty() {
        return state;
    }

    public Pane getContent() {
        return cell;
    }
}