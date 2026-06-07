package blocks;

class ZigZagMovement implements MovementBehavior {
    int timer = 0, dir = 1;
    public void move(int[] pos, int speed) {
        pos[1] += speed;
        timer++;
        if (timer % 30 == 0) dir *= -1;
        pos[0] += dir * 4;
    }
}
