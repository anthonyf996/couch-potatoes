package com.example.potato.couchpotatoes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {

    private enum MESSAGE_TYPE {SENT, RECEIVED};

    private final int MESSAGE_FETCH_LIMIT = 50;

    private LinearLayout layout;
    private ImageView sendButton;
    private EditText messageArea;
    private ScrollView scrollView;
    private DBHelper helper = new DBHelper();
    private String userID, chatRoom, displayName, messageID, timestamp, message, companion;
    private TextView userName;
    private Button b_select_spinner;

    private ArrayList<String> messageTime = new ArrayList<>();
    private Map<String,Boolean> messageIds = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // places toolbar into the screen
        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);

        // adds up navigation to the toolbar on top
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Activity title will not show in the toolbar
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // hide the keyboard until the user wants it
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // get views
        b_select_spinner = findViewById(R.id.b_select_spinner);
        layout = findViewById(R.id.layout_message_box);
        sendButton = findViewById(R.id.sendButton);
        messageArea = findViewById(R.id.messageArea);
        scrollView = findViewById(R.id.scrollView);

        // Get the current user's display name
        displayName = helper.getAuthUserDisplayName();

        // Get the current user's id
        userID = helper.getAuth().getUid();

        // Get the current chat room's id
        chatRoom = (String) getIntent().getExtras().get("chatID");
        // Get the other person in the chat
        companion = (String) getIntent().getExtras().get("otherUsers");

        // Display the current user's display name
        userName = findViewById(R.id.userName);
        userName.setText(companion);

        // Check if a spinner sent a message
        String spinner_message = getIntent().getStringExtra("message");
        if(!(spinner_message.equals("1"))){
            messageID = helper.getNewChildKey(helper.getChatMessagePath() + chatRoom);
            timestamp = helper.getNewTimestamp();
            message = spinner_message;

            // Clear the message text field on submitting a message
            messageArea.setText("");

            // Add the message to the chat.
            // Message data will be sent to the Firebase Database accordingly.
            if (!(message.equals(""))) {
                helper.addToChatMessage(chatRoom, messageID);
                helper.addToMessage(messageID, userID, displayName, chatRoom, timestamp, message);
            }
        }

        // Send button for messages
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageID = helper.getNewChildKey(helper.getChatMessagePath() + chatRoom);
                timestamp = helper.getNewTimestamp();
                message = messageArea.getText().toString();

                // Clear the message text field on submitting a message
                messageArea.setText("");

                // Add the message to the chat.
                // Message data will be sent to the Firebase Database accordingly.
                if (!(message.equals(""))) {
                    helper.addToChatMessage(chatRoom, messageID);
                    helper.addToMessage(messageID, userID, displayName, chatRoom, timestamp, message);
                }
            }
        });

        helper.fetchChat(chatRoom, new SimpleCallback<DataSnapshot>() {
            @Override
            public void callback(DataSnapshot data) {
                for (DataSnapshot messageSnapshot : data.getChildren()) {
                    String messageId = messageSnapshot.getKey();

                    // Only add new messages to scroll view
                    if (messageIds.get(messageId) != null) {
                        continue;
                    } else {
                        messageIds.put(messageId, true);
                    }

                    // Fetch all information corresponding to the current message
                    helper.fetchMessage(messageId, new SimpleCallback<DataSnapshot>() {
                        @Override
                        public void callback(DataSnapshot messageData) {
                            String from = (String) messageData.child("name").getValue();
                            String message = (String) messageData.child("text").getValue();
                            String timestamp = (String) messageData.child("timestamp").getValue();
                            String timeString = "";

                            //Compare the last msg timestamp with the cur one, add timestamp if theres a gap
                            if (messageTime.size() >= 1) {
                                timeString = getTimeString(messageTime.get(messageTime.size() - 1), timestamp);
                            } else {
                                timeString = getTimeString(timestamp);
                            }

                            if (!(timeString.equals(""))) {
                                displayTime(timeString);
                            }

                            if (from.equals(displayName)) {
                                addMessageBox(message, MESSAGE_TYPE.SENT);
                            } else {
                                addMessageBox(message, MESSAGE_TYPE.RECEIVED);
                            }

                            //Keep track of the time of the current message
                            messageTime.add(timestamp);
                        }
                    });
                }
            }
        });

        // OnClick to get to spinners
        b_select_spinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog();
            }
        });

        // force scroll view to bottom on creation
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    /*
     * TODO
     */
    private void createDialog(){
        //DIALOG FOR SPINNER
        String spinners[] = {"Spin the Wheel: Food", "Spin the Wheel: Activity", "Spin the Bottle: Nice","Spin the Bottle: Naughty"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
        builder.setIcon(R.mipmap.empty_wheel)
                .setTitle("Choose Your Spinner!")
                .setItems(spinners, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent;
                        switch (which) {
                            case 0:
                                intent = new Intent(((Dialog) dialog).getContext(), SpinToChooseActivity.class);
                                openSpinner(0, intent);
                                break;
                            case 1:
                                intent = new Intent(((Dialog) dialog).getContext(), SpinToChooseActivity.class);
                                openSpinner(1, intent);
                                break;
                            case 2:
                                intent = new Intent(((Dialog) dialog).getContext(), SpinToChooseActivity.class);
                                openSpinner(2, intent);
                                break;
                            case 3:
                                intent = new Intent(((Dialog) dialog).getContext(), SpinToChooseActivity.class);
                                openSpinner(3, intent);
                                break;
                        }
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,	int which) {
                        // Do something else
                    }
                });
        builder.create().show();
    }

    /*
     * TODO
     */
    private void openSpinner(int key, Intent intent) {
        intent.putExtra("key", key);
        intent.putExtra( "chatID", chatRoom );
        intent.putExtra("otherUsers", companion);
        finish();
        startActivity(intent);
    }

    /*
     * TODO: Add description
     */
    private String getTimeString(String lastMsg, String curMsg) {

        String timeStr = "";
        String hourStr = "";
        String lastMsgDate = lastMsg.split("  ")[0];
        String curMsgDate = curMsg.split("  ")[0];
        String lastMsgTime = lastMsg.split("  ")[1];
        String curMsgTime = curMsg.split("  ")[1];

        int intLastMsgDate = Integer.parseInt(lastMsgDate.replaceAll("-", ""));
        int intCurMsgDate = Integer.parseInt(curMsgDate.replaceAll("-", ""));
        int intLastMsgTime = Integer.parseInt(lastMsgTime.replaceAll(":", ""));
        int intCurMsgTime = Integer.parseInt(curMsgTime.replaceAll(":", ""));

        String[] date = curMsgDate.split("-");

        // TODO Throw this into a method
        //Convert 24 Hour format to AM/PM
        String dateStr = curMsgTime;
        DateFormat readFormat = new SimpleDateFormat( "HH:mm:ss");
        DateFormat writeFormat = new SimpleDateFormat( "hh:mm a");
        try {
            hourStr = writeFormat.format(readFormat.parse(dateStr));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // TODO throw a lot of this into methods
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String curDate = df.format(c.getTime()).split(" ")[0];
        int intCurDate = Integer.parseInt(curDate.replaceAll("-", ""));

        // TODO pls guys methods they are a thing
        if (intCurDate - intCurMsgDate < 1) {
            if (Math.abs(intCurMsgTime - intLastMsgTime) >= 30000) {
                timeStr = hourStr;

            }
            //If the message has been longer than 1 day
        } else if ((intCurMsgDate - intLastMsgDate) >=1) {
            String monthString = getMonth(date[1]);
            //Remove the 0 in front of day 01, 02, etc.
            int day = Integer.parseInt(date[2]);
            timeStr = monthString + " " + day + ", " + hourStr;

        }
        //If the message has been longer than 3 hours
        else if ((intCurMsgTime - intLastMsgTime) >= 30000) {
            timeStr = hourStr;
        }
        return timeStr;
    }

    /**
     * Display a message
     * @param message content
     * @param type whether it is a sent message or a received message
     */
    private void addMessageBox (String message, MESSAGE_TYPE type){
        TextView textView = new TextView(MessageActivity.this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1.0f;

        if (type == MESSAGE_TYPE.SENT) {
            textView.setText(message);
            textView.setTextSize(20);
            layoutParams.gravity = Gravity.END;
            textView.setBackgroundResource(R.drawable.new_bubble_in);
            textView.setTextColor(Color.WHITE);
        } else {
            textView.setText(message);
            textView.setTextSize(20);
            layoutParams.gravity = Gravity.START;
            textView.setBackgroundResource(R.drawable.new_bubble_out);
            textView.setTextColor(Color.BLACK);
        }

        textView.setLayoutParams(layoutParams);
        layout.addView(textView);

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    /**
     * Display time
     * @param time
     */
    private void displayTime(String time) {
        TextView textView = new TextView(MessageActivity.this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        textView.setTextColor(Color.GRAY);
        textView.setTextSize(14);
        textView.setText(time);
    }

    /*
     * Description: Edge case: This executes for the very first time a message is sent. Date
     * should always be displayed for first message
     */
    private String getTimeString(String curMsg) {
        String timeStr = "";
        String hourStr = "";
        String curMsgDate = curMsg.split(" {2}")[0];
        String curMsgTime = curMsg.split(" {2}")[1];
        String[] date = curMsgDate.split("-");
        String[] time = curMsgTime.split(":");

        hourStr = toAmPm(time);

        // get Month
        String monthString = getMonth(date[1]);
        int day = Integer.parseInt(date[2]);
        timeStr = monthString + " " + day + ", " + hourStr;

        return timeStr;
    }

    /**
     * Returns the time in AM PM format
     * @param time to convert
     * @return the time in AM PM format
     */
    private String toAmPm(String[] time) {
        int hour = Integer.parseInt(time[0]);
        if (hour >= 12) {
            return (hour - 12) + ":" + time[1] + " PM";
        } else {
            return time[0] + ":" + time[1] + " AM";
        }
    }

    /**
     * gets the month's abbreviation.
     * @param month string of the month number.
     * @return a string abbreviation of the month.
     */
    private String getMonth(String month) {
        String monthString;
        switch (month) {
            case "1":
                monthString = "Jan";
                break;
            case "2":
                monthString = "Feb";
                break;
            case "3":
                monthString = "Mar";
                break;
            case "4":
                monthString = "Apr";
                break;
            case "5":
                monthString = "May";
                break;
            case "6":
                monthString = "Jun";
                break;
            case "7":
                monthString = "Jul";
                break;
            case "8":
                monthString = "Aug";
                break;
            case "9":
                monthString = "Sep";
                break;
            case "10":
                monthString = "Oct";
                break;
            case "11":
                monthString = "Nov";
                break;
            case "12":
                monthString = "Dec";
                break;
            default:
                monthString = "Invalid month";
                break;
        }
        return monthString;
    }
}


