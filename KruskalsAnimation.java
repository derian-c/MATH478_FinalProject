import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

public class KruskalsAnimation extends Application {
    private final List<Circle> vertices = new ArrayList<>();
    private final List<Line> edgesToDraw = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Map<Circle, Circle> parent = new HashMap<>();
    private final Map<Circle, Integer> height = new HashMap<>();
    private int currEdgeDraw = 0;
    private int frames = 30;
    private boolean paused = false;
    private boolean slowingDown = false;
    private int edgeFrames = 0;
    private int currEdge = 0;
    private int numVertices = 0;
    private double diameter = 20;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Get screen size
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        Pane pane = new Pane();
        Scene scene = new Scene(pane, screenBounds.getWidth(), screenBounds.getHeight()-100);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Kruskal's Animation");
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight()-100);
        primaryStage.show();
        String filename = promptUser("Enter file name (don't enter anything if random vertices will be used):");
        numVertices = 0;
        if(filename.isEmpty()){
            numVertices = Integer.parseInt(promptUser("Enter number of vertices:"));
        }
        frames = Integer.parseInt(promptUser("Enter number of frames per edge drawing (program tries to run at 60 frames per second):"));
        if(!filename.isEmpty())
            parseFile(filename,screenBounds.getWidth(),screenBounds.getHeight()-100);
        runKruskals(pane, numVertices,filename.isEmpty());

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1000.0 / 60), e -> draw(pane)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        final int nVertices = numVertices;
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case SPACE:
                    paused = !paused;
                    break;
                case N: 
                    if(filename.isEmpty())
                        runKruskals(pane, nVertices, true);
                    break;
                case Q:
                    edgeFrames = 0;
                    currEdgeDraw = 0;
                    break;
                case E:
                    currEdgeDraw = edgesToDraw.size();
                    edgeFrames = 0;
                    break;
            }
        });
    }

    private void parseFile(String filename, double width, double height_){
        File file = new File(filename);
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        try{
            Scanner filescan = new Scanner(file);
            while(filescan.hasNextLine()){
                String line = filescan.nextLine();
                String[] inputs = line.split("[,]");
                double x = Double.parseDouble(inputs[0]);
                double y = Double.parseDouble(inputs[1]);
                Circle vertex = new Circle(x,y, diameter/2, Color.BLUE);
                vertices.add(vertex);
                parent.put(vertex, vertex);
                height.put(vertex, 1);
                if(x > maxX) maxX = x;
                if(x < minX) minX = x;
                if(y > maxY) maxY = y;
                if(y < minY) minY = y;
            }
        }catch(Exception e){
            throw new RuntimeException("File not found");
        }
        maxX -= minX;
        maxY -= minY;
        double shiftFactor = 1;
        if(maxY == 0){
            if(maxX == 0){
                minX = -width;
                minY = -height_;
            }else{
                shiftFactor = width/maxX;
            }
        }else if(maxX == 0){
            if(maxY == 0){
                minX = -width;
                minY = -height_;
            }else{
                shiftFactor = height_/maxY;
            }
        }else{
            shiftFactor = maxX/maxY > width/height_ ? width/maxX : height_/maxY;
        }
        for(Circle circ: vertices){
            double x = circ.getCenterX();
            double y = circ.getCenterY();
            circ.setCenterX((x-minX)*shiftFactor);
            circ.setCenterY((y-minY)*shiftFactor);
        }
        numVertices = vertices.size();
    }

    private void runKruskals(Pane pane, int numVertices, boolean random) {
        // Clear existing data
        pane.getChildren().clear();
        if(random){
            vertices.clear();
            parent.clear();
            height.clear();
        }
        edges.clear();
        edgesToDraw.clear();
        edgeFrames = 0;
        currEdge = 0;
        currEdgeDraw = 0;
        paused = false;
        diameter = pane.getWidth() / (15 * Math.log(numVertices + 1));

        // Create vertices
        if(random){
            Random rand = new Random();
            for (int i = 0; i < numVertices; i++) {
                double x = diameter / 2 + rand.nextDouble() * (pane.getWidth() - diameter);
                double y = diameter / 2 + rand.nextDouble() * (pane.getHeight() - diameter);
                Circle vertex = new Circle(x, y, diameter / 2, Color.BLUE);
                vertices.add(vertex);
                parent.put(vertex, vertex);
                height.put(vertex, 1);
            }
        }

        // Create edges
        for (int i = 0; i < numVertices; i++) {
            for (int j = i + 1; j < numVertices; j++) {
                Circle v1 = vertices.get(i);
                Circle v2 = vertices.get(j);
                double distance = Math.sqrt(Math.pow(v1.getCenterX() - v2.getCenterX(), 2) + Math.pow(v1.getCenterY() - v2.getCenterY(), 2));
                edges.add(new Edge(v1, v2, distance));
            }
        }
        edges.sort(Comparator.comparingDouble(e -> e.weight));

        // Run Kruskal's Algorithm
        while (edgesToDraw.size() < numVertices - 1) {
            Edge edge = edges.get(currEdge++);
            Circle v1 = edge.vertex1;
            Circle v2 = edge.vertex2;
            if (find(v1) != find(v2)) {
                union(v1, v2);
                edgesToDraw.add(new Line(v1.getCenterX(), v1.getCenterY(), v2.getCenterX(), v2.getCenterY()));
            }
        }

        // Add lines to the pane first
        for (Line line : edgesToDraw) {
            pane.getChildren().add(line);
        }

        // Add vertices to the pane
        for (Circle vertex : vertices) {
            pane.getChildren().add(vertex);
        }
    }

    private Circle find(Circle vertex) {
        if (parent.get(vertex) != vertex) {
            parent.put(vertex, find(parent.get(vertex)));
        }
        return parent.get(vertex);
    }

    private void union(Circle v1, Circle v2) {
        Circle root1 = find(v1);
        Circle root2 = find(v2);

        if (root1 != root2) {
            if (height.get(root1) < height.get(root2)) {
                parent.put(root1, root2);
            } else if (height.get(root1) > height.get(root2)) {
                parent.put(root2, root1);
            } else {
                parent.put(root2, root1);
                height.put(root1, height.get(root1) + 1);
            }
        }
    }

    private void draw(Pane pane) {
        if (!paused && currEdgeDraw < edgesToDraw.size()) {
            edgeFrames++;
            if (edgeFrames >= frames) {
                edgeFrames = 0;
                currEdgeDraw++;
            }
        }

        pane.getChildren().removeIf(node -> node instanceof Line);

        for (int i = 0; i < currEdgeDraw; i++) {
            Line line = edgesToDraw.get(i);
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(diameter/7);
            pane.getChildren().add(0, line);
        }

        if (currEdgeDraw < edgesToDraw.size()) {
            Line currentLine = edgesToDraw.get(currEdgeDraw);
            double alpha = (double) edgeFrames / frames;
            double x1 = currentLine.getStartX();
            double y1 = currentLine.getStartY();
            double x2 = currentLine.getEndX();
            double y2 = currentLine.getEndY();
            double x = x1 * (1 - alpha) + x2 * alpha;
            double y = y1 * (1 - alpha) + y2 * alpha;

            Line partialLine = new Line(x1, y1, x, y);
            partialLine.setStroke(Color.GREEN);
            partialLine.setStrokeWidth(diameter/7);
            pane.getChildren().add(0, partialLine);
            for (Circle vertex : vertices) {
                vertex.setFill(Color.BLUE);
            }
            Circle v1 = null, v2 = null;
            for (Circle vertex : vertices) {
                if (vertex.getCenterX() == x1 && vertex.getCenterY() == y1) {
                    v1 = vertex;
                }
                if (vertex.getCenterX() == x2 && vertex.getCenterY() == y2) {
                    v2 = vertex;
                }
            }
            if (v1 != null && v2 != null) {
                v1.setFill(Color.RED);
                v2.setFill(Color.RED);
            }
        }else{
            for (Circle vertex : vertices) {
                vertex.setFill(Color.BLUE);
            }
        }
    }

    private String promptUser(String message) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        return dialog.showAndWait().orElse("");
    }

    class Edge {
        Circle vertex1, vertex2;
        double weight;

        Edge(Circle vertex1, Circle vertex2, double weight) {
            this.vertex1 = vertex1;
            this.vertex2 = vertex2;
            this.weight = weight;
        }
    }
}
