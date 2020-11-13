package ar.edu.ips.aus.seminario2.buscaminas;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ar.edu.ips.aus.seminario2.buscaminas.util.FirebaseUtil;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private List<GameMetaData> games = new ArrayList<GameMetaData>();
    private List<int[][]> grids = new ArrayList<>();
    private ValueEventListener listener;

    public GameAdapter() {
        Query waitingQuery = FirebaseUtil.openFbReference("games")
                .orderByChild("status").equalTo("ready");
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    GameMetaData data = child.getValue(GameMetaData.class);
                    Log.d("GameAdapter: ", data.getTitle());
                    data.setId(child.getKey());
                    games.add(data);
                    GenericTypeIndicator<List<Map<String, Object>>> mapGenericTypeIndicator;
                    mapGenericTypeIndicator = new GenericTypeIndicator<List<Map<String, Object>>>() {
                    };
                    List<Map<String, Object>> boardData = child.child("board").getValue(mapGenericTypeIndicator);
                    //TODO prepare grid for boardData in advance
                    int[][] grid = GameEngine.getInstance().
                            generateGridDataFromBoard(boardData,
                                    (int) data.getWidth(),
                                    (int) data.getHeight());
                    grids.add(grid);
                }
                notifyItemInserted(games.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        waitingQuery.addListenerForSingleValueEvent(listener);
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.game_row, parent, false);
        return new GameViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        GameMetaData game = games.get(position);
        holder.bind(game);
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    public class GameViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView gameId;
        TextView gameTitle;

        public GameViewHolder(View itemView) {
            super(itemView);
            gameTitle = itemView.findViewById(R.id.gameTitle);
            gameId = itemView.findViewById(R.id.gameId);

            itemView.setOnClickListener(this);
        }

        public void bind(GameMetaData game) {
            gameTitle.setText(game.getTitle());
            gameId.setText(game.getId());
        }


        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            GameMetaData game = games.get(position);
            int[][] grid = grids.get(position);
            //retrieveGameData(game.getId());
            Intent intent = new Intent(view.getContext(), MainActivity.class);
            intent.putExtra("game", game);
            intent.putExtra("grid", grid);
            view.getContext().startActivity(intent);
        }
    }
}
