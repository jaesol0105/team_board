package com.beinny.teamboard.ui.home.boardList

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.beinny.teamboard.ui.adapters.BoardAdapter
import com.beinny.teamboard.databinding.FragmentBoardListBinding
import com.beinny.teamboard.models.Board
import com.beinny.teamboard.ui.tasklist.TaskListActivity
import androidx.recyclerview.widget.ConcatAdapter
import com.beinny.teamboard.models.BookmarkedBoards
import com.beinny.teamboard.ui.common.ViewModelFactory
import com.beinny.teamboard.utils.Constants

class BoardListFragment :Fragment(), BoardAdapter.CallBacks {
    private lateinit var binding: FragmentBoardListBinding
    private val viewModel: BoardListViewModel by activityViewModels { ViewModelFactory(requireContext())}

    private var callback: CallBacks? = null // MainActivity 연결 콜백
    private lateinit var bookmarkSectionAdapter : BookmarkSectionAdapter

    /** [콜백 인터페이스 : MainActivity()에서 구현] */
    interface CallBacks {
        fun bookmarkOnOff(boardDocId:String, bookmarked: Boolean)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CallBacks) {
            callback = context
        } else {
            throw RuntimeException("$context must implement Callback")
        }
    }

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
        /** LifeCycleOwner, ViewModel 바인딩 */
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setListAdapter()
    }

    /** [북마크 등록 및 해제 : BoardAdapter() 콜백 구현] */
    override fun bookmarkIconClick (board: Board) {
        callback?.bookmarkOnOff(board.documentId, board.bookmarked)
        if (board.bookmarked)
            viewModel.removeBookmarkedBoard(board)
        else
            viewModel.addBookmarkedBoard(board)
    }

    /** [작업목록 프래그먼트로 이동] */
    private fun boardClickListener(model :Board) {
        val intent = Intent(
            requireActivity(),
            TaskListActivity::class.java
        ) // TaskActivity 실행
        intent.putExtra(
            Constants.DOCUMENT_ID,
            model.documentId
        ) // 해당 보드의 documentId를 전달
        startActivity(intent)
    }

    /** [어댑터 설정] */
    private fun setListAdapter() {
        bookmarkSectionAdapter = BookmarkSectionAdapter(AdapterCallback())

        /** UI 갱신 */
        viewModel.boardList.observe(
            viewLifecycleOwner,
            Observer { boardsList ->
                boardsList?.let {
                    if (boardsList.size > 0) {
                        binding.rvBoardsList.visibility = View.VISIBLE
                        binding.tvNoBoardsAvailable.visibility = View.GONE

                        binding.rvBoardsList.layoutManager = LinearLayoutManager(requireActivity())
                        binding.rvBoardsList.setHasFixedSize(true)

                        /** 보드 목록 어댑터 */
                        val adapter = BoardAdapter(boardsList, this)
                        binding.rvBoardsList.adapter = ConcatAdapter(bookmarkSectionAdapter, adapter)
                        adapter.setOnClickListener(object :
                            BoardAdapter.OnClickListener {
                            override fun onClick(position: Int, model: Board) {
                                boardClickListener(model)
                            }
                        })
                    } else {
                        binding.rvBoardsList.visibility = View.GONE
                        binding.tvNoBoardsAvailable.visibility = View.VISIBLE
                    }
                }
            })

        viewModel.bookmarkedBoardList.observe(
            viewLifecycleOwner,
            Observer { bookmarkedBoardList ->
                bookmarkedBoardList?.let {
                    /** 북마크 바 어댑터 */
                    bookmarkSectionAdapter.submitList(listOf(BookmarkedBoards("북마크한 보드", bookmarkedBoardList)))
                }
            }
        )
    }

    /** [BookmarkSectionAdapter() 넘겨줄 콜백] */
    inner class AdapterCallback {
        // inner class로 구현해서 어댑터 생성시 넘겨준다
        fun onClick(model: Board){
            boardClickListener(model)
        }
    }

    companion object {
        fun newInstance(): BoardListFragment {
            return BoardListFragment()
        }
    }
}