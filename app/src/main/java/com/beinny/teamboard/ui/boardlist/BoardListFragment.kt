package com.beinny.teamboard.ui.boardlist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.beinny.teamboard.databinding.FragmentBoardListBinding
import com.beinny.teamboard.data.model.Board
import com.beinny.teamboard.ui.tasklist.TaskListActivity
import androidx.recyclerview.widget.ConcatAdapter
import com.beinny.teamboard.R
import com.beinny.teamboard.data.model.BookmarkedBoards
import com.beinny.teamboard.ui.common.repeatOnStarted
import com.beinny.teamboard.ui.common.toggleWith
import com.beinny.teamboard.ui.factory.ViewModelFactory
import com.beinny.teamboard.ui.shared.BoardListViewModel
import com.beinny.teamboard.utils.Constants

class BoardListFragment :Fragment(), BoardListListener {
    private lateinit var binding: FragmentBoardListBinding
    private lateinit var bookmarkSectionAdapter : BookmarkSectionAdapter
    private val viewModel: BoardListViewModel by activityViewModels { ViewModelFactory(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBoardListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setListAdapter()
    }

    /** 보드 어댑터 & 북마크 어댑터 */
    private fun setListAdapter() {
        bookmarkSectionAdapter = BookmarkSectionAdapter { board -> onBoardClick(board) }

        repeatOnStarted {
            viewModel.boardList.collect { boards ->
                if (boards.isNotEmpty()) {
                    binding.tvNoBoardsAvailable.toggleWith(binding.rvBoardsList)
                    val boardAdapter = BoardAdapter(boards, getString(R.string.board_list_subtitle),this@BoardListFragment)
                    binding.rvBoardsList.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        setHasFixedSize(true)
                        adapter = ConcatAdapter(bookmarkSectionAdapter, boardAdapter)
                    }
                } else {
                    binding.rvBoardsList.toggleWith(binding.tvNoBoardsAvailable)
                }

            }
        }
        repeatOnStarted {
            viewModel.bookmarkedBoardList.collect { boards ->
                bookmarkSectionAdapter.submitList(listOf(BookmarkedBoards(getString(R.string.bookmarked_board_list_subtitle), boards)))
            }
        }
    }

    /** TaskListActivity로 이동 */
    override fun onBoardClick(board: Board) {
        val intent = Intent(requireActivity(), TaskListActivity::class.java).apply {
            putExtra(Constants.EXTRA_DOCUMENT_ID, board.documentId)
        }
        startActivity(intent)
    }

    /** 북마크 상태 토글 */
    override fun onBookmarkClick(board: Board) {
        viewModel.toggleBookmark(board)
    }
}