package AmbrosiaUI.Widgets.Placements;

import AmbrosiaUI.Utility.Position;

/**
 * A class that holds scroll and has adjustable limits
 */
public class ScrollController {
    private final Position currentScroll = new Position(0,0);
    private int maxScrollX = 0;
    private int maxScrollY = 0;

    public ScrollController(int maxScrollX, int maxScrollY) {
        this.maxScrollX = maxScrollX;
        this.maxScrollY = maxScrollY;
    }

    public int getScrollX(){
        return currentScroll.x;
    }
    public int getScrollY(){
        return currentScroll.y;
    }
    public void setScrollY(int y){
        this.currentScroll.y = Math.max(0, Math.min(maxScrollY,y));
    }
    public void setScrollX(int x){
        this.currentScroll.x = Math.max(0, Math.min(maxScrollX,x));
    }

    public void reset(){
        currentScroll.x = 0;
        currentScroll.y = 0;
    }

    public int getMaxScrollX() {

        return maxScrollX;
    }

    public void setMaxScrollX(int maxScrollX) {
        if(currentScroll.x > maxScrollX){
            currentScroll.x = maxScrollX;
        }
        this.maxScrollX = maxScrollX;
    }

    public int getMaxScrollY() {
        return maxScrollY;
    }

    public void setMaxScrollY(int maxScrollY) {
        if(currentScroll.y > maxScrollY){
            currentScroll.y = maxScrollY;
        }
        this.maxScrollY = maxScrollY;
    }
}
