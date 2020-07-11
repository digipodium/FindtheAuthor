package digipodium.findtheauthor;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private Button btnSearch;
    private TextView textAuthor;
    private TextView textBookName;
    private EditText editBookName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSearch = findViewById(R.id.btnSearch);
        textBookName = findViewById(R.id.textBookName);
        textAuthor = findViewById(R.id.textAuthorName);
        editBookName = findViewById(R.id.editBookName);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBooks();
            }
        });
    }

    public void searchBooks() {
        String queryString = editBookName.getText().toString();
        new FetchBook(textAuthor, textBookName).execute(queryString);
    }

    // create a Asynctask class
    public class FetchBook extends AsyncTask<String, Void, String> {

        private WeakReference<TextView> textTitle;
        private WeakReference<TextView> textAuthor;
        //constructor


        public FetchBook(TextView textTitle, TextView textAuthor) {
            this.textTitle = new WeakReference<>(textTitle);
            this.textAuthor = new WeakReference<>(textAuthor);
        }

        @Override
        protected String doInBackground(String... query) {
            // make a request to API server
            return NetworkUtils.getBookInfo(query[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            // Parse the JSON data
            super.onPostExecute(result);
            try {
                // 1. raw string to JSONObject
                JSONObject jsonObject = new JSONObject(result);
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
                if(title!=null && authors !=null){
                    textTitle.get().setText(title);
                    textAuthor.get().setText(authors);
                }else{
                    textTitle.get().setText("Unknown Book Name");
                    textAuthor.get().setText("No results found");
                }

            } catch (Exception e) {
                e.printStackTrace();
                textTitle.get().setText("API error");
                textAuthor.get().setText("Please check Logcat");
            }
        }
    }


}