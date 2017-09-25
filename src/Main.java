import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
            String path = "mooses.png";
            MoosesHighlighter moosesHighlighter = new MoosesHighlighter(path);
            moosesHighlighter.searchMooses();
            moosesHighlighter.drawNewPicture();
            System.out.println("Result.png should apear");
    }
}
