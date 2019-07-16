package ru.yandex.sharov.example.notes.interact;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import ru.yandex.sharov.example.notes.data.NotesDatabase;
import ru.yandex.sharov.example.notes.interact.interactions.DatabaseInteractionsFactory;
import ru.yandex.sharov.example.notes.model.Note;

public class LocalRepositoryNoteInteractor {

    private static final Object LOCK = new Object();
    @Nullable
    private static LocalRepositoryNoteInteractor instance;
    @NonNull
    private final MutableLiveData<List<Note>> data;
    @NonNull
    private LiveData<List<Note>> dbData;
    @NonNull
    private DatabaseInteractionsFactory interactionsFactory;
    @NonNull
    private NotesDatabase database;

    private LocalRepositoryNoteInteractor(@NonNull Context context) {
        data = new MutableLiveData<>();
        initDataLayer(context);
    }

    private void initDataLayer(@NonNull Context context) {
        Consumer<NotesDatabase> consumer = notesDatabase -> {
            interactionsFactory = DatabaseInteractionsFactory.newInstance(notesDatabase);
            database = notesDatabase;
            dbData = database.getNoteDao().getAllNotes();
            dbData.observeForever(data::setValue);

        };

        InitDastabase initDastabase = new InitDastabase(context, consumer);
        initDastabase.execute();
    }

    @NonNull
    public static LocalRepositoryNoteInteractor getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new LocalRepositoryNoteInteractor(context);
                }
                return instance;
            }
        }
        return instance;
    }


    @NonNull
    public LiveData<List<Note>> getData() {
        return data;
    }

    public void addOrUpdateNote(@NonNull Note note) {
        interactionsFactory.createInsertNotesTask().execute(note);
    }

    public void addOrUpdateNotes(@NonNull List<Note> notes) {
        interactionsFactory.createMergeNotesTask().execute(notes.toArray(new Note[0]));
    }

    public void removeNote(long noteId) {
        AsyncTask<Long, Void, Void> removeNoteTask = interactionsFactory.createDeleteNotesTask();
        removeNoteTask.execute(noteId);
    }

    @Nullable
    public LiveData<Note> getNoteById(long noteId, @NonNull MutableLiveData<Note> noteData) {
        AsyncTask<Void, Void, Note> getNoteTask = interactionsFactory.createSelectOneNoteTask(noteId, noteData);
        getNoteTask.execute();
        return noteData;
    }

    private static class InitDastabase extends AsyncTask<Void, Void, NotesDatabase> {

        @NonNull
        private final Context context;
        @NonNull
        private final Consumer<NotesDatabase> dbConsumer;

        private InitDastabase(@NonNull Context context, @NonNull Consumer<NotesDatabase> dbConsumer) {
            this.context = context;
            this.dbConsumer = dbConsumer;
        }

        @Override
        protected NotesDatabase doInBackground(Void... voids) {
            return NotesDatabase.getInstance(context);
        }

        @Override
        protected void onPostExecute(NotesDatabase notesDB) {
            dbConsumer.accept(notesDB);
        }
    }
}
