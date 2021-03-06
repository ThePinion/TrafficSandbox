package pl.tcs.po.controllers;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import pl.tcs.po.models.Board;
import pl.tcs.po.models.blocks.Block;
import pl.tcs.po.models.blocks.BlockCreator;
import pl.tcs.po.models.blocks.BlockFactory;
import pl.tcs.po.models.blocks.Rotation;
import pl.tcs.po.views.BlockView;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class BoardController {

    private Board board;
    private GridPane grid = new GridPane();

    BlockCreator currentBlock = BlockCreator.EMPTY;
    Rotation currentRotation = Rotation.NORTH;
    Block previousBlock = BlockCreator.EMPTY.getNewBlock(null, currentRotation);
    boolean dragDetected = false;

    StackPane mainPane = new StackPane();
    VehicleCanvas vehicleCanvas;
    Canvas canvas;

    Text currentText = new Text();

    private int previousColumn = -1;
    private int previousRow = -1;

    private volatile boolean isRunning = false;
    private Thread simulationThread;

    public BoardController(Board board){
        this.board = board;
        setConstraints(grid);
        renderBlocks();
        mainPane.getChildren().add(grid);
        vehicleCanvas = new VehicleCanvas(board.getWidth() -2 , board.getHeight() - 2);
        vehicleCanvas.setOffset(Block.getDimensions().opposite());
        canvas = vehicleCanvas.canvas;
    }

    public Parent getParentView(){
        return mainPane;
    }

    private void setConstraints(GridPane grid) {
        setRowConstraints(grid);
        setColumnConstraints(grid);
    }

    private void renderBlocks(int fromColumn, int toColumn, int fromRow, int toRow) {
        for(int c=fromColumn;c<toColumn;c++){
            for(int r=fromRow;r<toRow;r++) grid.add(getBlockViewNode(c, r), c, r);
        }
    }

    public void updateBlock(int column, int row){
        if(!board.correctCoords(column, row)) {
            return;
        }
        grid.add(getBlockViewNode(column, row), column, row);
    }

    private void setColumnConstraints(GridPane grid) {
        for(int c=0;c< board.getWidth();c++){
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setMaxWidth(99);
            grid.getColumnConstraints().add(constraints);
        }
    }

    private void setRowConstraints(GridPane grid) {
        for(int r=0;r< board.getHeight();r++){
            RowConstraints constraints = new RowConstraints();
            constraints.setMaxHeight(99);
            grid.getRowConstraints().add(constraints);
        }
    }

    private Node getBlockViewNode(int column, int row){
        Node node = new BlockView(board.getBlock(column, row)).getNode();
        node.setOnMouseClicked(e -> {
            if(dragDetected) {
                dragDetected = false;
                return;
            }

            if(e.getButton().equals(MouseButton.PRIMARY)){
                board.setBlock(column, row, currentBlock.getNewBlock(board.cellPosition(column, row), currentRotation));
                updateBlock(column, row);
                System.out.println(board.getPath(1,1,column,row));
            }

            if(e.getButton().equals(MouseButton.SECONDARY)) {
                Block blockToRotate = board.getBlock(column, row);

                /**
                 * TODO: this should call blockToRotate.getRotated()
                 * so that we do not have a switch statement.
                 * Block should store its type, so that it knows, which type to return
                 */
                board.setBlock(column, row, BlockFactory.getRotated(blockToRotate));
                updateBlock(column, row);

                currentRotation = board.getBlock(column, row).getRotation();

                System.out.println(board.getPath(1,1,column,row));
            }

            previousBlock = board.getBlock(column, row);
        });

        node.setOnMouseEntered(e -> {
            if(enteredNewBlock(column, row)) {

                updatePreviousBlock();

                Block blockToFade = board.getBlock(column, row);
                /**
                 * TODO: this should call blockToFade.getFaded()
                 * so that we do not have a switch statement.
                 * Block should store its type, so that it knows, which type to return
                 */
                previousBlock = blockToFade;
                board.setBlock(column, row, BlockFactory.getFaded(currentBlock.getNewBlock(board.cellPosition(column, row), currentRotation)));

                updateBlock(column, row);
            }
        });

        node.setOnMouseExited(e -> {
            updatePreviousCoordinates(column, row);

            board.setBlock(column, row, previousBlock);
            //updateBlock(column, row) is called when mouse enters next block. This is necessary, because updateBlock makes mouse exit and enter again
        });

        return node;
    }

    private void updatePreviousCoordinates(int column, int row) {
        previousColumn = column;
        previousRow = row;
    }

    private boolean enteredNewBlock(int newColumn, int newRow) {
        return newColumn != previousColumn || newRow != previousRow;
    }

    public void toggleSimulation(){
        if(isRunning) stopSimulation();
        else startSimulation();
    }

    void startSimulation() {
        if (isRunning) throw new RuntimeException();
        isRunning = true;
        mainPane.getChildren().add(canvas);
        simulationThread = new Thread(this::simulation);
        simulationThread.start();
    }

    void simulation() {
        long prevFrameTimestamp = new Date().getTime();
        while(isRunning){

            long curTimestamp = new Date().getTime();
            long deltaTime = curTimestamp-prevFrameTimestamp;
            //System.out.println(curTimestamp + " - " + prevFrameTimestamp + " = " + deltaTime);
            board.update(deltaTime);

            vehicleCanvas.reset();
            for(var vehicle : board.getVehicles()){
                vehicleCanvas.drawVehicle(vehicle);
            }
            prevFrameTimestamp = curTimestamp;

            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void stopSimulation(){
        if(!isRunning) throw new RuntimeException();
        isRunning = false;
        mainPane.getChildren().remove(canvas);
    }

    void renderBlocks(){
        renderBlocks(1 ,board.getWidth()-1, 1, board.getHeight()-1);
    }

    public void updatePreviousBlock() {
        // does nothing if previous coords are not correct
        updateBlock(previousColumn, previousRow);
    }

    public void resetPrevious() {
        previousColumn = -1;
        previousRow = -1;
    }

    public void handleDragDetected() {
        dragDetected = true;
    }
}
