package com.ga.map;

/**
 * Created by Ga on 3/4/2017.
 */
public class ShadedPicture extends Picture {
    protected Shader shader;
    public ShadedPicture(String p, Shader shader) {
        super(p);
        setShader(shader);
    }
    public ShadedPicture(String p, int index, Shader shader){
        super(p, index);
        setShader(shader);
    }
    protected void setShader(Shader p){
        shader = p;
    }
    public void shade(){
        shader.render(image);
    }
}
