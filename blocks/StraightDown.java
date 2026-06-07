package blocks;

class StraightDown implements MovementBehavior {
    public void move(int[] pos, int speed) { pos[1] += speed; }
}
