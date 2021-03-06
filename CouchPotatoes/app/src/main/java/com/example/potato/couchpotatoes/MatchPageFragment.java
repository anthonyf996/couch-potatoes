package com.example.potato.couchpotatoes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Map;

import static com.example.potato.couchpotatoes.StringUtilities.paddSpace;

public class MatchPageFragment extends Fragment {
    private static final String ARG_LIST = "ARG_LIST";

    private final int BIO_SUBSTRING_LENGTH = 60;

    private ArrayList<String> matchedUserList;
    private DBHelper dbHelper;
    private boolean isDating;

    private String currMatchID;
    private TextView bioText;
    private TextView interestsHeader;
    private TextView interestsText;
    private TextView userInfoText;

    /**
     * Create a new fragment while passing in a list of the strings of the matched users
     *
     * @param matchedUserList - list of matched users as strings
     * @param isDating - true if this is the dating page, false if it's the friend page
     * @return new MatchPageFragment object
     */
    public static MatchPageFragment newInstance(ArrayList<String> matchedUserList, boolean isDating) {
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_LIST, matchedUserList);
        args.putBoolean("Is_Dating", isDating);
        MatchPageFragment fragment = new MatchPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DBHelper.getInstance();

        // Gets the matched user list
        matchedUserList = getArguments().getStringArrayList(ARG_LIST);
        isDating = getArguments().getBoolean("Is_Dating");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_match_page, container, false);
        bioText = view.findViewById(R.id.bioText);
        interestsHeader = view.findViewById(R.id.interestsHeader);
        interestsText = view.findViewById(R.id.interestsText);
        userInfoText = view.findViewById(R.id.userInfoText);
        LinearLayout matchingUserInfoLayout = view.findViewById(R.id.matchingUserInfoLayout);

        // If the user doesn't have a list of potential matches, tell them to update their profile
        if ( matchedUserList.isEmpty() ) {
            userInfoText.setText(R.string.no_new_matches_message);
        }

        // Otherwise, populate the fragment
        else {
            // Get the first user on the list
            currMatchID = matchedUserList.get( 0 );

            // Creates a gesture listener for the user text
            matchingUserInfoLayout.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {
                @Override
                public void onClick() {
                    // Begin new activity to display more information about the potential match
                    Intent intent = new Intent( getActivity().getApplicationContext(), MatchUserInfoActivity.class );
                    intent.putExtra( "currMatchID", currMatchID );
                    startActivity( intent );
                }

                /**
                 * Swipe left to dislike the user
                 */
                @Override
                public void onSwipeLeft() {
                    String currUserID = dbHelper.getAuth().getUid();
                    String potentMatchID = matchedUserList.get(0);
                    String timestamp = dbHelper.getNewTimestamp();

                    Toast.makeText(getActivity(), "Disliked!", Toast.LENGTH_SHORT).show();
                    dbHelper.addToDislike(currUserID, potentMatchID, timestamp);
                }

                /**
                 * Swipe right to like the user
                 */
                @Override
                public void onSwipeRight() {
                    String currUserID = dbHelper.getAuth().getUid();
                    String potentMatchID = matchedUserList.get(0);
                    String timestamp = dbHelper.getNewTimestamp();

                    Toast.makeText(getActivity(), "Liked!", Toast.LENGTH_SHORT).show();
                    dbHelper.addToLike(currUserID, potentMatchID, timestamp);

                    // If this is the dating page, add to dating like list
                    if (isDating)
                        dbHelper.addToDate( currUserID, potentMatchID, timestamp );

                        // Otherwise, add to friend like list
                    else
                        dbHelper.addToBefriend(currUserID, potentMatchID, timestamp);
                }
            });

            // Fetch and display info about the potential match
            dbHelper.fetchMatchInfo(currMatchID, new SimpleCallback<Map<String, Object>>() {
                @Override
                public void callback(Map<String, Object> data) {
                    String firstName = (String) data.get( "firstName" );
                    String lastName = (String) data.get( "lastName" );
                    String gender = (String) data.get( "gender" );
                    String bio = (String) data.get( "bio" );
                    String userInfo = "";
                    String genderAbbrev = "";

                    // Abbreviate gender. If non-binary, do not mention gender
                    if ( gender.equals( "male" ) )
                        genderAbbrev= "M";
                    else if ( gender.equals( "female" ) )
                        genderAbbrev = "F";

                    // Get the potential match's full name
                    // Omit the middle name - Personal preference - Can change later
                    String potentMatchName = dbHelper.getFullName( firstName, "", lastName );

                    // Display potential match's full name and gender on the same line
                    int numSpaces = 30;
                    userInfo += paddSpace( potentMatchName, genderAbbrev, numSpaces );
                    userInfoText.setText( userInfo );

                    // Display substring of bio here and full bio in MatchUserInfoActivity
                    if ( bio.length() <= BIO_SUBSTRING_LENGTH ) {
                        bioText.setText( bio );
                    }
                    else {
                        String bioSubString = bio.substring( 0, BIO_SUBSTRING_LENGTH ) + " ...";
                        bioText.setText( bioSubString );
                    }

                    String interestsHeaderStr = "Interests";
                    interestsHeader.setText( interestsHeaderStr );

                    // Fetch and display User's Interests
                    dbHelper.fetchMatchInterests(currMatchID, new SimpleCallback<DataSnapshot>() {
                        @Override
                        public void callback(DataSnapshot data) {
                            InterestStringBuilder builder = new InterestStringBuilder();
                            String interests = builder.getInterestString(data) ;
                            interestsText.setText( interests );
                        }
                    });
                }
            });
        }
        return view;
    }
}