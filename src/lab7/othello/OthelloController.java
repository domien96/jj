package lab7.othello;

import eventbroker.Event;
import eventbroker.EventBroker;
import eventbroker.EventListener;
import eventbroker.EventPublisher;
import lab7.game.events.GameMoveEvent;
import lab7.othello.exception.BoardIndexOutOfBoundsException;
import lab7.othello.exception.InvalidMoveException;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.util.ArrayList;
import java.util.List;


public class OthelloController extends EventPublisher implements EventListener{

    public void setRole(int role) {
        this.role = role;
    }

    int role=-2;

    OthelloModel model;
    
    public OthelloController(OthelloModel model){
        this.model = model;
        EventBroker.getEventBroker().addEventListener("Move",this);
    }

    public boolean isValidMove(int x, int y) throws BoardIndexOutOfBoundsException {
        return model.getTurn() == role && isValidMove(x,y,role);
    }
    
    private boolean isValidMove(int x, int y, int turn) throws BoardIndexOutOfBoundsException {
        if(!(model.inBounds(x,y)) || model.getState(x,y)!=0 )
            return false;

        // Op elke richting van deze buren kijken we of we een coin van de HUIDIGE speler tegenkomen
        int otherPlayerState = -2*turn+1; // 1->-1 en 0->1
        int currentPlayerState = 2*turn-1; // 1->1 en 0->-1
        for(int[] negb : neighboursFilteredByState(x,y,otherPlayerState)) {
            int difX = negb[0]-x, difY = negb[1]-y;
            int step =2; // We beginnnen met het vakje NA deze buur.
            int[] cur = {step*difX+x,step*difY+y};
            while(model.inBounds(cur[0],cur[1])) {
                if(model.getState(cur[0],cur[1])==currentPlayerState)
                    return true;
                cur[0] = ++step*difX+x;
                cur[1] = step*difY+y;
            }
        }
        return false;
    }

    /**
     * Geeft de buren terug die zich op het bord bevinden.
     * @param x
     * @param y
     * @return : arrays van coordinaten-koppels.
     */
    private List<int[]> neighbours(int x, int y) {
        int[][] relativePos = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
        List<int[]> result = new ArrayList<>();
        for (int[] relPos : relativePos) {
            if (model.inBounds(x+relPos[0],y+relPos[1]))
                result.add(new int[]{x+relPos[0],y+relPos[1]});
        }
        return result;
    }

    // Filter buren met state van andere player
    private List<int[]> neighboursFilteredByState(int x,int y,int state) {
        List<int[]> filteredNeighbours = new ArrayList<>(); // Buren met coin van de ANDERE speler.
        for(int[] negb : neighbours(x,y)) {
            if(model.getState(negb[0],negb[1])==state)
                filteredNeighbours.add(negb);
        }
        return filteredNeighbours;
    }
    
    public void doMove(int x, int y) throws InvalidMoveException, BoardIndexOutOfBoundsException {
        if(isValidMove(x,y)) {
            publishEvent(new GameMoveEvent(x,y,model.getTurn()));
            doMoveOnModel(x,y,model.getTurn());
        } else {
            throw new InvalidMoveException();
        }
    }

    private void doMoveOnModel(int x, int y, int turn) throws BoardIndexOutOfBoundsException {
        model.setValid(false);
        /////////////////
        int currentPlayerState = 2*turn-1; // 1->1 en 0->-1

        //beetje code duplicatie
        for(int[] negb : neighboursFilteredByState(x,y,-2*turn+1)) { // -2*model.getTurn()+1 == otherPlayerState
            int difX = negb[0]-x, difY = negb[1]-y;
            int step =2; // We beginnnen met het vakje NA deze buur.
            int[] cur = {step*difX+x,step*difY+y};
            while(model.inBounds(cur[0],cur[1])) {
                if(model.getState(cur[0],cur[1])==currentPlayerState) {
                    changeLine(x, y, cur[0], cur[1],currentPlayerState);
                    break;
                }
                cur[0] = ++step*difX+x;
                cur[1] = step*difY+y;
            }
        }
        int otherTurn = (turn+1)%2;
        if(playerHasPossibleMoves(otherTurn))
            model.setTurn(otherTurn);

        /////////////////
        model.setValid(true);
    }

    // Beide coordinaten moeten inbounds zijn. Als dit niet zo is, gebeurt er niets!
    private void changeLine(int xFrom, int yFrom, int xTo, int yTo, int state) throws BoardIndexOutOfBoundsException {
        if(model.inBounds(xFrom,yFrom) && model.inBounds(xTo,yTo)) {
            // Stepsize voor X en Y berekenen.
            int difX = (int) Math.signum(xTo-xFrom),
            difY = (int) Math.signum(yTo-yFrom);
            int x=xFrom,y=yFrom;
            while(!(x==xTo && y==yTo)) {
                model.setState(x,y,state);
                x += difX;
                y += difY;
            }
        }
    }
  
    public int isFinished(){
        int countBlack=0, countWhite=0;
        for(int row=0;row<model.getSize();row++) {
            for(int col=0;col<model.getSize();col++) {
                switch(model.getState(row,col)) {
                    case -1:
                        countBlack++;
                        break;
                    case 0:
                        try {
                            if(isValidMove(row,col,0) || isValidMove(row,col,1))
                                return 0;
                        } catch (BoardIndexOutOfBoundsException e) {
                            // should not happen
                        }
                        break;
                    case 1:
                        countWhite++;
                        break;
                }
            }
        }
        // FROM here : No possible moves for both players anymore.
        int res = (int) Math.signum(countWhite-countBlack); // fancy
        return  res == 0? 2 : res;
    }

    /**
     * Kijkt of speler nog geldige zetten heeft.
     * @param turn : 0 black; 1 wit.
     * @return
     */
    private boolean playerHasPossibleMoves(int turn) {
        for(int row=0;row<model.getSize();row++) {
            for(int col=0;col<model.getSize();col++) {
                if (model.getState(row,col)==0) {
                    try {
                        if(isValidMove(row,col,turn))
                            return true;
                    } catch (BoardIndexOutOfBoundsException e) {
                        // should not happen
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void handleEvent(Event event) {
        if(event instanceof GameMoveEvent) {
            GameMoveEvent e = (GameMoveEvent) event;
            try {
                doMoveOnModel(e.row,e.col,e.turn);
            } catch (BoardIndexOutOfBoundsException e1) {
                System.err.println("blabla");
            }
        }
    }
}
