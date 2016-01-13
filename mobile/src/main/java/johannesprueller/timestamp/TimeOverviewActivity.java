package johannesprueller.timestamp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.List;

public class TimeOverviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_overview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        if(bar != null)
        {
            bar.setDisplayHomeAsUpEnabled(true);
        }
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        List<List<TimeStampItem>> items = dbHelper.LoadTimeStampItems();

        RecyclerView recycler = (RecyclerView)findViewById(R.id.timeoverview);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recycler.setLayoutManager(llm);

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this);
        recycler.setAdapter(adapter);

    }

}
