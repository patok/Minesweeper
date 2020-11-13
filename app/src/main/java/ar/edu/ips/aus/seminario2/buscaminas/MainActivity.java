package ar.edu.ips.aus.seminario2.buscaminas;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import ar.edu.ips.aus.seminario2.buscaminas.util.FirebaseUtil;

public class MainActivity extends Activity {

    private static String TAG = "MAIN";

    private DatabaseReference boardRef;
    private DatabaseReference playersRef;
    private DatabaseReference votesRef;
    private GameEngine.GameBoardChildEventListener boardListener;
    private GameEngine.PlayersListener playersListener;
    private GameEngine.FinishingVotesListener finishingVotesListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GameMetaData game = (GameMetaData) getIntent().getSerializableExtra("game");
        int[][] gameGrid = (int[][]) getIntent().getSerializableExtra("grid");
        if (game != null){
            GameEngine.getInstance().setNewGame(false);
            GameEngine.getInstance().WIDTH = (int) game.getWidth();
            GameEngine.getInstance().HEIGHT = (int) game.getHeight();
            GameEngine.getInstance().BOMB_NUMBER = (int) game.getBombNumber();
            GameEngine.getInstance().newGameId = game.getId();
            GameEngine.getInstance().gameGrid = gameGrid;
            Log.d(TAG, "Joining Game: " + game.getId());
        } else {
            GameEngine.getInstance().setNewGame(true);
            GameEngine.getInstance().WIDTH = 10;
            GameEngine.getInstance().HEIGHT = 10;
            GameEngine.getInstance().BOMB_NUMBER = 10;
            GameEngine.getInstance().newGameId = null;
            Log.d(TAG, "Will create new game");
        }
        setContentView(R.layout.activity_main);
        Log.d("MainActivity","onCreate");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.msgInitDialog)
                .setTitle(R.string.titleInitDialog);
        builder.setPositiveButton("GO GO GO!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                MainActivity.this.finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        boardRef = FirebaseUtil.openFbReference("games/"+ GameEngine.getInstance().newGameId + "/board");
        boardListener = new GameEngine.GameBoardChildEventListener();
        boardRef.addChildEventListener(boardListener);

//        playersRef = FirebaseUtil.openFbReference("games/" + GameEngine.getInstance().newGameId + "/players");
//        playersListener = new GameEngine.PlayersListener();
//        playersRef.addChildEventListener(playersListener);
//
//        votesRef = FirebaseUtil.openFbReference("games/" + GameEngine.getInstance().newGameId + "/end_votes");
//        finishingVotesListener = new GameEngine.FinishingVotesListener();
//        votesRef.addValueEventListener(finishingVotesListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        boardRef.removeEventListener(boardListener);
        //playersRef.removeEventListener(playersListener);
        //votesRef.removeEventListener(finishingVotesListener);
    }


}
