package ar.edu.ips.aus.seminario2.buscaminas;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import ar.edu.ips.aus.seminario2.buscaminas.GameEngine;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GameEngine.getInstance().WIDTH = 16;
        GameEngine.getInstance().HEIGHT = 16;
        GameEngine.getInstance().BOMB_NUMBER = 34;

        setContentView(R.layout.activity_main);
        Log.e("MainActivity","onCreate");
    }
}
