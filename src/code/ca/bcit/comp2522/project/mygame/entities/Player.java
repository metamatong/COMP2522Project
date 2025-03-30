package ca.bcit.comp2522.project.mygame.entities;

public class Player
{
    int x;
    int y;
    int prevX;
    int prevY;
    boolean isUser;
    boolean isEliminated;
    boolean finished;  // new: has the player finished?
    boolean pushing = false;  // New: indicates that this player is currently pushing
    boolean pushed = false;   // New: indicates that this player is currently being pushed down
    // Time (nanoTime) at which the player died.
    long deathTimestamp = 0;
    long lastMoveTime = 0;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.prevX = x;
        this.prevY = y;
        this.isUser = false;
        this.isEliminated = false;
        this.finished = false;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getPrevX() {
        return prevX;
    }

    public int getPrevY() {
        return prevY;
    }

    public boolean isPushing() {
        return pushing;
    }

    public boolean isPushed() {
        return pushed;
    }

    public long getDeathTimestamp() {
        return deathTimestamp;
    }

    public long getLastMoveTime() {
        return lastMoveTime;
    }

    public boolean isUser() {
        return isUser;
    }

    public boolean isEliminated() {
        return isEliminated;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setPrevX(int prevX) {
        this.prevX = prevX;
    }

    public void setPrevY(int prevY) {
        this.prevY = prevY;
    }

    public void setPushing(boolean isPushing) {
        this.pushing = isPushing;
    }

    public void setPushed(boolean isPushed) {
        this.pushed = isPushed;
    }

    public void setDeathTimeStamp(long deathTimestamp) {
        this.deathTimestamp = deathTimestamp;
    }

    public void setLastMoveTIme(long lastMoveTime) {
        this.lastMoveTime = lastMoveTime;
    }

    public void setUser(boolean isUser) {
        this.isUser = isUser;
    }

    public void setEliminated(boolean isEliminated) {
        this.isEliminated = isEliminated;
    }

    public void setFinished(boolean isFinished) {
        this.finished = isFinished;
    }
}