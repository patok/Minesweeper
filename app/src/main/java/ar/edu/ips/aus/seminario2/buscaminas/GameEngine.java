package ar.edu.ips.aus.seminario2.buscaminas;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.edu.ips.aus.seminario2.buscaminas.util.FirebaseUtil;
import ar.edu.ips.aus.seminario2.buscaminas.util.Generator;
import ar.edu.ips.aus.seminario2.buscaminas.util.PrintGrid;
import ar.edu.ips.aus.seminario2.buscaminas.views.grid.Cell;

/**
 * Created by Marcell on 2016. 04. 01..
 */
public class GameEngine {
    private static final String TAG = "GAME_ENGINE";
    private static GameEngine instance;

    public static int BOMB_NUMBER = 10;
    public static int WIDTH = 10;
    public static int HEIGHT = 10;
    public static String PLAYER_NAME = "Sam";
    private static int totalNumberOfPayers = 0;

    private boolean newGame = true;
    public String newGameId;
    public int[][] gameGrid;
    private Cell[][] MinesweeperGrid;

    private boolean running = false;
    private Context context;

    public static GameEngine getInstance() {
        if( instance == null ){
            instance = new GameEngine();
        }
        return instance;
    }

    private GameEngine(){ }

    public void start() {
        running = true;
        // TODO change game status if appropiate
    }

