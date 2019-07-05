package ru.yandex.sharov.example.notes;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ru.yandex.sharov.example.notes.data.DBHelperStub;
import ru.yandex.sharov.example.notes.util.UIBehaviorHandlerFactory;
import ru.yandex.sharov.example.notes.util.UIUtil;

public class NotesListFragment extends Fragment {

    private static final String LOG_TAG = "[LOG_TAG:NoteLstFrgmnt]";

    private RecyclerView recyclerView;
    private NotesRecyclerViewAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private NoteItemOnClickListener listener;

    @NonNull
    public static NotesListFragment newInstance() {
        return new NotesListFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(LOG_TAG, " onAttach");
        UIUtil.assertContextImplementsInterface(context, NoteItemOnClickListenerProvider.class);
        listener = ((NoteItemOnClickListenerProvider) context).getListener();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(LOG_TAG, " onCreateView");
        View rootV = inflater.inflate(R.layout.notes_list_fragment, container, false);
        FloatingActionButton addNoteFab = rootV.findViewById(R.id.add_note_fab);
        addNoteFab.setOnClickListener(v -> listener.onAddingNote());
        initNoteListRecyclerView(rootV);
        initBottomSheet(rootV, addNoteFab);
        return rootV;
    }

    private void initBottomSheet(@NonNull View rootV, @NonNull FloatingActionButton fab) {
        View bottomSheet = rootV.findViewById(R.id.bottom_sheet);
        View closeOpenBtn = rootV.findViewById(R.id.bottom_sheet_close_open_btn);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setBottomSheetCallback(
                UIBehaviorHandlerFactory.createBottomSheetCallback(fab, closeOpenBtn)
        );
        closeOpenBtn.setOnClickListener(
                UIBehaviorHandlerFactory.createCloseOpenBtnOnClickListener(bottomSheetBehavior)
        );
        ToggleButton dateSortBtn = bottomSheet.findViewById(R.id.date_sort_toggle);
        dateSortBtn.setOnCheckedChangeListener(UIBehaviorHandlerFactory.createOnCheckedChangeListener(adapter));
        EditText searchEditText = rootV.findViewById(R.id.search_edit_text);
        searchEditText.addTextChangedListener(UIBehaviorHandlerFactory.createTextChangedListener(adapter));
    }

    private void initNoteListRecyclerView(@NonNull View rootV) {
        recyclerView = rootV.findViewById(R.id.recycler_view_note_list);
        layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new NotesRecyclerViewAdapter();
        adapter.setListener(listener);
        adapter.setDataList(DBHelperStub.getInstance().getData());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL); //TODO: сделать отступ под иконкой
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(adapter);
    }
}
