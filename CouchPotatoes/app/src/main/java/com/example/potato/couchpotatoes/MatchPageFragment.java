package com.example.potato.couchpotatoes;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchPageFragment extends Fragment {
    public static final String ARG_LIST = "ARG_LIST";

    private ArrayList<String> matchedUserList;
    private FloatingActionButton matchButton;
    private FloatingActionButton unmatchButton;
    private DBHelper helper;

    private String currMatchID;
    private TextView textView;
    private TextView genInfoHeader;
    private TextView bioHeader;
    private TextView bioText;
    private TextView interestsHeader;
    private TextView interestsText;

    private ImageView imgView;

    private String gUserInfo;

    /**
     * TODO: NOTE IF WE WANT TO PASS IN THE LIST DIRECTLY, WE NEED TO MAKE MATCHEDUSER EXTEND PARCELABLE
     * @param savedInstanceState
     */
//    public static MatchPageFragment newInstance(List<MatchedUser> matchedUserList) {
//        Bundle args = new Bundle();
//        args.putParcelableArrayList(ARG_LIST, matchedUserList);
//        MatchPageFragment fragment = new MatchPageFragment();
//        fragment.setArguments(args);
//        return fragment;
//    }

    /**
     * give it a list of the strings of the matched users
     * @param matchedUserList
     * @return
     */
    public static MatchPageFragment newInstance(ArrayList<String> matchedUserList ) {
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_LIST, matchedUserList);
        MatchPageFragment fragment = new MatchPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        helper = new DBHelper();

        matchedUserList = getArguments().getStringArrayList(ARG_LIST);

        matchButton = (FloatingActionButton) getActivity().findViewById(R.id.fab_match);
        unmatchButton = (FloatingActionButton) getActivity().findViewById(R.id.fab_unmatch);

        matchButton.setOnClickListener(onClickListener);
        unmatchButton.setOnClickListener(onClickListener);
    }
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            /* NOTE: MAY NOT NEED THIS
            String currUserID = helper.getAuth().getUid();
            String timestamp = "0000-00-00 00:00:00";

            Log.d( "TEST", matchType );

            switch (v.getId()) {
                case R.id.fab_match:
                    Log.d( "TEST", "LIKE" );
                    if ( matchType.equals( DATE_MATCH_TYPE ) ) {
                        helper.addToLike( currUserID, currDateMatchID, timestamp );
                    }
                    else if ( matchType.equals( FRIEND_MATCH_TYPE ) ) {
                        helper.addToLike( currUserID, currFriendMatchID, timestamp );
                    }
                    break;

                case R.id.fab_unmatch:
                    Log.d( "TEST", "DISLIKE" );
                    if ( matchType.equals( DATE_MATCH_TYPE ) ) {
                        helper.addToDislike( currUserID, currDateMatchID, timestamp );
                    }
                    else if ( matchType.equals( FRIEND_MATCH_TYPE ) ) {
                        helper.addToDislike( currUserID, currFriendMatchID, timestamp );
                    }
                    break;
            }
            */
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_match_page, container, false);
        textView = (TextView) view.findViewById(R.id.match_fragment_text);
        genInfoHeader = (TextView) view.findViewById(R.id.generalInfoHeader);
        bioHeader = (TextView) view.findViewById(R.id.bioHeader);
        bioText = (TextView) view.findViewById(R.id.bioText);
        interestsHeader = (TextView) view.findViewById(R.id.interestsHeader);
        interestsText = (TextView) view.findViewById(R.id.interestsText);

        if ( matchedUserList.isEmpty() ) {
            textView.setText( "No new matches. Try adding more interests!" );
        }
        else {
            currMatchID = matchedUserList.get( 0 );

            helper.getDb().getReference( helper.getUserPath() + currMatchID ).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, Object> res = new HashMap<>();

                    for ( DataSnapshot children : dataSnapshot.getChildren() ) {
                        res.put( children.getKey(), children.getValue() );
                    }

                    String generalInfoHeader = "General Info";

                    genInfoHeader.setText( generalInfoHeader );

                    String firstName = (String) res.get( "firstName" );
                    String middleName = (String) res.get( "middleName" );
                    String lastName = (String) res.get( "lastName" );
                    String gender = (String) res.get( "gender" );
                    String birth_date = (String) res.get( "birth_date" );
                    String bio = (String) res.get( "bio" );

                    //MatchedUser match = new MatchedUser( currMatchID, firstName, middleName, lastName, birth_date, gender, "", "", "", bio, 0, 0, false, false );

                    String userInfo = "";

                    // TODO Need a better way to format text
                    /*
                    String format = "%30s%30s\n";
                    userInfo += String.format( format, "First Name:", firstName );
                    userInfo += String.format( format, "Middle Name:", middleName );
                    userInfo += String.format( format, "Last Name:", lastName );
                    userInfo += String.format( format, "Gender:", gender );
                    userInfo += String.format( format, "Birth Day:", birth_date );
                    userInfo += String.format( format, "Bio:", bio );
                    */

                    // TODO Maybe fetch and display profile pic here also
                    //userInfo += "General Info:\n\n";
                    userInfo += paddSpace( "First Name:", "", 19 );
                    userInfo += firstName + "\n\n";
                    userInfo += paddSpace( "Middle Name:", "", 17 );
                    userInfo += middleName + "\n\n";
                    userInfo += paddSpace( "Last Name:", "", 18 );
                    userInfo += lastName + "\n\n";
                    userInfo += paddSpace( "Gender:", "", 19 );
                    userInfo += gender + "\n\n";
                    userInfo += paddSpace( "Birth Day:", "", 20 );
                    userInfo += birth_date + "\n";
                    //userInfo += paddSpace( "Bio:", "", 19 );
                    //userInfo += bio + "\n\n";

                    textView.setText( userInfo );

                    String bioHeaderStr = "Bio";

                    bioHeader.setText( bioHeaderStr );

                    bioText.setText( bio );

                    //gUserInfo = userInfo;

                    //gUserInfo += "\nInterests:";

                    String interestsHeaderStr = "Interests";

                    interestsHeader.setText( interestsHeaderStr );

                    // Fetch and display User's Interests
                    helper.getDb().getReference( helper.getUserInterestPath() ).child( currMatchID ).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String interests = "";

                            for ( DataSnapshot child : dataSnapshot.getChildren() ) {
                                String interest = child.getKey();
                                interests += interest;
                                interests += "\n\n";

                                for ( DataSnapshot subchild : child.getChildren() ) {
                                    String subcategory = subchild.getKey();
                                    String preference = (String) subchild.getValue();

                                    //Log.d( "TEST", "Interest: " + interest );
                                    //Log.d( "TEST", "Subcategory: " + subcategory );
                                    //Log.d( "TEST", "Preference: " + preference );

                                    /*
                                    if ( preference.equals( "like" ) || preference.equals( "like to try" ) ) {
                                        interests += "◆  ";
                                    }
                                    else {
                                        interests += "◇  ";
                                    }
                                    */
                                    int newLinePos = 22;
                                    interests += "◇  ";
                                    interests += addStrAtPos( subcategory, "\n     ", newLinePos );
                                    interests += "  -  ";
                                    interests += addStrAtPos( preference, "\n     ", newLinePos );
                                    interests += "\n";
                                }
                                interests += "\n";
                            }

                            //textView.setText( gUserInfo + "\n\n" + interests );
                            //textView.setText( userInfo );
                            //userInfo = "";
                            interestsText.setText( interests );
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d( "TEST", databaseError.getMessage() );
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d( "TEST", databaseError.getMessage() );
                }
            });
        }

        return view;
    }

    private String paddSpace( String title, String value, int desiredLength ) {
        String str = "";

        str += title;

        int numSpaces = desiredLength - title.length() - value.length();

        for ( int i = 0; i < numSpaces; i++ ) {
            str += "\t";
        }

        str += value;

        return str;
    }

    private String paddSpaceln( String title, String value, int desiredLength ) {
        return paddSpace( title, value + "\n", desiredLength );
    }

    private String paddSpaceEnd( String title, String value, int desiredLength ) {
        String str = "";

        str += title;

        str += value;

        int numSpaces = desiredLength - title.length() - value.length();

        for ( int i = 0; i < numSpaces; i++ ) {
            str += "\t";
        }

        str += "|";

        return str;
    }

    private String paddSpaceEndln( String title, String value, int desiredLength ) {
        return paddSpaceEnd( title, value, desiredLength ) + "\n";
    }

    private String addStrAtPos( String str, String addition, int position ) {
        String ret = "";

        for ( int i = 0; i < str.length(); i++ ) {
            if ( i == position ) {
                ret += addition;
            }
            ret += str.charAt( i );
        }

        return ret;
    }
}
