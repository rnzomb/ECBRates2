package zzzomb.ecbratesall;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends ListActivity {

    public static final String URL = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
    public static final String TAG_TO_SEARCH = "Cube";
    public static final String PREFS_NAME = "SharedPrefsFile";
    public static ArrayList<Currency> currencyArrayList = new ArrayList<>();

    private SharedPreferences storedData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ПРОВЕРКА НА ДАТУ
        checkUpdates();

        // ЧТЕНИЕ ИЗ ЛОКАЛЬНОГО ФАЙЛА
        readFromLocal();

        String[] from = new String[]{Currency.NAME, Currency.RATE};
        int[] to = new int[]{R.id.nameView, R.id.rateView};

        // Read from xml
        // currencyArrayList = readFromLocal();

        // Read from db
        currencyArrayList = loadFromDB();

        ListAdapter adapter = new SimpleAdapter(this, currencyArrayList, R.layout.currency_list_item, from, to);
        setListAdapter(adapter);
    }

    private ArrayList<Currency> readFromLocal() {
        ArrayList<Currency> curList = new ArrayList<>();
        String filePath = getFilesDir() + "/" + "ECB.xml";
        File xmlFile = new File(filePath);
        FileInputStream fis = null;
        XmlPullParserFactory factory = null;

        try {
            fis = new FileInputStream(xmlFile);
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);

            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(fis, null);

            String currentTag = "";

            int eventType = xpp.getEventType();
            String currency;
            String rate;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                currentTag = xpp.getName();

                if (
                        currentTag != null &&
                                eventType == 2 &&
                                currentTag.equals(MainActivity.TAG_TO_SEARCH) &&
                                xpp.getAttributeCount() > 0
                        )
                {

                        currency = xpp.getAttributeValue(null, "currency");
                        rate = xpp.getAttributeValue(null, "rate");

                        if(currency != null && rate != null) {
                            curList.add(new Currency(currency, rate));
                        }

                }
                eventType = xpp.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return curList;
    }

    private void saveXmlToInternal() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(MainActivity.URL);

                    //create the new connection
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.connect();
                    File SDCardRoot = getFilesDir();

                    File file = new File(SDCardRoot, "ECB.xml");
                    FileOutputStream fileOutput = new FileOutputStream(file);
                    InputStream inputStream = urlConnection.getInputStream();
                    int totalSize = urlConnection.getContentLength();

                    //variable to store total downloaded bytes
                    int downloadedSize = 0;
                    //create a buffer...
                    byte[] buffer = new byte[1024];
                    int bufferLength = 0; //used to store a temporary size of the buffer

                    //now, read through the input buffer and write the contents to the file
                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        //add the data in the buffer to the file in the file output stream (the file on the sd card
                        fileOutput.write(buffer, 0, bufferLength);
                        //add up the size so we know how much is downloaded
                        downloadedSize += bufferLength;

                    }
                    //close the output stream when done
                    fileOutput.close();
                    //catch some possible errors...
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private void saveDate(String date) {
        storedData = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor storedDataEditor = storedData.edit();
        storedDataEditor.putString("lastDate", date);
        storedDataEditor.commit();
    }

    private void checkUpdates() {
        storedData = getSharedPreferences(PREFS_NAME, 0);
        String lastDate = storedData.getString("lastDate", null);
        String now = null;

        if (lastDate != null) {
             now = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

          //  if (now.equals(lastDate))
          //     return;

            if(now != null) {
                DBCurrency dbConnector = new DBCurrency(this);
                saveDate(now);
                saveXmlToInternal();
                dbConnector.insertData(readFromLocal());
            }
        }
    }

    public ArrayList<Currency> loadFromDB(){
        DBCurrency dbConnector = new DBCurrency(this);
        return dbConnector.getEcb();
    }

}
