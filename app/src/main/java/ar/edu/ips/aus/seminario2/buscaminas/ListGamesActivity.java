package ar.edu.ips.aus.seminario2.buscaminas;

import android.app.Activity;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ListGamesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_games);

        RecyclerView gameList = findViewById(R.id.games);
        final GameAdapter adapter = new GameAdapter();
        gameList.setAdapter(adapter);

        LinearLayoutManager gamesLayoutManager
                = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        gameList.setLayoutManager(gamesLayoutManager);
    }

}


