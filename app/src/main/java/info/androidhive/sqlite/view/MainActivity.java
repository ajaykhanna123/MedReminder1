package info.androidhive.sqlite.view;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import info.androidhive.sqlite.R;
import info.androidhive.sqlite.database.DatabaseHelper;
import info.androidhive.sqlite.database.model.Note;
import info.androidhive.sqlite.utils.AlarmReceiver;
import info.androidhive.sqlite.utils.MyDividerItemDecoration;
import info.androidhive.sqlite.utils.RecyclerTouchListener;

public class MainActivity extends AppCompatActivity{
    private NotesAdapter mAdapter;
    private List<Note> notesList = new ArrayList<>();
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private TextView noNotesView;

    private DatabaseHelper db;
    final Calendar[] ccc = {Calendar.getInstance()};
    AlarmManager alarmManager;
    String taskName="";
    private static MainActivity inst;

    public static MainActivity instance() {
        return inst;
    }

//    final EditText txtTime=(EditText)findViewById(R.id.totime);
//    final Button btnTimePicker=(Button)findViewById(R.id.btn_t);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = findViewById(R.id.coordinator_layout);
        recyclerView = findViewById(R.id.recycler_view);
        noNotesView = findViewById(R.id.empty_notes_view);

        db = new DatabaseHelper(this);

        notesList.addAll(db.getAllNotes());


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoteDialog(false, null, -1);

            }
        });



        mAdapter = new NotesAdapter(this, notesList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        toggleEmptyNotes();

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        /**
         * On long press on RecyclerView item, open alert dialog
         * with options to choose
         * Edit and Delete
         * */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));
    }

    /**
     * Inserting new note in db
     * and refreshing the list
     */
    private void createNote(String note,String date,String time) {


        // inserting note in db and getting
        // newly inserted note id
        long id = db.insertNote(note,date,time);

        // get the newly inserted note from db
        Note n = db.getNote(id);

        if (n != null) {
            // adding new note to array list at 0 position
            notesList.add(0, n);

            // refreshing the list
            mAdapter.notifyDataSetChanged();

            toggleEmptyNotes();
        }
    }

    /**
     * Updating note in db and updating
     * item in the list by its position
     */
    private void updateNote(String note,String date,String time, int position) {
        Note n = notesList.get(position);
        // updating note text
        n.setNote(note);
        n.setDate(date);
        n.setTime(time);
        // updating note in db
        db.updateNote(n);

        // refreshing the list
        notesList.set(position, n);
        mAdapter.notifyItemChanged(position);

        toggleEmptyNotes();
    }

    /**
     * Deleting note from SQLite and removing the
     * item from the list by its position
     */
    private void deleteNote(int position) {
        // deleting the note from db
        db.deleteNote(notesList.get(position));

        // removing the note from the list
        notesList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyNotes();
    }

    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 0
     */
    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showNoteDialog(true, notesList.get(position), position);
                } else {
                    deleteNote(position);
                }
            }
        });
        builder.show();
    }


    /**
     * Shows alert dialog with EditText options to enter / edit
     * a note.
     * when shouldUpdate=true, it automatically displays old note and changes the
     * button text to UPDATE
     */
    private void showNoteDialog(final boolean shouldUpdate, final Note note, final int position) {

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.note_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputNote = view.findViewById(R.id.note);
//         EditText Date =view.findViewById(R.id.toDate);
//         EditText Time=view.findViewById(R.id.totime);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_note_title) : getString(R.string.lbl_edit_note_title));

        final Button btnDatePicker=view.findViewById(R.id.btn_d);
        final EditText txtDate=view.findViewById(R.id.toDate);
        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                txtDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });


        final Button btnTimePicker=view.findViewById(R.id.btn_t);
        final EditText txtTime=view.findViewById(R.id.totime);
        btnTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);
                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                ccc[0].set(Calendar.HOUR_OF_DAY, hourOfDay);
                                ccc[0].set(Calendar.MINUTE, minute);

                                txtTime.setText(hourOfDay + ":" + minute);

                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
        });


        if (shouldUpdate && note != null) {
            inputNote.setText(note.getNote());
            txtDate.setText(note.getDate());
            txtTime.setText(note.getTime());
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "update" : "save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show toast message when no text is entered
                if (TextUtils.isEmpty(inputNote.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Enter note!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }



                // check if user updating note
                if (shouldUpdate && note != null) {
                    // update note by it's id
                    updateNote(inputNote.getText().toString(),txtDate.getText().toString(),txtTime.getText().toString(), position);
                } else {
                    // create new note
                    createNote(inputNote.getText().toString(),txtDate.getText().toString(),txtTime.getText().toString());
                    if(inputNote.getText().toString().length()>0 && ccc[0]!=null) {
                        taskName = inputNote.getText().toString();
                        Log.d("calendar_time", "onClick: " + ccc[0].getTimeInMillis());
                        setAlarm(ccc[0].getTimeInMillis(), inputNote.getText().toString());
                        //scheduleNotification(MainActivity.this,ccc.getTimeInMillis(), (int) System.currentTimeMillis());

                        Toast.makeText(MainActivity.this, "Task Added..", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "All details required.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }
//    public void calltime(){
//        Calendar c = Calendar.getInstance();
//        int mHour = c.get(Calendar.HOUR_OF_DAY);
//        int mMinute = c.get(Calendar.MINUTE);
//
//        // Launch Time Picker Dialog
//        TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
//                new TimePickerDialog.OnTimeSetListener() {
//
//                    @Override
//                    public void onTimeSet(TimePicker view, int hourOfDay,
//                                          int minute) {
//
//                        txtTime.setText(hourOfDay + ":" + minute);
//                    }
//                }, mHour, mMinute, false);
//        timePickerDialog.show();
//    }
    /**
     * Toggling list and empty notes view
     */
    private void toggleEmptyNotes() {
        // you can check notesList.size() > 0

        if (db.getNotesCount() > 0) {
            noNotesView.setVisibility(View.GONE);
        } else {
            noNotesView.setVisibility(View.VISIBLE);
        }
    }
    //----------------------alarm code------------------

    public void setAlarm(long timeInMillis, String task)
    {
        Log.d("notif_task", "setAlarm: "+timeInMillis);
        AlarmManager manager= (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent=new Intent(MainActivity.this, AlarmReceiver.class);
        intent.putExtra("task",task);
        intent.putExtra("millis",(int) timeInMillis);
        final int _id = (int) System.currentTimeMillis();
//        Log.d("", "setAlarm: ");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int)timeInMillis, intent, PendingIntent.FLAG_ONE_SHOT);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent);
        //manager.set(AlarmManager.RTC, timeInMillis, pendingIntent);
        Date date = new Date(timeInMillis);
        Log.d("Date:", date.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else {
            manager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }



}





