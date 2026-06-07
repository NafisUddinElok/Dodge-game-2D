package blocks;

class StraightDown implements MovementBehavior {
    @Override
    public void move(int[] pos, int speed) { pos[1] += speed; }
}
