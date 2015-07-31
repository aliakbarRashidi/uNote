package com.yurkiv.materialnotes.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.yurkiv.materialnotes.R;
import com.yurkiv.materialnotes.adapter.NotesAdapter;
import com.yurkiv.materialnotes.fragment.NavigationDrawerFragment;
import com.yurkiv.materialnotes.model.Hashtag;
import com.yurkiv.materialnotes.model.Note;
import com.yurkiv.materialnotes.util.HashtagCallbacks;
import com.yurkiv.materialnotes.util.MentionCallbacks;
import com.yurkiv.materialnotes.util.Constants;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import io.realm.Realm;


public class ListNoteActivity extends ActionBarActivity implements SearchView.OnQueryTextListener, HashtagCallbacks, MentionCallbacks {

    private static final String TAG = ListNoteActivity.class.getSimpleName();
    private Toolbar toolbar;
    private NavigationDrawerFragment navigationDrawerFragment;

    private TextView textEmpty;
    private ListView listNotes;

    private List<Note> notesData;
    private List<Hashtag> hashtags;
    private NotesAdapter notesAdapter;
    private SearchView searchView;
    private MenuItem searchMenuItem;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notelist);

        // Open the default realm for the UI thread.
        realm = Realm.getInstance(this);

        toolbar = (Toolbar) findViewById(R.id.note_list_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);
        navigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), toolbar);

        textEmpty = (TextView) findViewById(R.id.textEmpty);
        listNotes = (ListView) findViewById(R.id.listNotes);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(listNotes);

        //initSimpleNote();
        setupNotesAdapter();
        updateView();

        listNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note note=notesData.get(position);
                Intent intent=new Intent(ListNoteActivity.this, ViewNoteActivity.class);
                intent.putExtra(Constants.EXTRA_NOTE, note.getId());
                startActivity(intent);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ListNoteActivity.this, EditNoteActivity.class);
                intent.putExtra(Constants.EXTRA_NOTE, "");
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onRestart() {
        updateData();
        super.onRestart();
    }

    private void initSimpleNote(){
        for (int i = 0; i < 10; i++) {
            realm.beginTransaction();
            Note note=realm.createObject(Note.class);
            note.setTitle("Note " + i);
            note.setContent("Content " + i);
            note.setUpdatedAt(new Date());
            realm.commitTransaction();
        }
    }

    private void setupNotesAdapter(){
        notesData = realm.where(Note.class).findAll();
        notesAdapter=new NotesAdapter(notesData);
        listNotes.setAdapter(notesAdapter);

        hashtags=realm.where(Hashtag.class).findAll();
        navigationDrawerFragment.updateNavigationDrawerHashtagList(hashtags);

    }

    private void updateView(){
        if (notesData.isEmpty()){
            listNotes.setVisibility(View.GONE);
            textEmpty.setVisibility(View.VISIBLE);
        } else {
            listNotes.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.GONE);
        }
    }

    private void updateData() {
        notesData = realm.where(Note.class).findAll();
        Log.i(TAG, notesData.toString());
        notesAdapter.notifyDataSetChanged();
        //TODO: Only unique
        hashtags=realm.where(Hashtag.class).findAll();
        Log.i(TAG, hashtags.toString());
        navigationDrawerFragment.updateNavigationDrawerHashtagList(hashtags);
        updateView();
    }

    private void sortList(MenuItem item, Comparator<Note> noteComparator) {
        Collections.sort(notesData, noteComparator);
        notesAdapter.notifyDataSetChanged();
        if (item.isChecked()) item.setChecked(false);
        else item.setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_note_list, menu);
        SearchManager searchManager= (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem=menu.findItem(R.id.search_note);
        searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sort_by_title:
                sortList(item, NotesAdapter.titleComparator);
                return true;
            case R.id.newest_first:
                sortList(item, NotesAdapter.newestFirstComparator);
                return true;
            case R.id.oldest_first:
                sortList(item, NotesAdapter.oldestFirstComparator);
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (navigationDrawerFragment.isDrawerOpen())
            navigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        notesAdapter.getFilter().filter(s);
        return true;
    }

    @Override
    public void onHashtagItemSelected(Hashtag hashtag) {
//        notesData=databaseHelper.getAllNotesByHashTags(hashtag);
        notesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMentionItemSelected(int position) {
        Toast.makeText(this, "mention selected -> " + position, Toast.LENGTH_SHORT).show();
    }
}
