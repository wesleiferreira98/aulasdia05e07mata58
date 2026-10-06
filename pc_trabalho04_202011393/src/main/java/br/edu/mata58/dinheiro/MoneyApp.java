package br.edu.mata58.dinheiro;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

/** A interface apenas renderiza snapshots; workers nunca acessam componentes JavaFX. */
public final class MoneyApp extends Application {
    private final Simulation simulation = new Simulation();
    private final Label[] slots = new Label[10];
    private final Label summary = new Label(), production = new Label(), consumption = new Label();
    private final Label counters = new Label(), stateLabel = new Label();
    private final TextArea log = new TextArea();
    private final ProgressBar occupancy = new ProgressBar(0);
    private final Button start = new Button("Iniciar"), pause = new Button("Pausar");
    private Timeline timeline;

    @Override public void start(Stage stage) {
        Label title = new Label("Produção de dinheiro");
        title.getStyleClass().add("title");
        Label subtitle = new Label("Produtor e consumidor • dez posições • exclusão mútua e disponibilidade");
        subtitle.setWrapText(true);
        HBox header = new HBox(20, title, stateLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12);
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new Label();
            slots[i].setMinSize(150, 105);
            slots[i].setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            slots[i].setAlignment(Pos.CENTER);
            grid.add(slots[i], i % 5, i / 5);
            GridPane.setHgrow(slots[i], Priority.ALWAYS);
        }
        occupancy.setMaxWidth(Double.MAX_VALUE);
        VBox bufferCard = new VBox(14, new Label("BUFFER CIRCULAR: NOTAS EM ORDEM FIFO"),
                summary, occupancy, grid, counters);
        bufferCard.getStyleClass().add("card");
        Label explanation = new Label("Vagas permitem produzir. Itens permitem consumir. "
                + "O mutex protege a inserção e a retirada. W indica a próxima escrita; R, a próxima leitura.");
        explanation.setWrapText(true);
        VBox rates = new VBox(12, rateControl("Banco / produtor", true), production,
                rateControl("Carro-forte / consumidor", false), consumption, explanation);
        rates.getStyleClass().add("card");
        start.setOnAction(event -> { simulation.start(); refresh(); });
        pause.setOnAction(event -> { simulation.pause(); refresh(); });
        Button reset = new Button("Reiniciar");
        reset.setOnAction(event -> { simulation.reset(); refresh(); });
        HBox buttons = new HBox(12, start, pause, reset);
        Label challenge = new Label("Experimente: produtor mais rápido → buffer cheio; "
                + "consumidor mais rápido → buffer vazio. Quem precisa esperar em cada caso?");
        challenge.setWrapText(true);
        log.setEditable(false); log.setPrefRowCount(6); log.setFocusTraversable(false);
        VBox content = new VBox(16, header, subtitle, bufferCard, rates, buttons, challenge,
                new Label("Últimos eventos (mais recente primeiro)"), log);
        content.setPadding(new Insets(24));
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        Scene scene = new Scene(scroll, 1000, 900);
        scene.getStylesheets().add(getClass().getResource("/simulation.css").toExternalForm());
        stage.setTitle("MATA58 · Produtor e consumidor | Weslei Ferreira Santos");
        stage.setScene(scene); stage.setMinWidth(950); stage.setMinHeight(650);
        timeline = new Timeline(new KeyFrame(Duration.millis(100), event -> refresh()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play(); refresh(); stage.show();
    }

    private final Slider producerRate = slider(), consumerRate = slider();
    private static Slider slider() {
        Slider slider = new Slider(0.2, 10, 2);
        slider.setShowTickLabels(true); slider.setShowTickMarks(true);
        slider.setMajorTickUnit(2); slider.setBlockIncrement(0.2);
        slider.setMaxWidth(Double.MAX_VALUE);
        return slider;
    }

    private HBox rateControl(String name, boolean producer) {
        Slider slider = producer ? producerRate : consumerRate;
        Label value = new Label(); value.setMinWidth(100);
        Runnable update = () -> {
            value.setText(String.format("%.1f itens/s", slider.getValue()));
            simulation.rates(producerRate.getValue(), consumerRate.getValue());
        };
        slider.valueProperty().addListener((observable, oldValue, newValue) -> update.run());
        update.run();
        Label label = new Label(name); label.setMinWidth(205);
        HBox row = new HBox(16, label, slider, value);
        row.setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(slider, Priority.ALWAYS);
        return row;
    }

    private void refresh() {
        Simulation.View view = simulation.view();
        BoundedBuffer.Snapshot buffer = view.buffer();
        String state = switch (view.state()) {
            case READY -> "Pronto"; case RUNNING -> "Em execução";
            case PAUSED -> "Pausado"; case CLOSED -> "Encerrado";
        };
        stateLabel.setText(state);
        start.setText(view.state() == Simulation.State.PAUSED ? "Continuar" : "Iniciar");
        start.setDisable(view.state() == Simulation.State.RUNNING);
        pause.setDisable(view.state() != Simulation.State.RUNNING);
        summary.setText(buffer.size() + " itens disponíveis   |   " + (10 - buffer.size()) + " vagas livres");
        occupancy.setProgress(buffer.size() / 10.0);
        counters.setText("Produzidas: " + buffer.produced() + "   Consumidas: " + buffer.consumed());
        production.setText("Produtor: " + view.producerStatus());
        consumption.setText("Consumidor: " + view.consumerStatus());
        for (int i = 0; i < slots.length; i++) {
            int item = buffer.slots().get(i);
            String pointers = (i == buffer.nextWrite() ? " W" : "") + (i == buffer.nextRead() ? " R" : "");
            slots[i].setText("Posição " + i + pointers + "\n\n" + (item == 0 ? "Vazia" : "Nota #" + item));
            slots[i].getStyleClass().setAll("slot", item == 0 ? "empty" : "occupied");
        }
        String text = String.join("\n", view.events());
        if (!text.equals(log.getText())) log.setText(text);
    }

    @Override public void stop() {
        if (timeline != null) timeline.stop();
        simulation.close();
    }
    public static void main(String[] args) { launch(args); }
}
