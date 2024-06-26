package AmbrosiaUI.Widgets.Icons;

import AmbrosiaUI.Utility.FileInterpreter.FileInterpreter;
import AmbrosiaUI.Utility.FileInterpreter.InterpretedCommand;
import AmbrosiaUI.Utility.Position;
import AmbrosiaUI.Utility.Rectangle;
import AmbrosiaUI.Utility.Size;
import AmbrosiaUI.Widgets.Icons.PathOperations.*;
import AmbrosiaUI.Widgets.Theme;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * An image defined by operations, similar to and svg
 */
public class PathImage extends FileInterpreter {
    private ArrayList<PathDrawable> oparations = new ArrayList<>();

    private static ArrayList<PathImage> allLoadedImages = new ArrayList<>();

    public static void reloadAllImagesFromFiles(){
        for (PathImage image: allLoadedImages){
            if(image.path != null) {
                image.oparations.clear();
                image.loadFromFile(image.path);
            }
        }
    }
    public Position getCenteredPosition(Rectangle bounds){
        return new Position(
                bounds.getX()+bounds.getWidth()/2-(int)((getWidth()/2)*getScale()),
                bounds.getY()+bounds.getHeight()/2-(int)((getHeight()/2)*getScale())
        );
    }

    private String path;

    private Size size = new Size(0, 0);
    private Theme Theme;

    private double scale = 1;

    public PathImage(Size size) {
        this.size = size;
        initFileloaderCommands();

        allLoadedImages.add(this);
    }

    public PathImage(String path) {
        initFileloaderCommands();
        loadFromFile(path);
        this.path = path;

        allLoadedImages.add(this);
    }

    public PathImage(ArrayList<PathDrawable> oparations, Size size, AmbrosiaUI.Widgets.Theme theme, double scale) {
        this.oparations = oparations;
        this.size = size;
        Theme = theme;
        this.scale = scale;
    }

    private void initFileloaderCommands() {

        this.addCommand(new InterpretedCommand("line", new InterpretedCommand.ArgumentType[]{
                InterpretedCommand.ArgumentType.INT,
                InterpretedCommand.ArgumentType.INT,
                InterpretedCommand.ArgumentType.INT,
                InterpretedCommand.ArgumentType.INT,
                InterpretedCommand.ArgumentType.STRING,
                InterpretedCommand.ArgumentType.INT
        }) {
            @Override
            public void execute(ArrayList<String> arguments) {
                PathImage.this.oparations.add(new PathLine(arguments));
            }
        });

        this.addCommand(new InterpretedCommand("rect", new InterpretedCommand.ArgumentType[]{
                InterpretedCommand.ArgumentType.INT, InterpretedCommand.ArgumentType.INT, InterpretedCommand.ArgumentType.INT, InterpretedCommand.ArgumentType.INT, // Rectangle
                InterpretedCommand.ArgumentType.STRING, InterpretedCommand.ArgumentType.INT // color, width
        }) {
            @Override
            public void execute(ArrayList<String> arguments) {
                PathImage.this.oparations.add(new PathRectangle(arguments));
            }
        });

        this.addCommand(new InterpretedCommand("fillRect", new InterpretedCommand.ArgumentType[]{
                InterpretedCommand.ArgumentType.INT, InterpretedCommand.ArgumentType.INT, InterpretedCommand.ArgumentType.INT, InterpretedCommand.ArgumentType.INT, // Rectangle
                InterpretedCommand.ArgumentType.STRING // color
        }) {
            @Override
            public void execute(ArrayList<String> arguments) {
                PathImage.this.oparations.add(new PathFillRectangle(arguments));
            }
        });

        this.addCommand(new InterpretedCommand("fillPoly4", new InterpretedCommand.ArgumentType[]{
                InterpretedCommand.ArgumentType.INT, InterpretedCommand.ArgumentType.INT, // p1
                InterpretedCommand.ArgumentType.INT, InterpretedCommand.ArgumentType.INT, // p2
                InterpretedCommand.ArgumentType.INT, InterpretedCommand.ArgumentType.INT, // p3
                InterpretedCommand.ArgumentType.INT, InterpretedCommand.ArgumentType.INT, // p4
                InterpretedCommand.ArgumentType.STRING // color
        }) {
            @Override
            public void execute(ArrayList<String> arguments) {
                PathImage.this.oparations.add(new PathFillPoly(arguments));
            }
        });

        this.addCommand(new InterpretedCommand("oval", new InterpretedCommand.ArgumentType[]{
                InterpretedCommand.ArgumentType.INT, InterpretedCommand.ArgumentType.INT, // p1
                InterpretedCommand.ArgumentType.INT, InterpretedCommand.ArgumentType.INT, // p2
                InterpretedCommand.ArgumentType.INT,
                InterpretedCommand.ArgumentType.STRING // color
        }) {
            @Override
            public void execute(ArrayList<String> arguments) {
                PathImage.this.oparations.add(new PathOval(arguments));
            }
        });

        this.addCommand(new InterpretedCommand("size", new InterpretedCommand.ArgumentType[]{InterpretedCommand.ArgumentType.INT, InterpretedCommand.ArgumentType.INT}) {
            @Override
            public void execute(ArrayList<String> arguments) {
                PathImage.this.setSize(new Size(
                        Integer.parseInt(arguments.get(0)),
                        Integer.parseInt(arguments.get(1))
                ));
            }
        });
    }