    public void stop() {
        running = false;
        // TODO vote game to finish
        DatabaseReference dbRef = FirebaseUtil.openFbReference("games/" + this.newGameId + "/end_votes");
        dbRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                long count = (long) currentData.getValue();
                currentData.setValue(count+1);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed)
                    Log.d(TAG, "Voted game to finish!");
                else
                    Log.d(TAG, "Error when trying to set finish vote: " + error);
            }
        });
    }

    public boolean isRunning() {
        return this.running;
    }

    public void createGrid(Context context){
        Log.e("GameEngine","createGrid is working");
        this.context = context;

        // TODO create the grid based on game data
        int[][] generatedGrid;
        if (!isNewGame()){
            generatedGrid = this.gameGrid;
        } else {
            // generate grid data
            generatedGrid = Generator.generate(BOMB_NUMBER, WIDTH, HEIGHT);
        }
        PrintGrid.print(generatedGrid,WIDTH,HEIGHT);

        // build grid representation
        if (isNewGame()) {
            createFirebaseGame(generatedGrid);
        }
        setGrid(context,generatedGrid);
    }

    public int[][] generateGridDataFromBoard(List<Map<String,Object>> boardData, int width, int height) {
        int [][] grid = new int[width][height];
        for( int x = 0 ; x< width ;x++ ){
            grid[x] = new int[height];
        }

        for( int x = 0 ; x< width ;x++ ){
            for (int y = 0; y < height; y++) {
                grid[x][y] = ((Long)boardData.get(x*width+y).get("value")).intValue();
            }
        }
        return grid;
    }

    public boolean isNewGame() {
        return newGame;
    }

    public void setNewGame(boolean value) {
        newGame = value;
    }

    private void setGrid( final Context context, final int[][] grid ){
        MinesweeperGrid= new Cell[WIDTH][HEIGHT];
        for( int x = 0 ; x < WIDTH ; x++ ){
            for( int y = 0 ; y < HEIGHT ; y++ ){
                if( MinesweeperGrid[x][y] == null ){
                    MinesweeperGrid[x][y] = new Cell( context , x,y);
                }
                // FIXME no need to setValue right now
                // FIXME then, grid is not needed here
                MinesweeperGrid[x][y].setValue(grid[x][y]);
                MinesweeperGrid[x][y].invalidate();
            }
        }
    }

    public Cell getCellAt(int position) {
        int x = position % WIDTH;
        int y = position / WIDTH;

        return MinesweeperGrid[x][y];
    }

    public Cell getCellAt( int x , int y ){
        return MinesweeperGrid[x][y];
    }

    public void click(final int x , final int y ){
        if( //isRunning() &&
                x >= 0 && y >= 0 &&
                x < WIDTH && y < HEIGHT &&
                !getCellAt(x,y).isClicked() ){

            updateFBCell(x, y);

            if( getCellAt(x,y).isBomb() ){
                onGameLost();
            }
        }

        // TODO move checkEnd() to game state change listener
        checkEnd();
    }

    private void updateFBCell(final int x, final int y) {
        Map<String, Object> updateValues = new HashMap<>();
        updateValues.put("clicked", true);
        updateValues.put("player_id", this.PLAYER_NAME);
        DatabaseReference dbRef = FirebaseUtil.openFbReference("games/"+ this.newGameId + "/board");
        int position = y * WIDTH + x;
        dbRef.child(String.valueOf(position)).updateChildren(updateValues,
                new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        Log.d(TAG, String.format("Actualizando %s - error? %b", ref.toString(), error != null));

                        if( getCellAt(x,y).getValue() == 0 ){
                            for( int xt = -1 ; xt <= 1 ; xt++ ){
                                for( int yt = -1 ; yt <= 1 ; yt++){
                                    if( xt != yt ){
                                        click(x + xt , y + yt);
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private boolean checkEnd(){
        int bombNotFound = BOMB_NUMBER;
        int notRevealed = WIDTH * HEIGHT;
        for ( int x = 0 ; x < WIDTH ; x++ ){
            for( int y = 0 ; y < HEIGHT ; y++ ){
                if( getCellAt(x,y).isRevealed() || getCellAt(x,y).isFlagged() ){
                    notRevealed--;
                }

                if( getCellAt(x,y).isFlagged() && getCellAt(x,y).isBomb() ){
                    bombNotFound--;
                }
            }
        }

        if( bombNotFound == 0 && notRevealed == 0 ){
            Toast.makeText(context,"Game won", Toast.LENGTH_SHORT).show();
            stop();
            // TODO change game status if appropiate
        }
        return false;
    }

    public void flag( int x , int y ){
        boolean isFlagged = getCellAt(x,y).isFlagged();
        getCellAt(x,y).setFlagged(!isFlagged);
        getCellAt(x,y).invalidate();
    }

    private void onGameLost(){
        // handle lost game
        stop();
        // TODO vote game to finish
        Toast.makeText(context,"Game lost", Toast.LENGTH_SHORT).show();

        // TODO get rid of this part: no need to reveal the rest of it
        // only maybe when finished
//        for ( int x = 0 ; x < WIDTH ; x++ ) {
//            for (int y = 0; y < HEIGHT; y++) {
//                getCellAt(x,y).setRevealed();
//            }
//        }
    }

    // método encargado de la creción del game en FB
    public void createFirebaseGame(int[][] generatedGrid) {
        Map<String, Object> board = new HashMap<String, Object>();
        for (int i = 0; i < generatedGrid.length; i++) {
            for (int j = 0; j < generatedGrid[i].length; j++) {
                Map<String, Object> values = new HashMap<String, Object>();
                board.put(String.valueOf(i*WIDTH+j), values);
                values.put("clicked", false);
                values.put("value", generatedGrid[i][j]);
            }
        }

        DatabaseReference gamesRef = FirebaseUtil.openFbReference("games");
        newGameId = gamesRef.push().getKey();
        gamesRef.child(newGameId).child("width").setValue(WIDTH);
        gamesRef.child(newGameId).child("height").setValue(HEIGHT);
        gamesRef.child(newGameId).child("mines").setValue(BOMB_NUMBER);

        gamesRef.child(newGameId).child("board").updateChildren(board);
        gamesRef.child(newGameId).child("end_votes").setValue(0);
        gamesRef.child(newGameId).child("status").setValue("ready");
        gamesRef.child(newGameId).child("title").setValue("Creado por: " + PLAYER_NAME);
        Log.d(TAG, "Created new game object: " + newGameId);
    }

    public static class GameBoardChildEventListener implements ChildEventListener {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//            // TODO make routine more robust
//            // TODO extract helper method
//            String key = snapshot.getKey();
//            int position = Integer.valueOf(key);
//            Map<String, Object> cell = (Map<String, Object>) snapshot.getValue();
//            boolean cellClicked = (boolean) cell.get("clicked");
//            long cellValue = (long) cell.get("value");
//
//            // TODO refactor into check status routines
//            Cell aCell = GameEngine.getInstance().getCellAt(position);
//            aCell.setValue((int)cellValue);
//            if (cellClicked)
//                aCell.setClicked();
        }


        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            // TODO refactor into helper method
            String key = snapshot.getKey();
            int position = Integer.valueOf(key);
            Map<String, Object> cell = (Map<String, Object>) snapshot.getValue();
            boolean cellClicked = (boolean) cell.get("clicked");
            long cellValue = (long) cell.get("value");
            String playerId = (String) cell.get("player_id");
            Log.d(TAG, String.format("Cell changed - value:%d clicked:%b player_id:%s", cellValue, cellClicked, playerId));

            // TODO refactor into check status routines
            // TODO moved into GameEngine class
            if (cellClicked &&
                    null != playerId && playerId.equals(GameEngine.PLAYER_NAME)) {
                Cell aCell = GameEngine.getInstance().getCellAt(position);
                aCell.setClicked();
            }

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    }

    public static class PlayersListener implements ChildEventListener {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            totalNumberOfPayers++;
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    }

    public static class FinishingVotesListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            long value = (long) snapshot.getValue();
            if (value + 1 >= totalNumberOfPayers) {
                // TODO stop the game
                DatabaseReference statusRef = FirebaseUtil.openFbReference("games/"+ GameEngine.getInstance().newGameId + "/status");;
                statusRef.setValue("stopped");
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    }
}
