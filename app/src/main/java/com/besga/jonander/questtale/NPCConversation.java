package com.besga.jonander.questtale;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class NPCConversation extends AppCompatActivity {

    MapEntity base;
    TextView npc_name;
    TextView npc_description;
    Button btnAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_npcconversation);

        Bundle b = this.getIntent().getExtras();
        if (b != null) {
            base = b.getParcelable("ENTITY_SELECTED");
        }

        npc_name = (TextView) findViewById(R.id.npc_name);
        npc_description = (TextView) findViewById(R.id.npc_description);
        btnAnswer = (Button) findViewById(R.id.btnAnswer);

        npc_name.setText(base.getEntityName());
        npc_description.setText(base.getEntityDescription());
        btnAnswer.setText(base.getEntityCloseAnswer());




        btnAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