    public void saveToFile(String filename) {
        try (FileWriter myWriter = new FileWriter(filename)) {
            myWriter.write("size " + getSize().width + " " + getSize().height + "\n");
            for(PathDrawable path: oparations) {
                myWriter.write(path.getName() + " " + String.join(" ",path.toArguments())+ "\n");
            }
            //myWriter.close();
        } catch (IOException ignored) {

        }
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public int getWidth() {
        return size.width;
    }

    public int getHeight() {
        return size.height;
    }

    public void draw(Graphics2D g2, Position startPosition) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (Theme == null) {
            return;
        }

        //g2.setClip(startPosition.x, startPosition.y, size.width,size.height);

        AffineTransform at = new AffineTransform();
        at.translate(startPosition.x, startPosition.y);
        at.scale(scale, scale);
        g2.setTransform(at);

        //g2.setColor(new Color(255,0,0));
        //g2.fillRect(0,0,20,20);

        Position position = new Position(0, 0);

        for (PathDrawable operation : oparations) {
            operation.draw(g2, position, Theme);
        }

        at = new AffineTransform();
        //at.translate(-scrollController.getScrollX(), -scrollController.getScrollY());

        g2.setTransform(at);
    }

    public void drawOperation(Graphics2D g2, Position startPosition, PathDrawable operation, Color color){
        AffineTransform at = new AffineTransform();
        at.translate(startPosition.x, startPosition.y);
        at.scale(scale, scale);
        g2.setTransform(at);

        //g2.setColor(new Color(255,0,0));
        //g2.fillRect(0,0,20,20);

        Position position = new Position(0, 0);

        operation.draw(g2, position, color);

        at = new AffineTransform();
        //at.translate(-scrollController.getScrollX(), -scrollController.getScrollY());

        g2.setTransform(at);
    }

    public PathImage getScaled(double scale){
        return new PathImage(oparations,size,this.getTheme(),scale);
    }

    public void setScale(double scale) {
        this.scale = Math.max(0.1,scale);
        //this.size.width = (int) (this.size.width * scale);
        //this.size.height = (int) (this.size.height * scale);
    }

    public Theme getTheme() {
        return Theme;
    }

    public void setTheme(Theme Theme) {
        this.Theme = Theme;
    }

    public void add(PathDrawable drawable) {
        oparations.add(drawable);
    }

    public double getScale() {
        return scale;
    }

    public ArrayList<PathDrawable> getOparations() {
        return oparations;
    }
}
