package info.androidhive.sqlite.database.model;

/**
 * Created by ravi on 20/02/18.
 */

public class Note {
    public static final String TABLE_NAME = "notes";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_DATE = "date";

    private int id;
    private String note;
    private String timestamp;
    private String time;
    private String date;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NOTE + " TEXT,"
                    + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"+
                    COLUMN_DATE + " DATE,"
                    +COLUMN_TIME+" TIME"
                    + ")";

    public Note() {
    }

    public Note(int id, String note, String timestamp,String date,String time) {
        this.id = id;
        this.note = note;
        this.timestamp = timestamp;
        this.date=date;
        this.time=time;
    }

    public int getId() {
        return id;
    }

    public String getNote() {
        return note;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDate(String date) {
        this.date= date;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
