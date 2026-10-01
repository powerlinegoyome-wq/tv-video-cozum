package com.videocozum.tv;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements SourceAdapter.OnItemClickListener {

    private static final String API_BASE = "https://altinkarma.frns.in/mobile_solved/mobile_watch.php";
    private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 12) VideoCozumTV/1.0";

    public static class NavigationStep {
        public String id;
        public String title;
        public List<SourceItem> items;

        public NavigationStep(String id, String title, List<SourceItem> items) {
            this.id = id;
            this.title = title;
            this.items = items;
        }
    }

    private Stack<NavigationStep> navStack = new Stack<>();
    private List<SourceItem> currentItems = new ArrayList<>();

    // Yayınevi Sekmeleri
    private Button btnTabAltinKarma;
    private Button btnTabOkyanus;
    private TextView txtBadgeLine1;
    private TextView txtBadgeLine2;
    private LinearLayout containerAltinKarma;
    private LinearLayout containerOkyanus;

    // Altın Karma Görünümleri
    private View layoutHeaderBar;
    private ImageButton btnBack;
    private TextView txtHeaderTitle;
    private EditText edtSearch;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SourceAdapter adapter;

    // Okyanus Görünümleri
    private EditText edtOkyanusCode;
    private Button btnOkyanusGo;
    private Button btnOpenAkilliogretim;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_main);

        initViews();
        setupPublisherTabs();
        setupSearch();
        setupOkyanus();

        // Varsayılan olarak Altın Karma Kütüphanesini aç
        selectPublisher(true);
    }

    private void initViews() {
        btnTabAltinKarma = findViewById(R.id.btn_tab_altinkarma);
        btnTabOkyanus = findViewById(R.id.btn_tab_okyanus);
        txtBadgeLine1 = findViewById(R.id.txt_badge_line1);
        txtBadgeLine2 = findViewById(R.id.txt_badge_line2);
        containerAltinKarma = findViewById(R.id.container_altinkarma);
        containerOkyanus = findViewById(R.id.container_okyanus);

        layoutHeaderBar = findViewById(R.id.layout_header_bar);
        btnBack = findViewById(R.id.btn_back);
        txtHeaderTitle = findViewById(R.id.txt_header_title);
        edtSearch = findViewById(R.id.edt_search);
        recyclerView = findViewById(R.id.recycler_source_list);
        progressBar = findViewById(R.id.progress_loading);

        edtOkyanusCode = findViewById(R.id.edt_okyanus_code);
        btnOkyanusGo = findViewById(R.id.btn_okyanus_go);
        btnOpenAkilliogretim = findViewById(R.id.btn_open_akilliogretim);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SourceAdapter(this);
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> handleBack());
    }

    private void setupPublisherTabs() {
        btnTabAltinKarma.setOnClickListener(v -> selectPublisher(true));
        btnTabOkyanus.setOnClickListener(v -> selectPublisher(false));
    }

    private void selectPublisher(boolean isAltinKarma) {
        if (isAltinKarma) {
            btnTabAltinKarma.setSelected(true);
            btnTabOkyanus.setSelected(false);
            txtBadgeLine1.setText("ALTIN KARMA");
            txtBadgeLine2.setText("YAYINLARI");
            containerAltinKarma.setVisibility(View.VISIBLE);
            containerOkyanus.setVisibility(View.GONE);

            if (navStack.isEmpty()) {
                loadLevel("2", "", true);
            } else {
                recyclerView.post(() -> {
                    if (recyclerView.getChildCount() > 0) {
                        recyclerView.getChildAt(0).requestFocus();
                    }
                });
            }
        } else {
            btnTabAltinKarma.setSelected(false);
            btnTabOkyanus.setSelected(true);
            txtBadgeLine1.setText("OKYANUS");
            txtBadgeLine2.setText("YAYINCILIK");
            containerAltinKarma.setVisibility(View.GONE);
            containerOkyanus.setVisibility(View.VISIBLE);
            btnOpenAkilliogretim.requestFocus();
        }
    }

    private void setupSearch() {
        edtSearch.setOnClickListener(v -> {
            edtSearch.setFocusable(true);
            edtSearch.setFocusableInTouchMode(true);
            edtSearch.requestFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(edtSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupOkyanus() {
        btnOkyanusGo.setOnClickListener(v -> {
            String code = edtOkyanusCode.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(this, "Lütfen karekod numarasını girin!", Toast.LENGTH_SHORT).show();
                return;
            }
            openPlayer("https://www.akilliogretim.com/VideoList/" + code, "Okyanus Test " + code);
        });

        btnOpenAkilliogretim.setOnClickListener(v -> {
            openPlayer("https://www.akilliogretim.com", "Akıllı Öğretim - Okyanus");
        });
    }

    private void loadLevel(String id, String title, boolean isSourceList) {
        progressBar.setVisibility(View.VISIBLE);
        edtSearch.setText("");

        executor.execute(() -> {
            try {
                String action = isSourceList ? "source_list" : "content_list";
                String endpoint = API_BASE + "?action=" + action + "&id=" + id;

                HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
                conn.setRequestProperty("User-Agent", USER_AGENT);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject root = new JSONObject(sb.toString());

                if (isSourceList) {
                    JSONArray sources = root.optJSONArray("sources");
                    if (sources != null && sources.length() > 0) {
                        List<SourceItem> items = new ArrayList<>();
                        for (int i = 0; i < sources.length(); i++) {
                            JSONObject obj = sources.getJSONObject(i);
                            String sId = obj.optString("id");
                            String name = obj.optString("nm");
                            String pid = obj.optString("pid");
                            boolean parent = "true".equalsIgnoreCase(obj.optString("parent", "true"));
                            items.add(new SourceItem(sId, name, pid, parent));
                        }

                        mainHandler.post(() -> {
                            progressBar.setVisibility(View.GONE);
                            displayStep(new NavigationStep(id, title, items));
                        });
                        return;
                    } else {
                        // Eğer sources boşsa doğrudan soru listesini (content_list) yükle
                        loadLevel(id, title, false);
                        return;
                    }
                } else {
                    // Soru Listesi (Screenshot 6: 1. Soru, 2. Soru...)
                    JSONArray contents = root.optJSONArray("contents");
                    List<SourceItem> items = new ArrayList<>();
                    if (contents != null) {
                        for (int i = 0; i < contents.length(); i++) {
                            JSONObject obj = contents.getJSONObject(i);
                            String qId = obj.optString("id");
                            String name = obj.optString("nm");
                            String solvedType = obj.optString("solved_type");
                            String swf = obj.optString("swf");
                            String video = obj.optString("video");
                            String audio = obj.optString("audio");
                            String url = obj.optString("url");
                            items.add(new SourceItem(qId, name, solvedType, swf, video, audio, url));
                        }
                    }

                    mainHandler.post(() -> {
                        progressBar.setVisibility(View.GONE);
                        displayStep(new NavigationStep(id, title, items));
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Bağlantı hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayStep(NavigationStep step) {
        navStack.push(step);
        currentItems = step.items;
        adapter.setData(currentItems);

        // Başlık çubuğu görünürlüğü
        if (navStack.size() > 1) {
            layoutHeaderBar.setVisibility(View.VISIBLE);
            txtHeaderTitle.setText(step.title);
        } else {
            layoutHeaderBar.setVisibility(View.GONE);
        }

        // TV kumandası için ilk satıra otomatik odaklan (Turuncu çerçeve hemen belirir)
        recyclerView.post(() -> {
            if (recyclerView.getChildCount() > 0) {
                recyclerView.getChildAt(0).requestFocus();
            }
        });
    }

    @Override
    public void onItemClick(SourceItem item, int position) {
        if (item.type == SourceItem.TYPE_CATEGORY) {
            // Üst kategori (TYT, AYT, Sınıflar, Kitaplar) -> Alt kategorileri aç
            loadLevel(item.id, item.name, true);
        } else if (item.type == SourceItem.TYPE_TEST) {
            // Test veya Ders (Türkçe, Matematik, 1. Test vb.) -> Soruları yükle (content_list)
            loadLevel(item.id, item.name, false);
        } else if (item.type == SourceItem.TYPE_QUESTION) {
            // Soruya tıklandı -> Çözüm videosunu oynat
            openQuestionPlayer(item);
        }
    }

    private void openQuestionPlayer(SourceItem item) {
        String playerUrl = null;

        if ("fernus".equalsIgnoreCase(item.solvedType) || (item.video != null && !item.video.trim().isEmpty())) {
            String swf = (item.swf != null) ? item.swf : "";
            String pdf = swf.replace(".swf", ".pdf");
            String audio = (item.audio != null) ? item.audio : "";
            String video = (item.video != null) ? item.video : "";

            playerUrl = "https://altinkarma.frns.in/soru_cozum/web_player/?mp3=" + audio
                    + "&pdf=" + pdf
                    + "&swf=" + swf
                    + "&xaml=" + video;
        } else if (item.url != null && !item.url.trim().isEmpty()) {
            playerUrl = item.url;
        }

        if (playerUrl == null || playerUrl.trim().isEmpty()) {
            Toast.makeText(this, "Bu sorunun çözüm videosu henüz eklenmemiş!", Toast.LENGTH_SHORT).show();
            return;
        }

        openPlayer(playerUrl, item.name);
    }

    private void openPlayer(String url, String title) {
        Intent intent = new Intent(this, TVPlayerActivity.class);
        intent.putExtra("TARGET_URL", url);
        intent.putExtra("PAGE_TITLE", title);
        startActivity(intent);
    }

    private void handleBack() {
        if (navStack.size() > 1) {
            navStack.pop();
            NavigationStep prev = navStack.peek();
            currentItems = prev.items;
            adapter.setData(currentItems);
            edtSearch.setText("");

            if (navStack.size() > 1) {
                layoutHeaderBar.setVisibility(View.VISIBLE);
                txtHeaderTitle.setText(prev.title);
            } else {
                layoutHeaderBar.setVisibility(View.GONE);
            }

            recyclerView.post(() -> {
                if (recyclerView.getChildCount() > 0) {
                    recyclerView.getChildAt(0).requestFocus();
                }
            });
        } else {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (containerOkyanus.getVisibility() == View.VISIBLE) {
                selectPublisher(true);
                return true;
            }
            if (navStack.size() > 1) {
                handleBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
