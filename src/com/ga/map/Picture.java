package com.ga.map;

import com.ga.game.FileManager;
import com.ga.game.Settings;
import com.ga.tool.P;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Ga on 5/6/2016. asd
 */
public class Picture {
    private P size = new P();
    private static HashMap<String,BufferedImage> cache = new HashMap<>();
    private static HashMap<String,String> directory = new HashMap<>();
    private static HashMap<String,Integer> multiples = new HashMap<>();
    public boolean visible = true;
    public BufferedImage image;
    public int sizeRadius = 10;
    private P halfSize = new P();
    public Random rand;
    private int type = 1; //1:center,2:bottom

    public static void init(){
        for (String[] i : FileManager.fileCache.get("png")){
            int randIndex = i[0].indexOf("-");
            if (randIndex != -1){
                String category = i[0].substring(0, randIndex);
                if (multiples.get(category) == null) multiples.put(category, 1);
                else multiples.put(category, multiples.get(category)+1);
            }
            directory.put(i[0], i[1]);
        }
        cache.put("Drop",getTexture(loadCache("Icon"),24,24,0,0));
        cache.put("Buy",getTexture(loadCache("Icon"),24,24,1,0));
        cache.put("Describe",getTexture(loadCache("Icon"),24,24,2,0));
        cache.put("POI",getTexture(loadCache("Icon"),24,24,3,0));
        cache.put("Move",getTexture(loadCache("Icon"),24,24,4,0));
    }
    public Picture(Random rand){
        this.rand = rand;
    }
    public void set(BufferedImage image){
        this.image = image;
        if (this.image != null) setSize(image.getWidth(), image.getHeight());
    }
    public void set(String name){
        if (name != null) set(load(name));
    }
    public void set(String name, int index){
        if (name != null) set(getTexture(load(name),32,64,index,0));
    }
    public BufferedImage getImage(){
        return image;
    }
    public void setSize(P p){
        setSize(p.x, p.y);
    }
    public void setSize(float x, float y){
        size = new P(x,y);
        sizeRadius = (int) size.getMult(.5f,.5f).getDist();
        setCenterType(type);
    }
    public P getSize(){
        return size;
    }
    public int getCenterType(){
        return type;
    }
    public void setCenterType(int p){
        type = p;
        if (type == 1) halfSize = new P(size.x/2, size.y/2);
        else if (type == 2) halfSize = new P(size.x/2, size.y-8);
        else if (type == 3) halfSize = new P(8, size.y/2);
    }
    private BufferedImage load(String dir){
        BufferedImage image = null;
        String file = directory.get(dir);
        if (file == null){
            Integer num = multiples.get(dir);
            if (num != null) {
                if (rand == null) rand = new Random();
                dir += "-" + rand.nextInt(num);
                file = directory.get(dir);
            }
        }
        if (file != null) {
            try {
                image = ImageIO.read(new File(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (dir != null) {
            Global.p("Can't read picture file ["+dir + "]");
            try {
                image = ImageIO.read(new File("Data/Error.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return toCompatibleImage(image);
    }
    public static BufferedImage loadCache(String dir){
        BufferedImage image = cache.get(dir);
        if (image != null) return image;
        String file = directory.get(dir);
        if (file != null) {
            try {
                image = ImageIO.read(new File(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
            cache.put(dir, image);
        } else if (dir != null) Global.p("Can't read picture cache ["+dir + "]");
        return toCompatibleImage(image);
    }
    static BufferedImage toCompatibleImage(BufferedImage image)
    {
        GraphicsConfiguration gfx_config = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getDefaultScreenDevice().
                getDefaultConfiguration();
        if (image.getColorModel().equals(gfx_config.getColorModel()))
            return image;
        BufferedImage new_image = gfx_config.createCompatibleImage(
                image.getWidth(), image.getHeight(), Transparency.BITMASK);
        // get the graphics context of the new image to draw the old image on
        Graphics2D g2d = (Graphics2D) new_image.getGraphics();

        // actually draw the image and dispose of context no longer needed
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        // return the new optimized image
        return new_image;
    }
    private static BufferedImage getTexture(BufferedImage image, int sx, int sy, int px, int py) {
        return image.getSubimage(px * sx, py * sy, sx, sy);
    }
    static void newImage(String path, String p, BufferedImage image){
        String fullPath = path+p+".png";
        FileManager.save(fullPath,"");
        File output = new File("Data/"+fullPath);
        try {
            ImageIO.write(image, "png", output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cache.put(p,image);
    }
    public Picture copy(){
        Picture r = new Picture(rand);
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                result.setRGB(x, y, image.getRGB(x, y));
            }
        }
        r.set(result);
        return r;
    }
    public void modify(Shader shader, int p){
        if (image != null) image = shader.render(image, p);
        if (image != null) image = toCompatibleImage(image);
    }
    public void draw(Graphics2D g, float x, float y, float rot, int dir, float sizeX, float sizeY){
        if (visible && Global.inCamera(x,y, sizeX, sizeY)) {
            float xMid = halfSize.x*dir;
            float yMid = halfSize.y;
            float rx = x + Global.rel.x;
            float ry = y + Global.rel.y;
            x = rx - xMid;
            y = ry - yMid;
            g.rotate(rot, rx, ry);
            drawImage(g, (int) x, (int) y, (int) (sizeX*dir), (int) (sizeY));
            g.rotate(-rot, rx, ry);
        }
    }
    public void draw(Graphics2D g, float x, float y, float rot, int dir){
        draw(g,x,y,rot,dir,size.x,size.y);
    }
    protected void drawImage(Graphics2D g, int x, int y, int sizeX, int sizeY){
        g.drawImage(image, x, y, sizeX, sizeY, null);
    }
}