package com.enrandomlabs.jasensanders.v1.shopenator;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.enrandomlabs.jasensanders.v1.shopenator.database.DataContract;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //Loader state represents ViewType State
    private int mListLoader = 0;
    //Triggers a search based on the above Loader/View state
    private static final int SEARCH_LOADER = 3500;

    //State variables
    private String mSortOrder = DataContract.ItemEntry.COLUMN_ADD_DATE + DataContract.DESC;
    private Fragment mCurrentFragment;
    private int mPermission;
    private boolean mTwoPaneMode = false;


    //Views
    private SearchView mSearchView;
    private TextView mEmptyView;
    private RecyclerView itemList;
    private ListItemAdapter listItemAdapter;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    public MainListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainListFragment newInstance(String param1, String param2) {
        MainListFragment fragment = new MainListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_main_list, container, false);

        // Set two pane mode
        mTwoPaneMode = (root.findViewById(R.id.detail_container) != null);
        // Initialize empty view
        mEmptyView = root.findViewById(R.id.recyclerView_empty);

        // Initialize recycler view
        itemList = root.findViewById(R.id.content_list);

        // Load available data from ShopenatorProvider
        getLoaderManager().initLoader(mListLoader, null, this);



        return root;
    }



    @Override
    @NonNull
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {


        return new CursorLoader(getActivity(),
                DataContract.ItemEntry.buildUriAll(),
                DataContract.ITEMLIST_COLUMNS,
                null,
                null,
                mSortOrder);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data.moveToFirst()) {

            mEmptyView.setVisibility(View.GONE);
            itemList.setVisibility(View.VISIBLE);
            listItemAdapter = new ListItemAdapter(getActivity());
            listItemAdapter.swapCursor(data);

            //This is only used in TWO PANE mode
            listItemAdapter.setOnItemClickedListener(new ListItemAdapter.OnItemClickedListener() {
                @Override
                public void onItemClicked(Uri data, String status) {
                    launchTwoPaneFragment(data, status);
                }
            });
            itemList.setAdapter(listItemAdapter);
            LinearLayoutManager list = new LinearLayoutManager(getActivity());
            list.setOrientation(LinearLayoutManager.VERTICAL);
            itemList.setLayoutManager(list);
            itemList.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL, R.drawable.line_divider));

        } else {

            itemList.setAdapter(null);
            itemList.removeAllViews();
            itemList.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        if(listItemAdapter != null) {
            listItemAdapter.swapCursor(null);
        }

    }

    //Only used in TWO PANE mode
    public void launchTwoPaneFragment(Uri data, String status) {

        //its a two pane view so load the fragment
        AddNewFragment fragment = AddNewFragment.newInstance(data);
        mCurrentFragment = fragment;
        getFragmentManager().beginTransaction()
                .replace(R.id.detail_container, fragment)
                .commit();

    }
}
