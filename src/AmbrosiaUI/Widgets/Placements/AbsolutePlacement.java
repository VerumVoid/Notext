package AmbrosiaUI.Widgets.Placements;

import AmbrosiaUI.Utility.Size;
import AmbrosiaUI.Utility.Position;
import AmbrosiaUI.Widgets.Widget;

/**
 * A placements that gives its children absolute x and y positions
 */
public class AbsolutePlacement extends Placement {
    public static class AbsolutePlacementCell extends PlacementCell {
        private final Position position;
        private final Size size;
        public AbsolutePlacementCell(Widget bound_element, Position position, Size size) {
            this.boundElement = bound_element;
            this.position = position;
            this.size = size;
        }

        public Position getPosition() {
            return position;
        }

        public Size getSize() {
            return size;
        }
    }

    public AbsolutePlacement(Position root_position) {
        this.rootPosition = root_position;
    }
    public void add(Widget w, Position pos, Size size){
        setupWidget(w);
        this.children.add(new AbsolutePlacementCell(w, pos, size));
    }

    public Position getPosition(int index){
        return ((AbsolutePlacementCell) this.children.get(index)).getPosition().getOffset(this.getRootPosition());
    }

    @Override
    public int getWidth(int index) {
        return ((AbsolutePlacementCell) this.children.get(index)).getSize().width;
    }

    @Override
    public int getHeight(int index) {
        return ((AbsolutePlacementCell) this.children.get(index)).getSize().height;
    }

}
