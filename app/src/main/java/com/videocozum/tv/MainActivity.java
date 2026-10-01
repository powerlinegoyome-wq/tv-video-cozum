package com.videocozum.tv;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final int PUBLISHER_OKYANUS = 0;
    public static final int PUBLISHER_ALTINKARMA = 1;

    private int currentPublisher = PUBLISHER_OKYANUS;
    private StringBuilder barcodeBuilder = new StringBuilder();

    private TextView txtBarcodeDisplay;
    private TextView txtPublisherInfo;
    private FrameLayout cardOkyanus;
    private FrameLayout cardAltinKarma;
    private Button btnSubmit;
    private Button btnClear;
    private Button btnBrowsePortal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupPublisherCards();
        setupNumpad();
        updatePublisherUI();
    }

    private void initViews() {
        txtBarcodeDisplay = findViewById(R.id.txt_barcode_display);
        txtPublisherInfo = findViewById(R.id.txt_selected_publisher_info);
        cardOkyanus = findViewById(R.id.card_okyanus);
        cardAltinKarma = findViewById(R.id.card_altinkarma);
        btnSubmit = findViewById(R.id.btn_submit);
        btnClear = findViewById(R.id.btn_clear);
        btnBrowsePortal = findViewById(R.id.btn_browse_portal);

        // Varsayılan olarak ilk numara butonuna odaklan
        findViewById(R.id.btn_num_1).requestFocus();
    }

    private void setupPublisherCards() {
        cardOkyanus.setOnClickListener(v -> {
            currentPublisher = PUBLISHER_OKYANUS;
            updatePublisherUI();
        });

        cardAltinKarma.setOnClickListener(v -> {
            currentPublisher = PUBLISHER_ALTINKARMA;
            updatePublisherUI();
        });

        btnBrowsePortal.setOnClickListener(v -> {
            String url = (currentPublisher == PUBLISHER_OKYANUS)
                    ? "https://www.akilliogretim.com"
                    : "https://altinkarma.video-cozum.com";
            String title = (currentPublisher == PUBLISHER_OKYANUS) ? "Okyanus Kütüphane" : "Altın Karma Kütüphane";
            openPlayer(url, title);
        });
    }

    private void updatePublisherUI() {
        if (currentPublisher == PUBLISHER_OKYANUS) {
            cardOkyanus.setSelected(true);
            cardAltinKarma.setSelected(false);
            txtPublisherInfo.setText("Aktif Yayın: 🌊 Okyanus (Akıllı Öğretim)");
            txtBarcodeDisplay.setHint("Okyanus Karekod No (Örn: 36457)");
        } else {
            cardOkyanus.setSelected(false);
            cardAltinKarma.setSelected(true);
            txtPublisherInfo.setText("Aktif Yayın: ⭐ Altın Karma Video Çözüm");
            txtBarcodeDisplay.setHint("Altın Karma Karekod Kodu");
        }
    }

    private void setupNumpad() {
        int[] numIds = {
                R.id.btn_num_0, R.id.btn_num_1, R.id.btn_num_2, R.id.btn_num_3,
                R.id.btn_num_4, R.id.btn_num_5, R.id.btn_num_6, R.id.btn_num_7,
                R.id.btn_num_8, R.id.btn_num_9
        };

        for (int id : numIds) {
            Button btn = findViewById(id);
            btn.setOnClickListener(v -> {
                String digit = btn.getText().toString();
                if (barcodeBuilder.length() < 12) {
                    barcodeBuilder.append(digit);
                    txtBarcodeDisplay.setText(barcodeBuilder.toString());
                }
            });
        }

        btnClear.setOnClickListener(v -> {
            if (barcodeBuilder.length() > 0) {
                barcodeBuilder.deleteCharAt(barcodeBuilder.length() - 1);
                txtBarcodeDisplay.setText(barcodeBuilder.toString());
            }
        });

        btnClear.setOnLongClickListener(v -> {
            barcodeBuilder.setLength(0);
            txtBarcodeDisplay.setText("");
            return true;
        });

        btnSubmit.setOnClickListener(v -> {
            String code = barcodeBuilder.toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(this, "Lütfen kitaptaki karekod numarasını girin!", Toast.LENGTH_SHORT).show();
                return;
            }

            String targetUrl;
            String title;
            if (currentPublisher == PUBLISHER_OKYANUS) {
                targetUrl = "https://www.akilliogretim.com/VideoList/" + code;
                title = "Okyanus Test " + code;
            } else {
                targetUrl = "https://altinkarma.video-cozum.com/soru/" + code;
                title = "Altın Karma " + code;
            }

            openPlayer(targetUrl, title);
        });
    }

    private void openPlayer(String url, String title) {
        Intent intent = new Intent(this, TVPlayerActivity.class);
        intent.putExtra("TARGET_URL", url);
        intent.putExtra("PAGE_TITLE", title);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Kumandanın geri tuşunda numaratörde sayı varsa tek tek sil
        if (keyCode == KeyEvent.KEYCODE_BACK && barcodeBuilder.length() > 0) {
            barcodeBuilder.deleteCharAt(barcodeBuilder.length() - 1);
            txtBarcodeDisplay.setText(barcodeBuilder.toString());
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
