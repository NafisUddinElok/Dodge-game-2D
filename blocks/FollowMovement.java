package blocks;

class FollowMovement implements MovementBehavior {
    int playerX;
    public FollowMovement(int px) { playerX = px; }
    public void move(int[] pos, int speed) {
        pos[1] += speed;
        if (pos[0] < playerX) pos[0] += 2;
        else if (pos[0] > playerX) pos[0] -= 2;
    }
    public void setPlayerX(int px) { playerX = px; }
}
