package digipodium.findtheauthor;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.GestureDetector;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    private Button btnSearch;
    private TextView textAuthor;
    private TextView textBookName;
    private EditText editBookName;

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSearch = findViewById(R.id.btnSearch);
        textBookName = findViewById(R.id.textBookName);
        textAuthor = findViewById(R.id.textAuthorName);
        editBookName = findViewById(R.id.editBookName);

        // handles orientation changes
        if (getSupportLoaderManager().getLoader(0) != null) {
            getSupportLoaderManager().initLoader(0, null, this);
        }

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InputMethodManager methodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (methodManager != null) {
                    methodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                if (connMgr != null) {
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        searchBooks();
                        textBookName.setText("");
                        textAuthor.setText("Loading...");
                    } else {
                        textBookName.setText("No network ! check connection");
                        textAuthor.setText("");
                    }
                }
            }
        });
    }

    public void searchBooks() {
        String queryString = editBookName.getText().toString();

        if (queryString.length() > 2) {
            Bundle queryBundle = new Bundle();
            queryBundle.putString("query", queryString);
            getSupportLoaderManager().restartLoader(0, queryBundle, this);
        } else {
            editBookName.setError("enter a book name");
            editBookName.requestFocus();
        }
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        String query = "";
        if (args != null) {
            query = args.getString("query");
        }
        return new BookLoader(this, query); // our loader class
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        try {
            // 1. raw string to JSONObject
            JSONObject jsonObject = new JSONObject(data);
            // 2. get the book data array
            JSONArray itemsArray = jsonObject.getJSONArray("items");

            // 3. loop variables
            int i = 0;
            String title = null;
            String authors = null;

            // 4. Look for results in array, exit when both title and author are found
            // or when all items are checked
            while (i < itemsArray.length() && (authors == null && title == null)) {
                JSONObject book = itemsArray.getJSONObject(i);
                JSONObject volume = book.getJSONObject("volumeInfo");
                try {
                    title = volume.getString("title");
                    authors = volume.getString("authors");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                i++;
            }
            if (title != null && authors != null) {
                textBookName.setText(title);
                textAuthor.setText(authors);
            } else {
                textBookName.setText("Unknown Book Name");
                textAuthor.setText("No results found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            textBookName.setText("API error");
            textAuthor.setText("Please check Logcat");
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    public void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

}