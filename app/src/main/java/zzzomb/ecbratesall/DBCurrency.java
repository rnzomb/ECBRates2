package zzzomb.ecbratesall;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBCurrency extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ecb.db";
    private static final int DATABASE_VERSION = 1;
    public static final String ECB_TABLE = "ecb";
    public static final String UID = "_id";

    // Fields
    public static final String CURRENCYCODE = "currencycode";
    public static final String CURRENCYVALUE = "currencyvalue";

    public DBCurrency(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "
                + ECB_TABLE +
                " (" + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CURRENCYCODE + " VARCHAR(255), " + CURRENCYVALUE + " VARCHAR(255));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ECB_TABLE);
        onCreate(db);
    }

    public void insertData(ArrayList<Currency> currencyList) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues ecbValues = new ContentValues();
        db.execSQL("DELETE FROM " + ECB_TABLE);

        for(Currency cur : currencyList) {
            ecbValues.put(CURRENCYCODE, cur.get("name"));
            ecbValues.put(CURRENCYVALUE, cur.get("rate"));
            db.insert(ECB_TABLE, null, ecbValues);
        }

        db.close();
    }

    public ArrayList<Currency> getEcb() {
        ArrayList<Currency> ecb = new ArrayList();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + ECB_TABLE, null);
        if (cursor.moveToFirst()) {
            do {
                Currency currency = new Currency(cursor.getString(1), cursor.getString(2));
                ecb.add(currency);
            } while (cursor.moveToNext());
        }
        return ecb;
    }

}