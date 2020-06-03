package com.example.words;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WordsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WordsFragment extends Fragment {
    private WordViewModel wordViewModel;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;
    private FloatingActionButton floatingActionButton;
    private LiveData<List<Word>> filterWords;
    private List<Word> allWords;
    private boolean undoAction;
    //private DividerItemDecoration dividerItemDecoration;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public WordsFragment() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WordsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WordsFragment newInstance(String param1, String param2) {
        WordsFragment fragment = new WordsFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_words, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clearData:
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setTitle("清空数据");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        wordViewModel.deleteAllWords();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create();
                builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        int maxWidth = searchView.getMaxWidth();
        searchView.setMaxWidth((int) (0.6 * maxWidth));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String pattern = newText.trim();
                filterWords.removeObservers(getViewLifecycleOwner());
                filterWords = wordViewModel.findWordsWithPattern(pattern);
                filterWords.observe(getViewLifecycleOwner(), new Observer<List<Word>>() {
                    @Override
                    public void onChanged(List<Word> words) {
                        int temp = myAdapter.getItemCount();
                        //myAdapter.setAllWords(words);
                        allWords = words;
                        if (temp != words.size()) {
                            myAdapter.submitList(words);
                        }
                    }
                });

                return true;
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        wordViewModel = new ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(WordViewModel.class);
        recyclerView = requireActivity().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        myAdapter = new MyAdapter(wordViewModel);
        recyclerView.setItemAnimator(new DefaultItemAnimator() {
            @Override
            public void onAddFinished(RecyclerView.ViewHolder item) {
                super.onAddFinished(item);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (linearLayoutManager != null) {
                    int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
                    int lastPosition = linearLayoutManager.findLastVisibleItemPosition();
                    for (int i = firstPosition; i <= lastPosition; i++) {
                        MyAdapter.MyViewHolder holder = (MyAdapter.MyViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                        if (holder != null) {
                            holder.textViewNumber.setText(String.valueOf(i + 1));
                        }
                    }
                }
            }
        });
        //dividerItemDecoration = new DividerItemDecoration(requireActivity(),DividerItemDecoration.VERTICAL);
        recyclerView.setAdapter(myAdapter);
        //recyclerView.addItemDecoration(dividerItemDecoration);
        filterWords = wordViewModel.getAllWordsLive();
        filterWords.observe(getViewLifecycleOwner(), new Observer<List<Word>>() {
            @Override
            public void onChanged(List<Word> words) {
                int temp = myAdapter.getItemCount();
                //myAdapter.setAllWords(words);
                allWords = words;
                if (temp != words.size() && !undoAction) {
                    if (temp < words.size()) {
                        recyclerView.smoothScrollBy(0,-200);
                    }
                    undoAction = false;
                    myAdapter.submitList(words);
                    //myAdapter.notifyItemInserted(0);
                    //myAdapter.notifyDataSetChanged();
                }
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                //Word wordFrom = allWords.get(viewHolder.getAdapterPosition());
                //Word wordTo = allWords.get(target.getAdapterPosition());
                //int idTemp = wordFrom.getId();
                //wordFrom.setId(wordTo.getId());
                //wordTo.setId(idTemp);
                //wordViewModel.updateWords(wordFrom,wordTo);
                //myAdapter.notifyItemMoved(viewHolder.getAdapterPosition(),target.getAdapterPosition());
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final Word wordToDelete = allWords.get(viewHolder.getAdapterPosition());
                wordViewModel.deleteWords(wordToDelete);
                Snackbar.make(requireActivity().findViewById(R.id.wordsFragmentView),"删除了一个词汇！",Snackbar.LENGTH_SHORT)
                        .setAction("撤销", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                undoAction = true;
                                wordViewModel.insertWords(wordToDelete);
                            }
                        })
                        .show();

            }
        }).attachToRecyclerView(recyclerView);

        floatingActionButton = requireActivity().findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate((R.id.action_wordsFragment_to_addFragment));
            }
        });
    }

    @Override
    public void onResume() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        super.onResume();
    }
}