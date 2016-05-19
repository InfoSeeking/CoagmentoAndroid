package com.coagmento.mobile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class DocumentViewerActivity extends AppCompatActivity {

    private ImageView exitButton;
    private TextView docTitleView, docTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_viewer);

        if(getSupportActionBar() != null) getSupportActionBar().hide();

        exitButton = (ImageView) findViewById(R.id.exit_document_button);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        docTitleView = (TextView) findViewById(R.id.document_title);
        docTextView = (TextView) findViewById(R.id.document_text);

        Bundle b = getIntent().getExtras();
        docTitleView.setText(b.getString("DOC_TITLE"));
        docTextView.setText(b.getString("DOC_TEXT"));

    }
}
