package android.jamiltz.com.replicationfilter;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.ReplicationFilter;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jamesnocentini on 06/02/15.
 */
public class Application extends android.app.Application {

    private String SYNC_URL = "http://10.0.3.2:4984/testdb";

    private Database database;

    @Override
    public void onCreate() {
        super.onCreate();

        Manager manager = null;
        try {
            manager = new Manager(new AndroidContext(getApplicationContext()), Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            database = manager.getDatabase("testdb");
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        // create 10 documents and delete 5
        for(int i = 0; i < 10; i++){
            Document doc = createDocument(i);
            if (i % 2 == 0) {
                try {
                    doc.delete();
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }
        }

        // start replication
        startPushReplication();

    }

    private Document createDocument(int number) {

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Calendar calendar = GregorianCalendar.getInstance();
        String currentTimeString = dateFormatter.format(calendar.getTime());

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("type", "test_doc");
        properties.put("created_at", currentTimeString);
        
        Document document = database.getDocument(String.valueOf(number));

        try {
            document.putProperties(properties);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        return document;
    }

    private void startPushReplication() {

        URL syncUrl = null;
        try {
            syncUrl = new URL(SYNC_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Replication pushReplication = database.createPushReplication(syncUrl);
        database.setFilter("unDeleted", new ReplicationFilter() {
            @Override
            public boolean filter(SavedRevision savedRevision, Map<String, Object> stringObjectMap) {
                return !savedRevision.isDeletion();
            }
        });
        pushReplication.setContinuous(false);
        pushReplication.setFilter("unDeleted");

        pushReplication.start();
    }

}
