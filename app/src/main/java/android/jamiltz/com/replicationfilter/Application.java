package android.jamiltz.com.replicationfilter;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.ReplicationFilter;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.auth.AuthenticatorFactory;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by jamesnocentini on 06/02/15.
 */
public class Application extends android.app.Application {

    private String TAG = "todolite";

    private String SYNC_URL = "http://10.0.2.2:4984/testdb";

    private Database db;

    @Override
    public void onCreate() {
        super.onCreate();

        Manager manager = null;
        try {
            manager = new Manager(new AndroidContext(getApplicationContext()), Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            Log.e(TAG, "cannot instantiate manager", e);
        }

        try {
            db = manager.getDatabase("testdb");
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        // create 10 documents and delete 5
        for(int i = 0; i < 10; i++){

            Document doc = null;
            if (i % 2 == 0) {
                doc = createDocument(i, true);
            } else {
                doc = createDocument(i, false);
            }

            if (i % 2 == 0) {
                try {
                    doc.delete();
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }
        }

        // start replication
        startPushWithFilterFunc();

    }

    private Document createDocument(int number, boolean flag) {

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Calendar calendar = GregorianCalendar.getInstance();
        String currentTimeString = dateFormatter.format(calendar.getTime());

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("type", "test_doc");
        properties.put("created_at", currentTimeString);
        if (flag == true) {
            properties.put("name", "Waldo");
        }

        Document document = db.getDocument(String.valueOf(number));

        try {
            document.putProperties(properties);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        return document;
    }

    private void startPushWithFilterFunc() {

        URL syncUrl = null;
        try {
            syncUrl = new URL(SYNC_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Replication pushReplication = db.createPushReplication(syncUrl);
        db.setFilter("unDeleted", new ReplicationFilter() {
            @Override
            public boolean filter(SavedRevision savedRevision, Map<String, Object> stringObjectMap) {
                return !savedRevision.isDeletion();
            }
        });
        pushReplication.setContinuous(false);
        pushReplication.setFilter("unDeleted");

        pushReplication.start();

        db.setFilter("byOwner", new ReplicationFilter() {
            @Override
            public boolean filter(SavedRevision revision, Map<String, Object> params) {
                String nameParam = (String) params.get("name");
                return nameParam != null && nameParam == revision.getProperty("owner");
            }
        });


        pushReplication.start();

    }

    private void startPushWithArrayOfIds() {

        URL syncUrl = null;
        try {
            syncUrl = new URL(SYNC_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        db.setFilter("byOwner", new ReplicationFilter() {
            @Override
            public boolean filter(SavedRevision revision, Map<String, Object> params) {
                String nameParam = (String) params.get("name");
                return false;
            }
        });

        Replication pushReplication = db.createPushReplication(syncUrl);

        List<String> ids = Arrays.asList("2");
        Document doc = db.getDocument("2");
        System.out.print(doc);
        pushReplication.setDocIds(ids);

        pushReplication.start();

    }

    private void startPushWithFilterFuncWithParams() {
        URL syncUrl = null;
        try {
            syncUrl = new URL(SYNC_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Replication pushReplication = db.createPushReplication(syncUrl);

        db.setFilter("byOwner", new ReplicationFilter() {
            @Override
            public boolean filter(SavedRevision revision, Map<String, Object> params) {
                String nameParam = (String) params.get("name");
                return nameParam != null && nameParam == revision.getProperty("owner");
            }
        });

        pushReplication.setFilter("byOwner");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "Waldo");

        pushReplication.setFilterParams(params);

        pushReplication.start();

    }

}
