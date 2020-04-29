package application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

public class Graph extends DisplayMode {

  DataManager dm;
  String[] timeLabels;
  private boolean slidersVisible;
  VBox settings;
  Slider sliderStart;
  Slider sliderEnd;
  LineChart<String, Number> chart;
  CategoryAxis xAxis;
  NumberAxis yAxis;
  XYChart.Series<String, Number> series;
  String scopeName, dataName;
  private final String[] SCOPE_NAMES = {"Global", "Country", "State", "City"};
  private final String[] DATA_NAMES = {"Confirmed", "Dead", "Recovered"};

  Graph() {
    super();
    dm = new DataManager();
    try {
      dm.loadTries();
    } catch (Exception e) {
      e.printStackTrace();
    }
    timeLabels = dm.getTimeLabels();
    slidersVisible = true;
    setupSettings();

    scopeName = SCOPE_NAMES[0];
    dataName = DATA_NAMES[0];
    xAxis = new CategoryAxis();
    yAxis = new NumberAxis();
    xAxis.setAnimated(false);
    yAxis.setAnimated(false);
    chart = new LineChart<String, Number>(xAxis, yAxis);
    chart.setAnimated(false);
    series = new XYChart.Series<String, Number>();
    updateChart();
    chart.getData().add(series);
  }

  @Override
  void reset() {

  }

  @Override
  public Node getDisplayPane() {
    return chart;
  }

  private void updateChart() {
    xAxis.setLabel("Date");
    yAxis.setLabel("Number of " + dataName);
    chart.setTitle(dataName + " cases, " + scopeName);

    List<DataPoint> list = dm.gt.getAll();
    DataPoint d = list.get(0);

    Collection<XYChart.Data<String, Number>> col = new ArrayList<>();

    for (int time = (int) sliderStart.getValue(); time < (int) sliderEnd.getValue(); time++) {
      col.add(new XYChart.Data<String, Number>(timeLabels[time], d.confirmedList.get(time)));
      time++;
    }
    series.getData().setAll(col);
  }

  @Override
  public Node getSettingsPane() {
    return settings;
  }

  private void setupSettings() {
    settings = new VBox();
    Button time = new Button("Time Range");

    // setup time range slider and label
    Label sliderLabel = new Label("Choose Time Range:");
    sliderStart = new Slider(0, 94, 0);
    sliderEnd = new Slider(0, 94, 94);
    Label range = new Label("Time Range: " + timeLabels[(int) sliderStart.getValue()] + " to "
        + timeLabels[(int) sliderEnd.getValue()]);

    sliderStart.setShowTickLabels(true);
    sliderStart.setShowTickMarks(true);
    sliderStart.setBlockIncrement(10);
    sliderStart.setSnapToTicks(true);
    sliderEnd.setShowTickLabels(true);
    sliderEnd.setShowTickMarks(true);
    sliderEnd.setBlockIncrement(10);
    sliderEnd.setSnapToTicks(true);
    sliderLabel.managedProperty().bind(sliderLabel.visibleProperty());
    sliderStart.managedProperty().bind(sliderStart.visibleProperty());
    sliderEnd.managedProperty().bind(sliderEnd.visibleProperty());
    range.managedProperty().bind(range.visibleProperty());
    sliderStart.setVisible(slidersVisible);
    sliderEnd.setVisible(slidersVisible);

    time.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        if (slidersVisible) {
          sliderLabel.setVisible(false);
          sliderStart.setVisible(false);
          sliderEnd.setVisible(false);
          range.setVisible(false);
          slidersVisible = false;
        } else {
          sliderLabel.setVisible(true);
          sliderStart.setVisible(true);
          sliderEnd.setVisible(true);
          range.setVisible(true);
          slidersVisible = true;
        }
      }
    });
    final ChangeListener<Number> startListener = new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
        if (sliderStart.getValue() >= sliderEnd.getValue()) {
          sliderEnd.setValue(sliderStart.getValue() + 1);
        } else if (sliderEnd.getValue() <= sliderStart.getValue()) {
          sliderEnd.setValue(sliderStart.getValue() + 1);
        }
        range.setText("Time Range: " + timeLabels[(int) sliderStart.getValue()] + " to "
            + timeLabels[(int) sliderEnd.getValue()]);
        updateChart();
      }
    };
    final ChangeListener<Number> endListener = new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
        if (sliderStart.getValue() >= sliderEnd.getValue()) {
          sliderStart.setValue(sliderEnd.getValue() - 1);
        } else if (sliderEnd.getValue() <= sliderStart.getValue()) {
          sliderStart.setValue(sliderEnd.getValue() - 1);
        }
        range.setText("Time Range: " + timeLabels[(int) sliderStart.getValue()] + " to "
            + timeLabels[(int) sliderEnd.getValue()]);
        updateChart();
      }
    };
    sliderStart.valueProperty().addListener(startListener);
    sliderEnd.valueProperty().addListener(endListener);

    Label scopeLabel = new Label("Scope:");
    final ToggleGroup scope = new ToggleGroup();
    RadioButton gl = new RadioButton("Global");
    RadioButton cn = new RadioButton("Country");
    RadioButton st = new RadioButton("State");
    RadioButton ct = new RadioButton("City");
    gl.setToggleGroup(scope);
    cn.setToggleGroup(scope);
    st.setToggleGroup(scope);
    ct.setToggleGroup(scope);
    gl.setSelected(true);

    scope.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
      public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle,
          Toggle new_toggle) {
        if (scope.getSelectedToggle() != null) {
          if (((Labeled) scope.getSelectedToggle()).getText().equals(SCOPE_NAMES[0])) {
            scopeName = SCOPE_NAMES[0];
          }
          if (((Labeled) scope.getSelectedToggle()).getText().equals(SCOPE_NAMES[1])) {
            scopeName = SCOPE_NAMES[1];
          }
          if (((Labeled) scope.getSelectedToggle()).getText().equals(SCOPE_NAMES[2])) {
            scopeName = SCOPE_NAMES[2];
          }
          if (((Labeled) scope.getSelectedToggle()).getText().equals(SCOPE_NAMES[3])) {
            scopeName = SCOPE_NAMES[3];
          }
        }
        updateChart();
      }
    });

    final ToggleGroup data = new ToggleGroup();
    Label dataLabel = new Label("Data:");
    RadioButton c = new RadioButton("Confirmed");
    RadioButton d = new RadioButton("Dead");
    RadioButton r = new RadioButton("Recovered");
    c.setToggleGroup(data);
    d.setToggleGroup(data);
    r.setToggleGroup(data);
    c.setSelected(true);

    data.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

      public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle,
          Toggle new_toggle) {
        if (data.getSelectedToggle() != null) {
          if (((Labeled) data.getSelectedToggle()).getText().equals(DATA_NAMES[0]))
            dataName = DATA_NAMES[0];
          if (((Labeled) data.getSelectedToggle()).getText().equals(DATA_NAMES[1]))
            dataName = DATA_NAMES[1];
          if (((Labeled) data.getSelectedToggle()).getText().equals(DATA_NAMES[2]))
            dataName = DATA_NAMES[2];
        }
        updateChart();
      }
    });

    settings.getChildren().addAll(time, sliderLabel, sliderStart, sliderEnd, range, scopeLabel, gl,
        cn, st, ct, dataLabel, c, d, r);
  }

}
