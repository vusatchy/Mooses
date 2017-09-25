import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
            String path = "moose.png";
            MooseHighlighter mooseHighlighter = new MooseHighlighter(path);
            mooseHighlighter.searchMoose();
            mooseHighlighter.drawNewPicture();
            System.out.println("Result.png should apear");
    }
}
