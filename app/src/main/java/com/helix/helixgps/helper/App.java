package  com.helix.helixgps.helper;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class App extends AppCompatActivity {

    protected Context c;
    protected SessionManager sesi;
    protected HelixHelper helper;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        c = this;
        sesi = new SessionManager(c);
        helper = new HelixHelper();

    }
}
