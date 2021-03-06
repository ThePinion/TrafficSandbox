package pl.tcs.po.models;

import pl.tcs.po.models.blocks.*;
import pl.tcs.po.models.mobile.Vector2;
import pl.tcs.po.models.mobile.Vehicle;

import java.util.*;

public class Board {

    private Block[][] blocks;
    private final int width;
    private final int height;

    public Board(int width, int height){
        this.width = width;
        this.height = height;
        blocks = new Block[width][height];
        fillBlocksEmpty();
        //setBlocksRandom();
    }

    public Vector2 cellPosition(int column, int row) {
        return new Vector2(column * Block.getDimensions().width(), row * Block.getDimensions().height());
    }

    private void fillBlocksEmpty(){
        for(int c=0;c<width;c++){
            for(int r=0;r<height;r++)blocks[c][r] = new EmptyBlock(cellPosition(c, r));
        }
    }

    public Block getBlock(int column, int row){return blocks[column][row];}

    private int getTargetIndex( Block target, int sourceIndex){
        return (10 + sourceIndex - target.getRotation().index)%4;
    }

    private Block getNeighbourBlock(int myColumn, int myRow, int index){
        int c = switch (index){
            case 1 -> 1;
            case 3 -> -1;
            default -> 0;
        };
        int r = switch (index){
            case 0 -> -1;
            case 2 -> 1;
            default -> 0;
        };
        return blocks[myColumn + c][myRow + r];
    }

    public void setBlock(int column, int row, Block block){
        //System.out.println("New " + block.getName() + " Block: ["+column+", "+row+"]");
        blocks[column][row] = block;
        for(int i=0;i<4;i++){
            int directionIndex = (i + block.getRotation().index) % 4;
            Block target = getNeighbourBlock(column, row, directionIndex);
            int targetIndex = getTargetIndex(target, directionIndex);
            block.setOutConnection(new BlockConnection(block, i, target, targetIndex));
            target.setOutConnection(new BlockConnection(target, targetIndex, block, i));
        }
    }

    public int getWidth(){return width;}
    public int getHeight(){return height;}

    public ArrayList<BlockConnection> getPath(Block source, Block target){
        if(source.equals(target))return new ArrayList<>();

        HashMap<Block, BlockConnection> visited = new HashMap<>();
        Queue<Block> queue = new LinkedList<>();


        queue.add(source);

        boolean pathFound = false;

        while (!queue.isEmpty()){
            Block current = queue.remove();
            if(current.equals(target)){
                pathFound = true;
                break;
            }
            for(BlockConnection connection : current.getOutConnections()){
                if(connection == null) continue;
                if(visited.containsKey( connection.target))continue;
                visited.put(connection.target, connection);
                queue.add(connection.target);
            }
        }

        if(!pathFound) return null;

        ArrayList<BlockConnection> output = new ArrayList<>();

        var current = visited.get(target);

        while(current.source != source){
            output.add(current);
            current = visited.get(current.source);
        }

        output.add(current);

        Collections.reverse(output);

        return output;
    }

    public ArrayList<BlockConnection> getPath(int sourceX, int sourceY, int targetX, int targetY){
        return getPath(blocks[sourceX][sourceY], blocks[targetX][targetY]);
    }

    public boolean correctCoords(int column, int row) {
        return column >= 0 && column < getWidth() && row >= 0 && row < getHeight();
    }

    public void update(long deltaTime){
        for(var row : blocks)for(var block : row)block.update(deltaTime);
    }

    public Collection<Vehicle> getVehicles(){
        List<Vehicle> vehicles = new LinkedList<>();
        for(var row : blocks)for(var block : row)vehicles.addAll(block.getVehicles());
        return vehicles;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(width).append("," + height + ",");
//        for(Block[] blockArr : blocks) {
//            for(Block b : blockArr) {
//                sb.append(b.toString());
//            }
//        }
        for(int i = 0; i < getWidth(); i++) {
            for(int j = 0; j < getHeight(); j++) {
                sb.append(blocks[i][j].toString());
            }
        }
        return sb.toString();
    }
}
