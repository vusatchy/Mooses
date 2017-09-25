

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MoosesHighlighter {

    class Square implements  Comparable{
        int maxX;
        int minX;
        int maxY;
        int minY;

        public Square(int maxX, int minX, int maxY, int minY) {
            this.maxX = maxX;
            this.minX = minX;
            this.maxY = maxY;
            this.minY = minY;
        }

        public int calculateArea() {
            return (maxX-minX)*(maxY-minY);
        }

        public boolean intersect(int x,int y){
            boolean result=false;
            if(x>=minX&&x<=maxX&&y<=maxY&&y>=minY){
                return true;
            }
            return result;
        }



        @Override
        public String toString() {
            return "Square{" +
                    "maxX=" + maxX +
                    ", minX=" + minX +
                    ", maxY=" + maxY +
                    ", minY=" + minY +
                    "} area= "+calculateArea();
        }

        @Override
        public int compareTo(Object o) {
            return calculateArea()-((Square) o).calculateArea();
        }
    }


    private BufferedImage img ;


    private int minX;
    private int minY;

    private int maxX;
    private int maxY;

    private List<Square> squares=null;

    private boolean [][] binaryMatrixOfMoosesPixels;
    private boolean [][] storyMatrix;

    public MoosesHighlighter(String path){
        try {
            img = ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        squares=new ArrayList<Square>();
        binaryMatrixOfMoosesPixels =new boolean[img.getWidth()][img.getHeight()];
        storyMatrix = new boolean[img.getWidth()][img.getHeight()];
        initMatrixes();
    }

    private void initMatrixes() {
        for(int x=0;x<img.getWidth();x++){
            for(int y=0;y<img.getHeight();y++) {
                if(img.getRGB(x,y)==-1){
                    binaryMatrixOfMoosesPixels[x][y]=false;
                }
                else binaryMatrixOfMoosesPixels[x][y]=true;

                storyMatrix[x][y]=false;
            }
        }

    }

    public void searchMooses(){
        for(int x=0;x<img.getWidth();x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                if (binaryMatrixOfMoosesPixels[x][y]) {
                    if (squares.isEmpty()) {
                        resetCounters();
                        search(x, y);
                        squares.add(new Square(maxX, minX, maxY, minY));
                    } else {
                        boolean allow = true;
                        for (Square sftp : squares) {
                            if (sftp.intersect(x, y)) {
                                allow = false;
                            }
                        }
                        if (allow) {
                            resetCounters();
                            search(x, y);
                            squares.add(new Square(maxX, minX, maxY, minY));
                        }
                    }
                }
            }
        }

        }

    private int classificationFilter() {
        int maxDiff=0;
        int filter=0;
        for (int i=1;i<squares.size();i++) {
            int temp = Math.abs(squares.get(i - 1).calculateArea() - squares.get(i).calculateArea());
            if (maxDiff < temp) {
                maxDiff = temp;
                filter = squares.get(i - 1).calculateArea();
            }
        }
        return filter;
    }


    public void drawNewPicture() throws IOException {
        squares=squares.stream().sorted().collect(Collectors.toList());
        squares=squares.stream().filter(x->x.calculateArea()>classificationFilter()).collect(Collectors.toList());
        Graphics graphics = img.getGraphics();
        graphics.setColor(new Color(1));
        for ( Square square: squares
             ) {

            graphics.drawRect(square.minX,square.minY,square.maxX-square.minX,square.maxY-square.minY);
        }
        ImageIO.write(img, "PNG", new File("result.png"));
    }


    //looking for square in which figures could be inscribed
    private void search(int x,int y){
        if(x>=0&&x<img.getWidth()&&y>=0&&y<img.getHeight()){//ArrayOutOfIndexException possible
            if(binaryMatrixOfMoosesPixels[x][y]) { //is black
                if (!storyMatrix[x][y]) { // and wasn't written at storyMatrix yet
                    if (binaryMatrixOfMoosesPixels[x][y]) {
                        if (x > maxX) {
                            maxX = x;
                        }
                        if (x < minX) {
                            minX = x;
                        }
                        if (y > maxY) {
                            maxY = y;
                        }
                        if (y < minY) {
                            minY = y;
                        }
                    }
                    storyMatrix[x][y] = true;
                    search(x,y+1);//up
                    search(x,y-1);//down
                    search(x+1,y);//right
                    search(x-1,y);//left
                }
            }
        }
        else return;
    }

    public void resetCounters(){
        minX=img.getHeight();
        maxX=0;

        maxY=0;
        minY=img.getHeight();
    }
}
