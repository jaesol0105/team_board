package com.beinny.teamboard.ui.home.bookmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.beinny.teamboard.databinding.FragmentBookmarkBinding
import com.beinny.teamboard.ui.common.ViewModelFactory
import com.beinny.teamboard.ui.home.boardList.BoardListViewModel

class BookmarkFragment : Fragment() {
    private lateinit var binding: FragmentBookmarkBinding

    private val viewModel: BoardListViewModel by activityViewModels { ViewModelFactory(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

}