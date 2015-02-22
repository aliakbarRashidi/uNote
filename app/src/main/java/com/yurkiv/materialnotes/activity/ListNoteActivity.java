package com.yurkiv.materialnotes.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
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
import com.yurkiv.materialnotes.data.DatabaseHelper;
import com.yurkiv.materialnotes.fragment.NavigationDrawerFragment;
import com.yurkiv.materialnotes.model.Note;
import com.yurkiv.materialnotes.adapter.NotesAdapter;
import com.yurkiv.materialnotes.util.HashtagCallbacks;
import com.yurkiv.materialnotes.util.MentionCallbacks;
import com.yurkiv.materialnotes.util.RequestResultCode;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class ListNoteActivity extends ActionBarActivity implements SearchView.OnQueryTextListener, HashtagCallbacks, MentionCallbacks {

    private static final String EXTRA_NOTE = "EXTRA_NOTE";
    private static final int VIEW_NOTE_RESULT_CODE = 5;

    private Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private TextView textEmpty;
    private ListView listNotes;

    private List<Note> notesData;
    private NotesAdapter notesAdapter;
    private SearchView searchView;
    private MenuItem searchMenuItem;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notelist);

        mToolbar = (Toolbar) findViewById(R.id.note_list_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);

        databaseHelper=new DatabaseHelper(getApplicationContext());

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
                intent.putExtra(EXTRA_NOTE, note);
                startActivityForResult(intent, RequestResultCode.REQUEST_CODE_VIEW_NOTE);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ListNoteActivity.this, EditNoteActivity.class);
                startActivityForResult(intent, RequestResultCode.REQUEST_CODE_ADD_NOTE);
            }
        });

    }

    private void initSimpleNote(){
        for (int i = 0; i < 10; i++) {
            Note note=new Note();
            note.setTitle("Note " + i);
            note.setContent("Content " + i);
            note.setUpdatedAt(new Date());
            databaseHelper.createNote(note);
        }
    }

    private void setupNotesAdapter(){
        notesData=databaseHelper.getAllNotes();
        notesAdapter=new NotesAdapter(notesData);
        listNotes.setAdapter(notesAdapter);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==RequestResultCode.REQUEST_CODE_VIEW_NOTE){
            if (resultCode==RESULT_OK){
                updateNote(data);
            } else if (resultCode==RequestResultCode.RESULT_CODE_DELETE_NOTE){
                deleteNote(data);
            }
        }
        if (requestCode==RequestResultCode.REQUEST_CODE_ADD_NOTE){
            if (resultCode==RESULT_OK){
                addNote(data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addNote(Intent data){
        Note note= (Note) data.getSerializableExtra(EXTRA_NOTE);
        long noteId=databaseHelper.createNote(note);
        note.setId(noteId);
        notesData.add(note);
        updateView();
        notesAdapter.notifyDataSetChanged();
    }

    private void updateNote(Intent data) {
        Note updatedNote= (Note) data.getSerializableExtra(EXTRA_NOTE);
        databaseHelper.updateNote(updatedNote);
        for (Note note: notesData){
            if (note.getId().equals(updatedNote.getId())){
                note.setTitle(updatedNote.getTitle());
                note.setContent(updatedNote.getContent());
                note.setUpdatedAt(updatedNote.getUpdatedAt());
            }
        }
        notesAdapter.notifyDataSetChanged();
    }

    private void deleteNote(Intent data) {
        Note deletedNote= (Note) data.getSerializableExtra(EXTRA_NOTE);
        databaseHelper.deleteNote(deletedNote);
        notesData.remove(deletedNote);
        updateView();
        notesAdapter.notifyDataSetChanged();
        Toast.makeText(ListNoteActivity.this, "The note has been deleted.", Toast.LENGTH_LONG).show();
    }


    private void sortList(MenuItem item, Comparator<Note> noteComparator) {
        Collections.sort(notesData, noteComparator);
        notesAdapter.notifyDataSetChanged();
        if (item.isChecked()) item.setChecked(false);
        else item.setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
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
        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
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
    public void onHashtagItemSelected(int position) {
        Toast.makeText(this, "hashtag selected -> " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMentionItemSelected(int position) {
        Toast.makeText(this, "mention selected -> " + position, Toast.LENGTH_SHORT).show();
    }
}
