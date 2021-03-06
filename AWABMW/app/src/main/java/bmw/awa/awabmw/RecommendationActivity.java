package bmw.awa.awabmw;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import android.transition.Transition;

import android.view.GestureDetector;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.extras.toolbar.MaterialMenuIconToolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class RecommendationActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    @Bind(R.id.tabs)
    TabLayout mTabLayout;
    @Bind(R.id.view_pager)
    ViewPager viewPager;
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private SearchView mSearchView;
    private MaterialMenuIconToolbar menuIcon;
    private LinearLayoutManager mLayoutManager;
    private boolean isDrawerOpened;

    private DrawerLayout mDrawerLayout;
    private RecyclerView mRecyclerView;
    private DrawerAdapter mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;


    @Bind(R.id.tool_back)
    ImageButton toolBack;

    private String[] factor = new String[]{"tempo", "album", "artist"};
    private Drawable[] iconSelector;

    //unimplement
    String playingArtist, playingTrack;
    private static final long ANIM_DURATION = 1000;
    private View bgViewGroup;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void setupLayout() {
        bgViewGroup = findViewById(R.id.drawer_layout);
    }

    private void setupWindowAnimations() {
        setupEnterAnimations();
        setupExitAnimations();
    }
    @TargetApi(21)
    private void setupEnterAnimations() {
        Transition enterTransition = getWindow().getSharedElementEnterTransition();
        enterTransition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                animateRevealShow(bgViewGroup);
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });
    }
    @TargetApi(21)
    private void setupExitAnimations() {
//        Transition sharedElementReturnTransition = getWindow().getSharedElementReturnTransition();
//        sharedElementReturnTransition.setStartDelay(ANIM_DURATION);


        Transition returnTransition = getWindow().getReturnTransition();
        returnTransition.setDuration(ANIM_DURATION);
        returnTransition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                animateRevealShow(bgViewGroup);
            }

            @Override
            public void onTransitionEnd(Transition transition) {}

            @Override
            public void onTransitionCancel(Transition transition) {}

            @Override
            public void onTransitionPause(Transition transition) {}

            @Override
            public void onTransitionResume(Transition transition) {}
        });
    }

    @TargetApi(21)
    private void animateRevealShow(View viewRoot) {
        int cx = (viewRoot.getLeft() + viewRoot.getRight()) / 2;
        int cy = (viewRoot.getTop() + viewRoot.getBottom()) / 2;
        int finalRadius = Math.max(viewRoot.getWidth(), viewRoot.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, cx, cy, 0, finalRadius);
        viewRoot.setVisibility(View.VISIBLE);
        anim.setDuration(ANIM_DURATION);
        anim.start();
    }

    @TargetApi(21)
    private void animateRevealHide(final View viewRoot) {
        int cx = (viewRoot.getLeft() + viewRoot.getRight()) / 2;
        int cy = (viewRoot.getTop() + viewRoot.getBottom()) / 2;
        int initialRadius = viewRoot.getWidth();

        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, cx, cy, initialRadius, 0);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                viewRoot.setVisibility(View.INVISIBLE);
            }
        });
        anim.setDuration(ANIM_DURATION);
        anim.start();
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);

        ButterKnife.bind(this);
        setupLayout();
        setupWindowAnimations();

        iconSelector = new Drawable[]
                {
                        getResources().getDrawable(R.drawable.selector_genre),
                        getResources().getDrawable(R.drawable.selector_artist),
                        getResources().getDrawable(R.drawable.selector_album)
                };

        /**
         * Toolbar
         */
        toolbar.inflateMenu(R.menu.menu_recommendation);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        toolbar.setBackgroundColor(Color.parseColor("#88000000"));
        getSupportActionBar().setHomeButtonEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                menuIcon.animateState(MaterialMenuDrawable.IconState.ARROW);
                finish();
                return;
            }
        });



        /**
         * searchView
         */
        mSearchView = (SearchView) toolbar.getMenu().findItem(R.id.menu_search).getActionView();
        mSearchView.setIconified(false);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        /**
         * DrawerLayout
         */
        // ドロワーの開け閉めをActionBarDrawerToggleで監視
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // ドロワー開くボタン(三本線)を表示
        mDrawerToggle.setDrawerIndicatorEnabled(true);

//        mDrawerToggle.setHomeAsUpIndicator(menuIcon.setState(MaterialMenuDrawable.IconState.ARROW));

