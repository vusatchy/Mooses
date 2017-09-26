

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class MooseHighlighter {

    class Square implements Comparable {
        int maxX;
        int minX;
        int maxY;
        int minY;
        int realArea;

        public Square(int maxX, int minX, int maxY, int minY, int realArea) {
            this.maxX = maxX;
            this.minX = minX;
            this.maxY = maxY;
            this.minY = minY;
            this.realArea = realArea;
        }

        public int calculateSquareArea() {
            return (maxX - minX) * (maxY - minY);
        }

        public boolean intersect(int x, int y) {
            boolean result = false;
            if (x >= minX && x <= maxX && y <= maxY && y >= minY) {
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
                    ", realArea=" + realArea + " sq "+ calculateSquareArea()+
                    " cor "+getCorelation();
        }

        @Override
        public int compareTo(Object o) {
            return Double.compare(getCorelation(),((Square)o).getCorelation());
        }

        public double getCorelation(){
            return ((double)realArea/ calculateSquareArea());
        }
    }


    private BufferedImage img;


    private int minX;
    private int minY;

    int area;
    private final int BLACK = -16777216;
    private int maxX;
    private int maxY;

    private List<Square> squares = null;

    private boolean[][] binaryMatrixOfMoosesPixels;
    private boolean[][] storyMatrix;

    public MooseHighlighter(String path) {
        try {
            img = ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        squares = new ArrayList<Square>();
        binaryMatrixOfMoosesPixels = new boolean[img.getWidth()][img.getHeight()];
        storyMatrix = new boolean[img.getWidth()][img.getHeight()];
        initMatrixes();
    }

    private void initMatrixes() {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                if (img.getRGB(x, y) == BLACK) { //isWhite
                    binaryMatrixOfMoosesPixels[x][y] = true;
                } else binaryMatrixOfMoosesPixels[x][y] = false;

                storyMatrix[x][y] = false;
            }
        }

    }

    public void searchMoose() {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                if (binaryMatrixOfMoosesPixels[x][y]) {
                    if (squares.isEmpty()) {
                        resetCounters();
                        search(x, y);
                        storyMatrix[x][y]=true;
                        squares.add(new Square(maxX, minX, maxY, minY,area));
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
                            storyMatrix[x][y]=true;
                            squares.add(new Square(maxX, minX, maxY, minY,area));
                        }
                    }
                }
            }
        }

    }

    private double classificationFilter() {
        double maxDiff = 0;
        double filter = 0;
        for (int i = 1; i < squares.size(); i++) {
            double temp = Math.abs(squares.get(i-1).getCorelation()-squares.get(i).getCorelation());
            if (maxDiff < temp) {
                maxDiff = temp;
                filter = squares.get(i).getCorelation();
            }
        }
        return filter;
    }


    public void drawNewPicture() throws IOException {
        squares = squares.stream().filter(x->x.getCorelation()<1).sorted().collect(Collectors.toList());
        squares = squares.stream().filter(x -> x.getCorelation() < classificationFilter()).collect(Collectors.toList());
        Graphics graphics = img.getGraphics();
        graphics.setColor(new Color(Color.RED.getRGB()));
        for (Square square : squares
                ) {

            graphics.drawRect(square.minX, square.minY, square.maxX - square.minX, square.maxY - square.minY);
        }
        ImageIO.write(img, "PNG", new File("result.png"));
    }


    class Point {
        int x;
        int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return this.x;
        }

        public void setX(int x) {
            x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            y = y;
        }

        @Override
        public String toString() {
            return "Point{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }


    private boolean isValidPoint(int x, int y) {
        boolean result = false;
        if (x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight()) {
            result = true;
        }
        return result;
    }

    //counting area
    private void search(int x, int y) {
        Stack<Point> stack = new Stack<Point>();
        stack.push( new Point(x,y));
        while (!stack.empty()) {
            Point point= stack.firstElement();
            stack.removeElement(point);
            if (isValidPoint(point.x, point.y)) {//ArrayOutOfIndexException possible
                if (binaryMatrixOfMoosesPixels[point.x][point.y]) { //is black
                    if (!storyMatrix[point.x][point.y]) { // and wasn't written at storyMatrix yet
                        if (point.x > maxX) {
                            maxX = point.x;
                        }
                        if (point.x < minX) {
                            minX = point.x;
                        }
                        if (point.y > maxY) {
                            maxY = point.y;
                        }
                        if (point.y < minY) {
                            minY = point.y;
                        }
                        area++;
                        storyMatrix[point.x][point.y] = true;
                        stack.push(new Point(point.x + 1, point.y));//up
                        stack.push(new Point(point.x - 1, point.y));//down
                        stack.push(new Point(point.x, point.y + 1));//right
                        stack.push(new Point(point.x, point.y - 1));//left
                    }
                }
            }
        }
    }

    //looking for square in which figures could be inscribed


    public void resetCounters(){
        minX=img.getWidth();
        maxX=0;
        area=0;
        maxY=0;
        minY=img.getHeight();
    }
}