//        mDrawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
//            @Override
//            public void onDrawerSlide(View drawerView, float slideOffset) {
//                menuIcon.setTransformationOffset(
//                        MaterialMenuDrawable.AnimationState.BURGER_ARROW,
//                        isDrawerOpened ? 2 - slideOffset : slideOffset
//                );
//            }
//
//            @Override
//            public void onDrawerOpened(View drawerView) {
////                super.onDrawerOpened(drawerView);
//                isDrawerOpened = true;
//            }
//
//            @Override
//            public void onDrawerClosed(View drawerView) {
////                super.onDrawerClosed(drawerView);
//                isDrawerOpened = false;
//            }
//
//            @Override
//            public void onDrawerStateChanged(int newState) {
////                super.onDrawerStateChanged(newState);
//                if (newState == DrawerLayout.STATE_IDLE) {
//                    if (isDrawerOpened) menuIcon.setState(MaterialMenuDrawable.IconState.ARROW);
//                    else menuIcon.setState(MaterialMenuDrawable.IconState.ARROW);
//                }
//            }
//        });

        /**
         * RecyclerView
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.drawer_view);

        // RecyclerView内のItemサイズが固定の場合に設定すると、パフォーマンス最適化
        mRecyclerView.setHasFixedSize(true);

        // レイアウトの選択
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // XML
        TypedArray drawerMenuList = getResources().obtainTypedArray(R.array.drawer_menu_list);
        int menuLength = drawerMenuList.length();

        // RecyclerView.Adapter に渡すデータ
        final ArrayList<HashMap<String, Object>> drawerMenuArr = new ArrayList<>();

        for (int i = 0; i < menuLength; i++) {
            TypedArray itemArr = getResources().obtainTypedArray(drawerMenuList.getResourceId(i, 0));
            int itemLength = itemArr.length();
            HashMap<String, Object> content = new HashMap<>();
            drawerMenuArr.add(content);
            for (int j = 0; j < itemLength; j++) {
                TypedArray contentArr = getResources().obtainTypedArray(itemArr.getResourceId(j, 0));

                // key-value
                if (contentArr.getString(0).contains("icon")) {
                    content.put(contentArr.getString(0), contentArr.getDrawable(1));
                } else {
                    content.put(contentArr.getString(0), contentArr.getString(1));
                }
                contentArr.recycle();
            }
            itemArr.recycle();
        }

        // XMLを読込んで表示する
        mAdapter = new DrawerAdapter(drawerMenuArr);

        mRecyclerView.setAdapter(mAdapter);

        // GestureDetectorを使って、onSingleTapUpを検知
        final GestureDetector mGestureDetector = new GestureDetector(getApplicationContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        return true;
                    }
                });

        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
                if (mGestureDetector.onTouchEvent(e)) {

                    // onSingleTapUpの時に、タッチしているViewを取得
                    View childView = view.findChildViewUnder(e.getX(), e.getY());
                    int potision = mRecyclerView.getChildPosition(childView);

                    // タッチしているViewのデータを取得
                    HashMap<String, Object> data = drawerMenuArr.get(potision);

                    // Menu アイテムのみ
                    if (data.get("text").toString().equals("My History")) {
                        // ドロワー閉じる
                        mDrawerLayout.closeDrawers();
                        startActivity(new Intent(getApplicationContext(), MyPlaylistActivity.class));

                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {

            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }
        });

        menuIcon = new MaterialMenuIconToolbar(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN) {
            @Override
            public int getToolbarViewId() {
                return R.id.toolbar;
            }
        };

        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.addTab(mTabLayout.newTab());
        mTabLayout.addTab(mTabLayout.newTab());
        mTabLayout.addTab(mTabLayout.newTab());

        mTabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.accent));

        FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return TestFragment.newInstance(position + 1);
            }

            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return null;
            }
        };

        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(this);

        mTabLayout.setupWithViewPager(viewPager);
        for (int x = 0; x < mTabLayout.getTabCount(); x++) {
            mTabLayout.getTabAt(x).setTag(factor[x]).setIcon(iconSelector[x]);
        }

        SharedPreferences data = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
        long nowPlayingId = data.getLong("key", 1);
        Item item = new Select().from(Item.class).where("Id = ?", nowPlayingId).executeSingle();

        ((SmartImageView)findViewById(R.id.bg_jacket)).setImageUrl(item.artworkUrl100);
    }

    @OnClick(R.id.tool_back)
    public void onClickBack() {
        finish();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        isDrawerOpened = mDrawerLayout.isDrawerOpen(Gravity.LEFT); // or END, LEFT, RIGHT
        menuIcon.syncState(savedInstanceState);

        // ActionBarDrawerToggleとMainActivityの状態を同期する
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // ActionBarDrawerToggleにイベント渡す
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        menuIcon.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recommendation, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.menu.menu_recommendation:

        }

        // ActionBarDrawerToggleにイベント渡す
        // 渡さないと、ドロワーボタンを押しても開かない
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public static class TestFragment extends Fragment {

        ExpandableListView listExpandable;
        ArrayList<Element> elements;

        public TestFragment() {
        }

        public static TestFragment newInstance(int page) {
            Bundle args = new Bundle();
            args.putInt("page", page);
            TestFragment fragment = new TestFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            int page = getArguments().getInt("page", 0);
            View view = inflater.inflate(R.layout.fragment_test, container, false);
            ((TextView) view.findViewById(R.id.ftxt_description)).setText("Page " + page);
            listExpandable = (ExpandableListView) view.findViewById(R.id.list_expandable);

            /**
             * ExpandableListView
             */
            elements = new ArrayList<>();

            try {
                switch (page) {
                    case 1:
                        ((TextView) view.findViewById(R.id.ftxt_description)).setText("同じジャンルの曲");
                        for (int i = 0; i < 20; i++) {
                            Element x = (new Element("page" + page + ": " + i + "th element", Item.getRandom()));
                            elements.add(x);
                        }
                        break;
                    case 3:
                        ((TextView) view.findViewById(R.id.ftxt_description)).setText("他のアルバムの曲");
//                        for (int i = 0; i < 20; i++) {
//                            Element x = (new Element("page" + page + ": " + i + "th element", Item.getRandom()));
//                            elements.add(x);
//                        }
                        for(int z = 0; z < 10; z++) {
                            Item ii = Item.getRandom();
                            List<Item> list = Item.getByAlbum(ii.collectionName, ii.artistName);
                            Element tmp = new Element(Item.getByAlbum(ii.collectionName, ii.artistName).get(0));

                            for (Item i : list) {
                                tmp.addChild(new Element(i));
                            }

                            elements.add(tmp);
                        }


                        break;
                    case 2:
                        ((TextView) view.findViewById(R.id.ftxt_description)).setText("似たアーティストの曲");
                        for(int z = 0; z < 10; z++) {
                            Item ii = Item.getRandom();
                            List<Item> list = Item.getByAlbum(ii.collectionName, ii.artistName);
                            Element tmp = new Element(Item.getByAlbum(ii.collectionName, ii.artistName).get(0));

                            for (Item i : list) {
                                tmp.addChild(new Element(i));
                            }

                            elements.add(tmp);
                        }
                        break;
                    default:
                        break;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "TrackDatabase is empty!!", Toast.LENGTH_SHORT).show();

                Element elem = new Element("Empty");
                elements.add(elem);
            }

            listExpandable.setGroupIndicator(getResources().getDrawable(android.R.color.transparent));
            ExpandListAdapter expandListAdapter = new ExpandListAdapter(getActivity(), elements, page);

            listExpandable.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//                    Element e = elements.get(groupPosition).getChildren().get(childPosition);
//                    Item selected = new Item(e.getTrackName(), e.getpreviewUrl(), e.getArtworkUrl100(), e.getArtistName(), e.getCollectionName(), e.getRegisterTime());
//                    selected.save();
//                    startActivity(new Intent(getActivity(), PlayerActivity.class));
                    return false;
                }
            });

            listExpandable.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {


//                    if (!elements.get(groupPosition).isParent()) {
////                        Element e = elements.get(groupPosition);
////                        Item selected = new Item(e.getTrackName(), e.getpreviewUrl(), e.getArtworkUrl100(), e.getArtistName(), e.getCollectionName(), e.getRegisterTime());
////                        selected.save();
////                        startActivity(new Intent(getActivity(), PlayerActivity.class));
//                    } else {
////                        animateRotation();
//                    }
                    return false;
                }
            });

            listExpandable.setAdapter(expandListAdapter);

            return view;
        }
    }
}
